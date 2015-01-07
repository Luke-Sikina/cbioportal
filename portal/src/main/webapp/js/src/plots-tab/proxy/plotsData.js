var plotsData = (function() {
    
    var data = {
                x: {
                    raw: [],
                    stat: false //whether data is retrieved
                },
                y: {
                    raw: [],
                    stat: false //whether data is retrieved
                }
            },
        stat = {
                x: {
                    min: "",
                    max: "",
                    edge: ""
                },
                y: {
                    min: "",
                    max: "",
                    edge: ""
                },
                hasCnaAnno: false,
                retrieved: false
            },
        datum = { //each associates with one individual dot in the plots 
                caseId : "",
                xVal : "",
                yVal : "",
                cna_anno: "", //if same gene on each axis and having discretized cna data
                mutation : {}  //Mutation ID
            },
        dotsContent = {}; //json of datums -- final recipe for rendering the view
        
    var ajaxCall = function(axis, callback_func) {
        if ($("#" + ids.sidebar[axis].data_type).val() === vals.data_type.genetic) {
            
            var paramsGetProfileData = {  //webservice call to get profile data
                cancer_study_id: window.PortalGlobals.getCancerStudyId(),
                gene_list: $("#" + ids.sidebar[axis].gene).val(),
                genetic_profile_id: $("#" + ids.sidebar[axis].profile_name).val(),
                case_set_id: window.PortalGlobals.getCaseSetId(),
                case_ids_key: window.PortalGlobals.getCaseIdsKey()
            };
            $.post("getProfileData.json", paramsGetProfileData, inner_profile_callback_func, "json");
            
            function inner_profile_callback_func(profileData) { 
                var _tmp = {}; //convert to json format
                for (var key in profileData[$("#" + ids.sidebar[axis].gene).val()]) {
                    var _obj = profileData[$("#" + ids.sidebar[axis].gene).val()][key];
                    _tmp[key] = _obj[$("#" + ids.sidebar[axis].profile_name).val()];
                }
                callback_func(axis, _tmp);
            }
            
        } else if ($("#" + ids.sidebar[axis].data_type).val() === vals.data_type.clin) {
            var paramsGetClinicalAttributes = { //webservice call to get clinical data
                cmd : "getClinicalData",
                cancer_study_id: window.PortalGlobals.getCancerStudyId(),
                case_set_id : window.PortalGlobals.getCaseSetId(),
                format : "json"
            };
            $.post("webservice.do", paramsGetClinicalAttributes, inner_callback_func, "json");
            
            function inner_callback_func(clinicalData) {
                var _tmp = {};
                $.each(clinicalData.data, function(index, obj) { //convert to json format
                    if (obj.attr_id === $("#" + ids.sidebar[axis].clin_attr).val()) {
                        _tmp[obj.sample] = obj.attr_val;
                    }
                });
                callback_func(axis, _tmp);
            }
        
        }
    };
    

    
    var merge = function(axis, result) {
        //fill in x, y value
        data[axis].stat = true;
        data[axis].raw = result;
        if (data.x.stat && data.y.stat) {
            for(var xCaseId in data.x.raw) {
                var _obj = data.y.raw;
                if (_obj.hasOwnProperty(xCaseId)) {
                    if (!isEmpty(data.x.raw[xCaseId]) && !isEmpty(data.y.raw[xCaseId])) { //eliminate empty values
                        var _datum = jQuery.extend(true, {}, datum);
                        _datum.caseId = xCaseId;
                        _datum.xVal = data.x.raw[xCaseId];
                        _datum.yVal = data.y.raw[xCaseId];
                        dotsContent[xCaseId] = _datum;
                    }
                }
            }
            if (genetic_vs_genetic() || genetic_vs_clinical()) {
                //get mutation data
                var _gene_list = "";
                if ($("#" + ids.sidebar.x.data_type).val() === vals.data_type.genetic) {
                    _gene_list += $("#" + ids.sidebar.x.gene).val();
                }
                if ($("#" + ids.sidebar.y.data_type).val() === vals.data_type.genetic &&
                        $("#" + ids.sidebar.y.gene).val() !== $("#" + ids.sidebar.x.gene).val()) {
                    _gene_list += " " + $("#" + ids.sidebar.y.gene).val();
                }
                if (_gene_list !== "") {
                    var proxy = DataProxyFactory.getDefaultMutationDataProxy();
                    proxy.getMutationData(_gene_list, mutationCallback);
                }                 
            } else {
                analyseData();
                stat.retrieved = true;
            }
        }
    };
    
    function mutationCallback(mutationData) {
        var mutationDetailsUtil = new MutationDetailsUtil(new MutationCollection(mutationData));
        var mutationMap = mutationDetailsUtil.getMutationCaseMap();
        for (var key in mutationMap) {
            $.each(mutationMap[key], function(index, obj) {
                if (dotsContent.hasOwnProperty(key.toUpperCase())) {
                    if (typeof(dotsContent[key.toUpperCase()].mutation[obj.geneSymbol]) !== "undefined") {
                        dotsContent[key.toUpperCase()].mutation[obj.geneSymbol].details += "; " + obj.proteinChange;
                        dotsContent[key.toUpperCase()].mutation[obj.geneSymbol].type = mutationTranslator(obj.mutationType);
                    } else {
                        dotsContent[key.toUpperCase()].mutation[obj.geneSymbol] = {
                            "details": obj.proteinChange,
                            "type": mutationTranslator(obj.mutationType)
                        };
                    }                    
                }
            });
        }
        if (genetic_vs_genetic()) {
            //get cna data
            var cna_annotation_profile_name = "";
            if (isSameGene()) {
                $.each(metaData.getGeneticProfilesMeta($("#" + ids.sidebar.y.gene).val()), function(index, obj) {
                    $.each(discretized_cna_profile_keywords, function(_index, keyword) {
                        if (obj.id.toLowerCase().indexOf(keyword) !== -1) {
                            cna_annotation_profile_name = obj.id;
                            return false;
                        }
                    });
                }); 
                if (cna_annotation_profile_name !== "") {
                    var paramsGetProfileData = {  //webservice call to get profile data
                        cancer_study_id: window.PortalGlobals.getCancerStudyId(),
                        gene_list: $("#" + ids.sidebar.y.gene).val(),
                        genetic_profile_id: cna_annotation_profile_name,
                        case_set_id: window.PortalGlobals.getCaseSetId(),
                        case_ids_key: window.PortalGlobals.getCaseIdsKey()
                    };
                    $.post("getProfileData.json", paramsGetProfileData, inner_profile_callback_func, "json");

                    function inner_profile_callback_func(_result) {
                        stat.hasCnaAnno = true;
                        $.each(Object.keys(dotsContent), function(index, caseId) {
                            dotsContent[caseId].cna_anno = _result[$("#" + ids.sidebar.y.gene).val()][caseId][cna_annotation_profile_name];
                        });
                        analyseData();
                        stat.retrieved = true;
                    };
                }
            } else {
                analyseData();
                stat.retrieved = true;
            }
        } else if (genetic_vs_clinical()) {
            //translate: assign text value a numeric value for clinical data
            var _axis, _axis_key;
            if ($("#" + ids.sidebar.x.data_type).val() === vals.data_type.clin) {
                _axis = "x";
                _axis_key = "xVal";
            } else if ($("#" + ids.sidebar.y.data_type).val() === vals.data_type.clin) {
                _axis = "y";
                _axis_key = "yVal";
            }
            var _arr = [];
            _arr.length = 0;
            for(var key in dotsContent) {
                _arr.push(dotsContent[key][_axis_key]);
            }
            if (!is_numeric(_arr)) {
                clinical_data_interpreter.process(dotsContent, _axis);
                for (var key in dotsContent) {
                    dotsContent[key][_axis_key] = clinical_data_interpreter.convert_to_numeric(dotsContent[key][_axis_key], _axis);
                }                        
            }
            analyseData();
            stat.retrieved = true; 
        } 
    }

    function analyseData() {    //pDataX, pDataY: array of single dot objects
        var tmp_xData = [];
        var tmp_yData = [];
        for (var key in dotsContent) {
            tmp_xData.push(parseFloat(dotsContent[key].xVal));
            tmp_yData.push(parseFloat(dotsContent[key].yVal));
        }

        stat.x.min = Math.min.apply(Math, tmp_xData);
        stat.x.max = Math.max.apply(Math, tmp_xData);
        stat.x.edge = (stat.x.max - stat.x.min) * 0.2;
        stat.y.min = Math.min.apply(Math, tmp_yData);
        stat.y.max = Math.max.apply(Math, tmp_yData);
        stat.y.edge = (stat.y.max - stat.y.min) * 0.1;
    }

    return {
        fetch: function(axis) {
            
            stat.retrieved = false;
            
            data[axis].stat = false;
            data[axis].raw.length = 0;
            dotsContent = {}; //need to regenerated dots content 
            
            var tmp = setInterval(function () {timer();}, 1000);
            function timer() {
                if (metaData.getRetrieveStatus() !== -1) {
                    clearInterval(tmp);
                    ajaxCall(axis, merge);
                }
            }
        },
        get: function(callback_func) {
            var tmp = setInterval(function () {timer();}, 1000);
            function timer() {
                //if (Object.keys(dotsContent).length !== 0) {
                if (stat.retrieved) {
                    clearInterval(tmp);
                    callback_func(dotsContent);
                }
            }
        },
        stat: function() {
            return stat;
        }
    };
    
}());