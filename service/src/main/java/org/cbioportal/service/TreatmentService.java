package org.cbioportal.service;

import org.cbioportal.model.TreatmentRow;

import java.util.List;
import java.util.Map;

public interface TreatmentService {
    public List<TreatmentRow> getAllTreatmentRows(List<String> samples, List<String> studies);
    
    public Map<String, List<String>> getAllTreatmentsForEachPatient(List<String> samples, List<String> studies);
}
