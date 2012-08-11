<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
  <meta name="layout" content="main"/>
  <title>Customer</title>
</head>
<body>
<g:form url="/customers" method="post">
  <g:hiddenField name="id" value="${vendor.id}"/>

  <div class="form-actions">
    <g:submitButton name="update" class="save btn-inverse" value="Update"/>
  </div>
</g:form>
</body>
</html>
