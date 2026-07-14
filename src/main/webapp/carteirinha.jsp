<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="model.Usuario" %>
<%
    Usuario usuario = (Usuario) request.getAttribute("usuario");
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="pt-br">
<head>
<meta charset="UTF-8">
<title>Carteirinha · <%= usuario.getNome() %></title>
<link rel="stylesheet" href="<%= ctx %>/css/style.css">
</head>
<body>

<div class="print-page">
    <div class="print-toolbar no-print">
        <button class="btn btn-azul" onclick="window.print()">Imprimir / salvar como PDF</button>
        <a class="btn btn-outline" href="<%= ctx %>/home">Voltar</a>
    </div>

    <div class="carteirinha">
        <img src="<%= usuario.getFotoUrl() != null ? ctx + "/" + usuario.getFotoUrl() : "https://api.dicebear.com/7.x/initials/svg?seed=" + usuario.getNome() %>" alt="Foto do membro">
        <div>
            <p class="selo">CLUBE DE LEITURA ENTRE ASPAS</p>
            <h2 style="margin:4px 0;"><%= usuario.getNome() %></h2>
            <p style="margin:2px 0;"><strong>Matrícula:</strong> <%= usuario.getMatricula() %></p>
            <p style="margin:2px 0;"><strong>E-mail:</strong> <%= usuario.getEmail() %></p>
            <p style="margin:2px 0;"><strong>Cargo:</strong> <%= usuario.getCargo() %></p>
            <p style="margin:2px 0;"><strong>Gênero favorito:</strong> <%= usuario.getGeneroFav() %></p>
        </div>
    </div>

    <p class="subtitulo" style="text-align:center; margin-top:20px;">
        Esta carteirinha comprova a associação ao Clube de Leitura Entre Aspas.
    </p>
</div>

</body>
</html>
