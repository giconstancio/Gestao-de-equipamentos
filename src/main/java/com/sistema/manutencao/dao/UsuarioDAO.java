package com.sistema.manutencao.dao;

import com.sistema.manutencao.config.DatabaseConnection;
import com.sistema.manutencao.model.Usuario;
import com.sistema.manutencao.model.enums.PerfilUsuario;
import com.sistema.manutencao.model.enums.StatusUsuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/* Classe DAO de Usuário
Responsável por realizar ações relacionadas aos usuários dentro do banco de dados, sendo a camada intermediária entre o
banco e a classe Service
*/
public class UsuarioDAO {

    // Método de cadastro de Usuário no banco de dados
    // Recebe o objeto Usuário como parâmetro e cadastra seus atributos no sistema
    public Usuario cadastrarUsuario(Usuario u) {
        String sql = "INSERT INTO usuarios(nome, email, senha, perfil) VALUES (?,?,?,?)";

        try(Connection conn = new DatabaseConnection().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, u.getNome());
            stmt.setString(2, u.getEmail());
            stmt.setString(3, u.getSenha());
            stmt.setString(4, u.getPerfil().name());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    u.setIdUsuario(rs.getInt(1));
                }
            } return u; // Retorna o Usuário cadastrado já com o ID atribuído
        } catch (Exception e) {
            throw new RuntimeException("Erro ao cadastrar usuário: " + e);
        }
    }

    // Método responsável por listar todos os usuários do sistema
    // Retorna uma lista com os objetos dos Usuários completos
    public List<Usuario> listarUsuarios() {
        // Ordenados por ordem alfabética
        String sql = "SELECT * FROM usuarios ORDER BY nome";

        try(Connection conn = new DatabaseConnection().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {
            List<Usuario> usuarios = new ArrayList<>();
            while(rs.next()) {
                usuarios.add(mapRow(rs)); // Adiciona cada um na lista que será retornada (com auxílio do método de conversão)
            }
            return usuarios;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao listar usuários: " + e);
        }
    }

    // Método responsável por listar todos os usuários filtrados pelo perfil parametrizado (Gestor ou Técnico)
    public List<Usuario> listarUsuariosPorPerfil(PerfilUsuario perfilUsuario) {
        // Ordenados por ordem alfabética
        String sql = "SELECT * FROM usuarios WHERE perfil = ? ORDER BY nome";

        try(Connection conn = new DatabaseConnection().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, perfilUsuario.name());
            List<Usuario> usuarios = new ArrayList<>();

            try(ResultSet rs = stmt.executeQuery()) {
                while(rs.next()) {
                    usuarios.add(mapRow(rs)); // Adiciona cada um na lista que será retornada (com auxílio do método de conversão)
                }
            } return usuarios;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao listar usuários com perfil " + perfilUsuario + ": " + e);
        }
    }

    // Método responsável por retornar o objeto completo do Usuário com base no ID informado pelo parâmetro
    public Usuario buscarUsuarioPorId(int idUsuario) {
        String sql = "SELECT * FROM usuarios WHERE id = ?";

        try(Connection conn = new DatabaseConnection().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);
            try(ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar usuário por ID: " + e);
        }
    }

    // Método responsável por retornar o objeto completo do Usuário com base no email informado pelo parâmetro
    // Usado para o método de autenticação
    public Usuario buscarUsuarioPorEmail(String email) {
        String sql = "SELECT * FROM usuarios WHERE email = ?";

        try(Connection conn = new DatabaseConnection().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try(ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar usuário por email: " + e);
        }
    }

    // Método de edição de atributos dos usuários
    // Pode ser usado em diferentes contextos para edições de campos específicos
    public Usuario editarUsuario(Usuario u) {
        String sql = "UPDATE usuarios SET nome=?, email=?, senha=?, perfil=?, status_tecnico=?, ativo=? WHERE id = ?";

        try(Connection conn = new DatabaseConnection().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, u.getNome());
            stmt.setString(2, u.getEmail());
            stmt.setString(3, u.getSenha());
            stmt.setString(4, u.getPerfil().name());
            stmt.setString(5, u.getStatus() != null ? u.getStatus().name() : null);
            stmt.setBoolean(6, u.isAtivo());
            stmt.setInt(7, u.getIdUsuario());
            stmt.executeUpdate();
            return u;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao editar usuário: " + e);
        }
    }

    // Método de soft delete do Usuário, tornando-o inativo dentro do sistema e o impedindo de seguir com outras ações
    public void inativarUsuario(int idUsuario) {
        String sql = "UPDATE usuarios SET ativo=false WHERE id = ?";

        try(Connection conn = new DatabaseConnection().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);
            stmt.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao inativar usuário: " + e);
        }
    }

    // Método interno auxiliar para criação e mapeamento dos dados do Usuário retornado por um ResultSet, ou seja, um objeto
    // Usuario com os valores obtidos pelo ResultSet
    private Usuario mapRow(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setIdUsuario(rs.getInt("id"));
        u.setNome(rs.getString("nome"));
        u.setEmail(rs.getString("email"));
        u.setPerfil(PerfilUsuario.valueOf(rs.getString("perfil")));

        String statusStr = rs.getString("status_tecnico");
        u.setStatus(statusStr != null ? StatusUsuario.valueOf(statusStr) : null);

        u.setAtivo(rs.getBoolean("ativo"));
        u.setCriadoEm(rs.getTimestamp("criado_em").toLocalDateTime());
        return u;
    }
}
