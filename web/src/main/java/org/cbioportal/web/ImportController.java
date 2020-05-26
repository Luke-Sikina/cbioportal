package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.cbioportal.model.ImportLog;
import org.cbioportal.model.ImportLogType;
import org.cbioportal.model.ImportStudy;
import org.cbioportal.model.User;
import org.cbioportal.persistence.SecurityRepository;
import org.cbioportal.service.ImportService;
import org.cbioportal.web.config.annotation.InternalApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

@InternalApi
@RestController
@Validated
@Api(tags = "Import", description = " ")
public class ImportController {
    @Autowired
    ImportService importService;
    
    @Autowired
    SecurityRepository securityRepository;

    @Value("${db.password}")
    String dbPassword;
    
    @Value("${db.host}")
    String dbHost;
    
    @Value("${importer.log}")
    String logDir;
    
    @Value("${host}")
    String host;

    @Value("${importer.bin}")
    String importer;
    
    private static final ConcurrentHashMap<String, Semaphore> importLocks = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Semaphore> validationLocks = new ConcurrentHashMap<>();

    @PreAuthorize("hasPermission(#studyId, 'CancerStudyId', 'read')")
    @RequestMapping(value = "/logs/{logType}/{studyId}/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get the specified log file")
    public ResponseEntity<ImportLog> getLog(
        @PathVariable("logType") String logType,
        @PathVariable("studyId") String studyId,
        @PathVariable("id") String id
    ) {
        ImportLog log = importService.getLog(logType, studyId, id);
        return new ResponseEntity<>(log, log == null ? HttpStatus.NOT_FOUND : HttpStatus.OK);
    }

    @PreAuthorize("hasPermission(#studyId, 'CancerStudyId', 'read')")
    @RequestMapping(value = "/logs/{logType}/{studyId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get the specified log file")
    public ResponseEntity<List<ImportLog>> getAllLogsForStudy(
        @PathVariable("logType") String logType,
        @PathVariable("studyId") String studyId
    ) {
        List<ImportLog> logs = importService.getAllLogsForStudy(studyId, logType);
        return new ResponseEntity<>(logs, HttpStatus.OK);
    }

    @RequestMapping(value = "/importer/", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get a list of all studies in the importer")
    public ResponseEntity<List<ImportStudy>> getAllImporterStudies() {
        List<ImportStudy> studies = importService.getAllStudies();
        return new ResponseEntity<>(studies, HttpStatus.OK);
    }

    @PreAuthorize("hasPermission(#studyId, 'CancerStudyId', 'read')")
    @RequestMapping(value = "/importer/{studyId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get study details")
    public ResponseEntity<ImportStudy> getImporterStudy(
        @PathVariable("studyId") String studyId
    ) {
        ImportStudy study = importService.getStudy(studyId);
        return new ResponseEntity<>(study, HttpStatus.OK);
    }

    @PreAuthorize("hasPermission(#studyId, 'CancerStudyId', 'read')")
    @RequestMapping(value = "/importer/{studyId}/import", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Run a trial import of the studyId")
    public ResponseEntity<String> runTrialImport(
        @PathVariable("studyId") String studyId,
        Authentication authentication
    ) throws IOException, InterruptedException {
        // nb: computeIfAbsent is atomic for ConcurrentHashMap, so this works
        Semaphore lock = importLocks.computeIfAbsent(studyId, k -> new Semaphore(1, true));
        lock.acquire();
        
        String username = getUserName(authentication);
        ImportStudy study = importService.getStudy(studyId);
        if (study == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (study.isImportRunning()) {
            return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
        }

        ImportLog log = new ImportLog();
        log.setLogType(ImportLogType.Import.name().toLowerCase());
        log.setTestRun(true);
        log.setStartDate(new Date());
        log.setRequester(username);
        log.setStudyId(studyId);
        log.setText("<span>Import still running...</span>");
        study.setImportRunning(true);

        Integer id;
        importService.addImportLog(log);
        id = importService.getLastId();
        importService.updateStudyAsImporting(studyId);

	    String commandTemplate = "%s -command import -path %s -name %s -dbpass %s -dbaddr %s:3306 -username %s -logid %d -log_dir %s -host %s";
        String command = String.format(commandTemplate, importer, study.getStudyPath(), studyId, dbPassword, dbHost, username, id, logDir, host);
        String passwordlessCommand = String.format(commandTemplate, importer, study.getStudyPath(), studyId, "PASSWORD", dbHost, username, id, logDir, host);
        System.out.println(passwordlessCommand);
        Runtime.getRuntime().exec(command).waitFor();
        
        lock.release();

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PreAuthorize("hasPermission(#studyId, 'CancerStudyId', 'read')")
    @RequestMapping(value = "/importer/{studyId}/validate", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Run a trial validation of the studyId")
    public ResponseEntity<String> runTrialValidation(
        @PathVariable("studyId") String studyId,
        Authentication authentication
    ) throws IOException, InterruptedException {
        // nb: computeIfAbsent is atomic for ConcurrentHashMap, so this works
        Semaphore lock = validationLocks.computeIfAbsent(studyId, k -> new Semaphore(1, true));
        lock.acquire();

        String username = getUserName(authentication);
        ImportStudy study;

        study = importService.getStudy(studyId);
        if (study == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (study.isValidationRunning()) {
            return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
        }

        study.setValidationRunning(true);
        importService.updateStudyAsValidating(studyId);

	    String commandTemplate = "%s -command validate -path %s -name %s -dbpass %s -dbaddr %s:3306 -username %s -log_dir %s -host %s";
        String command = String.format(commandTemplate, importer, study.getStudyPath(), studyId, dbPassword, dbHost, username, logDir, host);
        String passwordlessCommand = String.format(commandTemplate, importer, study.getStudyPath(), studyId, "PASSWORD", dbHost, username, logDir, host);

        System.out.println(passwordlessCommand);
        Runtime.getRuntime().exec(command);
        
        lock.release();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private String getUserName(Authentication authentication) {
        String username = authentication == null ? "no_auth" : authentication.getName();
        User user = importService.getUser(username);
        return user == null ? username : user.getName().replace(" ", "_");
    }
}
