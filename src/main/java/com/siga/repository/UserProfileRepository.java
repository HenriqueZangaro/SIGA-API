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
}

