<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="model.Livro" %>
<%
    Livro livro = (Livro) request.getAttribute("livro");
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="pt-br">
<head>
<meta charset="UTF-8">
<title>Ficha de leitura<%= livro != null ? " · " + livro.getNome() : "" %></title>
<link rel="stylesheet" href="<%= ctx %>/css/style.css">
</head>
<body>

<div class="print-page">
    <div class="print-toolbar no-print">
        <button class="btn btn-azul" onclick="window.print()">Imprimir / salvar como PDF</button>
        <a class="btn btn-outline" href="<%= ctx %>/livro">Voltar</a>
    </div>

    <% if (livro == null) { %>
        <div class="msg msg-erro"><%= request.getAttribute("erro") %></div>
    <% } else { %>
        <div class="ficha-leitura">
            <img class="capa-ficha" src="<%= livro.getCapaUrl() != null ? ctx + "/" + livro.getCapaUrl() : "https://placehold.co/300x420/54aaf5/fff?text=" + livro.getGenero() %>">
            <h1 style="margin-top:0;"><%= livro.getNome() %></h1>
            <p class="subtitulo"><%= livro.getAutor() %> · <%= livro.getGenero() %> · <%= livro.getAnoPublicacao() %></p>
            <p><%= livro.getSinopse() != null ? livro.getSinopse() : "" %></p>
        </div>

        <div style="clear:both;"></div>

        <div style="margin-top:20px;">
            <span class="campo-data"><label>Data de início da leitura</label>____ / ____ / ______</span>
            <span class="campo-data"><label>Data de término da leitura</label>____ / ____ / ______</span>
        </div>

        <div>
            <label style="margin-top:16px;">Minhas anotações</label>
            <div class="area-notas"></div>
        </div>
    <% } %>
</div>

</body>
</html>
