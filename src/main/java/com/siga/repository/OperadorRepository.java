package com.siga.repository;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.siga.model.Operador;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Repository
public class OperadorRepository {

    private final Firestore firestore;
    private static final String COLLECTION_NAME = "operadores";

    @Autowired
    public OperadorRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public List<Operador> findAll() {
        try {
            System.out.println("🔍 Repository: Iniciando busca no Firestore...");
            System.out.println("🔍 Repository: Firestore client válido: " + (firestore != null));
            
            List<QueryDocumentSnapshot> documents = firestore.collection(COLLECTION_NAME)
                    .get()
                    .get()
                    .getDocuments();

            List<Operador> operadores = new ArrayList<>();
            
            for (QueryDocumentSnapshot document : documents) {
                Operador operador = document.toObject(Operador.class);
                
                if (operador != null) {
                    operador.setId(document.getId());
                    operadores.add(operador);
                }
            }
            
            System.out.println("✅ Buscou " + operadores.size() + " operadores do Firebase");
            return operadores;
            
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Erro ao buscar operadores: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erro ao buscar operadores", e);
        } catch (Exception e) {
            System.err.println("❌ Erro inesperado ao buscar operadores: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erro inesperado ao buscar operadores", e);
        }
    }

    public Operador findById(String id) {
        try {
            var document = firestore.collection(COLLECTION_NAME)
                    .document(id)
                    .get()
                    .get();
            
            if (document.exists()) {
                Operador operador = document.toObject(Operador.class);
                
                if (operador != null) {
                    operador.setId(document.getId());
                    System.out.println("✅ Buscou operador " + id + " do Firebase");
                    return operador;
                }
            }
            
            System.out.println("⚠️ Operador " + id + " não encontrado");
            return null;
            
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Erro ao buscar operador por ID: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar operador por ID", e);
        }
    }

    public List<Operador> findByFazendaId(String fazendaId) {
        try {
            System.out.println("🔍 Repository: Buscando operadores da fazenda: " + fazendaId);
            
            List<QueryDocumentSnapshot> documents = firestore.collection(COLLECTION_NAME)
                    .whereArrayContains("fazendaIds", fazendaId)
                    .get()
                    .get()
                    .getDocuments();

            List<Operador> operadores = new ArrayList<>();
            
            for (QueryDocumentSnapshot document : documents) {
                Operador operador = document.toObject(Operador.class);
                
                if (operador != null) {
                    operador.setId(document.getId());
                    operadores.add(operador);
                }
            }
            
            System.out.println("✅ Encontrados " + operadores.size() + " operadores para fazenda " + fazendaId);
            return operadores;
            
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Erro ao buscar operadores por fazenda: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar operadores por fazenda", e);
        }
    }
}