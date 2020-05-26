<%
    String url = request.getRequestURL().toString();
    String baseUrl = url.substring(0, url.length() - request.getRequestURI().length()) + request.getContextPath();
    baseUrl = baseUrl.replace("https://", "").replace("http://", "");
%>
<%@ taglib prefix = "c" uri = "http://java.sun.com/jsp/jstl/core" %>
<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>
<!DOCTYPE html>
<html class="cbioportal-frontend">
<head>
    <meta http-equiv="Content-type" content="text/html; charset=utf-8"/>
        
    <c:if test = "${GlobalProperties.showSitemaps()==false}">
       <meta name="robots" content="noindex" />
    </c:if>
    
    <link rel="icon" href="/images/cbioportal_icon.png"/>
    <title>cBioPortal for Cancer Genomics</title>
    <script src="https://code.jquery.com/jquery-1.12.1.min.js"></script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/js/bootstrap.min.js" integrity="sha384-0mSbJDEHialfmuBBQP6A4Qrprq5OVfW37PRR3j5ELqxss1yVqOtnepnHVP9aJ7xS" crossorigin="anonymous"></script>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/semantic-ui/2.2.9/semantic.min.css">
    
    <script>
        window.frontendConfig = {
            configurationServiceUrl:"//" + '<%=baseUrl%>' +  "/config_service.jsp",
            appVersion:'<%=GlobalProperties.getAppVersion()%>',
            apiRoot: '//'+ '<%=baseUrl%>/', 
            baseUrl: '<%=baseUrl%>',
            basePath: '<%=request.getContextPath()%>',  
        };
        
        <%-- write configuration to page so we do not have to load it via service--%>
        <%@include file="./config_service.jsp" %>
        
        if (/localdev=true/.test(window.location.href)) {
            localStorage.setItem("localdev", "true");
        }
        if (/localdist=true/.test(window.location.href)) {
            localStorage.setItem("localdist", "true");
        }
        window.localdev = localStorage.localdev === 'true';
        window.localdist = localStorage.localdist === 'true';
        window.heroku = localStorage.heroku;
        window.netlify = localStorage.netlify;
        
        if (window.localdev || window.localdist) {
            window.frontendConfig.frontendUrl = "//localhost:3000/"
            localStorage.setItem("e2etest", "true");
        } else if (window.heroku) {
            window.frontendConfig.frontendUrl = ['//',localStorage.heroku,'.herokuapp.com','/'].join('');
            localStorage.setItem("e2etest", "true");
        } else if (window.netlify) {
            window.frontendConfig.frontendUrl = ['//',localStorage.netlify,'.netlify.com','/'].join('');
            localStorage.setItem("e2etest", "true");
        } else if('<%=GlobalProperties.getFrontendUrl()%>'){
            window.frontendConfig.frontendUrl = '<%=GlobalProperties.getFrontendUrl()%>';
        }

        if(!window.frontendConfig.frontendUrl) {
            window.frontendConfig.frontendUrl = '//' + '<%=baseUrl%>/';
        }

    </script>
     
    <script type="text/javascript" src="//<%=baseUrl%>/js/src/load-frontend.js?<%=GlobalProperties.getAppVersion()%>"></script>
          
    <script>
        window.frontendConfig.customTabs && window.frontendConfig.customTabs.forEach(function(tab){
            if (tab.pathsToJs) {
                tab.pathsToJs.forEach(function(src){
                    document.write('<scr'+'ipt type="text/javascript" src="'+ src +'"></sc'+'ript>');
                });
            }
        });
    </script>
    
    <script>
            loadReactApp(window.frontendConfig);
    </script>
    
    <%@include file="./tracking_include.jsp" %>

    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.css" rel="stylesheet" />

</head>

<body>
    <div id="reactRoot"></div>
    
    
    
</body>
</html>
