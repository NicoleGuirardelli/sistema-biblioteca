package com.biblioteca.servidor.controller;

import com.biblioteca.servidor.model.Livro;
import com.biblioteca.servidor.service.BibliotecaService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.http.ResponseEntity;

@RestController // informa ao Spring que essa classe recebe req. HTTP e retorna
public class BibliotecaController {
    //controller utiliza a service para executar operacoes
    private final BibliotecaService bibliotecaService;

    //o spring vai injetar a service automatico
    public BibliotecaController(BibliotecaService bibliotecaService) {
        this.bibliotecaService = bibliotecaService;

    }

    @GetMapping("/biblioteca") //define o endereço da rota
    public String mensagemInicial(){
        return "Bem-vindo a biblioteca";
    }
    @GetMapping("/livros")
    public List<Livro> listarLivros(){
        return bibliotecaService.listarLivros();
    }

    //rota para cadrastro de livro
    @PostMapping("/livros")
    public Livro cadastrarLivro(@RequestBody Livro livro){
        return bibliotecaService.cadastrarLivro(livro.getTitulo(),  livro.getAutor());
    }

    // busca um livro pelo id (e 404 se nao existir)
    @GetMapping("/livros/{id}")
    public ResponseEntity<Livro> buscarLivro(@PathVariable int id){
        Livro livro = bibliotecaService.buscarLivro(id);

        if(livro == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(livro);
    }

    // atualiza titulo e autor de um livro
    @PutMapping("/livros/{id}")
    public String atualizarLivro(@PathVariable int id, @RequestBody Livro livro){
        return bibliotecaService.atualizarLivro(id, livro.getTitulo(), livro.getAutor());
    }

    // remove um livro
    @DeleteMapping("/livros/{id}")
    public String deletarLivro(@PathVariable int id){
        return bibliotecaService.deletarLivro(id);
    }

    @PutMapping("livros/{id}/emprestar")
    public String emprestarLivro(@PathVariable int id){
        return bibliotecaService.emprestarLivro(id);
    }

    @PutMapping("livros/{id}/devolver")
    public String devolverLivro(@PathVariable int id){
        return bibliotecaService.devolverLivro(id);
    }
}
