package com.biblioteca.servidor.service;

import com.biblioteca.servidor.model.Livro;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class BibliotecaService {
    private final List<Livro> livros = new ArrayList<>();
    private int proxId= 1;

    public synchronized Livro cadastrarLivro(String titulo, String autor){
        Livro livro = new Livro(proxId++, titulo, autor);
        livros.add(livro);

        return livro;
    }

    public synchronized List<Livro> listarLivros(){
        return new ArrayList<>(livros);
    }
}
