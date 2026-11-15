package com.biblioteca.servidor.controller;

import com.biblioteca.servidor.model.Livro;
import com.biblioteca.servidor.service.BibliotecaService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

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

    @PutMapping("livros/{id}/emprestar")
    public String emprestarLivro(@PathVariable int id){
        return bibliotecaService.emprestarLivro(id);
    }
}
