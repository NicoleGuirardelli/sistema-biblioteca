package com.biblioteca.servidor.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController // informa ao Spring que essa classe recebe req. HTTP e retorna
public class BibliotecaController {


    @GetMapping("/biblioteca") //define o endereço da rota
    public String mensagemInicial(){
        return "Bem-vindo a biblioteca";
    }
}
