<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="pt-br">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Entre Aspas<%= (request.getAttribute("titulo") != null ? " · " + request.getAttribute("titulo") : "") %></title>
<link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<%-- componente reusável incluído via jsp:include --%>
<jsp:include page="/components/menu.jsp" />
<main class="container">
