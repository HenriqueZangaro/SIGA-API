package com.siga.repository;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.siga.model.UserProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Repository
public class UserProfileRepository {

    private final Firestore firestore;
    private static final String COLLECTION_NAME = "userProfiles";

    @Autowired
    public UserProfileRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    /**
     * Busca UserProfile por UID do Firebase Auth
     */
    public UserProfile findByUid(String uid) {
        try {
            DocumentSnapshot document = firestore.collection(COLLECTION_NAME)
                    .document(uid)
                    .get()
                    .get();
            
            if (document.exists()) {
                UserProfile userProfile = document.toObject(UserProfile.class);
                return userProfile;
            }
            
            return null;
            
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Erro ao buscar UserProfile por UID: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar UserProfile por UID", e);
        }
    }

    /**
     * Busca UserProfile por email
     */
    public UserProfile findByEmail(String email) {
        try {
            List<QueryDocumentSnapshot> documents = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("email", email)
                    .limit(1)
                    .get()
                    .get()
                    .getDocuments();
            
            if (!documents.isEmpty()) {
                return documents.get(0).toObject(UserProfile.class);
            }
            
            return null;
            
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Erro ao buscar UserProfile por email: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar UserProfile por email", e);
        }
    }

    /**
     * Busca UserProfile por operadorId
     */
    public UserProfile findByOperadorId(String operadorId) {
        try {
            List<QueryDocumentSnapshot> documents = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("operadorId", operadorId)
                    .limit(1)
                    .get()
                    .get()
                    .getDocuments();
            
            if (!documents.isEmpty()) {
                return documents.get(0).toObject(UserProfile.class);
            }
            
            return null;
            
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Erro ao buscar UserProfile por operadorId: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar UserProfile por operadorId", e);
        }
    }

    /**
     * Busca todos os UserProfiles com role 'operador'
     */
    public List<UserProfile> findAllOperadores() {
        try {
            List<QueryDocumentSnapshot> documents = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("role", "operador")
                    .get()
                    .get()
                    .getDocuments();
            
            List<UserProfile> operadores = new ArrayList<>();
            for (QueryDocumentSnapshot document : documents) {
                UserProfile userProfile = document.toObject(UserProfile.class);
                if (userProfile != null) {
                    operadores.add(userProfile);
                }
            }
            
            return operadores;
            
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Erro ao buscar operadores: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar operadores", e);
        }
    }

    /**
     * Busca todos os UserProfiles por role
     */
    public List<UserProfile> findAllByRole(String role) {
        try {
            List<QueryDocumentSnapshot> documents = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("role", role)
                    .get()
                    .get()
                    .getDocuments();
            
            List<UserProfile> users = new ArrayList<>();
            for (QueryDocumentSnapshot document : documents) {
                try {
                    UserProfile userProfile = document.toObject(UserProfile.class);
                    if (userProfile != null) {
                        userProfile.setUid(document.getId());
                        users.add(userProfile);
                    }
                } catch (Exception e) {
                    System.err.println("❌ Erro ao deserializar UserProfile " + document.getId() + ": " + e.getMessage());
                }
            }
            
            System.out.println("✅ Repository: Encontrados " + users.size() + " usuários com role '" + role + "'");
            return users;
            
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Erro ao buscar usuários por role: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar usuários por role", e);
        }
    }

    /**
     * Busca usuários por proprietarioId e permissão
     * Usado para notificar admins/donos do proprietário sobre novos chamados
     */
    public List<UserProfile> findByProprietarioIdAndPermissao(String proprietarioId, List<String> permissoes) {
        try {
            System.out.println("🔍 Repository: Buscando usuários do proprietário " + proprietarioId + " com permissões: " + permissoes);
            
            // Buscar todos os usuários do proprietário
            List<QueryDocumentSnapshot> documents = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("proprietarioId", proprietarioId)
                    .get()
                    .get()
                    .getDocuments();
            
            List<UserProfile> users = new ArrayList<>();
            for (QueryDocumentSnapshot document : documents) {
                try {
                    UserProfile userProfile = document.toObject(UserProfile.class);
                    if (userProfile != null) {
                        userProfile.setUid(document.getId());
                        
                        // Filtrar por permissão
                        if (userProfile.getPermissao() != null && 
                            permissoes.contains(userProfile.getPermissao().toLowerCase())) {
                            users.add(userProfile);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("❌ Erro ao deserializar UserProfile " + document.getId() + ": " + e.getMessage());
                }
            }
            
            System.out.println("✅ Repository: Encontrados " + users.size() + " usuários com permissões " + permissoes);
            return users;
            
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Erro ao buscar usuários por proprietário e permissão: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar usuários por proprietário e permissão", e);
        }
    }
}

