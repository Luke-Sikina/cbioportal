package org.cbioportal.web.parameter;

import org.cbioportal.model.TemporalRelation;

public class SampleTreatmentFilter {
    private TemporalRelation preOrPost;
    private String treatment;

    public TemporalRelation getPreOrPost() {
        return preOrPost;
    }

    public void setPreOrPost(TemporalRelation preOrPost) {
        this.preOrPost = preOrPost;
    }

    public String getTreatment() {
        return treatment;
    }

    public void setTreatment(String treatment) {
        this.treatment = treatment;
    }
}
