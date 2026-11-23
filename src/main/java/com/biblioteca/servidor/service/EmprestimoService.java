package com.biblioteca.servidor.service;

import com.biblioteca.servidor.model.Emprestimo;
import com.biblioteca.servidor.model.Perfil;
import com.biblioteca.servidor.model.StatusEmprestimo;
import com.biblioteca.servidor.model.Usuario;
import com.biblioteca.servidor.repository.EmprestimoRepository;
import com.biblioteca.servidor.repository.LivroRepository;
import com.biblioteca.servidor.repository.UsuarioRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmprestimoService {
    private final UsuarioRepository usuarioRepository;
    private final LivroRepository livroRepository;
    private final EmprestimoRepository emprestimoRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public EmprestimoService(UsuarioRepository usuarioRepository,
                             LivroRepository livroRepository,
                             EmprestimoRepository emprestimoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.livroRepository = livroRepository;
        this.emprestimoRepository = emprestimoRepository;
    }

    @Transactional
    public Emprestimo realizar(Usuario logado, Integer livroId, String matriculaAluno) {
        if (livroId == null) {
            throw new ApiException(400, "Informe o livro");
        }

        Usuario aluno = localizarAluno(logado, matriculaAluno);
        if (!aluno.isAtivo() || aluno.getPerfil() != Perfil.ALUNO) {
            throw new ApiException(409, "só é possível emprestar a aluno com cadastro ativo");
        }

        long ativos = emprestimoRepository.countByUsuarioIdAndStatus(aluno.getId(), StatusEmprestimo.ATIVO);
        if (ativos >= 3) {
            throw new ApiException(409, "limite de 3 empréstimos ativos");
        }
        if (!livroRepository.existsById(livroId)) {
            throw new ApiException(404, "Livro não encontrado");
        }

        int atualizados = livroRepository.decrementarSeDisponivel(livroId);
        if (atualizados == 0) {
            throw new ApiException(409, "sem exemplar disponível");
        }

        Integer alunoId = aluno.getId();
        entityManager.clear();

        LocalDate hoje = LocalDate.now();
        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setUsuario(entityManager.getReference(Usuario.class, alunoId));
        emprestimo.setLivro(entityManager.getReference(com.biblioteca.servidor.model.Livro.class, livroId));
        emprestimo.setDataEmprestimo(hoje);
        emprestimo.setDataPrevista(hoje.plusDays(Emprestimo.PRAZO_DIAS));
        emprestimo.setStatus(StatusEmprestimo.ATIVO);
        emprestimo.setEmAtraso(false);
        Emprestimo salvo = emprestimoRepository.saveAndFlush(emprestimo);
        Integer emprestimoId = salvo.getId();
        entityManager.clear();
        return emprestimoRepository.findById(emprestimoId).orElseThrow();
    }

    @Transactional
    public Emprestimo devolver(Usuario logado, int emprestimoId) {
        Emprestimo emprestimo = emprestimoRepository.findByIdForUpdate(emprestimoId)
                .orElseThrow(() -> new ApiException(404, "Empréstimo não encontrado"));

        if (emprestimo.getStatus() != StatusEmprestimo.ATIVO) {
            throw new ApiException(409, "empréstimo inexistente ou já encerrado");
        }
        if (logado.getPerfil() == Perfil.ALUNO && !emprestimo.getUsuario().getId().equals(logado.getId())) {
            throw new ApiException(403, "sem permissão");
        }

        Integer livroId = emprestimo.getLivro().getId();
        entityManager.detach(emprestimo.getLivro());
        emprestimo.encerrar(LocalDate.now());

        int atualizados = livroRepository.incrementarDisponivel(livroId);
        if (atualizados == 0) {
            throw new ApiException(500, "falha ao atualizar disponibilidade");
        }
        return emprestimo;
    }

    @Transactional(readOnly = true)
    public List<Emprestimo> listarDoAluno(Usuario logado) {
        if (logado.getPerfil() != Perfil.ALUNO) {
            throw new ApiException(403, "sem permissão");
        }
        return emprestimoRepository.listarDoUsuario(logado.getId());
    }

    @Transactional(readOnly = true)
    public List<Emprestimo> listarTodos(Usuario logado, String statusTexto, String termo) {
        if (logado.getPerfil() != Perfil.BIBLIOTECARIO) {
            throw new ApiException(403, "sem permissão");
        }
        StatusEmprestimo status = null;
        if (statusTexto != null && !statusTexto.isBlank()) {
            try {
                status = StatusEmprestimo.valueOf(statusTexto.trim().toUpperCase());
            } catch (IllegalArgumentException excecao) {
                throw new ApiException(400, "dados inválidos");
            }
        }
        String filtro = termo == null || termo.isBlank() ? null : termo.trim();
        return emprestimoRepository.listar(status, filtro);
    }

    private Usuario localizarAluno(Usuario logado, String matriculaAluno) {
        if (logado.getPerfil() == Perfil.BIBLIOTECARIO) {
            if (matriculaAluno == null || matriculaAluno.isBlank()) {
                throw new ApiException(400, "Informe a matrícula do aluno");
            }
            return usuarioRepository.findByMatriculaForUpdate(matriculaAluno.trim())
                    .orElseThrow(() -> new ApiException(404, "Aluno não encontrado"));
        }
        return usuarioRepository.findByIdForUpdate(logado.getId())
                .orElseThrow(() -> new ApiException(401, "não autenticado"));
    }
}
