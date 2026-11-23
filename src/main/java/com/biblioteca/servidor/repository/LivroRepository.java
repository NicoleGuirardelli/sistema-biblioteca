package com.biblioteca.servidor.repository;

import com.biblioteca.servidor.model.Livro;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LivroRepository extends JpaRepository<Livro, Integer> {
    boolean existsByIsbn(String isbn);

    @Query("""
            select l from Livro l
            where lower(l.titulo) like lower(concat('%', :termo, '%'))
               or lower(l.categoria) like lower(concat('%', :termo, '%'))
               or lower(l.isbn) like lower(concat('%', :termo, '%'))
            """)
    List<Livro> buscar(@Param("termo") String termo);

    @Modifying(flushAutomatically = true)
    @Query("""
            update Livro l
            set l.qtdDisponivel = l.qtdDisponivel - 1
            where l.id = :id and l.qtdDisponivel > 0
            """)
    int decrementarSeDisponivel(@Param("id") Integer id);

    @Modifying(flushAutomatically = true)
    @Query("""
            update Livro l
            set l.qtdDisponivel = l.qtdDisponivel + 1
            where l.id = :id and l.qtdDisponivel < l.qtdTotal
            """)
    int incrementarDisponivel(@Param("id") Integer id);
}
