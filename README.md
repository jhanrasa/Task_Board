# Task_Board

# 📋 Gerenciador de Boards (Kanban CLI)

Este é um sistema de gerenciamento de tarefas via linha de comando (CLI) desenvolvido em **Java**, utilizando **JDBC** para persistência de dados no **MySQL**. O projeto segue uma arquitetura em camadas (DTO, DAO, Service e UI).

## 🚀 Funcionalidades

* **Gestão de Boards**: Criação e exclusão de quadros de projetos.
* **Fluxo Kanban**: Criação de cards que seguem o fluxo: *A Fazer -> Fazendo -> Concluído*.
* **Bloqueios e Desbloqueios**: Possibilidade de travar uma tarefa com um motivo específico, impedindo sua movimentação até que seja resolvida.
* **Visão Detalhada**: Visualização hierárquica colorida (ANSI) dos boards e seus respectivos cards ativos.
* **Filtro Inteligente**: Tarefas concluídas são automaticamente ocultadas da visão principal para manter o foco no trabalho pendente.

## 🛠️ Tecnologias Utilizadas

* **Java 17+**
* **MySQL 8.0**
* **Lombok**: Para redução de código boilerplate (Getters, Setters, Builders).
* **JDBC**: Para comunicação direta com o banco de dados.
* **Maven**: Gestão de dependências.

## ⚙️ Como Configurar o Banco de Dados

1. Certifique-se de ter o MySQL rodando em sua máquina.
2. Execute o script SQL abaixo para criar a estrutura necessária:

```sql
CREATE DATABASE board_manager_db;
USE board_manager_db;

CREATE TABLE BOARDS (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE BOARDS_COLUMNS (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    board_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    kind ENUM('INITIAL', 'PENDING', 'FINAL', 'CANCEL') NOT NULL,
    column_order INT NOT NULL,
    FOREIGN KEY (board_id) REFERENCES BOARDS(id) ON DELETE CASCADE
);

CREATE TABLE CARDS (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    board_column_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (board_column_id) REFERENCES BOARDS_COLUMNS(id) ON DELETE CASCADE
);

CREATE TABLE CARDS_BLOCKS (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    card_id BIGINT NOT NULL,
    reason VARCHAR(255) NOT NULL,
    blocked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    unblocked_at TIMESTAMP NULL,
    unblock_reason VARCHAR(255) NULL,
    FOREIGN KEY (card_id) REFERENCES CARDS(id) ON DELETE CASCADE
);



## Desenvolvido por Jean Domiciano de Menezes como parte dos estudos de java
