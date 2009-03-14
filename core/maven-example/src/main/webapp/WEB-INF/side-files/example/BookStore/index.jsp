<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
  This side JSP for example.BookStore is used to serve the URL "/"
--%>
<html>
  <head><title>Book Store</title></head>
  <body>
    <%-- side files can include static resources. --%>
    <img src="logo.png">

    <h2>Inventory</h2>
    <c:forEach var="i" items="${it.items}">
      <a href="items/${i.key}">${i.value.title}</a><br>
    </c:forEach>

    <h2>Others</h2>
    <p>
      <%--
        this jumps to another side file "count.jsp"
      --%>
      <a href="count">count inventory</a>
    <p>
      <a href="hello">invoke action method</a>
    <p>
      <%-- resources files are served normally. --%>
      <a href="help/help.html">regular resources</a>
    </p>
  </body>
</html>