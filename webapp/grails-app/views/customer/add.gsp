<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
  <meta name="layout" content="orchestra"/>
  <title>Services</title>
</head>
<body>
<g:form url="/vendors" method="post">
  <div class="fieldcontain ${hasErrors(bean: command, field: 'vendor.name', 'error')} required">
    <label for="vendor.name">Name<span class="required-indicator">*</span></label>
    <g:textField name="vendor.name" required="1" value="${command.vendor.name}"/>
  </div>

  <div class="fieldcontain ${hasErrors(bean: command)} required">
    <label for="vendor.sectorRef">Sector<span class="required-indicator">*</span></label>
    <g:select name="vendor.sectorRef"
              value="${command.vendor.sectorRef}"
              from="${sectors}"
              optionValue="name"
              optionKey="id"/>
  </div>

  <div class="fieldcontain required">
    <label for="vendor.global">Global<span class="required-indicator">*</span></label>
    <g:checkBox name="vendor.global"
                value="${command.vendor.global}"
                title="Global" />
  </div>

  <hr/>

  <div class="fieldcontain ${hasErrors(bean: command, field: 'user.username', 'error')} required">
    <label for="user.username">Username<span class="required-indicator">*</span></label>
    <g:textField name="user.username" required="1" value="${command.user.username}"/>
  </div>
  <div class="fieldcontain ${hasErrors(bean: command, field: 'user.password', 'error')} required">
    <label for="user.password">Password<span class="required-indicator">*</span></label>
    <g:passwordField name="user.password" required="1" value="${command.user.password}"/>
  </div>

  <div class="fieldcontain ${hasErrors(bean: command, field: 'user.firstName', 'error')} required">
    <label for="user.firstName">First Name<span class="required-indicator">*</span></label>
    <g:textField name="user.firstName" required="1" value="${command.user.firstName}"/>
  </div>
  <div class="fieldcontain ${hasErrors(bean: command, field: 'user.lastName', 'error')} required">
    <label for="user.lastName">First Name<span class="required-indicator">*</span></label>
    <g:textField name="user.lastName" required="1" value="${command.user.lastName}"/>
  </div>

  <div class="form-actions">
    <g:submitButton name="save" class="save btn-inverse" value="Add"/>
  </div>
</g:form>
</body>
</html>