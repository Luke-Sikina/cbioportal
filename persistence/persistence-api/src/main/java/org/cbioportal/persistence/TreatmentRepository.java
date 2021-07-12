package org.cbioportal.persistence;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cbioportal.model.ClinicalEventSample;
import org.cbioportal.model.GroupedClinicalEvent;
import org.cbioportal.model.Treatment;
import org.springframework.cache.annotation.Cacheable;

public interface TreatmentRepository {
    public List<GroupedClinicalEvent> getEventTimeline(List<String> eventValues, List<String> studyIds);
    public List<GroupedClinicalEvent> getEventTimeline(List<String> eventValues, byte[] hash);

    @Cacheable(cacheResolver = "generalRepositoryCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
    public Map<String, List<Treatment>> getTreatmentsByPatientId(List<String> sampleIds, List<String> studyIds);
    public Map<String, List<Treatment>> getTreatmentsByPatientId(byte[] hash);

    @Cacheable(cacheResolver = "generalRepositoryCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
    public Map<String, List<ClinicalEventSample>> getSamplesByPatientId(List<String> sampleIds, List<String> studyIds);
    public Map<String, List<ClinicalEventSample>> getSamplesByPatientId(byte[] hash);

    @Cacheable(cacheResolver = "generalRepositoryCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
    public Map<String, List<ClinicalEventSample>> getShallowSamplesByPatientId(List<String> sampleIds, List<String> studyIds);
    public Map<String, List<ClinicalEventSample>> getShallowSamplesByPatientId(byte[] hash);

    @Cacheable(cacheResolver = "generalRepositoryCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
    public Set<String> getAllUniqueTreatments(List<String> sampleIds, List<String> studyIds);
    public Set<String> getAllUniqueTreatments(byte[] hash);

    @Cacheable(cacheResolver = "generalRepositoryCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
    public Integer getTreatmentCount(List<String> studies);

    @Cacheable(cacheResolver = "generalRepositoryCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
    public Integer getSampleCount(List<String> studies);
}
