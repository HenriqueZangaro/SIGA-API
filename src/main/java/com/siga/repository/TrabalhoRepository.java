package com.siga.repository;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.siga.model.Trabalho;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Repository
public class TrabalhoRepository {

    private final Firestore firestore;
    private static final String COLLECTION_NAME = "trabalhos";

    @Autowired
    public TrabalhoRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public List<Trabalho> findAll() {
        try {
            System.out.println("🔍 Repository: Iniciando busca no Firestore...");
            System.out.println("🔍 Repository: Firestore client válido: " + (firestore != null));
            
            List<QueryDocumentSnapshot> documents = firestore.collection(COLLECTION_NAME)
                    .get()
                    .get()
                    .getDocuments();

            List<Trabalho> trabalhos = new ArrayList<>();
            
            for (QueryDocumentSnapshot document : documents) {
                Trabalho trabalho = document.toObject(Trabalho.class);
                
                if (trabalho != null) {
                    trabalho.setId(document.getId());
                    trabalhos.add(trabalho);
                }
            }
            
            System.out.println("✅ Buscou " + trabalhos.size() + " trabalhos do Firebase");
            return trabalhos;
            
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Erro ao buscar trabalhos: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erro ao buscar trabalhos", e);
        } catch (Exception e) {
            System.err.println("❌ Erro inesperado ao buscar trabalhos: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erro inesperado ao buscar trabalhos", e);
        }
    }

    public Trabalho findById(String id) {
        try {
            var document = firestore.collection(COLLECTION_NAME)
                    .document(id)
                    .get()
                    .get();
            
            if (document.exists()) {
                Trabalho trabalho = document.toObject(Trabalho.class);
                
                if (trabalho != null) {
                    trabalho.setId(document.getId());
                    System.out.println("✅ Buscou trabalho " + id + " do Firebase");
                    return trabalho;
                }
            }
            
            System.out.println("⚠️ Trabalho " + id + " não encontrado");
            return null;
            
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Erro ao buscar trabalho por ID: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar trabalho por ID", e);
        }
    }

    public List<Trabalho> findByFazendaId(String fazendaId) {
        try {
            System.out.println("🔍 Repository: Buscando trabalhos da fazenda: " + fazendaId);
            
            List<QueryDocumentSnapshot> documents = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("fazendaId", fazendaId)
                    .get()
                    .get()
                    .getDocuments();

            List<Trabalho> trabalhos = new ArrayList<>();
            
            for (QueryDocumentSnapshot document : documents) {
                Trabalho trabalho = document.toObject(Trabalho.class);
                
                if (trabalho != null) {
                    trabalho.setId(document.getId());
                    trabalhos.add(trabalho);
                }
            }
            
            System.out.println("✅ Encontrados " + trabalhos.size() + " trabalhos para fazenda " + fazendaId);
            return trabalhos;
            
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Erro ao buscar trabalhos por fazenda: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar trabalhos por fazenda", e);
        }
    }

    public List<Trabalho> findByTalhaoId(String talhaoId) {
        try {
            System.out.println("🔍 Repository: Buscando trabalhos do talhão: " + talhaoId);
            
            List<QueryDocumentSnapshot> documents = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("talhaoId", talhaoId)
                    .get()
                    .get()
                    .getDocuments();

            List<Trabalho> trabalhos = new ArrayList<>();
            
            for (QueryDocumentSnapshot document : documents) {
                Trabalho trabalho = document.toObject(Trabalho.class);
                
                if (trabalho != null) {
                    trabalho.setId(document.getId());
                    trabalhos.add(trabalho);
                }
            }
            
            System.out.println("✅ Encontrados " + trabalhos.size() + " trabalhos para talhão " + talhaoId);
            return trabalhos;
            
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Erro ao buscar trabalhos por talhão: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar trabalhos por talhão", e);
        }
    }

    public List<Trabalho> findByMaquinaId(String maquinaId) {
        try {
            System.out.println("🔍 Repository: Buscando trabalhos da máquina: " + maquinaId);
            
            List<QueryDocumentSnapshot> documents = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("maquinaId", maquinaId)
                    .get()
                    .get()
                    .getDocuments();

            List<Trabalho> trabalhos = new ArrayList<>();
            
            for (QueryDocumentSnapshot document : documents) {
                Trabalho trabalho = document.toObject(Trabalho.class);
                
                if (trabalho != null) {
                    trabalho.setId(document.getId());
                    trabalhos.add(trabalho);
                }
            }
            
            System.out.println("✅ Encontrados " + trabalhos.size() + " trabalhos para máquina " + maquinaId);
            return trabalhos;
            
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Erro ao buscar trabalhos por máquina: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar trabalhos por máquina", e);
        }
    }

    public List<Trabalho> findByOperadorId(String operadorId) {
        try {
            System.out.println("🔍 Repository: Buscando trabalhos do operador: " + operadorId);
            
            List<QueryDocumentSnapshot> documents = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("operadorId", operadorId)
                    .get()
                    .get()
                    .getDocuments();

            List<Trabalho> trabalhos = new ArrayList<>();
            
            for (QueryDocumentSnapshot document : documents) {
                Trabalho trabalho = document.toObject(Trabalho.class);
                
                if (trabalho != null) {
                    trabalho.setId(document.getId());
                    trabalhos.add(trabalho);
                }
            }
            
            System.out.println("✅ Encontrados " + trabalhos.size() + " trabalhos para operador " + operadorId);
            return trabalhos;
            
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Erro ao buscar trabalhos por operador: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar trabalhos por operador", e);
        }
    }

    public List<Trabalho> findBySafraId(String safraId) {
        try {
            System.out.println("🔍 Repository: Buscando trabalhos da safra: " + safraId);
            
            List<QueryDocumentSnapshot> documents = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("safraId", safraId)
                    .get()
                    .get()
                    .getDocuments();

            List<Trabalho> trabalhos = new ArrayList<>();
            
            for (QueryDocumentSnapshot document : documents) {
                Trabalho trabalho = document.toObject(Trabalho.class);
                
                if (trabalho != null) {
                    trabalho.setId(document.getId());
                    trabalhos.add(trabalho);
                }
            }
            
            System.out.println("✅ Encontrados " + trabalhos.size() + " trabalhos para safra " + safraId);
            return trabalhos;
            
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Erro ao buscar trabalhos por safra: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar trabalhos por safra", e);
        }
    }
}