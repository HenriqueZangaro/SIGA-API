package com.siga.repository;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.siga.model.Talhao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Repository
public class TalhaoRepository {

    private final Firestore firestore;
    private static final String COLLECTION_NAME = "talhoes";

    @Autowired
    public TalhaoRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public List<Talhao> findAll() {
        try {
            System.out.println("🔍 Repository: Iniciando busca no Firestore...");
            System.out.println("🔍 Repository: Firestore client válido: " + (firestore != null));
            
            List<QueryDocumentSnapshot> documents = firestore.collection(COLLECTION_NAME)
                    .get()
                    .get()
                    .getDocuments();

            List<Talhao> talhoes = new ArrayList<>();
            
            for (QueryDocumentSnapshot document : documents) {
                Talhao talhao = document.toObject(Talhao.class);
                
                if (talhao != null) {
                    talhao.setId(document.getId());
                    talhoes.add(talhao);
                }
            }
            
            System.out.println("✅ Buscou " + talhoes.size() + " talhões do Firebase");
            return talhoes;
            
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Erro ao buscar talhões: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erro ao buscar talhões", e);
        } catch (Exception e) {
            System.err.println("❌ Erro inesperado ao buscar talhões: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erro inesperado ao buscar talhões", e);
        }
    }

    public Talhao findById(String id) {
        try {
            var document = firestore.collection(COLLECTION_NAME)
                    .document(id)
                    .get()
                    .get();
            
            if (document.exists()) {
                Talhao talhao = document.toObject(Talhao.class);
                
                if (talhao != null) {
                    talhao.setId(document.getId());
                    System.out.println("✅ Buscou talhão " + id + " do Firebase");
                    return talhao;
                }
            }
            
            System.out.println("⚠️ Talhão " + id + " não encontrado");
            return null;
            
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Erro ao buscar talhão por ID: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar talhão por ID", e);
        }
    }

    public List<Talhao> findByFazendaId(String fazendaId) {
        try {
            System.out.println("🔍 Repository: Buscando talhões da fazenda: " + fazendaId);
            
            List<QueryDocumentSnapshot> documents = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("fazendaId", fazendaId)
                    .get()
                    .get()
                    .getDocuments();

            List<Talhao> talhoes = new ArrayList<>();
            
            for (QueryDocumentSnapshot document : documents) {
                Talhao talhao = document.toObject(Talhao.class);
                
                if (talhao != null) {
                    talhao.setId(document.getId());
                    talhoes.add(talhao);
                }
            }
            
            System.out.println("✅ Encontrados " + talhoes.size() + " talhões para fazenda " + fazendaId);
            return talhoes;
            
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Erro ao buscar talhões por fazenda: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar talhões por fazenda", e);
        }
    }
}