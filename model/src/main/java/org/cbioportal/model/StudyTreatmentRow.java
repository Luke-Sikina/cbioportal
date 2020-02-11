package org.cbioportal.model;

import java.io.Serializable;

public class StudyTreatmentRow implements Serializable {
    public final String patientId;
    public final String treatment;
    public final TemporalRelation preOrPost;
    public int count;
    public final int end;
    public float frequency;

    public StudyTreatmentRow(String treatment, String patientId, TemporalRelation preOrPost, int end) {
        this.treatment = treatment;
        this.patientId = patientId;
        this.preOrPost = preOrPost;
        this.end = end;
        this.count = 0;
    }

    @Override
    public String toString() {
        return preOrPost.name() + "-" + treatment;
    }
}
