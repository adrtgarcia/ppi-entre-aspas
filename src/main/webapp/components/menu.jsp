<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%
    String ctx = request.getContextPath();
    String uri = request.getRequestURI();
%>
<nav class="navbar">
    <a class="navbar-brand" href="<%= ctx %>/home">ENTRE ASPAS</a>
    <ul class="navbar-links">
        <li><a href="<%= ctx %>/livro"     class="<%= uri.endsWith("livro") ? "active" : "" %>">LIVROS</a></li>
        <li><a href="<%= ctx %>/desafio"   class="<%= uri.endsWith("desafio") ? "active" : "" %>">DESAFIOS</a></li>
        <li><a href="<%= ctx %>/avaliacao" class="<%= uri.endsWith("avaliacao") ? "active" : "" %>">AVALIAÇÕES</a></li>
        <li><a href="<%= ctx %>/usuario"   class="<%= uri.endsWith("usuario") ? "active" : "" %>">USUÁRIOS</a></li>
        <li><a href="<%= ctx %>/sobre.jsp" class="<%= uri.endsWith("sobre.jsp") ? "active" : "" %>">SOBRE O CLUBE</a></li>
    </ul>
</nav>
