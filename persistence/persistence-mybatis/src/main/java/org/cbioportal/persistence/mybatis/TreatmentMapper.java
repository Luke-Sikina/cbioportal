package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.ClinicalEventSample;
import org.cbioportal.model.GroupedClinicalEvent;
import org.cbioportal.model.Treatment;

import java.util.List;
import java.util.Set;

import org.cbioportal.model.ClinicalEventSample;
import org.cbioportal.model.GroupedClinicalEvent;
import org.cbioportal.model.Treatment;

import java.util.List;
import java.util.Set;


public interface TreatmentMapper {
    List<GroupedClinicalEvent> getEventTimeline(List<String> eventValues, List<String> studyIds);
    List<GroupedClinicalEvent> getEventTimelineH(List<String> eventValues, byte[] hash);
    
    List<Treatment> getAllTreatments(List<String> sampleIds, List<String> studyIds);
    List<Treatment> getAllTreatmentsH(byte[] hash);

    List<ClinicalEventSample> getAllSamples(List<String> sampleIds, List<String> studyIds);
    List<ClinicalEventSample> getAllSamplesH(byte[] hash);
    
    List<ClinicalEventSample> getAllShallowSamples(List<String> sampleIds, List<String> studyIds);
    List<ClinicalEventSample> getAllShallowSamplesH(byte[] hash);

    Set<String> getAllUniqueTreatments(List<String> sampleIds, List<String> studyIds);
    Set<String> getAllUniqueTreatmentsH(byte[] hash);

    Integer getTreatmentCount(List<String> sampleIds, List<String> studyIds);

    Integer getSampleCount(List<String> sampleIds, List<String> studyIds);
}
