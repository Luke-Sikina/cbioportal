package org.cbioportal.model;

import java.util.Date;

public class ImportLog {
    private int id;
    private boolean testRun;
    private String studyId;
    private String text;
    private String rawText;
    private String logType;
    private Date startDate;
    private String requester;
    private String passed;

    public String getPassed() {
        return passed;
    }

    public void setPassed(String passed) {
        this.passed = passed;
    }

    public String getRawText() {
        return rawText;
    }

    public void setRawText(String rawText) {
        this.rawText = rawText;
    }

    public String getRequester() {
        return requester;
    }

    public void setRequester(String requester) {
        this.requester = requester;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean getTestRun() {
        return testRun;
    }

    public void setTestRun(boolean testRun) {
        this.testRun = testRun;
    }

    public String getStudyId() {
        return studyId;
    }

    public void setStudyId(String studyId) {
        this.studyId = studyId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getLogType() {
        return logType;
    }

    public void setLogType(String logType) {
        this.logType = logType;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
}
