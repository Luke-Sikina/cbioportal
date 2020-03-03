package org.cbioportal.web.parameter;

import java.util.List;

public class PatientTreatmentFilter {
    private boolean has;
    private String treatment;

    public boolean isHas() {
        return has;
    }

    public void setHas(boolean has) {
        this.has = has;
    }

    public String getTreatment() {
        return treatment;
    }

    public void setTreatment(String treatment) {
        this.treatment = treatment;
    }
    
    public boolean filter(List<String> treatments) {
        if (has) {
            return treatments.stream().anyMatch(t -> t.equals(treatment));
        } else {
            return treatments.stream().noneMatch(t -> t.equals(treatment));
        }
    }
}
