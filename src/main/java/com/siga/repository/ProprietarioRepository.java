package com.siga.repository;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.siga.model.Proprietario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Repository
public class ProprietarioRepository {

    private final Firestore firestore;
    private static final String COLLECTION_NAME = "proprietarios";

    @Autowired
    public ProprietarioRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public List<Proprietario> findAll() {
        try {
            System.out.println("🔍 Repository: Iniciando busca no Firestore...");
            System.out.println("🔍 Repository: Firestore client válido: " + (firestore != null));
            
            List<QueryDocumentSnapshot> documents = firestore.collection(COLLECTION_NAME)
                    .get()
                    .get()
                    .getDocuments();

            List<Proprietario> proprietarios = new ArrayList<>();
            
            for (QueryDocumentSnapshot document : documents) {
                Proprietario proprietario = document.toObject(Proprietario.class);
                
                if (proprietario != null) {
                    proprietario.setId(document.getId());
                    proprietarios.add(proprietario);
                }
            }
            
            System.out.println("✅ Buscou " + proprietarios.size() + " proprietários do Firebase");
            return proprietarios;
            
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Erro ao buscar proprietários: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erro ao buscar proprietários", e);
        } catch (Exception e) {
            System.err.println("❌ Erro inesperado ao buscar proprietários: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erro inesperado ao buscar proprietários", e);
        }
    }

    public Proprietario findById(String id) {
        try {
            var document = firestore.collection(COLLECTION_NAME)
                    .document(id)
                    .get()
                    .get();
            
            if (document.exists()) {
                Proprietario proprietario = document.toObject(Proprietario.class);
                
                if (proprietario != null) {
                    proprietario.setId(document.getId());
                    System.out.println("✅ Buscou proprietário " + id + " do Firebase");
                    return proprietario;
                }
            }
            
            System.out.println("⚠️ Proprietário " + id + " não encontrado");
            return null;
            
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Erro ao buscar proprietário por ID: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar proprietário por ID", e);
        }
    }

    public Proprietario findByDocumento(String documento) {
        try {
            System.out.println("🔍 Repository: Buscando proprietário por documento: " + documento);
            
            List<QueryDocumentSnapshot> documents = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("documento", documento)
                    .get()
                    .get()
                    .getDocuments();

            if (!documents.isEmpty()) {
                Proprietario proprietario = documents.get(0).toObject(Proprietario.class);
                
                if (proprietario != null) {
                    proprietario.setId(documents.get(0).getId());
                    System.out.println("✅ Encontrado proprietário com documento " + documento);
                    return proprietario;
                }
            }
            
            System.out.println("⚠️ Proprietário com documento " + documento + " não encontrado");
            return null;
            
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Erro ao buscar proprietário por documento: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar proprietário por documento", e);
        }
    }
}