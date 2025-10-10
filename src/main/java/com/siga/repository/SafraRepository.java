package com.siga.repository;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.siga.model.Safra;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Repository
public class SafraRepository {

    private final Firestore firestore;
    private static final String COLLECTION_NAME = "safras";

    @Autowired
    public SafraRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public List<Safra> findAll() {
        try {
            System.out.println("🔍 Repository: Iniciando busca no Firestore...");
            System.out.println("🔍 Repository: Firestore client válido: " + (firestore != null));
            
            List<QueryDocumentSnapshot> documents = firestore.collection(COLLECTION_NAME)
                    .get()
                    .get()
                    .getDocuments();

            List<Safra> safras = new ArrayList<>();
            
            for (QueryDocumentSnapshot document : documents) {
                Safra safra = document.toObject(Safra.class);
                
                if (safra != null) {
                    safra.setId(document.getId());
                    safras.add(safra);
                }
            }
            
            System.out.println("✅ Buscou " + safras.size() + " safras do Firebase");
            return safras;
            
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Erro ao buscar safras: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erro ao buscar safras", e);
        } catch (Exception e) {
            System.err.println("❌ Erro inesperado ao buscar safras: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erro inesperado ao buscar safras", e);
        }
    }

    public Safra findById(String id) {
        try {
            var document = firestore.collection(COLLECTION_NAME)
                    .document(id)
                    .get()
                    .get();
            
            if (document.exists()) {
                Safra safra = document.toObject(Safra.class);
                
                if (safra != null) {
                    safra.setId(document.getId());
                    System.out.println("✅ Buscou safra " + id + " do Firebase");
                    return safra;
                }
            }
            
            System.out.println("⚠️ Safra " + id + " não encontrada");
            return null;
            
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Erro ao buscar safra por ID: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar safra por ID", e);
        }
    }

    public List<Safra> findByFazendaId(String fazendaId) {
        try {
            System.out.println("🔍 Repository: Buscando safras da fazenda: " + fazendaId);
            
            List<QueryDocumentSnapshot> documents = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("fazendaId", fazendaId)
                    .get()
                    .get()
                    .getDocuments();

            List<Safra> safras = new ArrayList<>();
            
            for (QueryDocumentSnapshot document : documents) {
                Safra safra = document.toObject(Safra.class);
                
                if (safra != null) {
                    safra.setId(document.getId());
                    safras.add(safra);
                }
            }
            
            System.out.println("✅ Encontradas " + safras.size() + " safras para fazenda " + fazendaId);
            return safras;
            
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Erro ao buscar safras por fazenda: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar safras por fazenda", e);
        }
    }
}