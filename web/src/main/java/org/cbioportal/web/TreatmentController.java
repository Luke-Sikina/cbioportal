package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.cbioportal.model.PatientTreatmentRow;
import org.cbioportal.model.SampleTreatmentRow;
import org.cbioportal.model.TypeOfCancer;
import org.cbioportal.service.CancerTypeService;
import org.cbioportal.service.TreatmentService;
import org.cbioportal.service.exception.CancerTypeNotFoundException;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.cbioportal.web.config.annotation.PublicApi;
import org.cbioportal.web.parameter.*;
import org.cbioportal.web.parameter.sort.CancerTypeSortBy;
import org.cbioportal.web.util.StudyViewFilterApplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@PublicApi
@RestController
@Validated
@Api(tags = "Treatments", description = " ")
public class TreatmentController {

    @Autowired
    private TreatmentService treatmentService;
    
    @Autowired
    private StudyViewFilterApplier studyViewFilterApplier;

    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', 'read')")
    @RequestMapping(value = "/treatments/{studyId}/patient", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get all cancer types")
    public ResponseEntity<List<PatientTreatmentRow>> getAllPatientTreatmens(
        @ApiParam(required = true, value = "Study view filter")
        @Valid @RequestBody(required = false) StudyViewFilter studyViewFilter,
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
        @Valid
        @RequestAttribute(required = false, value = "interceptedStudyViewFilter")
        StudyViewFilter interceptedStudyViewFilter
    ) {
        List<SampleIdentifier> sampleIdentifiers = studyViewFilterApplier.apply(interceptedStudyViewFilter);
        List<String> sampleIds = sampleIdentifiers.stream()
            .map(SampleIdentifier::getSampleId)
            .distinct()
            .collect(Collectors.toList());
        List<String> studyIds = sampleIdentifiers.stream()
            .map(SampleIdentifier::getStudyId)
            .distinct()
            .collect(Collectors.toList());
        List<PatientTreatmentRow> treatments = treatmentService.getAllTreatmentPatientRows(sampleIds, studyIds);
        return new ResponseEntity<>(treatments, HttpStatus.OK);
    }


    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', 'read')")
    @RequestMapping(value = "/treatments/{studyId}/sample", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get all cancer types")
    public ResponseEntity<List<SampleTreatmentRow>> getAllSampleTreatments(
        @ApiParam(required = true, value = "Study view filter")
        @Valid @RequestBody(required = false) StudyViewFilter studyViewFilter,
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface
        @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
        @ApiIgnore // prevent reference to this attribute in the swagger-ui interface. this attribute is needed for the @PreAuthorize tag above.
        @Valid
        @RequestAttribute(required = false, value = "interceptedStudyViewFilter")
        StudyViewFilter interceptedStudyViewFilter
    ) {
        List<SampleIdentifier> sampleIdentifiers = studyViewFilterApplier.apply(interceptedStudyViewFilter);
        List<String> sampleIds = sampleIdentifiers.stream()
            .map(SampleIdentifier::getSampleId)
            .distinct()
            .collect(Collectors.toList());
        List<String> studyIds = sampleIdentifiers.stream()
            .map(SampleIdentifier::getStudyId)
            .distinct()
            .collect(Collectors.toList());
        List<SampleTreatmentRow> treatments = treatmentService.getAllTreatmentSampleRows(sampleIds, studyIds);
        return new ResponseEntity<>(treatments, HttpStatus.OK);
    }
}
