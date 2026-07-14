# Entre Aspas — Documentação da Estrutura do Projeto

Este documento explica como o sistema **Entre Aspas** (clube de leitura) está organizado por dentro:
a arquitetura em camadas, como cada camada conversa com a outra, e — com um foco especial —
**como o login funciona** e **como as permissões de administrador/usuário comum são checadas em
cada ação do sistema**.

O projeto é um **Java Web clássico**: Servlets + JSP + JDBC, sem frameworks (sem Spring, sem
Hibernate). Tudo é "feito na mão", o que é ótimo para entender exatamente o que está acontecendo
em cada etapa.

---

## 1. Visão geral da arquitetura

O projeto segue (de forma simplificada) o padrão **MVC**:

```
Navegador (JSP renderizado no servidor)
        │  HTTP GET/POST
        ▼
   Servlet (Controller)
        │  chama métodos de
        ▼
   DAO (Data Access Object)
        │  SQL via JDBC
        ▼
   Banco de dados MySQL (clube_do_livro)
```

- **Model** (`model/*.java`) — classes Java simples (POJOs) que representam as tabelas do banco:
  `Usuario`, `Livro`, `Desafio`, `Avaliacao`, `UsuarioDesafio`.
- **DAO** (`dao/*.java`) — uma classe por tabela principal, responsável por todo o SQL
  (`UsuarioDAO`, `LivroDAO`, `DesafioDAO`, `AvaliacaoDAO`, `UsuarioDesafioDAO`) + `ConexaoDB`,
  que abre a conexão com o MySQL.
- **Servlets** (`servlets/*.java`) — os "controllers". Recebem a requisição HTTP, decidem o que
  fazer (chamando os DAOs), e no final sempre fazem um `RequestDispatcher.forward(...)` para uma
  página JSP (o equivalente Java de `<jsp:forward>`).
- **JSP** (`webapp/*.jsp`) — a "view". Só existe para *exibir* dados que o Servlet colocou como
  atributo da requisição (`request.setAttribute(...)`). Não há chamadas AJAX nem API REST — cada
  clique em botão gera um GET/POST normal, que recarrega a página inteira.
- **Filtro** (`filters/AuthFilter.java`) — roda **antes** de qualquer Servlet/JSP e bloqueia
  quem não está logado.

Não existe camada de "Service" separada — a regra de negócio fica dentro do próprio Servlet
(nos métodos privados `cadastrar()`, `alterar()`, `remover()` etc.) e, quando envolve
apenas dados de uma tabela, dentro do DAO.

---

## 2. Estrutura de pastas

```
src/main/java/
├── model/       → Usuario, Livro, Desafio, Avaliacao, UsuarioDesafio
├── dao/         → ConexaoDB, UsuarioDAO, LivroDAO, DesafioDAO, AvaliacaoDAO, UsuarioDesafioDAO
├── util/        → SenhaUtil (codifica senha), UploadUtil (salva arquivos de upload)
├── filters/     → AuthFilter (exige login em quase todas as rotas)
└── servlets/    → LoginServlet, CadastroServlet, LogoutServlet, HomeServlet,
                   UsuarioServlet, LivroServlet, DesafioServlet, AvaliacaoServlet,
                   CarteirinhaServlet, FichaLeituraServlet

src/main/webapp/
├── login.jsp, cadastro.jsp, home.jsp, livros.jsp, desafios.jsp,
│   avaliacoes.jsp, usuarios.jsp, sobre.jsp, carteirinha.jsp, ficha-leitura.jsp
├── components/  → header.jsp, menu.jsp, footer.jsp (incluídos via <jsp:include>)
├── css/, js/    → style.css, app.js (modais e confirmações no cliente)
└── WEB-INF/web.xml → mapeamento de URL → Servlet
```

---

## 3. Model — as entidades

Cada classe em `model/` é um espelho de uma tabela do banco, com getters/setters e, às vezes,
pequenas regras de negócio que fazem sentido "morar" no próprio objeto.

| Classe           | Tabela            | Campos-chave para este documento                          |
|-------------------|-------------------|-------------------------------------------------------------|
| `Usuario`         | `usuario`         | `cargo` ("usuario" ou "administrador"), `id`                |
| `Livro`            | `livro`            | `criadoPorId` (quem cadastrou)                              |
| `Desafio`          | `desafio`          | `criadoPorId` (quem criou), `livros` (preenchido via join)  |
| `Avaliacao`        | `avaliacao`        | `usuarioId` (quem avaliou)                                   |
| `UsuarioDesafio`   | `usuario_desafio`  | tabela associativa N:N entre usuário e desafio               |

### Relacionamentos (modelo de dados)

```
usuario 1 ──── N livro        (livro.criado_por_id → usuario.id)
usuario 1 ──── N desafio      (desafio.criado_por_id → usuario.id)
usuario 1 ──── N avaliacao    (avaliacao.usuario_id → usuario.id)
livro   1 ──── N avaliacao    (avaliacao.livro_id → usuario.id)

usuario N ──── N desafio      (tabela usuario_desafio: usuario_id, desafio_id,
                                concluido, desistiu, data_participacao)
desafio N ──── N livro        (tabela desafio_livro: desafio_id, livro_id)
```

Esses relacionamentos aparecem o tempo todo nas regras de permissão e nos fluxos de
CRUD — por exemplo, é por causa do `criado_por_id` em `livro` e `desafio` que o sistema
sabe *quem pode editar o quê* (seção 6).

---

## 4. DAO — acesso a dados

Todo DAO segue o mesmo padrão. Exemplo (`LivroDAO.buscarPorId`):

```java
public Livro buscarPorId(int id) throws SQLException {
    String sql = "SELECT * FROM livro WHERE id = ?";
    try (Connection conn = ConexaoDB.getConexao();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setInt(1, id);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return mapear(rs);
        }
    }
    return null;
}
```

Pontos importantes:

- **PreparedStatement sempre** — evita SQL Injection (nenhuma query concatena `String` do
  usuário diretamente no SQL).
- **`try-with-resources`** — a conexão/`PreparedStatement`/`ResultSet` são fechados automaticamente.
- **`ConexaoDB.getConexao()`** guarda uma única conexão estática (`private static Connection
  conexao`) e a reabre sempre que ela estiver fechada. Como cada método DAO fecha a conexão no
  final do `try-with-resources`, na prática cada chamada gera uma nova conexão física com o MySQL.
  Funciona bem para um projeto de laboratório (uso não concorrente), mas **não é um pool de
  conexões de verdade** — vale saber disso se o projeto crescer.
- **Exceções sobem para o Servlet.** O DAO nunca decide o que mostrar na tela — ele só lança
  `SQLException` e quem decide a mensagem de erro é o Servlet.
- **Métodos "internos" que recebem `Connection`** — alguns métodos privados (ex.:
  `DesafioDAO.listarLivrosDoDesafio(Connection conn, int desafioId)`) recebem a conexão já aberta
  por fora. Isso é usado para permitir **transações** — por exemplo, ao apagar um livro
  (`LivroDAO.remover`), o próprio DAO abre uma transação (`conn.setAutoCommit(false)`), desvincula
  o livro de todos os desafios, verifica se algum desafio ficou "vazio" (sem nenhum livro) e, se
  sim, remove esse desafio também — tudo dentro do mesmo `commit`/`rollback`, para não deixar o
  banco num estado inconsistente.

---

## 5. Servlets — o padrão de controller

Praticamente todo Servlet de CRUD (`LivroServlet`, `DesafioServlet`, `AvaliacaoServlet`,
`UsuarioServlet`) segue exatamente a mesma receita:

```java
// GET → lista tudo (ou busca por "q") e encaminha para a JSP de listagem
protected void doGet(...) {
    List<X> itens = termo != null ? xDAO.buscar(termo) : xDAO.listarTodos();
    request.setAttribute("itens", itens);
    request.getRequestDispatcher("/x.jsp").forward(request, response);
}

// POST → olha o parâmetro "acao" e decide o que fazer
protected void doPost(...) {
    Usuario logado = (Usuario) session.getAttribute("usuarioLogado");
    if (logado == null) { redireciona para login; return; }

    switch (acao) {
        case "cadastrar" -> cadastrar(request, logado);
        case "alterar"   -> alterar(request, logado);
        case "remover"   -> remover(request, logado);
    }
    doGet(request, response); // recarrega a lista (sucesso ou erro) e forward de novo
}
```

Ou seja: **todo POST termina chamando `doGet` de novo**, que reconstrói a lista e faz o
`forward` para a JSP. É assim que a página consegue mostrar a mensagem de sucesso/erro
*e* a lista atualizada com uma única requisição — sem AJAX.

---

## 6. Login — passo a passo completo

### 6.1 Onde a senha é guardada

`SenhaUtil` (em `util/`) transforma a senha em Base64 antes de salvar:

```java
public static String codificar(String senhaTexto) {
    return Base64.getEncoder().encodeToString(senhaTexto.getBytes(StandardCharsets.UTF_8));
}
```

> ⚠️ Base64 **não é criptografia** — é só uma codificação reversível. O próprio código
> deixa isso documentado no Javadoc da classe. Serve para não gravar a senha 100% em texto
> puro, mas não seria seguro em produção (o ideal seria hash com salt, como BCrypt).

### 6.2 O fluxo de autenticação

```
[login.jsp]                    [LoginServlet]                  [UsuarioDAO]
    │  POST /login                  │                                │
    │  email + senha  ─────────────▶│                                │
    │                                │ senha = SenhaUtil.codificar()  │
    │                                │ ──────────────────────────────▶│
    │                                │      autenticar(email, senha)  │
    │                                │        SELECT * FROM usuario   │
    │                                │        WHERE email=? AND senha=?
    │                                │◀────────────────────────────── │
    │                                │   Usuario (ou null)             │
    │                                │                                │
    │   se usuario != null:         │                                │
    │     session.setAttribute("usuarioLogado", usuario)              │
    │     forward → /home  (HomeServlet monta a Home e dá forward)    │
    │                                │                                │
    │   se usuario == null:         │                                │
    │     request.setAttribute("erro", "E-mail ou senha inválidos.")  │
    │     forward → /login.jsp                                        │
```

Código real (`LoginServlet.doPost`):

```java
Usuario usuario = usuarioDAO.autenticar(email, SenhaUtil.codificar(senha));

if (usuario != null) {
    HttpSession session = request.getSession();
    session.setAttribute("usuarioLogado", usuario);
    request.getRequestDispatcher("/home").forward(request, response);
} else {
    request.setAttribute("erro", "E-mail ou senha inválidos.");
    request.getRequestDispatcher("/login.jsp").forward(request, response);
}
```

**O ponto-chave:** depois do login, o objeto `Usuario` inteiro (com `id`, `nome`, `cargo`
etc.) fica guardado na **sessão HTTP**, sob a chave `"usuarioLogado"`. É esse objeto que
todo o resto do sistema — Servlets *e* JSPs — vai reaproveitar para saber quem está logado
e o que essa pessoa pode fazer, sem precisar consultar o banco de novo a cada clique.

### 6.3 O filtro de autenticação (`AuthFilter`)

Antes mesmo de qualquer Servlet/JSP ser executado, o `AuthFilter` (mapeado para `/*` no
`@WebFilter("/*")`) intercepta a requisição:

```java
boolean logado = session != null && session.getAttribute("usuarioLogado") != null;

if (ehPublico || logado) {
    chain.doFilter(req, res);          // deixa passar
} else {
    request.setAttribute("erro", "Faça login para continuar.");
    request.getRequestDispatcher("/login.jsp").forward(request, response);
}
```

As únicas rotas "públicas" (não exigem sessão) são: `/login.jsp`, `/login`, `/cadastro.jsp`,
`/cadastro`, `/css/`, `/js/`, `/img/` e `/uploads/`. Todo o resto — `home`, `livro`,
`desafio`, `avaliacao`, `usuario`, `carteirinha`, `ficha-leitura`, `sobre.jsp` — exige
sessão ativa. É esse filtro que garante que ninguém "cole" a URL `/home` no navegador sem
antes ter feito login.

### 6.4 Cadastro (autocadastro) e Logout

- **Cadastro** (`CadastroServlet`): qualquer visitante pode se cadastrar, mas **sempre** com
  `cargo = "usuario"` — isso é fixado no código (`new Usuario(..., "usuario")`), não vem do
  formulário. Ou seja, **não existe like um checkbox "sou admin"** no cadastro público; a
  promoção a administrador só pode ser feita depois, por outro administrador já existente
  (via tela de Usuários — seção 7).
- **Logout** (`LogoutServlet`): simplesmente `session.invalidate()` e redireciona para
  `login.jsp`. Isso apaga o `"usuarioLogado"` da sessão, então o `AuthFilter` volta a barrar
  o acesso.

---

## 7. Permissões — como o sistema sabe quem pode fazer o quê

Essa é a parte central: **como sabemos se um usuário pode excluir seu próprio livro, e como
sabemos se alguém é administrador e pode fazer tudo?**

### 7.1 A regra mora no `Usuario`, não espalhada pelo código

O modelo `Usuario` tem dois métodos que concentram *toda* a lógica de permissão do sistema:

```java
// Usuario.java
public boolean isAdministrador()    { return "administrador".equals(cargo); }
public boolean isUsuarioComum()     { return "usuario".equals(cargo); }

/**
 * Regra geral do sistema: administrador tem CRUD completo sobre tudo;
 * um usuário comum só pode editar/apagar o que ele mesmo criou
 * (comparando o id do usuário logado com o criado_por_id do registro).
 */
public boolean podeGerenciar(Integer criadoPorId) {
    if (isAdministrador()) return true;
    return criadoPorId != null && criadoPorId == this.id;
}
```

Ou seja:
- **Ser administrador** é simplesmente ter a coluna `cargo = "administrador"` na tabela
  `usuario`. Não existe uma tabela separada de "admins" — é um campo dentro do próprio
  usuário.
- **`podeGerenciar(criadoPorId)`** é a função universal de permissão: passa-se o `criadoPorId`
  do registro que se quer editar (o livro, o desafio, a avaliação...) e ela responde
  `true`/`false`. Se o usuário é admin, sempre `true` (não importa quem criou). Se não é
  admin, só é `true` quando o `id` do usuário logado bate com o `criadoPorId` do registro.

Esse único método é reaproveitado em **todos** os lugares do sistema que precisam decidir
"posso editar isso?" — livros, desafios e (com uma pequena variação) usuários.

### 7.2 Onde fica o `criado_por_id`

Para o `podeGerenciar` funcionar, cada tabela que pode ser "dona" de alguém precisa guardar
quem a criou:

- `livro.criado_por_id` → preenchido no `LivroServlet.cadastrar()`:
  `livro.setCriadoPorId(logado.getId());`
- `desafio.criado_por_id` → preenchido no `DesafioServlet.cadastrar()`:
  `desafio.setCriadoPorId(logado.getId());`
- `avaliacao.usuario_id` → já é o próprio "dono" da avaliação (quem avaliou), então o
  `AvaliacaoServlet` compara diretamente (`avaliacao.getUsuarioId() != logado.getId() &&
  !logado.isAdministrador()`) em vez de usar `podeGerenciar` — mesma ideia, mas escrita "na
  mão" porque o campo já tem esse papel.

### 7.3 Exemplo completo: excluir um livro

Esse é o fluxo pedido como exemplo — "como sabemos se um usuário pode ou não excluir seu
livro":

**1. Na JSP (`livros.jsp`), a decisão é feita só para efeito visual:**

```jsp
<% for (Livro l : livros) {
     boolean podeGerenciar = logado.podeGerenciar(l.getCriadoPorId());
%>
    ...
    <% if (podeGerenciar) { %>
        <form action="<%= ctx %>/livro" method="post" enctype="multipart/form-data">
            ...
            <button type="submit" name="acao" value="remover" ...>Apagar livro</button>
        </form>
    <% } else { %>
        <p class="subtitulo">Apenas quem cadastrou este livro (ou um administrador) pode editá-lo.</p>
    <% } %>
```

Aqui, `logado` é o `Usuario` que veio da sessão (`session.getAttribute("usuarioLogado")`),
lido no topo da JSP:

```jsp
Usuario logado = (Usuario) session.getAttribute("usuarioLogado");
```

Ou seja: **sim, a JSP faz uma checagem em tempo de execução (runtime)**, usando scriptlets
(`<% ... %>`). Ela chama o mesmo `logado.podeGerenciar(...)` para decidir se mostra o
formulário de edição/exclusão ou uma mensagem informativa. Isso é só para a **experiência do
usuário** — evita mostrar um botão de "Apagar" que não vai funcionar.

**2. No `LivroServlet`, a checagem é refeita — e é ela quem realmente vale:**

```java
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
    ...
}
```

Repare que a permissão é conferida **de novo**, agora no servidor, comparando o
`criadoPorId` que está *realmente salvo no banco* (não o que veio do formulário). Isso é
importante: mesmo que alguém manipule o HTML no navegador e envie um POST direto para
`/livro` com `acao=remover&id=42` para um livro que não é dele, o Servlet vai buscar o
livro 42 no banco, ver que `criadoPorId` não bate com `logado.getId()` e — se o usuário não
for administrador — vai recusar com a mensagem de erro, sem apagar nada.

**Resumindo o fluxo de "excluir livro":**

```
usuário clica "Apagar livro"
        │
        ▼
POST /livro  (acao=remover, id=42)
        │
        ▼
LivroServlet.doPost()
   → pega "logado" da sessão
   → chama remover(request, logado)
        │
        ▼
remover():
   → livroDAO.buscarPorId(42)      // pega o criadoPorId real do banco
   → logado.podeGerenciar(criadoPorId)
        ├─ true  (é admin OU criou o livro) → livroDAO.remover(42) [+ desvincula de desafios]
        └─ false → seta "erro" e NÃO remove nada
        │
        ▼
doGet() recarrega a lista de livros e faz forward para livros.jsp
```

A **UI (JSP) e o Servidor (Servlet) checam a mesma regra de forma independente** — a JSP
para não *oferecer* uma ação inválida, o Servlet para efetivamente *impedir* que ela
aconteça. Essa dupla checagem (esconder no front + validar no back) é o padrão usado em
todo o sistema.

### 7.4 Exemplo: tela de Usuários (uma regra ligeiramente diferente)

Em `UsuarioServlet.alterar`, a regra de "posse" é um pouco diferente da de livro/desafio,
porque um usuário sempre "é dono de si mesmo":

```java
boolean editandoProprioPerfil = logado.getId() == id;

// usuário comum só edita o próprio perfil; administrador edita qualquer um
if (!logado.podeGerenciar(editandoProprioPerfil ? logado.getId() : null)) {
    request.setAttribute("erro", "Você só pode editar o seu próprio perfil.");
    return;
}
```

Aqui `podeGerenciar` recebe `logado.getId()` (se a pessoa está editando a si mesma) ou
`null` (se está tentando editar outra pessoa). Como `podeGerenciar` só retorna `true` para
um `criadoPorId` nulo quando `isAdministrador()` também é `true`, o resultado é: **usuário
comum só mexe no próprio perfil; administrador mexe em qualquer perfil.** É o mesmo método,
reaproveitado com um truque no parâmetro.

Além disso, só administradores podem mudar o **cargo** de alguém (promover/rebaixar):

```java
if (logado.isAdministrador()) {
    String cargo = request.getParameter("cargo");
    if (cargo != null && !cargo.isBlank()) usuario.setCargo(cargo);
}
```

Ou seja, mesmo que um usuário comum manipule o formulário e envie `cargo=administrador` no
POST, o `if` acima nem chega a olhar esse parâmetro — só é lido quando quem está fazendo a
requisição (`logado`) já é administrador.

### 7.5 Onde mais aparece o `isAdministrador()`

- **`AvaliacaoServlet.alterar/remover`** — usuário só edita/apaga a própria avaliação,
  exceto se `logado.isAdministrador()`.
- **`usuarios.jsp`** — só mostra o campo `<select name="cargo">` (para promover/rebaixar)
  se `logado.isAdministrador()`; e só mostra o botão "Remover" se
  `logado.isAdministrador() && logado.getId() != u.getId()` (admin não pode se auto-remover
  por ali — para isso existe o fluxo separado de "Apagar conta" na Home).
- **`sobre.jsp`** — não usa `podeGerenciar` (é uma página informativa, sem edição), mas usa a
  ideia de "quem é admin" de outra forma: ela busca *todos* os usuários com
  `cargo = 'administrador'` diretamente do banco, via um novo método
  `UsuarioDAO.listarAdministradores()`:

  ```java
  public List<Usuario> listarAdministradores() throws SQLException {
      String sql = "SELECT * FROM usuario WHERE cargo = 'administrador' ORDER BY nome";
      ...
  }
  ```

  Essa página é uma exceção ao padrão Servlet→JSP: como `sobre.jsp` é acessada diretamente
  pela URL (`/sobre.jsp`, sem Servlet dedicado no `web.xml`), ela mesma chama o DAO num
  scriptlet no topo do arquivo:

  ```jsp
  <%
      List<Usuario> administradores = new UsuarioDAO().listarAdministradores();
  %>
  ```

  É um dos poucos lugares do projeto onde a JSP fala diretamente com o DAO, sem passar por
  um Servlet — aceitável aqui porque a página não faz nenhuma escrita no banco, só exibe uma
  lista pública de administradores.

### 7.6 O menu de navegação **não esconde nada por cargo**

Vale notar: `components/menu.jsp` mostra os mesmos links (Livros, Desafios, Avaliações,
Usuários, Sobre) para **todo mundo**, seja usuário comum ou administrador. O sistema não
esconde itens de menu por permissão — a diferenciação acontece **dentro** de cada tela
(quais botões de edição aparecem) e **dentro** de cada Servlet (o que de fato é permitido
executar). Isso é uma escolha de design válida para um projeto desse porte, mas é bom saber
disso: a "porta" de cada tela está sempre aberta para navegação, só a "ação" é que é
bloqueada.

---

## 8. Sessão HTTP — o "cofre" do usuário logado

Resumo de tudo que passa pela sessão:

- **Criada em** `LoginServlet` (`request.getSession()`), guarda o objeto `Usuario` inteiro
  sob a chave `"usuarioLogado"`.
- **Lida em** praticamente toda JSP (`Usuario logado = (Usuario)
  session.getAttribute("usuarioLogado");`) e todo Servlet (`(Usuario)
  session.getAttribute("usuarioLogado")`), sempre com `request.getSession(false)` nos
  Servlets — o `false` evita criar uma sessão nova à toa quando ela ainda não existe.
- **Atualizada** quando o próprio usuário edita o perfil (`UsuarioServlet.alterar`, se
  `editandoProprioPerfil`), para refletir imediatamente nome/foto/cargo novos sem exigir
  logout:
  ```java
  if (editandoProprioPerfil) session.setAttribute("usuarioLogado", usuario);
  ```
- **Destruída** em `LogoutServlet` (`session.invalidate()`) e também quando o próprio
  usuário apaga a conta (`UsuarioServlet.remover`, se `apagouAPropriaConta`).

Como o objeto `Usuario` fica todo em memória na sessão, **nenhuma tela precisa consultar o
banco de novo só para saber "quem sou eu" ou "sou admin?"** — é tudo lido direto do objeto
já carregado.

---

## 9. Fluxos completos (visão de ponta a ponta)

### 9.1 Login → Home

```
GET  /login.jsp           → mostra formulário
POST /login                → LoginServlet autentica, guarda na sessão, forward → /home
GET  /home (via forward)   → HomeServlet monta 3 listas (em andamento, concluídos,
                              avaliações) e faz forward → home.jsp
```

### 9.2 Cadastrar um livro

```
POST /livro (acao=cadastrar)
   → LivroServlet.doPost → cadastrar(request, logado)
       → lê nome/autor/gênero/ano/sinopse
       → salva a capa (UploadUtil.salvar) em /uploads/capas
       → livro.setCriadoPorId(logado.getId())   ← aqui nasce o "dono" do livro
       → livroDAO.inserir(livro)
   → doGet() recarrega /livro?  → forward → livros.jsp
```

### 9.3 Apagar um livro que é o único de um desafio

```
POST /livro (acao=remover, id=X)
   → LivroServlet.remover() confere permissão (seção 7.3)
   → livroDAO.remover(X):
       1. busca em quais desafios o livro X aparece (tabela desafio_livro)
       2. remove o vínculo desafio_livro do livro X
       3. para cada desafio afetado, conta quantos livros restaram
          → 0 livros restantes  ⇒ apaga o desafio inteiro (e as participações
                                    em usuario_desafio) — tudo dentro de uma
                                    transação (commit/rollback)
          → ainda há outros livros ⇒ desafio continua existindo normalmente
       4. só então apaga a linha do livro em si
```

Esse fluxo é um bom exemplo de como os **relacionamentos N:N** (`desafio_livro`) influenciam
diretamente uma regra de negócio que vai além de um simples `DELETE`.

### 9.4 Home → concluir/desistir de um desafio

```
POST /home (acao=concluir|desistir, desafioId=X)
   → HomeServlet.doPost valida que acao e desafioId vieram preenchidos
   → usuarioDesafioDAO.concluir(logado.getId(), X)   (UPDATE usuario_desafio SET concluido=1)
     ou
   → usuarioDesafioDAO.desistir(logado.getId(), X)   (UPDATE usuario_desafio SET desistiu=1)
   → montarEEncaminhar() recarrega as 3 listas da Home e forward → home.jsp
```

---

## 10. Impressão de documentos (carteirinha e ficha de leitura)

Dois Servlets simples, sem CRUD, só leitura:

- **`CarteirinhaServlet`** (`GET /carteirinha`) — pega o `Usuario` da sessão (precisa estar
  logado) e faz forward para `carteirinha.jsp`, que renderiza um layout específico para
  impressão (`window.print()` no botão).
- **`FichaLeituraServlet`** (`GET /ficha-leitura?id=`) — busca o `Livro` pelo `id` da URL e
  faz forward para `ficha-leitura.jsp`. Não tem checagem de "dono" porque qualquer usuário
  logado pode imprimir a ficha de qualquer livro do acervo (não é uma ação destrutiva).

---

## 11. Front-end: modais e confirmação (sem AJAX)

Todo o "modal" de detalhe/edição de cada item (livro, desafio, avaliação, usuário) é, na
verdade, **uma `<div>` escondida por CSS** (`.overlay`), já renderizada no HTML pela própria
JSP dentro do `for` da listagem — uma para cada item da tabela. O `js/app.js` só faz:

```js
function abrirModal(id) { document.getElementById(id).classList.add('aberto'); }
function fecharModal(id) { document.getElementById(id).classList.remove('aberto'); }
```

Ou seja, **não existe requisição assíncrona para "carregar os detalhes"** — os dados de
todos os itens (e de todos os modais) já vieram no primeiro `forward` do Servlet e estão
todos no HTML da página, só escondidos até o clique. O único uso de JavaScript "de verdade"
é o `confirm()` antes de ações destrutivas (`confirmarRemocao`), que é só uma camada de UX —
a permissão de fato continua sendo validada no servidor, como explicado na seção 7.

---

## 12. Resumo — perguntas rápidas

**"Como sabemos se um usuário pode excluir seu livro?"**
`logado.podeGerenciar(livro.getCriadoPorId())` — compara o `id` de quem está logado com o
`criado_por_id` gravado no livro. Chamado tanto na JSP (para decidir o que mostrar) quanto no
Servlet (para decidir o que de fato executar).

**"Como sabemos se alguém é administrador e pode fazer tudo?"**
Pelo campo `usuario.cargo`. `Usuario.isAdministrador()` checa `"administrador".equals(cargo)`.
E `podeGerenciar(...)` sempre retorna `true` de cara se `isAdministrador()` for `true`, não
importa quem criou o registro.

**"Isso é feito em runtime nos JSP?"**
Sim, nas JSPs isso acontece em *scriptlets* (`<% ... %>`), rodando a cada requisição, lendo o
`Usuario` da sessão HTTP. Mas essa checagem na JSP é só para a interface (mostrar ou esconder
botões) — quem realmente **garante** a permissão é o Servlet correspondente, que refaz a
mesma checagem usando os dados vindos do banco (não do formulário), antes de qualquer
`INSERT`/`UPDATE`/`DELETE`.
