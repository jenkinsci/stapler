<%@taglib prefix="st" uri="http://stapler.dev.java.net/" %>
<%--
  "index.jsp" is used to serve the URL of the object itself.
  In this example, this JSP is used to serve "/items/[id]/"
--%>
<html>
  <head><title>Book ${it.title}</title></head>
  <body>
    <%--
      "it" variable is set to the target Book object by the Stapler.
    --%>
    Name: ${it.title}<br>
    ISBN: ${it.isbn}<br>

    <%--
      st:include tag lets you include another side JSP from the
      inheritance hierarchy of the "it" object. Here, we are
      referring to the footer.jsp defined for the Item class,
      allowing CD and Book to share some JSPs.
    --%>
    <st:include page="footer.jsp"/>
  </body>
</html>