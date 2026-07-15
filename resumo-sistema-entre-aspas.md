# Entre Aspas — Como o sistema funciona

## Fluxo geral de uma requisição

```
JSP (tela) → requisição → Tomcat → consulta web.xml → web.xml mapeia URL → Servlet
Servlet → usa DAO (acesso ao banco) + Model (representa a tabela) → decide o resultado
Servlet → forward ou redirect → JSP de destino (sucesso ou erro)
```

Todo pedido do navegador chega no Tomcat, que olha o `web.xml` pra saber qual classe
Java (`Servlet`) é dona daquela URL. O Servlet processa a lógica usando um `DAO`
(que conversa com o banco via SQL) e um `Model` (a classe que representa a linha da
tabela), e no final decide pra qual JSP mandar a resposta.

---

## Login e Cadastro

**Login:**
1. Usuário informa e-mail e senha.
2. `LoginServlet` codifica a senha em Base64 e busca no banco (`UsuarioDAO.autenticar`).
3. Encontrou → grava o usuário na sessão → `forward` pra `/home`.
4. Não encontrou (ou deu erro) → `sendRedirect` pra `/login/erro`, que mostra
   `login-erro.jsp` com botões pra voltar ao login ou criar uma conta.

**Cadastro:**
1. Verifica se e-mail e matrícula já existem (colunas `UNIQUE` no banco).
2. Foto de perfil é **opcional** — se enviada, passa pelo `UploadUtil` (valida
   extensão, gera nome único com UUID, salva em disco). Se não enviar, o sistema usa
   uma API de terceiro (DiceBear) que gera um avatar a partir das iniciais do nome.
3. Toda conta nova entra automaticamente com `cargo = 'usuario'` — só um
   administrador pode promover alguém depois.

```sql
CREATE TABLE usuario (
   id, nome, matricula (UNIQUE), email (UNIQUE), senha, genero_fav,
   foto_url, cargo ENUM('usuario','administrador') DEFAULT 'usuario',
   criado_em, atualizado_em
);
```

---

## Home

| Ação | O que acontece |
|---|---|
| Imprimir carteirinha | Busca os dados do usuário no banco e usa `window.print()` do navegador |
| Baixar foto | Baixa o arquivo direto da pasta de uploads |
| Atualizar perfil | `UPDATE` no registro do usuário |
| Apagar conta | Livros/desafios criados por ele ficam **sem dono** (`criado_por_id = NULL`); avaliações dele são apagadas em cascata |
| Sair | Encerra a sessão, volta pro login |

- **Desafios em andamento** = desafios que o usuário participa e ainda não
  concluiu nem desistiu.
- **Desafios concluídos** = onde `concluido = 1`.
- **Minhas avaliações** = `SELECT * FROM avaliacao WHERE usuario_id = <logado>`.

---

## Livros

**Regras de permissão:**
- Qualquer usuário logado pode **cadastrar** um livro.
- Usuário comum só **altera/apaga** o que ele mesmo criou.
- Administrador altera/apaga **qualquer** livro.
- Se o criador da conta for apagado, o livro fica com `criado_por_id = NULL`
  (a partir daí, só administrador consegue editá-lo).

**Regra especial ao apagar um livro (a mais elaborada do sistema):**
1. O livro é desvinculado de todos os desafios em que aparecia.
2. Para cada desafio que perdeu esse livro: se **não sobrou nenhum outro livro**
   vinculado, o desafio inteiro é apagado junto (não faz sentido um desafio "vazio").
   Se ainda restam outros livros, o desafio continua existindo normalmente.
3. Só depois disso o livro em si é removido.

Essa operação toda roda dentro de **uma transação** (`conn.setAutoCommit(false)` +
`commit()`/`rollback()`), pra garantir que ou tudo acontece, ou nada acontece —
evita deixar o banco num estado inconsistente se algo falhar no meio.

```sql
CREATE TABLE livro (
   id, nome, autor, genero, ano_publicacao, sinopse, capa_url,
   criado_por_id INT,      -- preenchido depois que USUARIO existe (FK adicionada via ALTER TABLE)
   criado_em, atualizado_em
);
```

- **Imprimir ficha** → gera a ficha de leitura pronta pra virar PDF (capa, datas de
  início/fim de leitura, informações do banco).
- **Baixar capa** → baixa o arquivo direto da pasta de uploads.

---

## Desafios

**Regras de permissão:** mesmo padrão dos livros — qualquer usuário cria; usuário
comum só altera/apaga o que criou; administrador tem acesso total; se o criador for
apagado, o desafio fica com `criado_por_id = NULL`.

**Cascatas do banco:**
- Desafio apagado → linhas correspondentes em `usuario_desafio` (quem participava)
  são apagadas junto (`ON DELETE CASCADE`).
- A relação com livros (`desafio_livro`) também é `N:N`, mesma lógica do item
  anterior sobre apagar livro.

```sql
CREATE TABLE desafio_livro (
   desafio_id, livro_id,
   PRIMARY KEY (desafio_id, livro_id)
);
CREATE TABLE usuario_desafio (
   usuario_id, desafio_id, concluido, desistiu, data_participacao,
   PRIMARY KEY (usuario_id, desafio_id)
);
```

---

## Avaliações

- Qualquer usuário pode criar uma avaliação.
- Usuário comum só altera/apaga a que ele mesmo criou; administrador altera/apaga
  qualquer uma.
- **Um mesmo usuário só pode avaliar o mesmo livro uma única vez**
  (`UNIQUE KEY uq_usuario_livro (usuario_id, livro_id)` no banco).
- Se o livro é apagado → a avaliação é apagada em cascata.
- Se o usuário apaga a conta → as avaliações dele são apagadas em cascata
  (diferente de livro/desafio, que só ficam "órfãos" — aqui a avaliação não faz
  sentido sem o autor, então é removida de vez).

---

## Usuários

- Um usuário só cria a própria conta (sempre nasce com `cargo = 'usuario'`).
- Um usuário só altera/apaga a própria conta.
- **Administrador pode:**
  - Criar contas para outras pessoas.
  - Alterar o cargo de qualquer usuário (`usuario` ↔ `administrador`).
  - Alterar ou apagar qualquer conta, independente de quem seja.

---

## Onde e como os arquivos (foto/capa) são salvos

- Upload passa pelo `UploadUtil`: valida a extensão, gera um nome único (UUID) pra
  evitar colisão de nomes, e grava o arquivo físico dentro da pasta `uploads/` da
  aplicação **implantada no Tomcat** (não na pasta `src/main/webapp` do projeto-fonte
  — por isso essas pastas aparecem vazias no Eclipse, mesmo com uploads funcionando).
- O caminho relativo (ex.: `uploads/fotos/uuid.jpg`) é o que fica salvo no banco.
- Pra servir o arquivo de volta (visualizar ou baixar), **não existe nenhum Servlet
  específico** — o próprio Tomcat serve como arquivo estático (via seu servlet
  padrão), e o atributo HTML `download` no `<a>` é quem decide se o navegador abre
  a imagem ou baixa como arquivo.
- `AuthFilter` libera `/uploads/` como rota pública, senão as imagens não
  apareceriam nem nas telas de login/cadastro.

---

## Estrutura do projeto

```
src/main/java/
├── model/     → Usuario, Livro, Desafio, Avaliacao, UsuarioDesafio
├── dao/       → ConexaoDB, UsuarioDAO, LivroDAO, DesafioDAO, AvaliacaoDAO, UsuarioDesafioDAO
├── util/      → SenhaUtil (Base64), UploadUtil (upload de arquivos)
├── filters/   → AuthFilter (exige login em quase todas as rotas)
└── servlets/  → LoginServlet, CadastroServlet, LogoutServlet, HomeServlet,
                 UsuarioServlet, LivroServlet, DesafioServlet, AvaliacaoServlet,
                 CarteirinhaServlet, FichaLeituraServlet

src/main/webapp/
├── login.jsp, cadastro.jsp, home.jsp, livros.jsp, desafios.jsp,
│   avaliacoes.jsp, usuarios.jsp, sobre.jsp, carteirinha.jsp, ficha-leitura.jsp
├── components/ → header.jsp, menu.jsp, footer.jsp (via <jsp:include>)
├── css/, js/   → style.css, app.js (modais e confirmações no cliente)
└── WEB-INF/web.xml → mapeamento de URL → Servlet
```

## Banco de dados (visão geral das 6 tabelas)

| Tabela | Papel |
|---|---|
| `usuario` | membros do clube, com cargo `usuario`/`administrador` |
| `livro` | acervo, com `criado_por_id` apontando pra quem cadastrou |
| `desafio` | desafios de leitura, também com `criado_por_id` |
| `desafio_livro` | N:N — um desafio tem 1+ livros |
| `usuario_desafio` | N:N — um usuário participa de vários desafios ao mesmo tempo |
| `avaliacao` | 1 avaliação por par (usuário, livro) — `UNIQUE(usuario_id, livro_id)` |

As FKs de `criado_por_id` (em `livro` e `desafio`) são adicionadas via `ALTER TABLE`
depois que a tabela `usuario` já existe, com `ON DELETE SET NULL` — apagar o
usuário nunca apaga o conteúdo que ele criou, só remove o vínculo de autoria.
