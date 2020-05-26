package org.cbioportal.persistence;

import org.cbioportal.model.ImportLog;
import org.cbioportal.model.ImportStudy;

import java.util.List;

public interface ImportRepository {
    ImportStudy getStudy(String study);
    List<String> getUsersForStudy(String study);
    List<ImportLog> getAllLogsForStudy(String study, String logType);
    ImportLog getLog(String logType, String study, String id);
    List<ImportStudy> getAllStudies();
    void addImportLog(ImportLog importLog);
    Integer getLastId();
    void updateStudyAsValidating(String study);
    void updateStudyAsImporting(String study);
}
