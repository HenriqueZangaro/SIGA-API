package com.siga.service;

import com.siga.model.Operador;
import com.siga.model.UserProfile;
import com.siga.repository.OperadorRepository;
import com.siga.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Serviço de autenticação
 * Valida tokens do Firebase e busca informações do usuário
 */
@Service
public class AuthService {

    private final UserProfileRepository userProfileRepository;
    private final OperadorRepository operadorRepository;

    @Autowired
    public AuthService(UserProfileRepository userProfileRepository, OperadorRepository operadorRepository) {
        this.userProfileRepository = userProfileRepository;
        this.operadorRepository = operadorRepository;
    }

    /**
     * Busca informações completas do usuário operador
     * Admin do site (role: 'admin') também pode acessar
     * Retorna UserProfile + Operador (se for operador) ou dados simulados (se for admin)
     */
    public Map<String, Object> getOperadorInfo(String uid) {
        System.out.println("🔍 Service: Buscando informações do operador com UID: " + uid);
        
        // Buscar UserProfile
        UserProfile userProfile = userProfileRepository.findByUid(uid);
        
        if (userProfile == null) {
            throw new RuntimeException("UserProfile não encontrado para UID: " + uid);
        }
        
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("userProfile", userProfile);
        
        // Se for ADMIN do site, retornar dados simulados (admin tem acesso total)
        if ("admin".equalsIgnoreCase(userProfile.getRole())) {
            System.out.println("✅ Service: Admin do site detectado - Acesso total concedido");
            // Criar dados simulados para admin (não precisa de operador)
            Map<String, Object> adminData = new HashMap<>();
            adminData.put("id", "admin");
            adminData.put("nome", userProfile.getDisplayName() != null ? userProfile.getDisplayName() : "Administrador");
            adminData.put("proprietarioId", "all"); // Admin acessa todos
            resultado.put("operador", adminData);
            return resultado;
        }
        
        // Se for OPERADOR, buscar dados do operador
        if ("operador".equalsIgnoreCase(userProfile.getRole())) {
            Operador operador = null;
            if (userProfile.getOperadorId() != null) {
                operador = operadorRepository.findById(userProfile.getOperadorId());
                
                if (operador == null) {
                    throw new RuntimeException("Operador não encontrado com ID: " + userProfile.getOperadorId());
                }
                
                // Verificar se operador está ativo
                if (!"ativo".equalsIgnoreCase(operador.getStatus())) {
                    throw new RuntimeException("Operador não está ativo");
                }
            } else {
                throw new RuntimeException("UserProfile não possui operadorId vinculado");
            }
            
            // Converter Operador para Map para manter consistência
            Map<String, Object> operadorData = new HashMap<>();
            operadorData.put("id", operador.getId());
            operadorData.put("nome", operador.getNome());
            operadorData.put("proprietarioId", operador.getProprietarioId());
            operadorData.put("status", operador.getStatus());
            operadorData.put("email", operador.getEmail());
            operadorData.put("telefone", operador.getTelefone());
            
            resultado.put("operador", operadorData);
            System.out.println("✅ Service: Operador encontrado - " + operador.getNome());
            return resultado;
        }
        
        // Se não for nem admin nem operador
        throw new RuntimeException("Usuário não tem permissão para acessar recursos de ponto (role: " + userProfile.getRole() + ")");
    }

    /**
     * Valida se o usuário pode acessar dados de um proprietário
     */
    public boolean podeAcessarProprietario(String uid, String proprietarioId) {
        UserProfile userProfile = userProfileRepository.findByUid(uid);
        
        if (userProfile == null) {
            return false;
        }
        
        // Admin pode acessar tudo
        if ("admin".equalsIgnoreCase(userProfile.getRole())) {
            return true;
        }
        
        // Operador só pode acessar dados do seu proprietário
        if ("operador".equalsIgnoreCase(userProfile.getRole())) {
            if (userProfile.getOperadorId() != null) {
                Operador operador = operadorRepository.findById(userProfile.getOperadorId());
                if (operador != null && proprietarioId.equals(operador.getProprietarioId())) {
                    return true;
                }
            }
        }
        
        // User pode acessar se tiver permissão no proprietário
        if ("user".equalsIgnoreCase(userProfile.getRole())) {
            if (proprietarioId.equals(userProfile.getProprietarioId())) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Verifica se o usuário é admin
     */
    public boolean isAdmin(String uid) {
        UserProfile userProfile = userProfileRepository.findByUid(uid);
        return userProfile != null && "admin".equalsIgnoreCase(userProfile.getRole());
    }

    /**
     * Verifica se o usuário é operador
     */
    public boolean isOperador(String uid) {
        UserProfile userProfile = userProfileRepository.findByUid(uid);
        return userProfile != null && "operador".equalsIgnoreCase(userProfile.getRole());
    }

    /**
     * Verifica se o usuário é um user comum (dono/admin de proprietário)
     */
    public boolean isUser(String uid) {
        UserProfile userProfile = userProfileRepository.findByUid(uid);
        return userProfile != null && "user".equalsIgnoreCase(userProfile.getRole());
    }

    /**
     * Obtém o proprietarioId do usuário baseado em seu role
     * - Admin: retorna null (acesso a todos)
     * - User: retorna o proprietarioId do perfil
     * - Operador: retorna o proprietarioId do operador vinculado
     */
    public String getProprietarioId(String uid) {
        UserProfile userProfile = userProfileRepository.findByUid(uid);
        
        if (userProfile == null) {
            return null;
        }
        
        // Admin tem acesso a tudo
        if ("admin".equalsIgnoreCase(userProfile.getRole())) {
            return null; // null significa acesso total
        }
        
        // User: retorna o proprietarioId do perfil
        if ("user".equalsIgnoreCase(userProfile.getRole())) {
            return userProfile.getProprietarioId();
        }
        
        // Operador: busca o proprietarioId do operador
        if ("operador".equalsIgnoreCase(userProfile.getRole())) {
            if (userProfile.getOperadorId() != null) {
                Operador operador = operadorRepository.findById(userProfile.getOperadorId());
                if (operador != null) {
                    return operador.getProprietarioId();
                }
            }
        }
        
        return null;
    }

    /**
     * Obtém o UserProfile pelo UID
     */
    public UserProfile getUserProfile(String uid) {
        return userProfileRepository.findByUid(uid);
    }
}

