/*
 * Copyright (c) 2019 The Hyve B.V.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.cbioportal.service.impl;

import org.cbioportal.model.StudyTreatment;
import org.cbioportal.model.Treatment;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.StudyTreatmentRepository;
import org.cbioportal.persistence.TreatmentRepository;
import org.cbioportal.service.StudyTreatmentService;
import org.cbioportal.service.TreatmentService;
import org.cbioportal.service.exception.TreatmentNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class StudyTreatmentServiceImpl implements StudyTreatmentService {
	
	@Autowired
	private StudyTreatmentRepository studyTreatmentRepository;

    @Override
    public List<StudyTreatment> getTreatmentsForStudy(String studyId) {
        Map<String, List<StudyTreatment>> samplesPerPatient = studyTreatmentRepository.getSamplesForStudy(studyId).stream()
            .collect(Collectors.groupingBy(StudyTreatment::getPatient));
        
        
        
        
        

        return studyTreatmentRepository.getTreatmentsForStudy(studyId);
    }
    
    private StudyTreatmentRow processSamples(
        List<StudyTreatment> samples,
        StudyTreatmentRow pre,
        StudyTreatmentRow post,
        int treatmentEnd
    ) {
        for (StudyTreatment sample : samples) {
            if (sample.getStartDate() < treatmentEnd) {
                post.count++;
            } else {
                pre.count++;
            }
        }
    }
    
    private final class StudyTreatmentRow {
        public final String treatment;
        public final TemporalRelation preOrPost;
        public int count;
        public float frequency;

        public StudyTreatmentRow(String treatment, TemporalRelation preOrPost) {
            this.treatment = treatment;
            this.preOrPost = preOrPost;
            this.count = 0;
        }

        @Override
        public String toString() {
            return preOrPost.name() + "-" + treatment; 
        }
    }
    
    private static enum TemporalRelation {
        Pre, Post;
    }
    
    
}