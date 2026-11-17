package com.biblioteca.cliente;

import java.net.URI;
import java.net.http.*;
import java.util.Scanner;

public class ClienteBibliotecario  {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        int opcao=0;

        while(opcao!=3){
            System.out.println("biblioteca");
            System.out.println("1 cadastrar livro");
            System.out.println("2 consultar livros");
            System.out.println("3 sair");
            System.out.println("escolha uma opcao");

            if(!scanner.hasNextInt()){
                System.out.println("numero invalido \n Digite novamente");
                scanner.next();
                continue;

            }

            opcao=scanner.nextInt();
            scanner.nextLine();
            switch (opcao){
                case 1:
                    cadastrarLivro();
                    break;
                case 2:
                    consultarLivros();
                    break;
                case 3:
                    System.out.println("Saindo..");
                    break;
                default:
                    System.out.println("opcao invalida");
            }

        }
    }

    public static void consultarLivros() {
        try {
            HttpClient cliente = HttpClient.newHttpClient();

            HttpRequest requisicao = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/livros")).GET().build();

            HttpResponse<String> resposta = cliente.send(requisicao, HttpResponse.BodyHandlers.ofString());

            System.out.println("Livros cadastrados");
            System.out.println(resposta.body());

        } catch (Exception e) {
            System.out.println("Erro na consulta: " + e.getMessage());
        }
    }


    public static void cadastrarLivro() {

        Scanner scanner = new Scanner(System.in);

        System.out.println("Digite o nome do livro: ");
        String titulo = scanner.nextLine();

        System.out.println("Digite o autor do livro: ");
        String autor = scanner.nextLine();

        String json = String.format("{\"titulo\":\"%s\",\"autor\":\"%s\"}", titulo,autor);
        try {
            HttpClient cliente = HttpClient.newHttpClient();

            HttpRequest requisicao = HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/livros"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json)).build();

            HttpResponse<String> resposta = cliente.send(requisicao, HttpResponse.BodyHandlers.ofString());

            if(resposta.statusCode() == 200 || resposta.statusCode() == 201) {
                System.out.println("Livro cadastrado com sucesso");
                System.out.println(resposta.body());

            }else  {
                System.out.println("Erro na consulta: " + resposta.statusCode());
            }
        } catch (Exception e) {

            System.out.println("Erro no cadastro de livro : " + e.getMessage());
        }
    }
}