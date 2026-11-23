package com.biblioteca.servidor.repository;

import com.biblioteca.servidor.model.Emprestimo;
import com.biblioteca.servidor.model.StatusEmprestimo;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmprestimoRepository extends JpaRepository<Emprestimo, Integer> {
    long countByUsuarioIdAndStatus(Integer usuarioId, StatusEmprestimo status);

    boolean existsByLivroIdAndStatus(Integer livroId, StatusEmprestimo status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Emprestimo e where e.id = :id")
    Optional<Emprestimo> findByIdForUpdate(@Param("id") Integer id);
}
