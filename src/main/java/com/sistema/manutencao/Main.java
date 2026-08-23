package com.sistema.manutencao;

import com.sistema.manutencao.config.DatabaseConnection;
import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        System.out.println("Iniciando o sistema...");

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println(">> Conexão com o MySQL realizada com sucesso! Infraestrutura pronta.");
            }
        } catch (SQLException e) {
            System.err.println(">> Falha ao conectar ao banco de dados: " + e.getMessage());
        }
    }
}