package com.biblioteca.servidor.service;

import com.biblioteca.servidor.model.Livro;
import com.biblioteca.servidor.model.StatusEmprestimo;
import com.biblioteca.servidor.repository.EmprestimoRepository;
import com.biblioteca.servidor.repository.LivroRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BibliotecaService {
    private final LivroRepository livroRepository;
    private final EmprestimoRepository emprestimoRepository;

    public BibliotecaService(LivroRepository livroRepository, EmprestimoRepository emprestimoRepository) {
        this.livroRepository = livroRepository;
        this.emprestimoRepository = emprestimoRepository;
    }

    public List<Livro> listarLivros(String termo) {
        if (termo == null || termo.isBlank()) {
            return livroRepository.findAll();
        }
        return livroRepository.buscar(termo.trim());
    }

    public Livro buscarLivro(int id) {
        return livroRepository.findById(id).orElse(null);
    }

    @Transactional
    public Livro cadastrarLivro(Livro dados) {
        if (dados.getTitulo() == null || dados.getTitulo().isBlank()) {
            throw new ApiException(400, "Informe o título");
        }
        int quantidade = dados.getQtdTotal() > 0 ? dados.getQtdTotal() : 1;
        String isbn = textoOuNulo(dados.getIsbn());
        if (isbn != null && livroRepository.existsByIsbn(isbn)) {
            throw new ApiException(409, "ISBN já cadastrado");
        }

        Livro livro = new Livro();
        livro.setTitulo(dados.getTitulo().trim());
        livro.setIsbn(isbn);
        livro.setCategoria(textoOuNulo(dados.getCategoria()));
        livro.setQtdTotal(quantidade);
        livro.setQtdDisponivel(quantidade);
        return livroRepository.save(livro);
    }

    @Transactional
    public String atualizarLivro(int id, Livro dados) {
        Livro livro = livroRepository.findById(id).orElse(null);
        if (livro == null) {
            return "O livro não foi encontrado";
        }
        if (dados.getTitulo() != null && !dados.getTitulo().isBlank()) {
            livro.setTitulo(dados.getTitulo().trim());
        }
        if (dados.getCategoria() != null) {
            livro.setCategoria(textoOuNulo(dados.getCategoria()));
        }
        if (dados.getIsbn() != null) {
            String isbn = textoOuNulo(dados.getIsbn());
            if (isbn != null && livroRepository.existsByIsbn(isbn) && !isbn.equals(livro.getIsbn())) {
                throw new ApiException(409, "ISBN já cadastrado");
            }
            livro.setIsbn(isbn);
        }
        return "Livro atualizado com sucesso";
    }

    @Transactional
    public String deletarLivro(int id) {
        if (!livroRepository.existsById(id)) {
            return "O livro não foi encontrado";
        }
        if (emprestimoRepository.existsByLivroIdAndStatus(id, StatusEmprestimo.ATIVO)) {
            throw new ApiException(409, "livro com empréstimo ativo não pode ser excluído");
        }
        livroRepository.deleteById(id);
        return "Livro removido com sucesso";
    }

    private String textoOuNulo(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim();
    }
}
