package com.biblioteca.servidor.controller;

import com.biblioteca.servidor.model.Livro;
import com.biblioteca.servidor.model.Perfil;
import com.biblioteca.servidor.model.Usuario;
import com.biblioteca.servidor.service.ApiException;
import com.biblioteca.servidor.service.BibliotecaService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.http.ResponseEntity;

@RestController
public class BibliotecaController {
    private final BibliotecaService bibliotecaService;

    public BibliotecaController(BibliotecaService bibliotecaService) {
        this.bibliotecaService = bibliotecaService;
    }

    @GetMapping("/biblioteca")
    public String mensagemInicial() {
        return "Bem-vindo a biblioteca";
    }

    @GetMapping("/livros")
    public List<Livro> listarLivros(@RequestParam(required = false) String q) {
        return bibliotecaService.listarLivros(q);
    }

    @PostMapping("/livros")
    public Livro cadastrarLivro(@RequestBody Livro livro, HttpServletRequest request) {
        exigirBibliotecario(request);
        return bibliotecaService.cadastrarLivro(livro);
    }

    @GetMapping("/livros/{id}")
    public ResponseEntity<Livro> buscarLivro(@PathVariable int id) {
        Livro livro = bibliotecaService.buscarLivro(id);
        if (livro == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(livro);
    }

    @PutMapping("/livros/{id}")
    public String atualizarLivro(@PathVariable int id, @RequestBody Livro livro, HttpServletRequest request) {
        exigirBibliotecario(request);
        return bibliotecaService.atualizarLivro(id, livro);
    }

    @DeleteMapping("/livros/{id}")
    public String deletarLivro(@PathVariable int id, HttpServletRequest request) {
        exigirBibliotecario(request);
        return bibliotecaService.deletarLivro(id);
    }

    private void exigirBibliotecario(HttpServletRequest request) {
        Usuario logado = (Usuario) request.getAttribute(EmprestimoController.USUARIO_LOGADO);
        if (logado == null || logado.getPerfil() != Perfil.BIBLIOTECARIO) {
            throw new ApiException(403, "sem permissão");
        }
    }
}
