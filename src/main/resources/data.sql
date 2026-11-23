INSERT INTO usuario (nome, matricula, perfil, ativo)
SELECT 'Ana Lima', '2023001', 'ALUNO', TRUE
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE matricula = '2023001');

INSERT INTO usuario (nome, matricula, perfil, ativo)
SELECT 'Bruno Dias', '2023002', 'ALUNO', TRUE
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE matricula = '2023002');

INSERT INTO usuario (nome, matricula, perfil, ativo)
SELECT 'Carla Nunes', '2023003', 'ALUNO', TRUE
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE matricula = '2023003');

INSERT INTO usuario (nome, matricula, perfil, ativo)
SELECT 'Marina Costa', 'B001', 'BIBLIOTECARIO', TRUE
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE matricula = 'B001');

INSERT INTO livro (titulo, isbn, categoria, qtd_total, qtd_disponivel)
SELECT 'Clean Code', '9780132350884', 'Software', 3, 3
WHERE NOT EXISTS (SELECT 1 FROM livro WHERE isbn = '9780132350884');

INSERT INTO livro (titulo, isbn, categoria, qtd_total, qtd_disponivel)
SELECT 'Design Patterns', '9780201633610', 'Software', 2, 2
WHERE NOT EXISTS (SELECT 1 FROM livro WHERE isbn = '9780201633610');

INSERT INTO livro (titulo, isbn, categoria, qtd_total, qtd_disponivel)
SELECT 'Dom Casmurro', '9788535902778', 'Literatura', 4, 4
WHERE NOT EXISTS (SELECT 1 FROM livro WHERE isbn = '9788535902778');

INSERT INTO livro (titulo, isbn, categoria, qtd_total, qtd_disponivel)
SELECT 'Use a Cabeca Java', '9788575225632', 'Software', 1, 1
WHERE NOT EXISTS (SELECT 1 FROM livro WHERE isbn = '9788575225632');

INSERT INTO livro (titulo, isbn, categoria, qtd_total, qtd_disponivel)
SELECT 'Capitaes da Areia', '9788535914849', 'Literatura', 2, 2
WHERE NOT EXISTS (SELECT 1 FROM livro WHERE isbn = '9788535914849');

INSERT INTO livro (titulo, isbn, categoria, qtd_total, qtd_disponivel)
SELECT 'Java Efetivo', '9788576082675', 'Software', 2, 2
WHERE NOT EXISTS (SELECT 1 FROM livro WHERE isbn = '9788576082675');

INSERT INTO livro (titulo, isbn, categoria, qtd_total, qtd_disponivel)
SELECT 'Engenharia de Software', '9788550800653', 'Software', 5, 5
WHERE NOT EXISTS (SELECT 1 FROM livro WHERE isbn = '9788550800653');

INSERT INTO livro (titulo, isbn, categoria, qtd_total, qtd_disponivel)
SELECT 'O Pequeno Principe', '9788577809088', 'Literatura', 3, 3
WHERE NOT EXISTS (SELECT 1 FROM livro WHERE isbn = '9788577809088');

INSERT INTO livro (titulo, isbn, categoria, qtd_total, qtd_disponivel)
SELECT 'O Cortico', '9788520925642', 'Literatura', 2, 2
WHERE NOT EXISTS (SELECT 1 FROM livro WHERE isbn = '9788520925642');

INSERT INTO livro (titulo, isbn, categoria, qtd_total, qtd_disponivel)
SELECT 'Arquitetura Limpa', '9788575227247', 'Software', 1, 1
WHERE NOT EXISTS (SELECT 1 FROM livro WHERE isbn = '9788575227247');
