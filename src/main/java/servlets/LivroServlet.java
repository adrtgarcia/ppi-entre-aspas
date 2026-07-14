package servlets;

import dao.LivroDAO;
import model.Livro;
import model.Usuario;
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
 * Servlet responsável pela tela "Livros" (CRUD, busca e exclusão definitiva).
 * Recebe o upload da CAPA DO LIVRO (arquivo 2 de 2 exigidos pelo laboratório).
 * Toda operação termina com jsp:forward (RequestDispatcher.forward) para livros.jsp.
 *
 * Regra de posse: qualquer usuário logado pode cadastrar um livro. Só quem
 * cadastrou (criado_por_id) ou um administrador pode alterar/remover.
 */
@MultipartConfig(maxFileSize = 5 * 1024 * 1024, maxRequestSize = 10 * 1024 * 1024)
public class LivroServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final LivroDAO livroDAO = new LivroDAO();

    // ── GET: lista ou busca ──────────────────────────────────────
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String termo = request.getParameter("q");
        try {
            List<Livro> livros = (termo != null && !termo.isBlank())
                    ? livroDAO.buscar(termo.trim())
                    : livroDAO.listarTodos();
            request.setAttribute("livros", livros);
            request.setAttribute("termoBusca", termo);
        } catch (SQLException e) {
            request.setAttribute("erro", "Erro ao consultar livros: " + e.getMessage());
        }

        request.getRequestDispatcher("/livros.jsp").forward(request, response);
    }

    // ── POST: cadastrar / alterar / remover ──────────────────────
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
        try {
            switch (acao != null ? acao : "") {
                case "cadastrar" -> cadastrar(request, logado);
                case "alterar"   -> alterar(request, logado);
                case "remover"   -> remover(request, logado);
                default -> request.setAttribute("erro", "Ação desconhecida: " + acao);
            }
        } catch (NumberFormatException e) {
            request.setAttribute("erro", "Dados numéricos inválidos.");
        } catch (SQLException | IOException | ServletException e) {
            request.setAttribute("erro", "Erro ao processar solicitação: " + e.getMessage());
        }

        doGet(request, response);
    }

    private void cadastrar(HttpServletRequest request, Usuario logado) throws SQLException, IOException, ServletException {
        String nome   = request.getParameter("nome");
        String autor  = request.getParameter("autor");
        String genero = request.getParameter("genero");
        int ano       = Integer.parseInt(request.getParameter("anoPublicacao"));
        String sinopse = request.getParameter("sinopse");

        Part capaPart = request.getPart("capa");
        String capaUrl = UploadUtil.salvar(capaPart, "capas", getServletContext());

        Livro livro = new Livro(nome, autor, genero, ano, sinopse, capaUrl);
        livro.setCriadoPorId(logado.getId());
        livroDAO.inserir(livro);

        request.setAttribute("sucesso", "Livro cadastrado com sucesso!");
    }

    private void alterar(HttpServletRequest request, Usuario logado) throws SQLException, IOException, ServletException {
        int id = Integer.parseInt(request.getParameter("id"));
        Livro livro = livroDAO.buscarPorId(id);
        if (livro == null) {
            request.setAttribute("erro", "Livro não encontrado (id=" + id + ")");
            return;
        }
        if (!logado.podeGerenciar(livro.getCriadoPorId())) {
            request.setAttribute("erro", "Você só pode editar livros cadastrados por você.");
            return;
        }

        livro.setNome(request.getParameter("nome"));
        livro.setAutor(request.getParameter("autor"));
        livro.setGenero(request.getParameter("genero"));
        livro.setAnoPublicacao(Integer.parseInt(request.getParameter("anoPublicacao")));
        livro.setSinopse(request.getParameter("sinopse"));

        Part capaPart = request.getPart("capa");
        String novaCapa = UploadUtil.salvar(capaPart, "capas", getServletContext());
        if (novaCapa != null) livro.setCapaUrl(novaCapa);

        livroDAO.atualizar(livro);
        request.setAttribute("sucesso", "Livro atualizado com sucesso!");
    }

    private void remover(HttpServletRequest request, Usuario logado) throws SQLException {
        int id = Integer.parseInt(request.getParameter("id"));
        Livro livro = livroDAO.buscarPorId(id);
        if (livro == null) {
            request.setAttribute("erro", "Livro não encontrado (id=" + id + ")");
            return;
        }
        if (!logado.podeGerenciar(livro.getCriadoPorId())) {
            request.setAttribute("erro", "Você só pode remover livros cadastrados por você.");
            return;
        }

        boolean ok = livroDAO.remover(id);
        request.setAttribute(ok ? "sucesso" : "erro",
                ok ? "Livro removido com sucesso!" : "Erro ao remover livro.");
    }
}
