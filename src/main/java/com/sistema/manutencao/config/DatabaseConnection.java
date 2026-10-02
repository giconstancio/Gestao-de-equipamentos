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

    private static String user;
    private static String password;

    // Configura usuário e senha da conexão com o banco de dados
    public static void configurar(String user, String password) {
        DatabaseConnection.user = user;
        DatabaseConnection.password = password;
    }

    // Construtor vazio
    public DatabaseConnection() { }

    /**
     * Abre e retorna uma NOVA conexão com o banco de dados a cada chamada
     * Todas as DAOs usam a conexão dentro de um try-with-resources, que fecha a conexão ao final do bloco.
     * Por isso não reaproveitamos uma única conexão estática: se ela fosse compartilhada, uma DAO poderia fechar
     * a conexão que outra parte do sistema ainda está usando
     */
    public static Connection getConnection() {
        try {
            // Estabelece uma nova conexão usando o DriverManager e as credenciais configuradas
            return DriverManager.getConnection(URL, user, password);
        } catch (SQLException e) {
            // Captura qualquer erro de SQL e lança
            // uma RuntimeException, o que ajuda a identificar falhas graves de infraestrutura rapidamente
            throw new RuntimeException("Erro ao conectar ao banco de dados: " + e.getMessage(), e);
        }
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
