<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="model.Usuario, model.Desafio, model.Livro, java.util.List" %>
<%
    request.setAttribute("titulo", "Desafios");
    Usuario logado = (Usuario) session.getAttribute("usuarioLogado");
    List<Desafio> desafios = (List<Desafio>) request.getAttribute("desafios");
    List<Livro> todosLivros = (List<Livro>) request.getAttribute("todosLivros");
    List<Integer> participando = (List<Integer>) request.getAttribute("participando");
    String ctx = request.getContextPath();
%>
<jsp:include page="/components/header.jsp" />

<h1>Desafios de Leitura</h1>
<p class="subtitulo">Participe de um ou mais desafios ao mesmo tempo!</p>

<% if (request.getAttribute("sucesso") != null) { %>
    <div class="msg msg-sucesso"><%= request.getAttribute("sucesso") %></div>
<% } %>
<% if (request.getAttribute("erro") != null) { %>
    <div class="msg msg-erro"><%= request.getAttribute("erro") %></div>
<% } %>

<form class="busca-form" action="<%= ctx %>/desafio" method="get">
    <input type="text" name="q" placeholder="Buscar por nome ou descrição..."
           value="<%= request.getAttribute("termoBusca") != null ? request.getAttribute("termoBusca") : "" %>">
    <button type="submit" class="btn btn-outline">Pesquisar</button>
    <button type="button" class="btn btn-rosa" onclick="abrirModal('modal-novo-desafio')">+ Criar desafio</button>
</form>

<div class="tabela-scroll">
    <table class="tabela">
        <thead>
            <tr>
                <th>Status</th>
                <th>Nome</th>
                <th>Período</th>
                <th>Participantes</th>
            </tr>
        </thead>
        <tbody>
            <% if (desafios == null || desafios.isEmpty()) { %>
                <tr><td colspan="4" class="tabela-vazia">Nenhum desafio cadastrado ainda.</td></tr>
            <% } %>
            <% for (Desafio d : desafios) {
                 boolean jaParticipa = participando != null && participando.contains(d.getId());
                 boolean podeGerenciar = logado.podeGerenciar(d.getCriadoPorId());
            %>
                <tr class="linha-clicavel" onclick="abrirModal('modal-desafio-<%= d.getId() %>')">
                    <td>
                        <% if (d.isFinalizado()) { %>
                            <span class="badge badge-cinza">Encerrado</span>
                        <% } else if (d.isEmAndamento()) { %>
                            <span class="badge">Em andamento</span>
                        <% } else { %>
                            <span class="badge badge-rosa">Em breve</span>
                        <% } %>
                    </td>
                    <td><%= d.getNome() %></td>
                    <td><%= d.getDataInicio() %> a <%= d.getDataFim() %></td>
                    <td><%= d.getTotalParticipantes() %></td>
                </tr>

                <div class="overlay" id="modal-desafio-<%= d.getId() %>">
                    <div class="modal">
                        <button class="modal-fechar" onclick="fecharModal('modal-desafio-<%= d.getId() %>')">&times;</button>
                        <h2><%= d.getNome() %></h2>
                        <p><%= d.getDescricao() %></p>
                        <p class="subtitulo">Período: <%= d.getDataInicio() %> até <%= d.getDataFim() %> · <%= d.getTotalParticipantes() %> participante(s)</p>

                        <p><strong>Livros do desafio:</strong>
                            <% for (Livro l : d.getLivros()) { %>
                                <span class="badge badge-cinza"><%= l.getNome() %></span>
                            <% } %>
                        </p>

                        <% if (!jaParticipa && !d.isFinalizado()) { %>
                            <form action="<%= ctx %>/desafio" method="post">
                                <input type="hidden" name="id" value="<%= d.getId() %>">
                                <button type="submit" name="acao" value="participar" class="btn btn-rosa">Participar deste desafio</button>
                            </form>
                        <% } else if (!jaParticipa) { %>
                            <p class="subtitulo">Este desafio já foi finalizado. Não é mais possível se inscrever.</p>
                        <% } else { %>
                            <p class="subtitulo">Você já está participando (veja na Home).</p>
                        <% } %>

                        <% if (podeGerenciar) { %>
                            <hr>
                            <form action="<%= ctx %>/desafio" method="post">
                                <input type="hidden" name="id" value="<%= d.getId() %>">
                                <div class="form-grid">
                                    <div><label>Nome</label><input type="text" name="nome" value="<%= d.getNome() %>" required></div>
                                    <div><label>Data início</label><input type="date" name="dataInicio" value="<%= d.getDataInicio() %>" required></div>
                                    <div><label>Data fim</label><input type="date" name="dataFim" value="<%= d.getDataFim() %>" required></div>
                                    <div style="grid-column:1/-1;"><label>Descrição</label><textarea name="descricao" required><%= d.getDescricao() %></textarea></div>
                                    <div style="grid-column:1/-1;">
                                        <label>Livros do desafio</label>
                                        <% for (Livro l : todosLivros) {
                                             boolean marcado = false;
                                             for (Livro dl : d.getLivros()) if (dl.getId() == l.getId()) marcado = true;
                                        %>
                                            <label style="font-weight:400; display:inline-block; margin-right:14px;">
                                                <input type="checkbox" name="livroIds" value="<%= l.getId() %>" <%= marcado ? "checked" : "" %> style="width:auto;">
                                                <%= l.getNome() %>
                                            </label>
                                        <% } %>
                                    </div>
                                </div>
                                <br>
                                <div style="display:flex; gap:10px;">
                                    <button type="submit" name="acao" value="alterar" class="btn btn-azul">Atualizar desafio</button>
                                    <button type="submit" name="acao" value="remover" class="btn btn-perigo"
                                            onclick="return confirmarRemocao('Apagar este desafio?')">Apagar desafio</button>
                                </div>
                            </form>
                        <% } %>
                    </div>
                </div>
            <% } %>
        </tbody>
    </table>
</div>

<!-- ── Modal: criar desafio ───────────────────────────────── -->
<div class="overlay" id="modal-novo-desafio">
    <div class="modal">
        <button class="modal-fechar" onclick="fecharModal('modal-novo-desafio')">&times;</button>
        <h2>Criar novo desafio</h2>
        <form action="<%= ctx %>/desafio" method="post">
            <div class="form-grid">
                <div><label>Nome</label><input type="text" name="nome" required></div>
                <div><label>Data início</label><input type="date" name="dataInicio" required></div>
                <div><label>Data fim</label><input type="date" name="dataFim" required></div>
                <div style="grid-column:1/-1;"><label>Descrição</label><textarea name="descricao" required></textarea></div>
                <div style="grid-column:1/-1;">
                    <label>Livros do desafio</label>
                    <% for (Livro l : todosLivros) { %>
                        <label style="font-weight:400; display:inline-block; margin-right:14px;">
                            <input type="checkbox" name="livroIds" value="<%= l.getId() %>" style="width:auto;"> <%= l.getNome() %>
                        </label>
                    <% } %>
                </div>
            </div>
            <br>
            <button type="submit" name="acao" value="cadastrar" class="btn btn-rosa">Criar desafio</button>
        </form>
    </div>
</div>

<jsp:include page="/components/footer.jsp" />
