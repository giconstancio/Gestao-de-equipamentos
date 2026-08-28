package com.sistema.manutencao.auth;

import com.sistema.manutencao.model.Usuario;
import com.sistema.manutencao.model.enums.PerfilUsuario;

// Classe utilitária para gerenciamento da sessão atual com informações do usuário logado, usado para definir acessos
public class SessaoAtual {
    private static Usuario usuarioLogado;

    public static void login(Usuario u) {
        usuarioLogado = u;
    }

    public static void logout() {
        usuarioLogado = null;
    }

    public static Usuario getUsuarioLogado() {
        return usuarioLogado;
    }

    public static boolean isGestor() {
        return usuarioLogado != null && usuarioLogado.getPerfil() == PerfilUsuario.GESTOR;
    }

    public static boolean isTecnico() {
        return usuarioLogado != null && usuarioLogado.getPerfil() == PerfilUsuario.TECNICO;
    }
}