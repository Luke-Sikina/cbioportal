package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.ImportLog;
import org.cbioportal.model.ImportStudy;
import org.cbioportal.persistence.ImportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ImportMyBatisRepository implements ImportRepository {
    @Autowired
    ImportMapper importMapper;
    
    @Override
    public ImportStudy getStudy(String study) {
        return importMapper.getStudy(study);
    }

    @Override
    public List<String> getUsersForStudy(String study) {
        return importMapper.getUsersForStudy(study);
    }

    @Override
    public List<ImportLog> getAllLogsForStudy(String study, String logType) {
        return importMapper.getAllLogsForStudy(study, logType);
    }

    @Override
    public ImportLog getLog(String logType, String study, String id) {
        return importMapper.getLog(logType, study, id);
    }

    @Override
    public List<ImportStudy> getAllStudies() {
        return importMapper.getAllStudies();
    }

    @Override
    public void addImportLog(ImportLog importLog) {
        importMapper.addImportLog(importLog);
    }

    @Override
    public void updateStudyAsValidating(String study) {
        importMapper.updateStudyAsValidating(study);
    }

    @Override
    public void updateStudyAsImporting(String study) {
        importMapper.updateStudyAsImporting(study);
    }

    @Override
    public Integer getLastId() {
        return importMapper.getLastId();
    }
}
