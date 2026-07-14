<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="model.Usuario, java.util.List" %>
<%
    request.setAttribute("titulo", "Usuários");
    Usuario logado = (Usuario) session.getAttribute("usuarioLogado");
    List<Usuario> usuarios = (List<Usuario>) request.getAttribute("usuarios");
    String ctx = request.getContextPath();
%>
<jsp:include page="/components/header.jsp" />

<h1>Membros do Clube</h1>
<p class="subtitulo">Todos os membros cadastrados no Entre Aspas.</p>

<% if (request.getAttribute("sucesso") != null) { %>
    <div class="msg msg-sucesso"><%= request.getAttribute("sucesso") %></div>
<% } %>
<% if (request.getAttribute("erro") != null) { %>
    <div class="msg msg-erro"><%= request.getAttribute("erro") %></div>
<% } %>

<form class="busca-form" action="<%= ctx %>/usuario" method="get">
    <input type="text" name="q" placeholder="Buscar por nome, e-mail ou matrícula..."
           value="<%= request.getAttribute("termoBusca") != null ? request.getAttribute("termoBusca") : "" %>">
    <button type="submit" class="btn btn-outline">Pesquisar</button>
    <a class="btn btn-rosa" href="<%= ctx %>/cadastro.jsp">+ Novo usuário</a>
</form>

<div class="tabela-scroll">
    <table class="tabela">
        <thead>
            <tr>
                <th>Foto</th>
                <th>Nome</th>
                <th>Cargo</th>
            </tr>
        </thead>
        <tbody>
            <% if (usuarios == null || usuarios.isEmpty()) { %>
                <tr><td colspan="3" class="tabela-vazia">Nenhum usuário cadastrado ainda.</td></tr>
            <% } %>
            <% for (Usuario u : usuarios) {
                 boolean podeGerenciar = logado.podeGerenciar(u.getId());
            %>
                <tr class="linha-clicavel" onclick="abrirModal('modal-usuario-<%= u.getId() %>')">
                    <td><img class="avatar-mini" src="<%= u.getFotoUrl() != null ? ctx + "/" + u.getFotoUrl() : "https://api.dicebear.com/7.x/initials/svg?seed=" + u.getNome() %>"></td>
                    <td><%= u.getNome() %></td>
                    <td><span class="badge <%= u.isAdministrador() ? "badge-rosa" : "badge-cinza" %>"><%= u.getCargo() %></span></td>
                </tr>

                <div class="overlay" id="modal-usuario-<%= u.getId() %>">
                    <div class="modal">
                        <button class="modal-fechar" onclick="fecharModal('modal-usuario-<%= u.getId() %>')">&times;</button>
                        <h2><%= u.getNome() %></h2>
                        <p class="subtitulo">
                            Matrícula: <%= u.getMatricula() %> · E-mail: <%= u.getEmail() %><br>
                            Gênero favorito: <%= u.getGeneroFav() %> · Cargo: <%= u.getCargo() %>
                        </p>
                        <hr>

                        <% if (podeGerenciar) { %>
                            <form action="<%= ctx %>/usuario" method="post" enctype="multipart/form-data">
                                <input type="hidden" name="id" value="<%= u.getId() %>">
                                <div class="form-grid">
                                    <div>
                                        <label>Nome</label>
                                        <input type="text" name="nome" value="<%= u.getNome() %>" required>
                                    </div>
                                    <div>
                                        <label>Gênero favorito</label>
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
                                        <label>Nova senha (opcional)</label>
                                        <input type="password" name="senha">
                                    </div>
                                    <div>
                                        <label>Nova foto</label>
                                        <input type="file" name="foto" accept=".jpg,.jpeg,.png,.webp">
                                    </div>
                                    <% if (logado.isAdministrador()) { %>
                                    <div>
                                        <label>Cargo</label>
                                        <select name="cargo">
                                            <option value="usuario" <%= "usuario".equals(u.getCargo()) ? "selected" : "" %>>Usuário</option>
                                            <option value="administrador" <%= "administrador".equals(u.getCargo()) ? "selected" : "" %>>Administrador</option>
                                        </select>
                                    </div>
                                    <% } %>
                                </div>
                                <br>
                                <div style="display:flex; gap:10px;">
                                    <button type="submit" name="acao" value="alterar" class="btn btn-azul">Salvar alterações</button>
                                    <% if (logado.isAdministrador() && logado.getId() != u.getId()) { %>
                                    <button type="submit" name="acao" value="remover" class="btn btn-perigo"
                                            onclick="return confirmarRemocao('Remover este usuário do clube?')">Remover</button>
                                    <% } %>
                                </div>
                            </form>
                            <% if (logado.getId() == u.getId()) { %>
                                <p class="subtitulo" style="margin-top:10px;">Para apagar sua própria conta, use o botão "Apagar conta" na tela Home.</p>
                            <% } %>
                        <% } else { %>
                            <p class="subtitulo">Você não tem permissão para editar este usuário.</p>
                        <% } %>
                    </div>
                </div>
            <% } %>
        </tbody>
    </table>
</div>

<jsp:include page="/components/footer.jsp" />
