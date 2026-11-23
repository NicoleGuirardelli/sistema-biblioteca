package com.biblioteca.servidor.repository;

import com.biblioteca.servidor.model.Emprestimo;
import com.biblioteca.servidor.model.StatusEmprestimo;
import jakarta.persistence.LockModeType;
import java.util.List;
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

    @Query("""
            select e from Emprestimo e
            join fetch e.usuario u
            join fetch e.livro l
            where u.id = :usuarioId
            order by e.dataEmprestimo desc, e.id desc
            """)
    List<Emprestimo> listarDoUsuario(@Param("usuarioId") Integer usuarioId);

    @Query("""
            select e from Emprestimo e
            join fetch e.usuario u
            join fetch e.livro l
            where (:status is null or e.status = :status)
              and (:termo is null
                   or lower(l.titulo) like lower(concat('%', :termo, '%'))
                   or lower(u.matricula) like lower(concat('%', :termo, '%'))
                   or lower(u.nome) like lower(concat('%', :termo, '%')))
            order by e.dataEmprestimo desc, e.id desc
            """)
    List<Emprestimo> listar(@Param("status") StatusEmprestimo status, @Param("termo") String termo);
}
