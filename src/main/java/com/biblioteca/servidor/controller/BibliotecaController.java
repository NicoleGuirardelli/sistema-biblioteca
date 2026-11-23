package com.biblioteca.servidor.controller;

import com.biblioteca.servidor.model.Livro;
import com.biblioteca.servidor.service.BibliotecaService;
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
    public Livro cadastrarLivro(@RequestBody Livro livro) {
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
    public String atualizarLivro(@PathVariable int id, @RequestBody Livro livro) {
        return bibliotecaService.atualizarLivro(id, livro);
    }

    @DeleteMapping("/livros/{id}")
    public String deletarLivro(@PathVariable int id) {
        return bibliotecaService.deletarLivro(id);
    }
}
