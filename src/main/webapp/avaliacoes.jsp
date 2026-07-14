<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="model.Usuario, model.Avaliacao, model.Livro, java.util.List" %>
<%
    request.setAttribute("titulo", "Avaliações");
    Usuario logado = (Usuario) session.getAttribute("usuarioLogado");
    List<Avaliacao> avaliacoes = (List<Avaliacao>) request.getAttribute("avaliacoes");
    List<Livro> todosLivros = (List<Livro>) request.getAttribute("todosLivros");
    String ctx = request.getContextPath();
%>
<jsp:include page="/components/header.jsp" />

<h1>Avaliações da Comunidade</h1>
<p class="subtitulo">O que os membros do clube estão achando dos livros lidos.</p>

<% if (request.getAttribute("sucesso") != null) { %>
    <div class="msg msg-sucesso"><%= request.getAttribute("sucesso") %></div>
<% } %>
<% if (request.getAttribute("erro") != null) { %>
    <div class="msg msg-erro"><%= request.getAttribute("erro") %></div>
<% } %>

<form class="busca-form" action="<%= ctx %>/avaliacao" method="get">
    <input type="text" name="q" placeholder="Buscar por usuário, livro ou comentário..."
           value="<%= request.getAttribute("termoBusca") != null ? request.getAttribute("termoBusca") : "" %>">
    <button type="submit" class="btn btn-outline">Pesquisar</button>
    <button type="button" class="btn btn-rosa" onclick="abrirModal('modal-nova-avaliacao')">+ Avaliar livro</button>
</form>

<div class="tabela-scroll">
    <table class="tabela">
        <thead>
            <tr>
                <th>Nota</th>
                <th>Livro</th>
                <th>Avaliado por</th>
            </tr>
        </thead>
        <tbody>
            <% if (avaliacoes == null || avaliacoes.isEmpty()) { %>
                <tr><td colspan="3" class="tabela-vazia">Nenhuma avaliação cadastrada ainda.</td></tr>
            <% } %>
            <% for (Avaliacao a : avaliacoes) {
                 boolean podeEditar = a.getUsuarioId() == logado.getId() || logado.isAdministrador();
            %>
                <tr class="linha-clicavel" onclick="abrirModal('modal-avaliacao-<%= a.getId() %>')">
                    <td><span class="badge badge-rosa">⭐ <%= a.getNota() %>/5</span></td>
                    <td><%= a.getNomeLivro() %></td>
                    <td><%= a.getNomeUsuario() %></td>
                </tr>

                <div class="overlay" id="modal-avaliacao-<%= a.getId() %>">
                    <div class="modal">
                        <button class="modal-fechar" onclick="fecharModal('modal-avaliacao-<%= a.getId() %>')">&times;</button>
                        <h2><%= a.getNomeLivro() %></h2>
                        <p class="subtitulo">Avaliado por <%= a.getNomeUsuario() %> · ⭐ <%= a.getNota() %>/5</p>
                        <p><%= a.getComentario() != null ? a.getComentario() : "" %></p>
                        <hr>

                        <% if (podeEditar) { %>
                            <form action="<%= ctx %>/avaliacao" method="post">
                                <input type="hidden" name="id" value="<%= a.getId() %>">
                                <div class="form-grid">
                                    <div>
                                        <label>Nota (1 a 5)</label>
                                        <input type="number" name="nota" min="1" max="5" value="<%= a.getNota() %>" required>
                                    </div>
                                    <div style="grid-column:1/-1;">
                                        <label>Comentário</label>
                                        <textarea name="comentario"><%= a.getComentario() != null ? a.getComentario() : "" %></textarea>
                                    </div>
                                </div>
                                <br>
                                <div style="display:flex; gap:10px;">
                                    <button type="submit" name="acao" value="alterar" class="btn btn-azul">Atualizar avaliação</button>
                                    <button type="submit" name="acao" value="remover" class="btn btn-perigo"
                                            onclick="return confirmarRemocao('Apagar esta avaliação?')">Apagar avaliação</button>
                                </div>
                            </form>
                        <% } else { %>
                            <p class="subtitulo">Apenas o autor pode editar esta avaliação.</p>
                        <% } %>
                    </div>
                </div>
            <% } %>
        </tbody>
    </table>
</div>

<!-- ── Modal: nova avaliação ──────────────────────────────── -->
<div class="overlay" id="modal-nova-avaliacao">
    <div class="modal">
        <button class="modal-fechar" onclick="fecharModal('modal-nova-avaliacao')">&times;</button>
        <h2>Avaliar um livro</h2>
        <form action="<%= ctx %>/avaliacao" method="post">
            <div class="form-grid">
                <div>
                    <label>Livro</label>
                    <select name="livroId" required>
                        <% for (Livro l : todosLivros) { %>
                            <option value="<%= l.getId() %>"><%= l.getNome() %></option>
                        <% } %>
                    </select>
                </div>
                <div>
                    <label>Nota (1 a 5)</label>
                    <input type="number" name="nota" min="1" max="5" required>
                </div>
                <div style="grid-column:1/-1;">
                    <label>Comentário</label>
                    <textarea name="comentario"></textarea>
                </div>
            </div>
            <br>
            <button type="submit" name="acao" value="cadastrar" class="btn btn-rosa">Avaliar livro</button>
        </form>
    </div>
</div>

<jsp:include page="/components/footer.jsp" />
