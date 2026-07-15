<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%
    // esta página chega via response.sendRedirect (nova requisição), então o
    // erro não vem em request.getAttribute — foi guardado na sessão como
    // "flash message" pelo LoginServlet. Lemos e apagamos na sequência, pra
    // não ficar "grudado" se a pessoa atualizar a página ou voltar aqui depois.
    String erro = (String) session.getAttribute("erroLogin");
    session.removeAttribute("erroLogin");
    session.removeAttribute("emailDigitadoLogin");

    if (erro == null) erro = "E-mail ou senha inválidos.";
%>
<!DOCTYPE html>
<html lang="pt-br">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Entre Aspas · Não foi possível entrar</title>
<link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body class="fundo-login">

<div class="login-centro">
    <div class="login-card" style="text-align:center;">

        <img class="login-logo" src="<%= request.getContextPath() %>/img/livro-icone.png" alt="Entre Aspas">

        <h1 class="login-titulo">Não foi possível entrar</h1>

        <div class="msg msg-erro" style="text-align:center;">
            <%= erro %>
        </div>

        <p class="subtitulo">
            Confira se o e-mail e a senha foram digitados corretamente e tente novamente,
            ou crie uma conta caso ainda não tenha uma.
        </p>

        <div style="display:flex; flex-direction:column; gap:10px; margin-top:20px;">
            <a class="btn btn-preto" href="<%= request.getContextPath() %>/login.jsp">Voltar para o login</a>
            <a class="btn btn-outline" href="<%= request.getContextPath() %>/cadastro.jsp">Criar uma conta</a>
        </div>
    </div>
</div>

</body>
</html>
