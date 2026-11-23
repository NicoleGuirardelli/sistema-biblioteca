package com.biblioteca.servidor;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import java.util.HashSet;
import java.util.Set;
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
