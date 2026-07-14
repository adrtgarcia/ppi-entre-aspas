<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="model.Usuario, model.Livro, java.util.List" %>
<%
    request.setAttribute("titulo", "Livros");
    Usuario logado = (Usuario) session.getAttribute("usuarioLogado");
    List<Livro> livros = (List<Livro>) request.getAttribute("livros");
    String ctx = request.getContextPath();
%>
<jsp:include page="/components/header.jsp" />

<h1>Acervo de Livros</h1>
<p class="subtitulo">Livros disponíveis para leitura e desafios do clube.</p>

<% if (request.getAttribute("sucesso") != null) { %>
    <div class="msg msg-sucesso"><%= request.getAttribute("sucesso") %></div>
<% } %>
<% if (request.getAttribute("erro") != null) { %>
    <div class="msg msg-erro"><%= request.getAttribute("erro") %></div>
<% } %>

<form class="busca-form" action="<%= ctx %>/livro" method="get">
    <input type="text" name="q" placeholder="Buscar por nome, autor ou gênero..."
           value="<%= request.getAttribute("termoBusca") != null ? request.getAttribute("termoBusca") : "" %>">
    <button type="submit" class="btn btn-outline">Pesquisar</button>
    <button type="button" class="btn btn-rosa" onclick="abrirModal('modal-novo-livro')">+ Cadastrar livro</button>
</form>

<div class="tabela-scroll">
    <table class="tabela">
        <thead>
            <tr>
                <th>Capa</th>
                <th>Nome</th>
                <th>Autor</th>
                <th>Gênero</th>
                <th>Ano</th>
            </tr>
        </thead>
        <tbody>
            <% if (livros == null || livros.isEmpty()) { %>
                <tr><td colspan="5" class="tabela-vazia">Nenhum livro cadastrado ainda.</td></tr>
            <% } %>
            <% for (Livro l : livros) {
                 boolean podeGerenciar = logado.podeGerenciar(l.getCriadoPorId());
            %>
                <tr class="linha-clicavel" onclick="abrirModal('modal-livro-<%= l.getId() %>')">
                    <td><img class="miniatura" src="<%= l.getCapaUrl() != null ? ctx + "/" + l.getCapaUrl() : "https://placehold.co/80x110/54aaf5/fff?text=." %>"></td>
                    <td><%= l.getNome() %></td>
                    <td><%= l.getAutor() %></td>
                    <td><span class="badge badge-cinza"><%= l.getGenero() %></span></td>
                    <td><%= l.getAnoPublicacao() %></td>
                </tr>

                <div class="overlay" id="modal-livro-<%= l.getId() %>">
                    <div class="modal">
                        <button class="modal-fechar" onclick="fecharModal('modal-livro-<%= l.getId() %>')">&times;</button>
                        <h2><%= l.getNome() %></h2>
                        <p class="subtitulo"><%= l.getAutor() %> · <%= l.getGenero() %> · <%= l.getAnoPublicacao() %></p>
                        <p><%= l.getSinopse() != null ? l.getSinopse() : "" %></p>
                        <a class="btn btn-outline btn-sm" href="<%= ctx %>/ficha-leitura?id=<%= l.getId() %>" target="_blank">Imprimir ficha de leitura</a>
                        <hr>

                        <% if (podeGerenciar) { %>
                            <form action="<%= ctx %>/livro" method="post" enctype="multipart/form-data">
                                <input type="hidden" name="id" value="<%= l.getId() %>">
                                <div class="form-grid">
                                    <div><label>Nome</label><input type="text" name="nome" value="<%= l.getNome() %>" required></div>
                                    <div><label>Autor</label><input type="text" name="autor" value="<%= l.getAutor() %>" required></div>
                                    <div>
                                        <label>Gênero</label>
                                        <select name="genero" required>
                                            <option value="" disabled <%= (l.getGenero() == null || l.getGenero().isBlank()) ? "selected" : "" %>>Selecione...</option>
                                            <option value="Ficção"    <%= "Ficção".equals(l.getGenero())    ? "selected" : "" %>>Ficção</option>
                                            <option value="Romance"   <%= "Romance".equals(l.getGenero())   ? "selected" : "" %>>Romance</option>
                                            <option value="Mistério"  <%= "Mistério".equals(l.getGenero())  ? "selected" : "" %>>Mistério</option>
                                            <option value="Fantasia"  <%= "Fantasia".equals(l.getGenero())  ? "selected" : "" %>>Fantasia</option>
                                            <option value="Terror"    <%= "Terror".equals(l.getGenero())    ? "selected" : "" %>>Terror</option>
                                            <option value="Biografia" <%= "Biografia".equals(l.getGenero()) ? "selected" : "" %>>Biografia</option>
                                            <option value="Poesia"    <%= "Poesia".equals(l.getGenero())    ? "selected" : "" %>>Poesia</option>
                                            <option value="Clássico"  <%= "Clássico".equals(l.getGenero())  ? "selected" : "" %>>Clássico</option>
                                            <option value="Outro"     <%= "Outro".equals(l.getGenero())     ? "selected" : "" %>>Outro</option>
                                        </select>
                                    </div>
                                    <div><label>Ano de publicação</label><input type="number" name="anoPublicacao" value="<%= l.getAnoPublicacao() %>" required></div>
                                    <div style="grid-column:1/-1;"><label>Sinopse</label><textarea name="sinopse"><%= l.getSinopse() != null ? l.getSinopse() : "" %></textarea></div>
                                    <div><label>Nova capa</label><input type="file" name="capa" accept=".jpg,.jpeg,.png,.webp"></div>
                                </div>
                                <br>
                                <div style="display:flex; gap:10px;">
                                    <button type="submit" name="acao" value="alterar" class="btn btn-azul">Atualizar livro</button>
                                    <button type="submit" name="acao" value="remover" class="btn btn-perigo"
                                            onclick="return confirmarRemocao('Apagar este livro do acervo?')">Apagar livro</button>
                                </div>
                            </form>
                        <% } else { %>
                            <p class="subtitulo">Apenas quem cadastrou este livro (ou um administrador) pode editá-lo.</p>
                        <% } %>
                    </div>
                </div>
            <% } %>
        </tbody>
    </table>
</div>

<!-- ── Modal: cadastrar livro ─────────────────────────────── -->
<div class="overlay" id="modal-novo-livro">
    <div class="modal">
        <button class="modal-fechar" onclick="fecharModal('modal-novo-livro')">&times;</button>
        <h2>Cadastrar novo livro</h2>
        <form action="<%= ctx %>/livro" method="post" enctype="multipart/form-data">
            <div class="form-grid">
                <div><label>Nome</label><input type="text" name="nome" required></div>
                <div><label>Autor</label><input type="text" name="autor" required></div>
                <div>
                    <label>Gênero</label>
                    <select name="genero" required>
                        <option value="" disabled selected>Selecione...</option>
                        <option value="Ficção">Ficção</option>
                        <option value="Romance">Romance</option>
                        <option value="Mistério">Mistério</option>
                        <option value="Fantasia">Fantasia</option>
                        <option value="Terror">Terror</option>
                        <option value="Biografia">Biografia</option>
                        <option value="Poesia">Poesia</option>
                        <option value="Clássico">Clássico</option>
                        <option value="Outro">Outro</option>
                    </select>
                </div>
                <div><label>Ano de publicação</label><input type="number" name="anoPublicacao" required></div>
                <div style="grid-column:1/-1;"><label>Sinopse</label><textarea name="sinopse"></textarea></div>
                <div><label>Capa do livro</label><input type="file" name="capa" accept=".jpg,.jpeg,.png,.webp"></div>
            </div>
            <br>
            <button type="submit" name="acao" value="cadastrar" class="btn btn-rosa">Cadastrar livro</button>
        </form>
    </div>
</div>

<jsp:include page="/components/footer.jsp" />
