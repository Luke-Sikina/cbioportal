package org.cbioportal.service;

import org.cbioportal.model.ImportLog;
import org.cbioportal.model.ImportStudy;
import org.cbioportal.model.User;

import java.util.List;

public interface ImportService {
    ImportStudy getStudy(String study);
    ImportLog getLog(String log, String study, String id);
    List<ImportStudy> getAllStudies();
    List<ImportLog> getAllLogsForStudy(String study, String logType);
    void addImportLog(ImportLog importLog);
    Integer getLastId();
    void updateStudyAsValidating(String study);
    void updateStudyAsImporting(String study);
    User getUser(String username);
}
