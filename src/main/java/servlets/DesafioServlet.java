package servlets;

import dao.DesafioDAO;
import dao.LivroDAO;
import dao.UsuarioDesafioDAO;
import model.Desafio;
import model.Usuario;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Servlet responsável pela tela "Desafios" (CRUD, busca, exclusão definitiva e
 * participação). Um desafio pode ter 1 ou mais livros (tabela desafio_livro).
 * Toda operação termina com jsp:forward (RequestDispatcher.forward) para desafios.jsp.
 *
 * Regra de posse: qualquer usuário logado pode criar um desafio. Só quem
 * criou (criado_por_id) ou um administrador pode alterar/remover.
 */
public class DesafioServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final DesafioDAO desafioDAO = new DesafioDAO();
    private final UsuarioDesafioDAO usuarioDesafioDAO = new UsuarioDesafioDAO();
    private final LivroDAO livroDAO = new LivroDAO();

    // ── GET: lista ou busca ──────────────────────────────────────
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String termo = request.getParameter("q");
        try {
            List<Desafio> desafios = (termo != null && !termo.isBlank())
                    ? desafioDAO.buscar(termo.trim())
                    : desafioDAO.listarTodos();
            request.setAttribute("desafios", desafios);
            request.setAttribute("termoBusca", termo);

            HttpSession session = request.getSession(false);
            Usuario logado = session != null ? (Usuario) session.getAttribute("usuarioLogado") : null;
            if (logado != null) {
                List<Integer> participando = new ArrayList<>();
                for (Desafio d : desafios) {
                    if (usuarioDesafioDAO.estaParticipando(logado.getId(), d.getId())) {
                        participando.add(d.getId());
                    }
                }
                request.setAttribute("participando", participando);
            }
            request.setAttribute("todosLivros", livroDAO.listarTodos());
        } catch (SQLException e) {
            request.setAttribute("erro", "Erro ao consultar desafios: " + e.getMessage());
        }

        request.getRequestDispatcher("/desafios.jsp").forward(request, response);
    }

    // ── POST: cadastrar / alterar / remover / participar ─────────
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String acao = request.getParameter("acao");
        HttpSession session = request.getSession(false);
        Usuario logado = session != null ? (Usuario) session.getAttribute("usuarioLogado") : null;

        if (logado == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        try {
            switch (acao != null ? acao : "") {
                case "cadastrar" -> cadastrar(request, logado);
                case "alterar"   -> alterar(request, logado);
                case "remover"   -> remover(request, logado);
                case "participar" -> participar(request, logado);
                default -> request.setAttribute("erro", "Ação desconhecida: " + acao);
            }
        } catch (NumberFormatException e) {
            request.setAttribute("erro", "Dados inválidos: " + e.getMessage());
        } catch (SQLException e) {
            request.setAttribute("erro", "Erro ao processar solicitação: " + e.getMessage());
        }

        doGet(request, response);
    }

    private List<Integer> lerLivroIds(HttpServletRequest request) {
        String[] valores = request.getParameterValues("livroIds");
        List<Integer> ids = new ArrayList<>();
        if (valores != null) {
            for (String v : valores) ids.add(Integer.parseInt(v));
        }
        return ids;
    }

    private void participar(HttpServletRequest request, Usuario logado) throws SQLException {
        int desafioId = Integer.parseInt(request.getParameter("id"));
        Desafio desafio = desafioDAO.buscarPorId(desafioId);
        if (desafio == null) {
            request.setAttribute("erro", "Desafio não encontrado.");
        } else if (desafio.isFinalizado()) {
            request.setAttribute("erro", "Este desafio já foi finalizado — não é mais possível se inscrever.");
        } else {
            usuarioDesafioDAO.participar(logado.getId(), desafioId);
            request.setAttribute("sucesso", "Você entrou no desafio!");
        }
    }

    private void cadastrar(HttpServletRequest request, Usuario logado) throws SQLException {
        String nome = request.getParameter("nome");
        String descricao = request.getParameter("descricao");
        LocalDate inicio = LocalDate.parse(request.getParameter("dataInicio"));
        LocalDate fim = LocalDate.parse(request.getParameter("dataFim"));

        Desafio desafio = new Desafio(nome, descricao, inicio, fim);
        desafio.setCriadoPorId(logado.getId());
        desafioDAO.inserir(desafio);
        desafioDAO.vincularLivros(desafio.getId(), lerLivroIds(request));

        request.setAttribute("sucesso", "Desafio cadastrado com sucesso!");
    }

    private void alterar(HttpServletRequest request, Usuario logado) throws SQLException {
        int id = Integer.parseInt(request.getParameter("id"));
        Desafio desafio = desafioDAO.buscarPorId(id);
        if (desafio == null) {
            request.setAttribute("erro", "Desafio não encontrado (id=" + id + ")");
            return;
        }
        if (!logado.podeGerenciar(desafio.getCriadoPorId())) {
            request.setAttribute("erro", "Você só pode editar desafios criados por você.");
            return;
        }

        desafio.setNome(request.getParameter("nome"));
        desafio.setDescricao(request.getParameter("descricao"));
        desafio.setDataInicio(LocalDate.parse(request.getParameter("dataInicio")));
        desafio.setDataFim(LocalDate.parse(request.getParameter("dataFim")));

        desafioDAO.atualizar(desafio);
        desafioDAO.vincularLivros(id, lerLivroIds(request));

        request.setAttribute("sucesso", "Desafio atualizado com sucesso!");
    }

    private void remover(HttpServletRequest request, Usuario logado) throws SQLException {
        int id = Integer.parseInt(request.getParameter("id"));
        Desafio desafio = desafioDAO.buscarPorId(id);
        if (desafio == null) {
            request.setAttribute("erro", "Desafio não encontrado (id=" + id + ")");
            return;
        }
        if (!logado.podeGerenciar(desafio.getCriadoPorId())) {
            request.setAttribute("erro", "Você só pode remover desafios criados por você.");
            return;
        }

        boolean ok = desafioDAO.remover(id);
        request.setAttribute(ok ? "sucesso" : "erro",
                ok ? "Desafio removido com sucesso!" : "Erro ao remover desafio.");
    }
}
