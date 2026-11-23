package com.biblioteca.servidor;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.biblioteca.servidor.model.Usuario;
import com.biblioteca.servidor.repository.UsuarioRepository;
import com.biblioteca.servidor.service.ApiException;
import com.biblioteca.servidor.service.EmprestimoService;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = {"/limpar.sql", "/data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class PrototipoFluxosTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EmprestimoService emprestimoService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void tabelasSeguemOModeloDaDocumentacao() {
        assertColunas("USUARIO", Set.of("ID", "NOME", "MATRICULA", "PERFIL", "ATIVO"));
        assertColunas("LIVRO", Set.of("ID", "TITULO", "ISBN", "CATEGORIA", "QTD_TOTAL", "QTD_DISPONIVEL"));
        assertColunas("EMPRESTIMO", Set.of(
                "ID", "USUARIO_ID", "LIVRO_ID", "DATA_EMPRESTIMO", "DATA_PREVISTA",
                "DATA_DEVOLUCAO", "STATUS", "EM_ATRASO"));
    }

    @Test
    void consultaEmprestimoEDevolucao() throws Exception {
        mockMvc.perform(post("/auth/login").contentType("application/json").content("{\"matricula\":\"000\"}"))
                .andExpect(status().isUnauthorized());

        String tokenAna = token("2023001");
        mockMvc.perform(get("/livros")).andExpect(status().isUnauthorized());

        mockMvc.perform(get("/livros").header("Authorization", "Bearer " + tokenAna))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(10));

        mockMvc.perform(get("/livros").param("q", "zzzz").header("Authorization", "Bearer " + tokenAna))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(get("/livros").param("q", "Literatura").header("Authorization", "Bearer " + tokenAna))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoria").value("Literatura"));

        int livroUnico = idDoLivro("9788575225632", tokenAna);
        MvcResult emprestimo = mockMvc.perform(post("/emprestimos")
                        .header("Authorization", "Bearer " + tokenAna)
                        .contentType("application/json")
                        .content("{\"livroId\":" + livroUnico + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ATIVO"))
                .andReturn();
        int emprestimoId = jsonMapper.readTree(emprestimo.getResponse().getContentAsString()).get("id").asInt();

        mockMvc.perform(get("/livros/" + livroUnico).header("Authorization", "Bearer " + tokenAna))
                .andExpect(jsonPath("$.qtdDisponivel").value(0));

        mockMvc.perform(post("/emprestimos")
                        .header("Authorization", "Bearer " + tokenAna)
                        .contentType("application/json")
                        .content("{\"livroId\":" + livroUnico + "}"))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/emprestimos/" + emprestimoId + "/devolucao")
                        .header("Authorization", "Bearer " + tokenAna))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ENCERRADO"))
                .andExpect(jsonPath("$.emAtraso").value(false));

        mockMvc.perform(get("/livros/" + livroUnico).header("Authorization", "Bearer " + tokenAna))
                .andExpect(jsonPath("$.qtdDisponivel").value(1));

        mockMvc.perform(post("/emprestimos/" + emprestimoId + "/devolucao")
                        .header("Authorization", "Bearer " + tokenAna))
                .andExpect(status().isConflict());
    }

    @Test
    void regrasDeAlunoAtivoLimiteEExclusao() throws Exception {
        jdbcTemplate.update("update usuario set ativo = false where matricula = '2023003'");
        String tokenCarla = token("2023003");
        int livro = idDoLivro("9780132350884", tokenCarla);

        mockMvc.perform(post("/emprestimos")
                        .header("Authorization", "Bearer " + tokenCarla)
                        .contentType("application/json")
                        .content("{\"livroId\":" + livro + "}"))
                .andExpect(status().isConflict());

        String tokenBruno = token("2023002");
        int abundante = idDoLivro("9788550800653", tokenBruno);
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/emprestimos")
                            .header("Authorization", "Bearer " + tokenBruno)
                            .contentType("application/json")
                            .content("{\"livroId\":" + abundante + "}"))
                    .andExpect(status().isOk());
        }
        mockMvc.perform(post("/emprestimos")
                        .header("Authorization", "Bearer " + tokenBruno)
                        .contentType("application/json")
                        .content("{\"livroId\":" + abundante + "}"))
                .andExpect(status().isConflict());

        String tokenBibliotecario = token("B001");
        mockMvc.perform(delete("/livros/" + abundante).header("Authorization", "Bearer " + tokenBibliotecario))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/emprestimos")
                        .header("Authorization", "Bearer " + tokenBibliotecario)
                        .contentType("application/json")
                        .content("{\"livroId\":" + livro + ",\"matriculaAluno\":\"2023001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataPrevista").exists());
    }

    @Test
    void alunoVeSoOsPropriosEmprestimosEBibliotecarioVeTodos() throws Exception {
        String tokenAna = token("2023001");
        String tokenBruno = token("2023002");
        String tokenBibliotecario = token("B001");
        int livroAna = idDoLivro("9780132350884", tokenAna);
        int livroBruno = idDoLivro("9780201633610", tokenBruno);

        mockMvc.perform(post("/emprestimos")
                        .header("Authorization", "Bearer " + tokenAna)
                        .contentType("application/json")
                        .content("{\"livroId\":" + livroAna + "}"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/emprestimos")
                        .header("Authorization", "Bearer " + tokenBruno)
                        .contentType("application/json")
                        .content("{\"livroId\":" + livroBruno + "}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/emprestimos/meus").header("Authorization", "Bearer " + tokenAna))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].matricula").value("2023001"));

        mockMvc.perform(get("/emprestimos/meus").header("Authorization", "Bearer " + tokenBruno))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].matricula").value("2023002"));

        mockMvc.perform(get("/emprestimos").header("Authorization", "Bearer " + tokenAna))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/emprestimos/meus").header("Authorization", "Bearer " + tokenBibliotecario))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/emprestimos").header("Authorization", "Bearer " + tokenBibliotecario))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(get("/emprestimos")
                        .param("status", "ATIVO")
                        .param("q", "2023001")
                        .header("Authorization", "Bearer " + tokenBibliotecario))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].titulo").value("Clean Code"));

        mockMvc.perform(get("/emprestimos")
                        .param("status", "ENCERRADO")
                        .header("Authorization", "Bearer " + tokenBibliotecario))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(get("/emprestimos")
                        .param("status", "QUALQUER")
                        .header("Authorization", "Bearer " + tokenBibliotecario))
                .andExpect(status().isBadRequest());
    }

    @Test
    void alunoConsultaMasNaoAlteraOAcervo() throws Exception {
        String tokenAna = token("2023001");
        String tokenBibliotecario = token("B001");
        int livro = idDoLivro("9780132350884", tokenAna);

        mockMvc.perform(get("/livros").header("Authorization", "Bearer " + tokenAna))
                .andExpect(status().isOk());

        mockMvc.perform(post("/livros")
                        .header("Authorization", "Bearer " + tokenAna)
                        .contentType("application/json")
                        .content("{\"titulo\":\"Livro do aluno\"}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/livros/" + livro)
                        .header("Authorization", "Bearer " + tokenAna)
                        .contentType("application/json")
                        .content("{\"titulo\":\"Titulo alterado\"}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/livros/" + livro).header("Authorization", "Bearer " + tokenAna))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/livros")
                        .header("Authorization", "Bearer " + tokenBibliotecario)
                        .contentType("application/json")
                        .content("{\"titulo\":\"Livro novo\",\"isbn\":\"9780000000001\",\"qtdTotal\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Livro novo"));
    }

    @Test
    void isbnExistenteIncrementaExemplares() throws Exception {
        String tokenBibliotecario = token("B001");

        mockMvc.perform(post("/livros")
                        .header("Authorization", "Bearer " + tokenBibliotecario)
                        .contentType("application/json")
                        .content("{\"titulo\":\"Clean Code\",\"isbn\":\"9780132350884\",\"qtdTotal\":2}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value("ISBN já cadastrado. Confirme para incrementar os exemplares."));

        mockMvc.perform(post("/livros")
                        .header("Authorization", "Bearer " + tokenBibliotecario)
                        .contentType("application/json")
                        .content("{\"titulo\":\"Clean Code\",\"isbn\":\"9780132350884\",\"qtdTotal\":2,\"incrementar\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.qtdTotal").value(5))
                .andExpect(jsonPath("$.qtdDisponivel").value(5));
    }

    @Test
    void duasRequisicoesSimultaneasPeloUltimoExemplar() throws Exception {
        Usuario ana = usuarioRepository.findByMatricula("2023001").orElseThrow();
        Usuario bruno = usuarioRepository.findByMatricula("2023002").orElseThrow();
        Integer livroId = jdbcTemplate.queryForObject(
                "select id from livro where isbn = '9788575225632'", Integer.class);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch prontos = new CountDownLatch(2);
        CountDownLatch inicio = new CountDownLatch(1);
        List<Integer> status = new ArrayList<>();

        for (Usuario usuario : List.of(ana, bruno)) {
            executor.submit(() -> {
                prontos.countDown();
                try {
                    inicio.await();
                    emprestimoService.realizar(usuario, livroId, null);
                    synchronized (status) {
                        status.add(200);
                    }
                } catch (ApiException excecao) {
                    synchronized (status) {
                        status.add(excecao.getStatus());
                    }
                } catch (InterruptedException excecao) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        prontos.await();
        inicio.countDown();
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        long sucessos = status.stream().filter(codigo -> codigo == 200).count();
        long recusas = status.stream().filter(codigo -> codigo == 409).count();
        Integer disponivel = jdbcTemplate.queryForObject(
                "select qtd_disponivel from livro where id = ?", Integer.class, livroId);
        Integer emprestimos = jdbcTemplate.queryForObject(
                "select count(*) from emprestimo where livro_id = ? and status = 'ATIVO'",
                Integer.class, livroId);

        if (sucessos != 1 || recusas != 1 || disponivel != 0 || emprestimos != 1) {
            throw new AssertionError("status=" + status + " qtd=" + disponivel + " emprestimos=" + emprestimos);
        }
    }

    private String token(String matricula) throws Exception {
        MvcResult resultado = mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content("{\"matricula\":\"" + matricula + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        return jsonMapper.readTree(resultado.getResponse().getContentAsString()).get("token").asText();
    }

    private int idDoLivro(String isbn, String token) throws Exception {
        MvcResult resultado = mockMvc.perform(get("/livros").param("q", isbn)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode livros = jsonMapper.readTree(resultado.getResponse().getContentAsString());
        return livros.get(0).get("id").asInt();
    }

    private void assertColunas(String tabela, Set<String> esperadas) {
        Set<String> colunas = new HashSet<>(jdbcTemplate.queryForList(
                "select column_name from information_schema.columns where upper(table_name) = ?",
                String.class,
                tabela));
        if (!colunas.equals(esperadas)) {
            throw new AssertionError("Colunas de " + tabela + ": " + colunas);
        }
    }
}
