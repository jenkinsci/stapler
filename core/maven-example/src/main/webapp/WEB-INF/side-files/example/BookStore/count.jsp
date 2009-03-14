<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--
  An example of another side JSP.
--%>
<html>
  <body>
    # of items: ${fn:length(it.items)}
  </body>
</html>