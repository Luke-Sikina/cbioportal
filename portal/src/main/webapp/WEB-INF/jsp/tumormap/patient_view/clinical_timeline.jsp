
  <script src="js/src/patient-view/clinical-timeline.js"></script>

  <style type="text/css">
    .axis path,
    .axis line {
      fill: none;
      stroke: black;
      shape-rendering: crispEdges;
    }

    .axis text {
      font-family: sans-serif;
      font-size: 10px;
    }

    .timeline-label {
      font-family: sans-serif;
      font-size: 12px;
    }
    
    #timeline .axis {
      transform: translate(0px,30px);
      -ms-transform: translate(0px,30px); /* IE 9 */
      -webkit-transform: translate(0px,30px); /* Safari and Chrome */
      -o-transform: translate(0px,30px); /* Opera */
      -moz-transform: translate(0px,30px); /* Firefox */
    }

    .coloredDiv {
      height:20px; width:20px; float:left;
    }
  </style>
  
  <script type="text/javascript">

    $(document).ready(function(){
        
        var params = {
            type:"diagnostic,treatment,lab_test",
            cancer_study_id:cancerStudyId,
            patient_id:patientId
        }
        
        $.post("clinical_timeline_data.json", 
            params,
            function(data){
                if (cbio.util.getObjectLength(data)===0) return;
                
                var timeData = prepareTimelineData(data);
                if (timeData.length===0) return;

                var width = $("#td-content").width() - 50;
                var timeline = clinicalTimeline().itemHeight(12).stack();
                var svg = d3.select("#timeline").append("svg").attr("width", width).datum(timeData).call(timeline);
                $("#timeline-container").show();
            }
            ,"json"
        );
            
        function prepareTimelineData(timelineData) {
            var timelineDataByType = {};
            
            timelineData.forEach(function(data) {
                var type = data["eventType"];
                if (!(type in timelineDataByType)) timelineDataByType[type] = [];
                timelineDataByType[type].push(data);
            });
            
            var ret = [];
            
            if ("diagnostic" in timelineDataByType) {
                ret.push({
                    label:"Diagnostics",
                    display:"circle",
                    times:formatTimePoints(timelineDataByType["diagnostic"])});
            }
            
            if ("lab_test" in timelineDataByType) {
                ret.push({
                    label:"Lab Tests",
                    display:"circle",
                    times:formatTimePoints(timelineDataByType["lab_test"])});
            }
            
            if ("treatment" in timelineDataByType) {
                var treatments = timelineDataByType["treatment"].sort(function(a,b){return a.startDate-b.startDate;});
                var treatmentGroups = separateTreatmentsByAgent(treatments);
                for (var agent in treatmentGroups) {
                    ret.push({
                        label:agent,
                        display:"rect",
                        times:formatTimePoints(treatmentGroups[agent])});
                }
            }
            
            return ret;
//            return [
//                    {label:"Diagnostics", display:"circle", times: [{"starting_time": 0, "tooltip":"First diagonosis"},{"starting_time": 200}, {"starting_time": 500}]},
//                    {label:"Lab Tests", display:"circle", times: [{"starting_time": -10}, ]},
//                    {label:"Therapy", display:"rect", times: [{"starting_time": 140, "ending_time": 360, "tooltip":"Chemo"}]},
//                  ];
        }
        
        function formatTimePoints(timePointsData) {
            var times = [];
            timePointsData.forEach(function(timePointData){
                times.push(formatATimePoint(timePointData));
            });
            return times;
        }
        
        function formatATimePoint(timePointData) {
            var startDate, stopDate;
            startDate = timePointData["startDate"];
            stopDate = timePointData["stopDate"];
            if (cbio.util.checkNullOrUndefined(stopDate)) stopDate = startDate;
            
            var tooltip = [];
            tooltip.push("<td>startDate</td><td>"+startDate+"</td>");
            tooltip.push("<td>stopDate</td><td>"+stopDate+"</td>");
            if ("eventData" in timePointData) {
                var eventData = timePointData["eventData"];
                for (var key in eventData) {
                    tooltip.push("<td>"+key+"</td><td>"+eventData[key]+"</td>");
                }
            }
            
            return {
                starting_time : startDate,
                ending_time : stopDate,
                tooltip : "<table class='timeline-tooltip-table uninitialized'><thead><tr><th>&nbsp;</th><th>&nbsp;</th></tr></thead><tr>" + tooltip.join("</tr><tr>") + "</tr></table>"
            };
        }
        
        function separateTreatmentsByAgent(treatments) {
            var ret = {};
            treatments.forEach(function(treatment) {
                var agent = treatment.eventData.agent;
                if (cbio.util.checkNullOrUndefined(agent)) {
                    agent = treatment.eventData.subtype;
                }
                if (cbio.util.checkNullOrUndefined(agent)) {
                    agent = treatment.eventData.type;
                }
                if (!(agent in ret)) {
                    ret[agent] = [];
                }
                ret[agent].push(treatment);
            });
            return ret;
        }

    });
  </script>

  <div id="timeline-container" style="display:hidden">
  <div id="timeline">
  
  </div>
      <br/>
  </div>
