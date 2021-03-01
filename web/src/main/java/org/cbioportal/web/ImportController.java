package org.cbioportal.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.cbioportal.model.ImportLog;
import org.cbioportal.model.ImportStudy;
import org.cbioportal.model.User;
import org.cbioportal.service.ImportService;
import org.cbioportal.web.config.annotation.InternalApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@InternalApi
@RestController
@Validated
@Api(tags = "Import", description = " ")
public class ImportController {
    private final CloseableHttpClient httpClient = HttpClients.createDefault();
    
    @Autowired
    ImportService importService;

    @RequestMapping(value = "/logs/{logType}/{studyId}/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get the specified log file")
    public ResponseEntity<ImportLog> getLog(
        @PathVariable("logType") String logType,
        @PathVariable("studyId") String studyId,
        @PathVariable("id") String id,
        Authentication authentication
    ) {
        HttpGet request = new HttpGet("http://importer:8080/log/" + studyId + "/" + id);
        setUserIdHeader(authentication, request);

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpStatus status = HttpStatus.resolve(response.getStatusLine().getStatusCode());
            ObjectMapper mapper = new ObjectMapper();
            ImportLog importLog = mapper.readValue(EntityUtils.toString(response.getEntity()), ImportLog.class);
            return new ResponseEntity<>(importLog, status == null ? HttpStatus.INTERNAL_SERVER_ERROR : status);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/logs/{logType}/{studyId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get the specified log file")
    public ResponseEntity<List<ImportLog>> getAllLogsForStudy(
        @PathVariable("logType") String logType,
        @PathVariable("studyId") String studyId,
        Authentication authentication
    ) {
        HttpGet request = new HttpGet("http://importer:8080/logs/" + studyId + "/" + logType);
        setUserIdHeader(authentication, request);


        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpStatus status = HttpStatus.resolve(response.getStatusLine().getStatusCode());
            ObjectMapper mapper = new ObjectMapper();
            List<ImportLog> importLogs = Arrays.asList(mapper.readValue(EntityUtils.toString(response.getEntity()), ImportLog[].class));
            return new ResponseEntity<>(importLogs, status == null ? HttpStatus.INTERNAL_SERVER_ERROR : status);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
    }

    @RequestMapping(value = "/importer/", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get a list of all studies in the importer")
    public ResponseEntity<List<ImportStudy>> getAllImporterStudies(Authentication authentication) {
        HttpGet request = new HttpGet("http://importer:8080/studies");
        setUserIdHeader(authentication, request);

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpStatus status = HttpStatus.resolve(response.getStatusLine().getStatusCode());
            ObjectMapper mapper = new ObjectMapper();
            List<ImportStudy> studies = Arrays.asList(mapper.readValue(EntityUtils.toString(response.getEntity()), ImportStudy[].class));
            return new ResponseEntity<>(studies, status == null ? HttpStatus.INTERNAL_SERVER_ERROR : status);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/importer/{studyId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get study details")
    public ResponseEntity<ImportStudy> getImporterStudy(
        @PathVariable("studyId") String studyId,
        Authentication authentication
    ) {
        HttpGet request = new HttpGet("http://importer:8080/studies/" + studyId);
        setUserIdHeader(authentication, request);

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpStatus status = HttpStatus.resolve(response.getStatusLine().getStatusCode());
            ObjectMapper mapper = new ObjectMapper();
            ImportStudy importLog = mapper.readValue(EntityUtils.toString(response.getEntity()), ImportStudy.class);
            return new ResponseEntity<>(importLog, status == null ? HttpStatus.INTERNAL_SERVER_ERROR : status);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/importer/{studyId}/import", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Run a trial import of the studyId")
    public ResponseEntity<String> runTrialImport(
        @PathVariable("studyId") String studyId,
        Authentication authentication
    ) throws IOException, InterruptedException {
        String username = getUserName(authentication);
        HttpGet request = new HttpGet("http://importer:8080/importer/" + studyId + "/" + username + "/import");
        setUserIdHeader(authentication, request);

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpStatus status = HttpStatus.resolve(response.getStatusLine().getStatusCode());
            return new ResponseEntity<>(status == null ? HttpStatus.INTERNAL_SERVER_ERROR : status);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/importer/{studyId}/validate", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Run a trial validation of the studyId")
    public ResponseEntity<String> runTrialValidation(
        @PathVariable("studyId") String studyId,
        Authentication authentication
    ) throws IOException, InterruptedException {
        String username = getUserName(authentication);
        HttpGet request = new HttpGet("http://importer:8080/importer/" + studyId + "/" + username + "/validate");
        setUserIdHeader(authentication, request);


        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpStatus status = HttpStatus.resolve(response.getStatusLine().getStatusCode());
            return new ResponseEntity<>(status == null ? HttpStatus.INTERNAL_SERVER_ERROR : status);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String getUserName(Authentication authentication) {
        String username = authentication == null ? "no_auth" : authentication.getName();
        User user = importService.getUser(username);
        return user == null ? username : user.getName().replace(" ", "_");
    }
    
    private void setUserIdHeader(Authentication authentication, HttpGet request) {
        String id =  authentication == null ? "no_auth" : authentication.getName();
        request.setHeader("requesterId", id);
    }
}
