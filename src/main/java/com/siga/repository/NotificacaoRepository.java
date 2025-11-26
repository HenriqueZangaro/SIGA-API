package com.siga.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.siga.model.Notificacao;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Repository
public class NotificacaoRepository {

    private static final String COLLECTION_NAME = "notificacoes";

    private Firestore getFirestore() {
        return FirestoreClient.getFirestore();
    }

    /**
     * Cria uma nova notificação
     */
    public String criar(Notificacao notificacao) {
        try {
            Firestore db = getFirestore();
            
            Map<String, Object> data = new HashMap<>();
            data.put("userId", notificacao.getUserId());
            data.put("titulo", notificacao.getTitulo());
            data.put("mensagem", notificacao.getMensagem());
            data.put("tipo", notificacao.getTipo());
            data.put("categoria", notificacao.getCategoria());
            data.put("lida", false);
            data.put("createdAt", Timestamp.now());
            data.put("updatedAt", Timestamp.now());
            
            if (notificacao.getDados() != null) {
                data.put("dados", notificacao.getDados());
            }

            ApiFuture<DocumentReference> future = db.collection(COLLECTION_NAME).add(data);
            DocumentReference docRef = future.get();
            
            System.out.println("✅ Repository: Notificação criada - ID: " + docRef.getId());
            return docRef.getId();

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Repository: Erro ao criar notificação: " + e.getMessage());
            throw new RuntimeException("Erro ao criar notificação", e);
        }
    }

    /**
     * Busca notificação por ID
     */
    public Notificacao findById(String id) {
        try {
            Firestore db = getFirestore();
            DocumentSnapshot document = db.collection(COLLECTION_NAME).document(id).get().get();

            if (!document.exists()) {
                return null;
            }

            Notificacao notificacao = document.toObject(Notificacao.class);
            if (notificacao != null) {
                notificacao.setId(document.getId());
            }
            return notificacao;

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Repository: Erro ao buscar notificação: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar notificação", e);
        }
    }

    /**
     * Busca todas as notificações de um usuário
     */
    public List<Notificacao> findByUserId(String userId) {
        try {
            Firestore db = getFirestore();
            Query query = db.collection(COLLECTION_NAME)
                    .whereEqualTo("userId", userId)
                    .orderBy("createdAt", Query.Direction.DESCENDING);

            ApiFuture<QuerySnapshot> future = query.get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            List<Notificacao> notificacoes = new ArrayList<>();
            for (QueryDocumentSnapshot document : documents) {
                try {
                    Notificacao notificacao = document.toObject(Notificacao.class);
                    if (notificacao != null) {
                        notificacao.setId(document.getId());
                        notificacoes.add(notificacao);
                    }
                } catch (Exception e) {
                    System.err.println("❌ Erro ao deserializar notificação " + document.getId() + ": " + e.getMessage());
                }
            }

            return notificacoes;

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Repository: Erro ao buscar notificações: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar notificações", e);
        }
    }

    /**
     * Busca notificações não lidas de um usuário
     */
    public List<Notificacao> findNaoLidasByUserId(String userId) {
        try {
            Firestore db = getFirestore();
            Query query = db.collection(COLLECTION_NAME)
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("lida", false)
                    .orderBy("createdAt", Query.Direction.DESCENDING);

            ApiFuture<QuerySnapshot> future = query.get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            List<Notificacao> notificacoes = new ArrayList<>();
            for (QueryDocumentSnapshot document : documents) {
                try {
                    Notificacao notificacao = document.toObject(Notificacao.class);
                    if (notificacao != null) {
                        notificacao.setId(document.getId());
                        notificacoes.add(notificacao);
                    }
                } catch (Exception e) {
                    System.err.println("❌ Erro ao deserializar notificação " + document.getId() + ": " + e.getMessage());
                }
            }

            return notificacoes;

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Repository: Erro ao buscar notificações não lidas: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar notificações", e);
        }
    }

    /**
     * Conta notificações não lidas de um usuário
     */
    public long countNaoLidasByUserId(String userId) {
        try {
            Firestore db = getFirestore();
            Query query = db.collection(COLLECTION_NAME)
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("lida", false);

            ApiFuture<QuerySnapshot> future = query.get();
            return future.get().size();

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Repository: Erro ao contar notificações: " + e.getMessage());
            throw new RuntimeException("Erro ao contar notificações", e);
        }
    }

    /**
     * Marca uma notificação como lida
     */
    public void marcarComoLida(String id) {
        try {
            Firestore db = getFirestore();
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("lida", true);
            updates.put("updatedAt", Timestamp.now());

            db.collection(COLLECTION_NAME).document(id).update(updates).get();
            System.out.println("✅ Repository: Notificação marcada como lida - ID: " + id);

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Repository: Erro ao marcar notificação como lida: " + e.getMessage());
            throw new RuntimeException("Erro ao atualizar notificação", e);
        }
    }

    /**
     * Marca todas as notificações de um usuário como lidas
     */
    public int marcarTodasComoLidas(String userId) {
        try {
            Firestore db = getFirestore();
            
            // Buscar todas não lidas
            Query query = db.collection(COLLECTION_NAME)
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("lida", false);

            ApiFuture<QuerySnapshot> future = query.get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            // Atualizar cada uma
            WriteBatch batch = db.batch();
            for (QueryDocumentSnapshot document : documents) {
                DocumentReference docRef = db.collection(COLLECTION_NAME).document(document.getId());
                batch.update(docRef, "lida", true, "updatedAt", Timestamp.now());
            }
            
            batch.commit().get();
            System.out.println("✅ Repository: " + documents.size() + " notificações marcadas como lidas");
            
            return documents.size();

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Repository: Erro ao marcar todas como lidas: " + e.getMessage());
            throw new RuntimeException("Erro ao atualizar notificações", e);
        }
    }

    /**
     * Deleta uma notificação
     */
    public void delete(String id) {
        try {
            Firestore db = getFirestore();
            db.collection(COLLECTION_NAME).document(id).delete().get();
            System.out.println("✅ Repository: Notificação deletada - ID: " + id);

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Repository: Erro ao deletar notificação: " + e.getMessage());
            throw new RuntimeException("Erro ao deletar notificação", e);
        }
    }
}

