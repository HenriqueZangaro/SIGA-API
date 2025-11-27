package com.siga.repository;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.siga.model.Fazenda;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Repository
public class FazendaRepository {

    private final Firestore firestore;
    private static final String COLLECTION_NAME = "fazendas";

    @Autowired
    public FazendaRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public List<Fazenda> findAll() {
        try {
            System.out.println("🔍 Repository: Iniciando busca no Firestore...");
            System.out.println("🔍 Repository: Firestore client válido: " + (firestore != null));
            
            List<QueryDocumentSnapshot> documents = firestore.collection(COLLECTION_NAME)
                    .get()
                    .get()
                    .getDocuments();

            List<Fazenda> fazendas = new ArrayList<>();
            
            for (QueryDocumentSnapshot document : documents) {
                Fazenda fazenda = document.toObject(Fazenda.class);
                
                if (fazenda != null) {
                    fazenda.setId(document.getId());
                    fazendas.add(fazenda);
                }
            }
            
            System.out.println("✅ Buscou " + fazendas.size() + " fazendas do Firebase");
            return fazendas;
            
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Erro ao buscar fazendas: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erro ao buscar fazendas", e);
        } catch (Exception e) {
            System.err.println("❌ Erro inesperado ao buscar fazendas: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erro inesperado ao buscar fazendas", e);
        }
    }

    public Fazenda findById(String id) {
        try {
            var document = firestore.collection(COLLECTION_NAME)
                    .document(id)
                    .get()
                    .get();
            
            if (document.exists()) {
                Fazenda fazenda = document.toObject(Fazenda.class);
                
                if (fazenda != null) {
                    fazenda.setId(document.getId());
                    System.out.println("✅ Buscou fazenda " + id + " do Firebase");
                    return fazenda;
                }
            }
            
            System.out.println("⚠️ Fazenda " + id + " não encontrada");
            return null;
            
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Erro ao buscar fazenda por ID: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar fazenda por ID", e);
        }
    }

    /**
     * Busca fazendas por proprietarioId (filtro de segurança)
     */
    public List<Fazenda> findByProprietarioId(String proprietarioId) {
        try {
            System.out.println("🔍 Repository: Buscando fazendas do proprietário: " + proprietarioId);
            
            List<QueryDocumentSnapshot> documents = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("proprietarioId", proprietarioId)
                    .get()
                    .get()
                    .getDocuments();

            List<Fazenda> fazendas = new ArrayList<>();
            
            for (QueryDocumentSnapshot document : documents) {
                Fazenda fazenda = document.toObject(Fazenda.class);
                
                if (fazenda != null) {
                    fazenda.setId(document.getId());
                    fazendas.add(fazenda);
                }
            }
            
            System.out.println("✅ Encontradas " + fazendas.size() + " fazendas para proprietário " + proprietarioId);
            return fazendas;
            
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Erro ao buscar fazendas por proprietário: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar fazendas por proprietário", e);
        }
    }
}

