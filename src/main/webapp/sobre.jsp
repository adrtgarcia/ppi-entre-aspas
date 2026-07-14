<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="dao.UsuarioDAO, model.Usuario, java.util.List" %>
<%
    request.setAttribute("titulo", "Sobre o clube");
    String ctx = request.getContextPath();

    List<Usuario> administradores = null;
    String erroAdmins = null;
    try {
        administradores = new UsuarioDAO().listarAdministradores();
    } catch (Exception e) {
        erroAdmins = "Não foi possível carregar os administradores do clube.";
    }
%>
<jsp:include page="/components/header.jsp" />

<h1>Sobre o Entre Aspas</h1>
<p class="subtitulo">Saiba mais sobre nosso clube!</p>

<div class="card">
    <h2>O projeto</h2>
    <p>
        O Entre Aspas é um sistema web desenvolvido durante os laboratórios de Programação Para Internet.
        Permite cadastrar livros, criar e participar de desafios de leitura, registrar avaliações sobre
        suas leituras. Também é possível obter sua <strong>carteirinha de membro</strong> e 
        <strong>fichas de leitura</strong> de casa livro!.
    </p>
    <p>
        Tecnicamente, o sistema utiliza componentes JSP reusáveis (<code>header.jsp</code>,
        <code>menu.jsp</code> e <code>footer.jsp</code>) incluídos via <code>&lt;jsp:include&gt;</code>,
        Servlets controladores que processam as requisições de login, cadastro e busca e encaminham
        o fluxo para páginas distintas via <code>RequestDispatcher.forward</code> (equivalente Java
        do <code>&lt;jsp:forward&gt;</code>), e upload/download de arquivos (foto de perfil e capa de livro).
    </p>
</div>

<div class="card">
    <h2>Integrantes do Grupo</h2>
    <p class="subtitulo">Responsáveis pela implementação do sistema.</p>

    <% if (erroAdmins != null) { %>
        <p class="msg msg-erro"><%= erroAdmins %></p>
    <% } else { %>
        <div class="tabela-scroll">
            <table class="tabela">
                <thead>
                    <tr>
                        <th>Foto</th>
                        <th>Nome</th>
                        <th>Matrícula</th>
                        <th>E-mail</th>
                    </tr>
                </thead>
                <tbody>
                    <% if (administradores == null || administradores.isEmpty()) { %>
                        <tr><td colspan="4" class="tabela-vazia">Nenhum administrador cadastrado no momento.</td></tr>
                    <% } %>
                    <% for (Usuario admin : administradores) { %>
                        <tr>
                            <td><img class="avatar-mini" src="<%= admin.getFotoUrl() != null ? ctx + "/" + admin.getFotoUrl() : "https://api.dicebear.com/7.x/initials/svg?seed=" + admin.getNome() %>"></td>
                            <td><%= admin.getNome() %></td>
                            <td><%= admin.getMatricula() %></td>
                            <td><%= admin.getEmail() %></td>
                        </tr>
                    <% } %>
                </tbody>
            </table>
        </div>
    <% } %>
</div>

<jsp:include page="/components/footer.jsp" />
