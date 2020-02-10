package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.StudyTreatment;
import org.cbioportal.model.Treatment;
import org.cbioportal.model.meta.BaseMeta;

import java.util.List;

public interface StudyTreatmentMapper {

    List<StudyTreatment> getTreatmentsForStudy(String studyId);
    List<StudyTreatment> getSamplesForStudy(String studyId);
}