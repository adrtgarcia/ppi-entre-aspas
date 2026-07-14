<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="pt-br">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Entre Aspas · Entrar</title>
<link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body class="fundo-login">

<div class="login-centro">
    <div class="login-card">

        <img class="login-logo" src="<%= request.getContextPath() %>/img/livro-icone.png" alt="Entre Aspas">

        <h1 class="login-titulo">Bem-vindo(a) de volta ao Entre Aspas</h1>
        <p class="subtitulo" style="text-align:center;">Entre na sua conta para ver seus desafios, avaliações e continuar sua leitura.</p>

        <% if (request.getAttribute("erro") != null) { %>
            <div class="msg msg-erro"><%= request.getAttribute("erro") %></div>
        <% } %>
        <% if (request.getAttribute("sucesso") != null) { %>
            <div class="msg msg-sucesso"><%= request.getAttribute("sucesso") %></div>
        <% } %>
        <% if ("1".equals(request.getParameter("contaRemovida"))) { %>
            <div class="msg msg-sucesso">Sua conta foi removida com sucesso.</div>
        <% } %>

        <form action="<%= request.getContextPath() %>/login" method="post">
            <label>E-mail <span class="obrigatorio">*</span></label>
            <input type="email" name="email" class="input-login" placeholder="Digite seu e-mail" required
                   value="<%= request.getAttribute("emailDigitado") != null ? request.getAttribute("emailDigitado") : "" %>">

            <label style="margin-top:14px;">Senha <span class="obrigatorio">*</span></label>
            <input type="password" name="senha" class="input-login" placeholder="Digite sua senha" required>

            <button type="submit" class="btn btn-preto" style="margin-top:20px;">Entrar</button>
        </form>

        <p class="link-secundario">
            Ainda não tem conta? <a href="<%= request.getContextPath() %>/cadastro.jsp"><strong>Cadastre-se</strong></a>
        </p>
    </div>
</div>

</body>
</html>
