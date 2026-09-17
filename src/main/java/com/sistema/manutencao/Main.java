package com.sistema.manutencao;

import com.sistema.manutencao.config.DatabaseConnection;
import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        System.out.println("Iniciando o sistema...");

        // Utilizamos o 'try-with-resources'
        // Isso é uma boa prática no Java, pois garante que a conexão será fechada (conn.close())
        // automaticamente ao final do bloco, evitando vazamento de memória e conexões travadas no banco
        try (Connection conn = DatabaseConnection.getConnection()) {
            
            // Valida se a conexão retornada realmente é válida e está aberta
            if (conn != null && !conn.isClosed()) {
                System.out.println(">> Conexão com o MySQL realizada com sucesso! Infraestrutura pronta.");
            }
            
        } catch (SQLException e) {
            // Caso ocorra alguma falha na tentativa de conexão, a mensagem de erro é capturada e exibida aqui
            System.err.println(">> Falha ao conectar ao banco de dados: " + e.getMessage());
        }
    }
}
