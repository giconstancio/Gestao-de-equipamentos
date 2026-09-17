package com.sistema.manutencao.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    // Lê as credenciais e a URL do banco a partir de variáveis de ambiente
    // O método auxiliar 'getEnvOrDefault' garante que, caso a variável não exista (por exemplo, ao rodar localmente), 
    // o sistema utilize os valores padrões fornecidos (ex: "root", "jdbc:mysql://...")
    private static final String URL = getEnvOrDefault(
        "DB_URL", 
        "jdbc:mysql://localhost:3306/db_manutencao?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
    );
    private static final String USER = getEnvOrDefault("DB_USER", "root");
    private static final String PASSWORD = getEnvOrDefault("DB_PASSWORD", "root");

    // Mantém a instância da conexão com o banco. Usamos 'static' para reaproveitar 
    // a mesma conexão em toda a aplicação
    private static Connection connection = null;

    // Construtor vazio
    public DatabaseConnection() { }

    /**
     * Retorna a conexão ativa com o banco de dados
     * Caso a conexão ainda não exista ou tenha sido fechada, uma nova será estabelecida
     */
    public static Connection getConnection() {
        try {
            // Verifica se a conexão é nula ou se foi encerrada anteriormente
            if (connection == null || connection.isClosed()) {
                // Estabelece uma nova conexão usando o DriverManager e as credenciais configuradas
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            }
        } catch (SQLException e) {
            // Captura qualquer erro de SQL e lança 
            // uma RuntimeException, o que ajuda a identificar falhas graves de infraestrutura rapidamente
            System.err.println("Erro ao conectar ao banco de dados: " + e.getMessage());
            throw new RuntimeException(e);
        }
        // Retorna a conexão pronta para uso
        return connection;
    }

    /**
     * Método auxiliar para ler variáveis de ambiente do sistema operacional
     * Se a variável de ambiente não estiver definida ou estiver vazia, ele retorna o valor padrão
     */
    private static String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value != null && !value.isBlank()) ? value : defaultValue;
    }
}
