CREATE DATABASE IF NOT EXISTS db_manutencao;
USE db_manutencao;

CREATE TABLE IF NOT EXISTS usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    perfil ENUM('GESTOR', 'TECNICO') NOT NULL,
    status_tecnico ENUM('DISPONIVEL', 'EM_ATENDIMENTO', 'AUSENTE') DEFAULT 'DISPONIVEL',
    ativo BOOLEAN DEFAULT TRUE,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS equipamentos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    codigo_patrimonio VARCHAR(50) NOT NULL UNIQUE,
    nome VARCHAR(100) NOT NULL,
    descricao TEXT,
    localizacao VARCHAR(100) NOT NULL,
    ativo BOOLEAN DEFAULT TRUE,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS ordens_servico (
    id INT AUTO_INCREMENT PRIMARY KEY,
    equipamento_id INT NOT NULL,
    tecnico_id INT NULL,
    gestor_abertura_id INT NOT NULL,
    descricao_falha TEXT NOT NULL,
    criticidade ENUM('BAIXA', 'MEDIA', 'ALTA', 'CRITICA') NOT NULL,
    status ENUM('ABERTA', 'EM_EXECUCAO', 'AGUARDANDO_PECA', 'REPARO_FINALIZADO', 'CONCLUIDA', 'CANCELADA') DEFAULT 'ABERTA',
    diagnostico_inicial TEXT,
    causa_raiz TEXT,
    horas_trabalhadas DECIMAL(5,2) DEFAULT 0.00,
    pecas_utilizadas TEXT,
    aberto_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    concluido_em TIMESTAMP NULL,
    FOREIGN KEY (equipamento_id) REFERENCES equipamentos(id),
    FOREIGN KEY (tecnico_id) REFERENCES usuarios(id),
    FOREIGN KEY (gestor_abertura_id) REFERENCES usuarios(id)
);

CREATE TABLE IF NOT EXISTS historico_intervencoes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    ordem_servico_id INT NOT NULL,
    usuario_id INT NOT NULL,
    descricao_acao TEXT NOT NULL,
    status_anterior VARCHAR(50),
    status_novo VARCHAR(50),
    registrado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ordem_servico_id) REFERENCES ordens_servico(id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

-- Usuários iniciais para teste (Senha padrão: 123456)
INSERT INTO usuarios (nome, email, senha, perfil, status_tecnico) VALUES 
('Administrador Gestor', 'gestor@sistema.com', '123456', 'GESTOR', NULL),
('Carlos Técnico', 'tecnico@sistema.com', '123456', 'TECNICO', 'DISPONIVEL');