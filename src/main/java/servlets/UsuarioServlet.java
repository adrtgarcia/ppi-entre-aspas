package servlets;

import dao.UsuarioDAO;
import model.Usuario;
import util.SenhaUtil;
import util.UploadUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Servlet responsável pela tela "Usuários" (listagem, busca, atualização e exclusão definitiva).
 * Todas as operações terminam com jsp:forward (RequestDispatcher.forward) para usuarios.jsp,
 * exceto quando o próprio usuário apaga a própria conta — nesse caso a sessão é encerrada
 * e o fluxo é redirecionado para o login.
 *
 * Regra de posse: usuário comum só edita/apaga o próprio cadastro.
 * Administrador tem CRUD completo sobre qualquer usuário (inclusive troca de cargo).
 */
@MultipartConfig(maxFileSize = 5 * 1024 * 1024, maxRequestSize = 10 * 1024 * 1024)
public class UsuarioServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    // ── GET: lista ou busca ──────────────────────────────────────
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String termo = request.getParameter("q");
        try {
            List<Usuario> usuarios = (termo != null && !termo.isBlank())
                    ? usuarioDAO.buscar(termo.trim())
                    : usuarioDAO.listarTodos();
            request.setAttribute("usuarios", usuarios);
            request.setAttribute("termoBusca", termo);
        } catch (SQLException e) {
            request.setAttribute("erro", "Erro ao consultar usuários: " + e.getMessage());
        }

        request.getRequestDispatcher("/usuarios.jsp").forward(request, response);
    }

    // ── POST: alterar / excluir ─────────────────────────────────
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        Usuario logado = session != null ? (Usuario) session.getAttribute("usuarioLogado") : null;
        if (logado == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        String acao = request.getParameter("acao");
        boolean apagouAPropriaConta = false;

        try {
            switch (acao != null ? acao : "") {
                case "alterar" -> alterar(request, logado, session);
                case "remover" -> apagouAPropriaConta = remover(request, logado);
                default -> request.setAttribute("erro", "Ação desconhecida: " + acao);
            }
        } catch (NumberFormatException e) {
            request.setAttribute("erro", "ID inválido.");
        } catch (SQLException | IOException | ServletException e) {
            request.setAttribute("erro", "Erro ao processar solicitação: " + e.getMessage());
        }

        // se o próprio usuário apagou a conta, encerra a sessão e volta pro login
        if (apagouAPropriaConta) {
            session.invalidate();
            response.sendRedirect(request.getContextPath() + "/login.jsp?contaRemovida=1");
            return;
        }

        // recarrega a lista e encaminha de volta (sucesso ou erro)
        doGet(request, response);
    }

    private void alterar(HttpServletRequest request, Usuario logado, HttpSession session)
            throws SQLException, IOException, ServletException {
        int id = Integer.parseInt(request.getParameter("id"));
        Usuario usuario = usuarioDAO.buscarPorId(id);
        if (usuario == null) {
            request.setAttribute("erro", "Usuário não encontrado (id=" + id + ")");
            return;
        }

        boolean editandoProprioPerfil = logado.getId() == id;

        // usuário comum só edita o próprio perfil; administrador edita qualquer um
        if (!logado.podeGerenciar(editandoProprioPerfil ? logado.getId() : null)) {
            request.setAttribute("erro", "Você só pode editar o seu próprio perfil.");
            return;
        }

        usuario.setNome(request.getParameter("nome"));
        usuario.setGeneroFav(request.getParameter("generoFav"));

        String senha = request.getParameter("senha");
        if (senha != null && !senha.isBlank()) usuario.setSenha(SenhaUtil.codificar(senha));

        // apenas administradores podem trocar o cargo de um usuário
        if (logado.isAdministrador()) {
            String cargo = request.getParameter("cargo");
            if (cargo != null && !cargo.isBlank()) usuario.setCargo(cargo);
        }

        Part fotoPart = request.getPart("foto");
        String novaFoto = UploadUtil.salvar(fotoPart, "fotos", getServletContext());
        if (novaFoto != null) usuario.setFotoUrl(novaFoto);

        usuarioDAO.atualizar(usuario);

        // mantém a sessão sincronizada quando o próprio usuário se edita
        if (editandoProprioPerfil) session.setAttribute("usuarioLogado", usuario);

        request.setAttribute("sucesso", "Usuário atualizado com sucesso!");
    }

    /** @return true se o próprio usuário logado apagou a própria conta (precisa encerrar a sessão) */
    private boolean remover(HttpServletRequest request, Usuario logado) throws SQLException {
        int id = Integer.parseInt(request.getParameter("id"));
        boolean proprioUsuario = logado.getId() == id;

        if (!logado.podeGerenciar(proprioUsuario ? logado.getId() : null)) {
            request.setAttribute("erro", "Você só pode apagar a sua própria conta.");
            return false;
        }

        boolean ok = usuarioDAO.remover(id);
        if (!ok) {
            request.setAttribute("erro", "Erro ao remover usuário.");
            return false;
        }

        request.setAttribute("sucesso", "Usuário removido com sucesso!");
        return proprioUsuario;
    }
}
