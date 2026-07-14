<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="model.Usuario, model.UsuarioDesafio, model.Avaliacao, java.util.List" %>
<%
    request.setAttribute("titulo", "Home");
    Usuario logado = (Usuario) session.getAttribute("usuarioLogado");
    List<UsuarioDesafio> emAndamento = (List<UsuarioDesafio>) request.getAttribute("emAndamento");
    List<UsuarioDesafio> concluidos = (List<UsuarioDesafio>) request.getAttribute("concluidos");
    List<Avaliacao> minhasAvaliacoes = (List<Avaliacao>) request.getAttribute("minhasAvaliacoes");
    String ctx = request.getContextPath();
%>
<jsp:include page="/components/header.jsp" />

<% if (request.getAttribute("sucesso") != null) { %>
    <div class="msg msg-sucesso"><%= request.getAttribute("sucesso") %></div>
<% } %>
<% if (request.getAttribute("erro") != null) { %>
    <div class="msg msg-erro"><%= request.getAttribute("erro") %></div>
<% } %>

<!-- ── Barra de perfil do usuário logado ─────────────────── -->
<div class="perfil-topo">
    <img class="avatar" src="<%= logado.getFotoUrl() != null ? ctx + "/" + logado.getFotoUrl() : "https://api.dicebear.com/7.x/initials/svg?seed=" + logado.getNome() %>" alt="Foto de perfil">
    <div class="info">
        <h2><%= logado.getNome() %></h2>
        <span class="cargo-tag"><%= logado.getCargo() %></span>
    </div>
    <div class="acoes">
        <a class="btn btn-azul" href="<%= ctx %>/carteirinha" target="_blank">Imprimir carteirinha</a>
        <button class="btn btn-outline" onclick="abrirModal('modal-editar-perfil')">Atualizar perfil</button>
        <button class="btn btn-perigo" onclick="abrirModal('modal-apagar-conta')">Apagar conta</button>
        <a class="btn btn-outline" href="<%= ctx %>/logout">Sair</a>
    </div>
</div>

<!-- ── Desafios em andamento ──────────────────────────────── -->
<h2>Meus desafios em andamento</h2>
<% if (emAndamento == null || emAndamento.isEmpty()) { %>
    <p class="subtitulo">Você não está participando de nenhum desafio no momento. Veja os disponíveis em <a href="<%= ctx %>/desafio" style="color:var(--rosa); font-weight:700;">Desafios</a>.</p>
<% } else { %>
    <div class="tabela-scroll">
        <table class="tabela">
            <thead>
                <tr>
                    <th>Status</th>
                    <th>Desafio</th>
                    <th>Período</th>
                </tr>
            </thead>
            <tbody>
                <% for (UsuarioDesafio ud : emAndamento) { %>
                    <tr class="linha-clicavel" onclick="abrirModal('modal-ud-<%= ud.getDesafioId() %>')">
                        <td><span class="badge">Em andamento</span></td>
                        <td><%= ud.getDesafio().getNome() %></td>
                        <td><%= ud.getDesafio().getDataInicio() %> a <%= ud.getDesafio().getDataFim() %></td>
                    </tr>

                    <div class="overlay" id="modal-ud-<%= ud.getDesafioId() %>">
                        <div class="modal">
                            <button class="modal-fechar" onclick="fecharModal('modal-ud-<%= ud.getDesafioId() %>')">&times;</button>
                            <h2><%= ud.getDesafio().getNome() %></h2>
                            <p><%= ud.getDesafio().getDescricao() %></p>
                            <p class="subtitulo">Período: <%= ud.getDesafio().getDataInicio() %> até <%= ud.getDesafio().getDataFim() %></p>
                            <hr>
                            <form action="<%= ctx %>/home" method="post" style="display:flex; gap:10px;">
                                <input type="hidden" name="desafioId" value="<%= ud.getDesafioId() %>">
                                <button type="submit" name="acao" value="concluir" class="btn btn-azul">Concluir desafio</button>
                                <button type="submit" name="acao" value="desistir" class="btn btn-perigo"
                                        onclick="return confirmarRemocao('Tem certeza que deseja desistir deste desafio?')">Desistir</button>
                            </form>
                        </div>
                    </div>
                <% } %>
            </tbody>
        </table>
    </div>
<% } %>

<!-- ── Desafios concluídos ────────────────────────────────── -->
<h2 style="margin-top:32px;">Desafios concluídos</h2>
<% if (concluidos == null || concluidos.isEmpty()) { %>
    <p class="subtitulo">Você ainda não concluiu nenhum desafio.</p>
<% } else { %>
    <div class="tabela-scroll">
        <table class="tabela">
            <thead>
                <tr>
                    <th>Status</th>
                    <th>Desafio</th>
                    <th>Período</th>
                </tr>
            </thead>
            <tbody>
                <% for (UsuarioDesafio ud : concluidos) { %>
                    <tr>
                        <td><span class="badge badge-concluido">Concluído</span></td>
                        <td><%= ud.getDesafio().getNome() %></td>
                        <td><%= ud.getDesafio().getDataInicio() %> a <%= ud.getDesafio().getDataFim() %></td>
                    </tr>
                <% } %>
            </tbody>
        </table>
    </div>
<% } %>

<!-- ── Minhas avaliações ──────────────────────────────────── -->
<h2 style="margin-top:32px;">Minhas avaliações</h2>
<% if (minhasAvaliacoes == null || minhasAvaliacoes.isEmpty()) { %>
    <p class="subtitulo">Você ainda não avaliou nenhum livro. Vá até <a href="<%= ctx %>/avaliacao" style="color:var(--rosa); font-weight:700;">Avaliações</a>.</p>
<% } else { %>
    <div class="tabela-scroll">
        <table class="tabela">
            <thead>
                <tr>
                    <th>Nota</th>
                    <th>Livro</th>
                    <th>Comentário</th>
                </tr>
            </thead>
            <tbody>
                <% for (Avaliacao a : minhasAvaliacoes) { %>
                    <tr>
                        <td><span class="badge badge-rosa">⭐ <%= a.getNota() %>/5</span></td>
                        <td><%= a.getNomeLivro() %></td>
                        <td><%= a.getComentario() != null ? a.getComentario() : "" %></td>
                    </tr>
                <% } %>
            </tbody>
        </table>
    </div>
<% } %>

<!-- ── Modal: alterar informações do próprio perfil ───────── -->
<div class="overlay" id="modal-editar-perfil">
    <div class="modal">
        <button class="modal-fechar" onclick="fecharModal('modal-editar-perfil')">&times;</button>
        <h2>Alterar minhas informações</h2>
        <form action="<%= ctx %>/usuario" method="post" enctype="multipart/form-data">
            <input type="hidden" name="acao" value="alterar">
            <input type="hidden" name="id" value="<%= logado.getId() %>">
            <div class="form-grid">
                <div>
                    <label>Nome</label>
                    <input type="text" name="nome" value="<%= logado.getNome() %>" required>
                </div>
                <div>
                    <label>Gênero favorito</label>
                    <select name="generoFav">
                        <option value="Ficção"  <%= "Ficção".equals(logado.getGeneroFav())  ? "selected" : "" %>>Ficção</option>
                        <option value="Romance" <%= "Romance".equals(logado.getGeneroFav()) ? "selected" : "" %>>Romance</option>
                        <option value="Mistério" <%= "Mistério".equals(logado.getGeneroFav()) ? "selected" : "" %>>Mistério</option>
                        <option value="Fantasia" <%= "Fantasia".equals(logado.getGeneroFav()) ? "selected" : "" %>>Fantasia</option>
                        <option value="Biografia" <%= "Biografia".equals(logado.getGeneroFav()) ? "selected" : "" %>>Biografia</option>
                        <option value="Outro" <%= "Outro".equals(logado.getGeneroFav()) ? "selected" : "" %>>Outro</option>
                    </select>
                </div>
                <div>
                    <label>Nova senha (deixe em branco para manter)</label>
                    <input type="password" name="senha">
                </div>
                <div>
                    <label>Nova foto de perfil</label>
                    <input type="file" name="foto" accept=".jpg,.jpeg,.png,.webp">
                </div>
            </div>
            <br>
            <button type="submit" class="btn btn-azul">Salvar alterações</button>
        </form>
    </div>
</div>

<!-- ── Modal: apagar a própria conta ──────────────────────── -->
<div class="overlay" id="modal-apagar-conta">
    <div class="modal">
        <button class="modal-fechar" onclick="fecharModal('modal-apagar-conta')">&times;</button>
        <h2>Apagar minha conta</h2>
        <p class="subtitulo">
            Essa ação apaga seu cadastro do Entre Aspas <strong>permanentemente</strong> (junto com suas
            avaliações e participações em desafios) e encerra sua sessão imediatamente. Não é possível
            desfazer ou recuperar os dados depois.
        </p>
        <form action="<%= ctx %>/usuario" method="post">
            <input type="hidden" name="id" value="<%= logado.getId() %>">
            <button type="submit" name="acao" value="remover" class="btn btn-perigo"
                    onclick="return confirmarRemocao('Tem certeza que deseja apagar sua conta? Essa ação encerra sua sessão agora.')">
                Sim, apagar minha conta
            </button>
        </form>
    </div>
</div>

<jsp:include page="/components/footer.jsp" />
