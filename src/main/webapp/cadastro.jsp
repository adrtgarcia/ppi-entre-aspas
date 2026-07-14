<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="pt-br">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Entre Aspas · Cadastro</title>
<link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body style="background: var(--cinza-borda); min-height:100vh; display:flex; align-items:center; justify-content:center; padding:30px;">

<div class="card" style="max-width:520px; width:100%;">
    <h1>Criar conta no Entre Aspas</h1>
    <p class="subtitulo">Cadastre-se e comece a participar dos desafios.</p>

    <% if (request.getAttribute("erro") != null) { %>
        <div class="msg msg-erro"><%= request.getAttribute("erro") %></div>
    <% } %>

    <form action="<%= request.getContextPath() %>/cadastro" method="post" enctype="multipart/form-data">
        <div class="form-grid">
            <div>
                <label>Nome completo</label>
                <input type="text" name="nome" required
                       value="<%= request.getAttribute("nomeDigitado") != null ? request.getAttribute("nomeDigitado") : "" %>">
            </div>
            <div>
                <label>Matrícula</label>
                <input type="text" name="matricula" required
                       value="<%= request.getAttribute("matriculaDigitada") != null ? request.getAttribute("matriculaDigitada") : "" %>">
            </div>
            <div>
                <label>E-mail</label>
                <input type="email" name="email" required
                       value="<%= request.getAttribute("emailDigitado") != null ? request.getAttribute("emailDigitado") : "" %>">
            </div>
            <div>
                <label>Senha</label>
                <input type="password" name="senha" required>
            </div>
            <div>
                <label>Gênero literário favorito</label>
                <select name="generoFav" required>
                    <option value="">-- Selecione --</option>
                    <option value="Ficção">Ficção</option>
                    <option value="Romance">Romance</option>
                    <option value="Mistério">Mistério</option>
                    <option value="Fantasia">Fantasia</option>
                    <option value="Terror">Terror</option>
                </select>
            </div>
            <div>
                <label>Foto de perfil</label>
                <input type="file" name="foto" accept=".jpg,.jpeg,.png,.webp">
            </div>
        </div>

        <br>
        <p class="subtitulo" style="font-size:0.8rem; margin:10px 0 0;">
            Toda conta nova entra como <strong>usuário</strong>. A promoção para administrador
            só pode ser feita por um administrador já existente no sistema.
        </p>
        <button type="submit" class="btn btn-rosa">Criar minha conta</button>
    </form>

    <p class="link-secundario">
        Já tem conta? <a href="<%= request.getContextPath() %>/login.jsp">Entrar</a>
    </p>
</div>

</body>
</html>
