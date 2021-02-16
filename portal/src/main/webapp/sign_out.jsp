<%@ page import="javax.servlet.http.HttpSession" %>

<%
  //  Clears the Session
  session.invalidate();

  // Redirect to Partners to Clear their Session
  response.sendRedirect("https://partnershealthcare.okta.com/login/signout");
%>

