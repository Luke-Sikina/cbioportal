window.initDatamanager = function (genetic_profile_ids, oql_query, cancer_study_ids, sample_ids, z_score_threshold, rppa_score_threshold,
					case_set_properties, cancer_study_names, profile_ids) {
	
	var objectValues = function (obj) {
	    return Object.keys(obj).map(function (key) {
		return obj[key];
	    });
	};
	var objectKeyDifference = function(from, by) {
	    var ret = {};
	    var from_keys = Object.keys(from);
	    for (var i=0; i<from_keys.length; i++) {
		if (!by[from_keys[i]]) {
		    ret[from_keys[i]] = true;
		}
	    }
	    return ret;
	};
	var objectKeyValuePairs = function(obj) {
	    return Object.keys(obj).map(function(key) {
		return [key, obj[key]];
	    });
	};
	var objectKeyUnion = function(list_of_objs) {
	    var union = {};
	    for (var i=0; i<list_of_objs.length; i++) {
		var keys = Object.keys(list_of_objs[i]);
		for (var j=0; j<keys.length; j++) {
		    union[keys[j]] = true;
		}
	    }
	    return union;
	};
	var stringListToObject = function(list) {
	    var ret = {};
	    for (var i=0; i<list.length; i++) {
		ret[list[i]] = true;
	    }
	    return ret;
	};
	var stringListDifference = function(from, by) {
	    return Object.keys(
		    objectKeyDifference(
			stringListToObject(from),
			stringListToObject(by)));
	};
	var stringListUnion = function(list_of_string_lists) {
	    return Object.keys(
		    objectKeyUnion(
		    list_of_string_lists.map(function(string_list) {
			return stringListToObject(string_list);
		    })
		    ));
	};
	var stringListUnique = function(list) {
	    return Object.keys(stringListToObject(list));
	};
	var getSimplifiedMutationType = function (type) {
	    var ret = null;
	    type = type.toLowerCase();
	    switch (type) {
		case "missense_mutation":
		case "missense":
		case "missense_variant":
		    ret = "missense";
		    break;
		case "frame_shift_ins":
		case "frame_shift_del":
		case "frameshift":
		case "frameshift_deletion":
		case "frameshift_insertion":
		case "de_novo_start_outofframe":
		case "frameshift_variant":
		    ret = "frameshift";
		    break;
		case "nonsense_mutation":
		case "nonsense":
		case "stopgain_snv":
		    ret = "nonsense";
		    break;
		case "splice_site":
		case "splice":
		case "splice site":
		case "splicing":
		case "splice_site_snp":
		case "splice_site_del":
		case "splice_site_indel":
		    ret = "splice";
		    break;
		case "translation_start_site":
		case "start_codon_snp":
		case "start_codon_del":
		    ret = "nonstart";
		    break;
		case "nonstop_mutation":
		    ret = "nonstop";
		    break;
		case "fusion":
		    ret = "fusion";
		    break;
		case "in_frame_del":
		case "in_frame_ins":
		case "indel":
		case "nonframeshift_deletion":
		case "nonframeshift":
		case "nonframeshift insertion":
		case "nonframeshift_insertion":
		case "targeted_region":
		    ret = "inframe";
		    break;
		default:
		    ret = "other";
		    break;
	    }
	    return ret;
	};
	
	var getCBioPortalMutationCounts = function (webservice_data) {
	    /* In: - webservice_data, a list of data obtained from the webservice API
	     * Out: Map from gene+","+start_pos+","+end_pos to cbioportal mutation count for that position range and gene
	     */
	    var counts_map = {};
	    var def = new $.Deferred();
	    var to_query = {};
	    for (var i = 0; i < webservice_data.length; i++) {
		var datum = webservice_data[i];
		if (datum.genetic_alteration_type !== "MUTATION_EXTENDED") {
		    continue;
		}
		var gene = datum.hugo_gene_symbol;
		var start_pos = datum.protein_start_position;
		var end_pos = datum.protein_end_position;
		if (gene && start_pos && end_pos && !isNaN(start_pos) && !isNaN(end_pos)) {
		    to_query[gene + ',' + parseInt(start_pos, 10) + ',' + parseInt(end_pos, 10)] = true;
		}
	    }
	    var queries = Object.keys(to_query).map(function (x) {
		var splitx = x.split(',');
		return {
		    gene: splitx[0],
		    start_pos: splitx[1],
		    end_pos: splitx[2]
		};
	    });
	    var genes = queries.map(function (q) {
		return q.gene;
	    });
	    var starts = queries.map(function (q) {
		return q.start_pos;
	    });
	    var ends = queries.map(function (q) {
		return q.end_pos;
	    });

	    window.cbioportal_client.getMutationCounts({
		'type': 'count',
		'per_study': false,
		'gene': genes,
		'start': starts,
		'end': ends,
		'echo': ['gene', 'start', 'end']
	    }).then(function (counts) {
		for (var i = 0; i < counts.length; i++) {
		    var gene = counts[i].gene;
		    var start = parseInt(counts[i].start, 10);
		    var end = parseInt(counts[i].end, 10);
		    counts_map[gene + ',' + start + ',' + end] = parseInt(counts[i].count, 10);
		}
		def.resolve(counts_map);
	    }).fail(function () {
		def.reject();
	    });
	    return def.promise();
	};
	var annotateCBioPortalMutationCount = function(webservice_data) {
	   /* in-place, idempotent
	    * In: - webservice_data, a list of data obtained from the webservice API
	    *	  - attribute_name, a string, where the count will be recorded in mutation data
	    * Out: promise, which resolves with the data which has been in-place modified,
	    *	    the mutation data given the integer attribute 'cbioportal_position_recurrence'
	    */
	   var def = new $.Deferred();
	   var attribute_name = 'cbioportal_mutation_count';
	   getCBioPortalMutationCounts(webservice_data).then(function(counts_map) {
	       for (var i=0; i<webservice_data.length; i++) {
		    var datum = webservice_data[i];
		    if (datum.genetic_alteration_type !== "MUTATION_EXTENDED") {
			continue;
		    }
		    var gene = datum.hugo_gene_symbol;
		    var start_pos = datum.protein_start_position;
		    var end_pos = datum.protein_end_position;
		    if (gene && start_pos && end_pos && !isNaN(start_pos) && !isNaN(end_pos)) {
			datum[attribute_name] = counts_map[gene + ',' + parseInt(start_pos, 10) + ',' + parseInt(end_pos, 10)];
		    }
	       }
	       def.resolve(webservice_data);
	   });
	   return def.promise();
	};
	
	var makeOncoprintClinicalData = function (webservice_clinical_data, sample_or_patient) {
	    var id_to_datum = {};
	    var id_attribute = (sample_or_patient === "sample" ? "sample_id" : "patient_id");
	    var ret = [];
	    for (var i = 0, _len = webservice_clinical_data.length; i < _len; i++) {
		var d = webservice_clinical_data[i];
		var id = d[id_attribute];
		if (!id_to_datum[id]) {
		    id_to_datum[id] = {'attr_id': d.attr_id, 'attr_val_counts':{}}
		}
		id_to_datum[id].attr_val_counts[d.attr_val] = id_to_datum[id].attr_val_counts[d.attr_val] || 0;
		id_to_datum[id].attr_val_counts[d.attr_val] += 1;
		
		d[sample_or_patient] = id;
	    }
	    var data = objectValues(id_to_datum);
	    for (var i=0; i<data.length; i++) {
		
	    }
	    return ret;
	};
	var makeOncoprintData = function(webservice_data, genes, ids, sample_or_patient, sample_to_patient_map) {
	    // To fill in for non-existent data, need genes and samples to do so for
	    genes = genes || [];
	    ids = ids || []; // to make blank data
	    // Gather data by id and gene
	    var gene_and_id_to_datum = {};
	    for (var i = 0; i < genes.length; i++) {
		var gene = genes[i].toLowerCase();
		for (var j = 0; j < ids.length; j++) {
		    var id = ids[j];
		    var new_datum = {};
		    new_datum['gene'] = gene;
		    new_datum[sample_or_patient] = id;
		    new_datum['data'] = [];
		    gene_and_id_to_datum[gene + "," + id] = new_datum;
		}
	    }
	    for (var i = 0; i < webservice_data.length; i++) {
		var datum = webservice_data[i];
		var gene = datum.hugo_gene_symbol.toLowerCase();
		var id = (sample_or_patient === "patient" ? sample_to_patient_map[datum.sample_id] : datum.sample_id);
		var gene_id_datum = gene_and_id_to_datum[gene + "," + id];
		if (gene_id_datum) {
		    gene_id_datum.data.push(datum);
		}
	    }

	    // Compute display parameters
	    var data = objectValues(gene_and_id_to_datum);
	    var cna_profile_data_to_string = {
		"-2": "homdel",
		"-1": "hetloss",
		"0": undefined,
		"1": "gain",
		"2": "amp"
	    };
	    var mut_rendering_priority = {'fusion': 0, 'trunc': 1, 'inframe': 2, 'missense': 3};
	    var cna_rendering_priority = {'amp': 0, 'homdel': 0, 'gain': 1, 'hetloss': 1};
	    var mrna_rendering_priority = {'up': 0, 'down': 0};
	    var prot_rendering_priority = {'up': 0, 'down': 0};

	    var selectDisplayValue = function (counts_obj, rendering_priority_obj) {
		var options = objectKeyValuePairs(counts_obj);
		if (options.length > 0) {
		    options.sort(function (kv1, kv2) {
			var rendering_priority_diff = rendering_priority_obj[kv1[0]] - rendering_priority_obj[kv2[0]];
			if (rendering_priority_diff < 0) {
			    return -1;
			} else if (rendering_priority_diff > 0) {
			    return 1;
			} else {
			    if (kv1[1] < kv2[1]) {
				return 1;
			    } else if (kv1[1] > kv2[1]) {
				return -1;
			    } else {
				return 0;
			    }
			}
		    });
		    return options[0][0];
		} else {
		    return undefined;
		}
	    };
	    for (var i = 0; i < data.length; i++) {
		var datum = data[i];
		var datum_events = datum.data;

		var disp_cna_counts = {};
		var disp_mrna_counts = {};
		var disp_prot_counts = {};
		var disp_mut_counts = {};
		var disp_mut_has_rec = {};

		for (var j = 0; j < datum_events.length; j++) {
		    var event = datum_events[j];
		    if (event.genetic_alteration_type === "COPY_NUMBER_ALTERATION") {
			var cna_event = cna_profile_data_to_string[event.profile_data];
			disp_cna_counts[cna_event] = disp_cna_counts[cna_event] || 0;
			disp_cna_counts[cna_event] += 1;
		    } else if (event.genetic_alteration_type === "MRNA_EXPRESSION") {
			if (datum.oql_regulation_direction) {
			    var mrna_event = (datum.oql_regulation_direction === 1 ? "up" : "down");
			    disp_mrna_counts[mrna_event] = disp_mrna_counts[mrna_event] || 0;
			    disp_mrna_counts[mrna_event] += 1;
			}
		    } else if (event.genetic_alteration_type === "PROTEIN_LEVEL") {
			if (datum.oql_regulation_direction) {
			    var prot_event = (datum.oql_regulation_direction === 1 ? "up" : "down");
			    disp_prot_counts[prot_event] = disp_prot_counts[prot_event] || 0;
			    disp_prot_counts[prot_event] += 1;
			}
		    } else if (event.genetic_alteration_type === "MUTATION_EXTENDED") {
			var mutation_type = event.simplified_mutation_type;
			// clamp all mutation types into one of the following four
			var oncoprint_mutation_type = (["missense", "inframe", "fusion"].indexOf(mutation_type) > -1 ? mutation_type : "trunc");
			disp_mut_counts[oncoprint_mutation_type] = disp_mut_counts[oncoprint_mutation_type] || 0;
			disp_mut_counts[oncoprint_mutation_type] += 1;

			if (event.cbioportal_mutation_count > 10) {
			    disp_mut_has_rec[oncoprint_mutation_type] = true;
			}
		    }
		}
		datum.disp_cna = selectDisplayValue(disp_cna_counts, cna_rendering_priority);
		datum.disp_mrna = selectDisplayValue(disp_mrna_counts, mrna_rendering_priority);
		datum.disp_prot = selectDisplayValue(disp_prot_counts, prot_rendering_priority);
		var disp_mut = selectDisplayValue(disp_mut_counts, mut_rendering_priority);
		datum.disp_mut = disp_mut;
		if (disp_mut) {
		    datum.disp_mut += (disp_mut_has_rec[disp_mut] ? '_rec' : '');
		}
	    }
	    return data;
	};
	var makeOncoprintSampleData = function(webservice_data, genes, sample_ids) {
	    return makeOncoprintData(webservice_data, genes, sample_ids, "sample");
	};
	var makeOncoprintPatientData = function(webservice_data, genes, patient_ids, sample_to_patient_map) {
	    return makeOncoprintData(webservice_data, genes, patient_ids, "patient", sample_to_patient_map);
	};
	
	return (function() {
	    var webservice_genomic_event_data;
	    var oncoprint_sample_genomic_event_data;
	    var oncoprint_patient_genomic_event_data;
	    var altered_samples;
	    
	    var default_oql = '';
		var dm_ret = {
			'oql_query': oql_query,
			'cancer_study_ids': cancer_study_ids,
			'sample_ids': sample_ids,
			'genetic_profile_ids': genetic_profile_ids,
			'mutation_counts':{},
			'getOQLQuery': function() {
				return this.oql_query;
			},
			'getQueryGenes': function() {
				return OQL.genes(this.oql_query);
			},
			'getGeneticProfileIds': function() {
				return this.genetic_profile_ids;
			},
			'getSampleIds': function() {
				return this.sample_ids;
			},
			'getPatientIds': function() {
				var def = new $.Deferred();
				var patients = {};
				window.cbioportal_client.getSamples({study_id: [this.getCancerStudyIds()[0]], sample_ids: this.getSampleIds()}).then(function(samples) {
				    for (var i=0; i<samples.length; i++) {
					patients[samples[i].patient_id] = true;
				    }
				    def.resolve(Object.keys(patients));
				});
				return def.promise();
			},
			'getCancerStudyIds': function() {
				return this.cancer_study_ids;
			},
			'getSampleSelect': function () {
			    return this.sample_select;
			},
			'getWebServiceGenomicEventData': function() {
			    var def = new $.Deferred();
			    if (webservice_genomic_event_data) {
				def.resolve(webservice_genomic_event_data);
			    } else {
				var self = this;
				var profile_types = {};
				window.cbioportal_client.getGeneticProfiles({genetic_profile_ids: dm_ret.getGeneticProfileIds()}).fail(function() {
				    def.reject();
				}).then(function(gp_response) {
				    for (var i=0; i<gp_response.length; i++) {
					profile_types[gp_response[i].id] = gp_response[i].genetic_alteration_type;
				    }
				    (function setDefaultOQL() {
					var all_profile_types = objectValues(profile_types);
					var default_oql_uniq = {};
					for (var i=0; i<all_profile_types.length; i++) {
					    var type = all_profile_types[i];
					    switch(type) {
						case "MUTATION_EXTENDED":
						    default_oql_uniq["MUT"] = true;
						    break;
						case "COPY_NUMBER_ALTERATION":
						    default_oql_uniq["AMP"] = true;
						    default_oql_uniq["HOMDEL"] = true;
						    break;
						case "MRNA_EXPRESSION":
						    default_oql_uniq["EXP>=" + z_score_threshold] = true;
						    default_oql_uniq["EXP<=-" + z_score_threshold] = true;
						    break;
						case "PROTEIN_LEVEL":
						    default_oql_uniq["PROT>=" + rppa_score_threshold] = true;
						    default_oql_uniq["PROT<=-" + rppa_score_threshold] = true;
						    break;
					    }
					}
					default_oql = Object.keys(default_oql_uniq).join(" ");
				    })();
				}).fail(function() {
				    def.reject();
				}).then(function() {
				    var genetic_profile_ids = self.getGeneticProfileIds();
				    var num_calls = genetic_profile_ids.length;
				    var all_data = [];
				    for (var i=0; i<self.getGeneticProfileIds().length; i++) {
					(function(I) {
					    window.cbioportal_client.getGeneticProfileDataBySample({
						'genetic_profile_ids':[genetic_profile_ids[I]], 
						'genes': self.getQueryGenes(),
						'sample_ids': self.getSampleIds()
					    }).fail(function() {
						def.reject();
					    }).then(function(data) {
						var genetic_alteration_type = profile_types[genetic_profile_ids[I]];
						if (genetic_alteration_type === "MUTATION_EXTENDED") {
						    for (var j=0; j<data.length; j++) {
							data[j].simplified_mutation_type = getSimplifiedMutationType(data[j].mutation_type);
							data[j].genetic_alteration_type = genetic_alteration_type;
						    }
						} else {
						    for (var j=0; j<data.length; j++) {
							data[j].genetic_alteration_type = genetic_alteration_type;
						    }
						}
						all_data = all_data.concat(data);

						num_calls -=1;
						if (num_calls === 0) {
						    webservice_genomic_event_data = OQL.filterCBioPortalWebServiceData(self.getOQLQuery(), all_data, default_oql, false);
						    annotateCBioPortalMutationCount(webservice_genomic_event_data).then(function() {
							def.resolve(webservice_genomic_event_data);
						    });
						}
					    });
					})(i);
				    }
				}).fail(function() {
				    def.reject();
				});
			    }
			    return def.promise();
			},
			'getOncoprintSampleGenomicEventData': function() {
			    var def = new $.Deferred();
			    var self = this;
			    if (oncoprint_sample_genomic_event_data) {
				def.resolve(oncoprint_sample_genomic_event_data);
			    } else {
				this.getWebServiceGenomicEventData().then(function(ws_data) {
				    var ws_data_by_oql_line = OQL.filterCBioPortalWebServiceData(self.getOQLQuery(), ws_data, default_oql, true);
				    for (var i=0; i<ws_data_by_oql_line.length; i++) {
					var line = ws_data_by_oql_line[i];
					line.oncoprint_data = makeOncoprintSampleData(line.data, [line.gene], self.getSampleIds());
					line.altered_samples = line.oncoprint_data.filter(function(datum) { return datum.data.length > 0; })
										    .map(function(datum) { return datum.sample; });
					line.unaltered_samples = stringListDifference(self.getSampleIds(), line.altered_samples);
				    }
				    oncoprint_sample_genomic_event_data = ws_data_by_oql_line;
				    def.resolve(oncoprint_sample_genomic_event_data);
				});
			    }
			    return def.promise();
			},
			'getAlteredSamples': function() {
			    var def = new $.Deferred();
			    if (altered_samples) {
				def.resolve(altered_samples);
			    } else {
				this.getOncoprintSampleGenomicEventData().then(function(data_by_line) {
				    altered_samples = stringListUnion(data_by_line.map(function(line) {
					return line.altered_samples;
				    }));
				    def.resolve(altered_samples);
				});
			    }
			    return def.promise();
			},
			'getUnalteredSamples': function() {
			    var def = new $.Deferred();
			    var self = this;
			    this.getAlteredSamples().then(function(altered_samples) {
				def.resolve(stringListDifference(self.getSampleIds(), altered_samples));
			    });
			    return def.promise();
			},
			'getOncoprintPatientGenomicEventData': function() {
			    var def = new $.Deferred();
			    var self = this;
			    if (oncoprint_patient_genomic_event_data) {
				def.resolve(oncoprint_patient_genomic_event_data);
			    } else {
				$.when(this.getWebServiceGenomicEventData(), this.getPatientIds(), this.getPatientSampleIdMap()).then(function(ws_data, patient_ids, sample_to_patient_map) {
				    var ws_data_by_oql_line = OQL.filterCBioPortalWebServiceData(self.getOQLQuery(), ws_data, default_oql, true);
				    for (var i = 0; i < ws_data_by_oql_line.length; i++) {
					var line = ws_data_by_oql_line[i];
					line.oncoprint_data = makeOncoprintPatientData(ws_data_by_oql_line[i].data, [ws_data_by_oql_line[i].gene], patient_ids, sample_to_patient_map);
					line.altered_patients = line.oncoprint_data.filter(function (datum) { return datum.data.length > 0; })
										    .map(function (datum) { return datum.patient; });
					line.unaltered_patients = stringListDifference(patient_ids, line.altered_patients);
				    }
				    oncoprint_patient_genomic_event_data = ws_data_by_oql_line;
				    def.resolve(oncoprint_patient_genomic_event_data);
				});
			    }
			    return def.promise();
			},
			'getAlteredPatients': function() {
			    var def = new $.Deferred();
			    $.when(this.getAlteredSamples(), this.getPatientSampleIdMap()).then(function(altered_samples, sample_to_patient_map) {
				def.resolve(stringListUnique(altered_samples.map(function(s) { return sample_to_patient_map[s]; })));
			    });
			    return def.promise();
			},
			'getUnalteredPatients': function() {
			    var def = new $.Deferred();
			    $.when(this.getAlteredPatients(), this.getPatientIds()).then(function(altered_patients, patient_ids) {
				def.resolve(stringListDifference(patient_ids, altered_patients));
			    });
			    return def.promise();
			},
			'getSampleClinicalAttributes': function () {
				// TODO: handle more than one study
				return window.cbioportal_client.getSampleClinicalAttributes({study_id: [this.getCancerStudyIds()[0]], sample_ids: this.getSampleIds()});
			},
			'getSampleClinicalData': function (attribute_ids) {
				// TODO: handle more than one study
				var def = new $.Deferred();
				window.cbioportal_client.getSampleClinicalData({study_id: [this.getCancerStudyIds()[0]], attribute_ids: attribute_ids, sample_ids: this.getSampleIds()}).then(function(data) {
					def.resolve(makeOncoprintClinicalData(data, true));
				}).fail(function() {
					def.reject();
				});
				return def.promise();
			},
			'getPatientClinicalAttributes': function() {
			    var def = new $.Deferred();
			    var self = this;
			    this.getPatientIds().then(function(patient_ids) {
				window.cbioportal_client.getPatientClinicalAttributes({study_id: [self.getCancerStudyIds()[0]], patient_ids: patient_ids}).then(function(attrs) {
				    def.resolve(attrs);
				}).fail(function() {
				    def.reject();
				});
			    }).fail(function() {
				def.reject();
			    });
			    return def.promise();
			},
			'getPatientClinicalData': function(attribute_ids) {
			    // TODO: handle more than one study
			    var def = new $.Deferred();
			    var self = this;
			    this.getPatientIds().then(function(patient_ids) {
				window.cbioportal_client.getPatientClinicalData({study_id: [self.getCancerStudyIds()[0]], attribute_ids: attribute_ids, patient_ids: patient_ids}).then(function(data) {
				    def.resolve(makeOncoprintClinicalData(data, false));
				}).fail(function() {
				    def.reject();
				});
			    }).fail(function() {
				def.reject();
			    });
			    return def.promise();
			},
			'getCaseSetId': function() {
				return case_set_properties.case_set_id;
			},
			'getCaseIdsKey': function() {
				return case_set_properties.case_ids_key;
			},
			'getSampleSetName': function() {
				return case_set_properties.case_set_name;
			},
			'getSampleSetDescription': function() {
				return case_set_properties.case_set_description;
			},
						'getPatientSampleIdMap': (function() {
				var sample_to_patient = {};
				var loaded = false;
				return function() {
					var def = new $.Deferred();
					if (loaded) {
						def.resolve(sample_to_patient);
					} else {
						window.cbioportal_client.getSamples({study_id: [this.getCancerStudyIds()[0]], sample_ids: this.getSampleIds()}).then(function(data) {
							for (var i=0; i<data.length; i++) {
								sample_to_patient[data[i].id] = data[i].patient_id;
							}
							loaded = true;
							def.resolve(sample_to_patient);
						}).fail(function() {
							def.reject();
						});
					}
					return def.promise();
				};
			})(),
			'getCancerStudyNames': function() {
				return cancer_study_names;
			},
			'getMutationProfileId': function() {
				return profile_ids.mutation_profile_id;
			}
						
		};
		
		return dm_ret;
	})();
};
