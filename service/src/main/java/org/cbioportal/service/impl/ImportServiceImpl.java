package org.cbioportal.service.impl;

import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.ImportLog;
import org.cbioportal.model.ImportStudy;
import org.cbioportal.model.User;
import org.cbioportal.persistence.ImportRepository;
import org.cbioportal.persistence.SecurityRepository;
import org.cbioportal.service.ImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ImportServiceImpl implements ImportService {
    @Autowired
    ImportRepository importRepository;
    
    @Autowired
    SecurityRepository securityRepository;
    
    @Override
    public ImportStudy getStudy(String study) {
        ImportStudy s = importRepository.getStudy(study);
        if (s == null) {
            return null;
        }
        s.setUsers(importRepository.getUsersForStudy("cbioportal:" + study.toUpperCase()));
        return s;
    }
    
    @Override
    public ImportLog getLog(String logType, String study, String id) {
        return importRepository.getLog(logType, study, id);
    }

    @Override
    @PostFilter("hasPermission(filterObject, 'read')")
    public List<ImportStudy> getAllStudies() {
        return new ArrayList<>(importRepository.getAllStudies());
    }

    @Override
    public List<ImportLog> getAllLogsForStudy(String study, String logType) {
        return importRepository.getAllLogsForStudy(study, logType);
    }

    @Override
    public void addImportLog(ImportLog importLog) {
        importRepository.addImportLog(importLog);
    }

    @Override
    public Integer getLastId() {
        return importRepository.getLastId();
    }

    @Override
    public void updateStudyAsValidating(String study) {
        importRepository.updateStudyAsValidating(study);
    }

    @Override
    public void updateStudyAsImporting(String study) {
        importRepository.updateStudyAsImporting(study);
    }

    @Override
    public User getUser(String username) {
        return securityRepository.getPortalUser(username);
    }
}
