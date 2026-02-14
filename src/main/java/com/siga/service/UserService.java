package com.siga.service;

import com.siga.dto.UserMeResponse;
import com.siga.model.Operador;
import com.siga.model.Proprietario;
import com.siga.model.UserProfile;
import com.siga.repository.OperadorRepository;
import com.siga.repository.ProprietarioRepository;
import com.siga.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * Serviço para operações relacionadas aos usuários
 * Consolida dados de UserProfile, Operador e Proprietario conforme estrutura real do Firestore
 */
@Service
public class UserService {

    private final UserProfileRepository userProfileRepository;
    private final OperadorRepository operadorRepository;
    private final ProprietarioRepository proprietarioRepository;
    
    // Roles válidos
    private static final List<String> VALID_ROLES = Arrays.asList("admin", "user", "operador");

    @Autowired
    public UserService(UserProfileRepository userProfileRepository, 
                      OperadorRepository operadorRepository,
                      ProprietarioRepository proprietarioRepository) {
        this.userProfileRepository = userProfileRepository;
        this.operadorRepository = operadorRepository;
        this.proprietarioRepository = proprietarioRepository;
    }

    /**
     * Busca dados completos do usuário logado
     * Consolida informações de UserProfile, Operador e Proprietario conforme estrutura real
     */
    public UserMeResponse getUserMeData(String uid) {
        System.out.println("🔍 UserService: Buscando dados completos para UID: " + uid);
        
        // Validações iniciais
        if (uid == null || uid.trim().isEmpty()) {
            throw new IllegalArgumentException("UID é obrigatório e não pode ser vazio");
        }
        
        // Buscar UserProfile
        UserProfile userProfile = userProfileRepository.findByUid(uid);
        if (userProfile == null) {
            throw new RuntimeException("Perfil de usuário não encontrado");
        }
        
        // Validar role
        String role = userProfile.getRole();
        if (role == null || !VALID_ROLES.contains(role.toLowerCase())) {
            throw new RuntimeException("Role inválido: " + role + ". Deve ser um dos valores: " + VALID_ROLES);
        }
        
        System.out.println("✅ UserService: UserProfile encontrado - Role: " + role);
        
        // Construir resposta base com dados do UserProfile
        UserMeResponse.UserMeResponseBuilder responseBuilder = UserMeResponse.builder()
                .uid(userProfile.getUid())
                .email(userProfile.getEmail())
                .role(userProfile.getRole())
                .permissao(userProfile.getPermissao())
                .proprietarioId(userProfile.getProprietarioId())
                .operadorId(userProfile.getOperadorId())
                .photoURL(userProfile.getPhotoURL())
                .bio(userProfile.getBio());
        
        // Enriquecer dados baseado no role do usuário
        switch (role.toLowerCase()) {
            case "admin":
                enrichAdminData(responseBuilder, userProfile);
                break;
            case "user":
                enrichUserData(responseBuilder, userProfile);
                break;
            case "operador":
                enrichOperadorData(responseBuilder, userProfile);
                break;
        }
        
        UserMeResponse response = responseBuilder.build();
        System.out.println("✅ UserService: Dados completos montados para: " + response.getNomeCompleto());
        
        return response;
    }
    
    /**
     * Enriquece dados para usuário ADMIN
     * Usar apenas dados do userProfiles
     */
    private void enrichAdminData(UserMeResponse.UserMeResponseBuilder builder, UserProfile userProfile) {
        System.out.println("🔧 UserService: Enriquecendo dados para ADMIN");
        
        builder
            .nomeCompleto(userProfile.getDisplayName() != null ? userProfile.getDisplayName() : "Administrador")
            .telefone(userProfile.getPhone()) // Vem de userProfiles.phone
            .permissao("admin")
            .proprietarioId("all") // Admin tem acesso a todos
            .proprietarioNome("Sistema - Acesso Total")
            .status("ativo");
    }
    
    /**
     * Enriquece dados para usuário USER (dono/admin/editor de proprietário)
     * Nome vem de userProfiles.displayName + dados complementares do proprietarios
     */
    private void enrichUserData(UserMeResponse.UserMeResponseBuilder builder, UserProfile userProfile) {
        System.out.println("🔧 UserService: Enriquecendo dados para USER");
        
        // Validar se tem proprietarioId
        if (userProfile.getProprietarioId() == null || userProfile.getProprietarioId().trim().isEmpty()) {
            throw new RuntimeException("Usuário do tipo 'user' deve ter proprietarioId vinculado");
        }
        
        try {
            Proprietario proprietario = proprietarioRepository.findById(userProfile.getProprietarioId());
            if (proprietario == null) {
                throw new RuntimeException("Dados do proprietário não encontrados");
            }
            
            builder
                // Nome vem sempre do userProfiles.displayName
                .nomeCompleto(userProfile.getDisplayName() != null ? userProfile.getDisplayName() : "Usuário") // userProfiles.displayName
                // Outros dados vêm do proprietário
                .documento(proprietario.getDocumento()) // proprietarios.documento (CPF/CNPJ formatado)
                .tipo(proprietario.getTipo()) // proprietarios.tipo ("PF" ou "PJ")
                .telefone(proprietario.getTelefone()) // proprietarios.telefone
                .endereco(proprietario.getEndereco()) // proprietarios.endereco (endereço completo)
                .proprietarioNome(proprietario.getNome()) // proprietarios.nome
                .status("ativo"); // Default para users
            
            System.out.println("✅ UserService: Dados do proprietário adicionados. Nome do usuário: " + userProfile.getDisplayName());
            
        } catch (RuntimeException e) {
            throw e; // Re-lançar erros de negócio
        } catch (Exception e) {
            System.err.println("❌ UserService: Erro interno ao buscar proprietário: " + e.getMessage());
            throw new RuntimeException("Erro interno ao buscar dados");
        }
    }
    
    /**
     * Enriquece dados para usuário OPERADOR
     * Nome vem de userProfiles.displayName + dados complementares do operadores
     */
    private void enrichOperadorData(UserMeResponse.UserMeResponseBuilder builder, UserProfile userProfile) {
        System.out.println("🔧 UserService: Enriquecendo dados para OPERADOR");
        
        // Validar se tem operadorId
        if (userProfile.getOperadorId() == null || userProfile.getOperadorId().trim().isEmpty()) {
            throw new RuntimeException("Usuário do tipo 'operador' deve ter operadorId vinculado");
        }
        
        try {
            Operador operador = operadorRepository.findById(userProfile.getOperadorId());
            if (operador == null) {
                throw new RuntimeException("Dados do operador não encontrados");
            }
            
            builder
                // Nome vem sempre do userProfiles.displayName
                .nomeCompleto(userProfile.getDisplayName() != null ? userProfile.getDisplayName() : "Usuário") // userProfiles.displayName
                // Outros dados vêm do operador
                .cpf(operador.getCpf()) // operadores.cpf (formatado)
                .telefone(operador.getTelefone()) // operadores.telefone
                .proprietarioId(operador.getProprietarioId()) // operadores.proprietarioId
                .proprietarioNome(operador.getProprietarioNome()) // operadores.proprietarioNome
                .status(operador.getStatus()) // operadores.status
                .especialidades(operador.getEspecialidades()) // operadores.especialidades (array)
                .fazendaIds(operador.getFazendaIds()) // operadores.fazendaIds (array)
                .fazendaNomes(operador.getFazendaNomes()); // operadores.fazendaNomes (array)
            
            // Email vem sempre do userProfile, não do operador
            // (já foi setado no builder principal)
            
            System.out.println("✅ UserService: Dados do operador adicionados. Nome do usuário: " + userProfile.getDisplayName());
            
        } catch (RuntimeException e) {
            throw e; // Re-lançar erros de negócio
        } catch (Exception e) {
            System.err.println("❌ UserService: Erro interno ao buscar operador: " + e.getMessage());
            throw new RuntimeException("Erro interno ao buscar dados");
        }
    }
    
    /**
     * Verifica se o usuário existe
     */
    public boolean userExists(String uid) {
        UserProfile userProfile = userProfileRepository.findByUid(uid);
        return userProfile != null;
    }
    
    /**
     * Busca UserProfile por UID
     */
    public UserProfile getUserProfile(String uid) {
        return userProfileRepository.findByUid(uid);
    }
}