<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
  <meta name="layout" content="main"/>
  <title>Customers</title>
</head>

<body>
<table class="table table-striped">
  <thead>
  <tr>
    <th>Name</th>
    <th></th>
  </tr>
  </thead>
  <tbody>
  <g:each in="${customers.page}" status="i" var="customer">
    <tr>
      <td>${customer.firstName}</td>
      <td>
        <g:link url="/customers/${vendor.id}"><input value="Edit" type="button" class="btn btn-primary "/></g:link>
        <g:form url="/customers/${vendor.id}" method="delete"> <g:submitButton name="Delete" class="btn btn-danger"/> </g:form>
      </td>
    </tr>
  </g:each>
  </tbody>
</table>
<g:link url="/customers/add">Add New Customer</g:link>
</body>
</html>