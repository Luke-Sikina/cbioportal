package org.cbioportal.model;

import java.util.Date;
import java.util.List;

public class ImportStudy {
    private String studyId;
    private String name;
    private String studyPath;
    private boolean imported;
    private boolean validated;
    private Date importDate;
    private Date validationDate;
    private boolean importRunning;
    private boolean validationRunning;
    private List<String> importLogs;
    private List<String> validationLogs;
    private List<String> users;

    public String getStudyPath() {
        return studyPath;
    }

    public void setStudyPath(String studyPath) {
        this.studyPath = studyPath;
    }

    public List<String> getImportLogs() {
        return importLogs;
    }

    public void setImportLogs(List<String> importLogs) {
        this.importLogs = importLogs;
    }

    public List<String> getValidationLogs() {
        return validationLogs;
    }

    public void setValidationLogs(List<String> validationLogs) {
        this.validationLogs = validationLogs;
    }

    public String getStudyId() {
        return studyId;
    }

    public void setStudyId(String studyId) {
        this.studyId = studyId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isImported() {
        return imported;
    }

    public void setImported(boolean imported) {
        this.imported = imported;
    }

    public boolean isValidated() {
        return validated;
    }

    public void setValidated(boolean validated) {
        this.validated = validated;
    }

    public Date getImportDate() {
        return importDate;
    }

    public void setImportDate(Date importDate) {
        this.importDate = importDate;
    }

    public Date getValidationDate() {
        return validationDate;
    }

    public void setValidationDate(Date validationDate) {
        this.validationDate = validationDate;
    }

    public boolean isImportRunning() {
        return importRunning;
    }

    public void setImportRunning(boolean importRunning) {
        this.importRunning = importRunning;
    }

    public boolean isValidationRunning() {
        return validationRunning;
    }

    public void setValidationRunning(boolean validationRunning) {
        this.validationRunning = validationRunning;
    }

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }
}
