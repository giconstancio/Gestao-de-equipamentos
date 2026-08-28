package com.sistema.manutencao.service;

import com.sistema.manutencao.dao.UsuarioDAO;
import com.sistema.manutencao.model.Usuario;
import com.sistema.manutencao.model.enums.PerfilUsuario;
import com.sistema.manutencao.model.enums.StatusUsuario;

import java.util.List;

/* Classe Service de Usuário
Responsável por organizar e aplicar as regras de negócio dos métodos relacionados aos usuários, sendo a camada
intermediária entre a entrada do sistema (Main - CLI) e a DAO, chamando diretamente os métodos da DAO para realização de
consultas e persistência efetivas na base de dados
*/
public class UsuarioService {
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    // Método para autenticação de usuário com validações de valores válidos e credenciais válidas
    public Usuario autenticar(String email, String senha) {
        if (email == null || email.isBlank() || senha == null || senha.isBlank()) {
            throw new IllegalArgumentException("Email e senha são obrigatórios");
        }

        Usuario u = usuarioDAO.buscarUsuarioPorEmail(email);
        if (u == null) {
            throw new IllegalArgumentException("Credenciais inválidas");
        }

        if (!u.getSenha().equals(senha)) {
            throw new IllegalArgumentException("Credenciais inválidas");
        }

        if (!u.isAtivo()) {
            throw new IllegalArgumentException("Sua conta está inativa. Entre em contato com o gestor do sistema");
        }

        return u; // Caso a autenticação passe por todas as validações retorne sucesso, o método retorna o Usuário encontrado
    }

    // Método para cadastro de Usuário com validação de campos obrigatórios com auxílio de um método interno auxiliar
    public Usuario cadastrarUsuario(String nome, String email, String senha, PerfilUsuario perfil) {
        validarCamposObrigatorios(nome, email, senha, perfil);

        Usuario u = new Usuario();
        u.setNome(nome);
        u.setEmail(email);
        u.setSenha(senha);
        u.setPerfil(perfil);
        u.setAtivo(true);
        // Preenche automaticamente o valor de Status do Usuário com base no Perfil
        u.setStatus(perfil == PerfilUsuario.TECNICO ? StatusUsuario.DISPONIVEL : null);

        return usuarioDAO.cadastrarUsuario(u);
    }

    // Método responsável por listar todos os usuários do banco de dados
    public List<Usuario> listarUsuarios() {
        return usuarioDAO.listarUsuarios();
    }

    // Método responsável por listar todos os usuários com perfil Técnico do banco de dados
    public List<Usuario> listarTecnicos() {
        return usuarioDAO.listarUsuariosPorPerfil(PerfilUsuario.TECNICO);
    }

    // Método responsável por listar todos os usuários com perfil Gestor do banco de dados
    public List<Usuario> listarGestores() {
        return usuarioDAO.listarUsuariosPorPerfil(PerfilUsuario.GESTOR);
    }

    //  Método responsável por listar todos os usuários disponíveis e ativos com perfil Técnico do banco de dados
    public List<Usuario> listarTecnicosDisponiveis() {
        return usuarioDAO.listarUsuariosPorPerfil(PerfilUsuario.TECNICO)
                .stream()
                // Faz uma segunda filtragem por Status = Disponível e ativo = true
                .filter(t -> t.getStatus() == StatusUsuario.DISPONIVEL && t.isAtivo())
                .toList();
    }

    // Método responsável por buscar um Usuário a partir do ID informado
    public Usuario buscarPorId(int idUsuario) {
        Usuario u = usuarioDAO.buscarUsuarioPorId(idUsuario);
        // Caso não encontre nenhum resultado, lança uma exceção
        if (u == null) {
            throw new IllegalArgumentException("Usuário não encontrado");
        }
        return u;
    }

    // Método de edição de Usuário com validação de campos obrigatórios com auxílio de um método interno auxiliar
    public Usuario editarUsuario(Usuario u) {
        validarCamposObrigatorios(u.getNome(), u.getEmail(), u.getSenha(), u.getPerfil());
        return usuarioDAO.editarUsuario(u);
    }

    // Método específico para alterar o Status do Usuário
    public void alterarDisponibilidade(int idUsuario, StatusUsuario novoStatus) {
        Usuario u = buscarPorId(idUsuario);

        // Faz uma verificação do perfil e lança uma exceção caso não seja um Usuário do tipo Técnico
        if (u.getPerfil() != PerfilUsuario.TECNICO) {
            throw new IllegalStateException("Apenas técnicos possuem disponibilidade");
        }

        u.setStatus(novoStatus);
        usuarioDAO.editarUsuario(u);
    }

    // Método de soft delete que inativa o Usuário
    public void inativarUsuario(int idUsuario) {
        buscarPorId(idUsuario); // Faz a busca para verificar se o Usuário é válido e existente dentro do sistema
        usuarioDAO.inativarUsuario(idUsuario); // Chama o método da DAO para efetivamente inativar o Usuário
    }

    // Método interno auxiliar para validação dos campos obrigatórios nos outros métodos da Service
    // Lança uma exceção em caso de campos não preenchidos ou formato incorreto
    private void validarCamposObrigatorios(String nome, String email, String senha, PerfilUsuario perfil) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }
        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new IllegalArgumentException("Email inválido");
        }
        if (senha == null || senha.isBlank()) {
            throw new IllegalArgumentException("Senha é obrigatória");
        }
        if (perfil == null) {
            throw new IllegalArgumentException("Perfil é obrigatório");
        }
    }
}