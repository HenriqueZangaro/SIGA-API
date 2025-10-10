package com.siga.repository;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.siga.model.Maquina;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Repository
public class MaquinaRepository {

    private final Firestore firestore;
    private static final String COLLECTION_NAME = "maquinas";

    @Autowired
    public MaquinaRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public List<Maquina> findAll() {
        try {
            System.out.println("🔍 Repository: Iniciando busca no Firestore...");
            System.out.println("🔍 Repository: Firestore client válido: " + (firestore != null));
            
            List<QueryDocumentSnapshot> documents = firestore.collection(COLLECTION_NAME)
                    .get()
                    .get()
                    .getDocuments();

            List<Maquina> maquinas = new ArrayList<>();
            
            for (QueryDocumentSnapshot document : documents) {
                Maquina maquina = document.toObject(Maquina.class);
                
                if (maquina != null) {
                    maquina.setId(document.getId());
                    maquinas.add(maquina);
                }
            }
            
            System.out.println("✅ Buscou " + maquinas.size() + " máquinas do Firebase");
            return maquinas;
            
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Erro ao buscar máquinas: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erro ao buscar máquinas", e);
        } catch (Exception e) {
            System.err.println("❌ Erro inesperado ao buscar máquinas: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erro inesperado ao buscar máquinas", e);
        }
    }

    public Maquina findById(String id) {
        try {
            var document = firestore.collection(COLLECTION_NAME)
                    .document(id)
                    .get()
                    .get();
            
            if (document.exists()) {
                Maquina maquina = document.toObject(Maquina.class);
                
                if (maquina != null) {
                    maquina.setId(document.getId());
                    System.out.println("✅ Buscou máquina " + id + " do Firebase");
                    return maquina;
                }
            }
            
            System.out.println("⚠️ Máquina " + id + " não encontrada");
            return null;
            
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Erro ao buscar máquina por ID: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar máquina por ID", e);
        }
    }

    public List<Maquina> findByFazendaId(String fazendaId) {
        try {
            System.out.println("🔍 Repository: Buscando máquinas da fazenda: " + fazendaId);
            
            List<QueryDocumentSnapshot> documents = firestore.collection(COLLECTION_NAME)
                    .whereArrayContains("fazendaIds", fazendaId)
                    .get()
                    .get()
                    .getDocuments();

            List<Maquina> maquinas = new ArrayList<>();
            
            for (QueryDocumentSnapshot document : documents) {
                Maquina maquina = document.toObject(Maquina.class);
                
                if (maquina != null) {
                    maquina.setId(document.getId());
                    maquinas.add(maquina);
                }
            }
            
            System.out.println("✅ Encontradas " + maquinas.size() + " máquinas para fazenda " + fazendaId);
            return maquinas;
            
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Erro ao buscar máquinas por fazenda: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar máquinas por fazenda", e);
        }
    }
}