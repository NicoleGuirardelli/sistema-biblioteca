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

    public synchronized String emprestarLivro(int id){

        for (Livro livro : livros){
            if(livro.getId() == id){

                if(!livro.isDisponivel()){
                    return "O livro já esta emprestado";
                }

                livro.setDisponivel(false);// marca como emprestado

                return "Seu empréstimo foi realizado com sucesso";
            }
        }

        return "O livro não foi encontrado";
    }
}
