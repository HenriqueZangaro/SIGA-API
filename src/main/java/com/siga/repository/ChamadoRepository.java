package com.siga.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.siga.model.Chamado;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Repository
public class ChamadoRepository {

    private static final String COLLECTION_NAME = "chamados";

    private Firestore getFirestore() {
        return FirestoreClient.getFirestore();
    }

    /**
     * Cria um novo chamado no Firestore
     */
    public String criarChamado(Chamado chamado) {
        try {
            Firestore db = getFirestore();
            chamado.setDataCriacao(Timestamp.now());
            chamado.setUltimaAtualizacao(Timestamp.now());
            chamado.setUpdatedAt(Timestamp.now());

            Map<String, Object> chamadoData = convertToMap(chamado);
            removeNullFields(chamadoData);

            ApiFuture<DocumentReference> future = db.collection(COLLECTION_NAME).add(chamadoData);
            DocumentReference docRef = future.get();
            
            System.out.println("✅ Repository: Chamado criado - ID: " + docRef.getId());
            return docRef.getId();

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Repository: Erro ao criar chamado: " + e.getMessage());
            throw new RuntimeException("Erro ao criar chamado", e);
        }
    }

    /**
     * Busca um chamado por ID
     */
    public Chamado findById(String id) {
        try {
            Firestore db = getFirestore();
            DocumentSnapshot document = db.collection(COLLECTION_NAME).document(id).get().get();

            if (!document.exists()) {
                throw new RuntimeException("Chamado não encontrado");
            }

            Chamado chamado = document.toObject(Chamado.class);
            if (chamado != null) {
                chamado.setId(document.getId());
            }
            return chamado;

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Repository: Erro ao buscar chamado: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar chamado", e);
        }
    }

    /**
     * Busca chamados por operador
     */
    public List<Chamado> findByOperadorId(String operadorId, String status, String tipo, String prioridade) {
        try {
            Firestore db = getFirestore();
            Query query = db.collection(COLLECTION_NAME).whereEqualTo("operadorId", operadorId);

            ApiFuture<QuerySnapshot> future = query.get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            List<Chamado> chamados = new ArrayList<>();
            for (QueryDocumentSnapshot document : documents) {
                try {
                    Chamado chamado = document.toObject(Chamado.class);
                    if (chamado != null) {
                        chamado.setId(document.getId());

                        // Aplicar filtros em memória
                        boolean incluir = true;

                        if (status != null && !status.isEmpty() && !status.equalsIgnoreCase(chamado.getStatus())) {
                            incluir = false;
                        }

                        if (tipo != null && !tipo.isEmpty() && !tipo.equalsIgnoreCase(chamado.getTipo())) {
                            incluir = false;
                        }

                        if (prioridade != null && !prioridade.isEmpty() && !prioridade.equalsIgnoreCase(chamado.getPrioridade())) {
                            incluir = false;
                        }

                        if (incluir) {
                            chamados.add(chamado);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("❌ Erro ao deserializar documento de chamado " + document.getId() + ": " + e.getMessage());
                }
            }

            // Ordenar por dataHoraRegistro (mais recente primeiro)
            chamados.sort((c1, c2) -> {
                if (c1.getDataHoraRegistro() == null) return 1;
                if (c2.getDataHoraRegistro() == null) return -1;
                return c2.getDataHoraRegistro().compareTo(c1.getDataHoraRegistro());
            });

            return chamados;

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Repository: Erro ao buscar chamados do operador: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar chamados", e);
        }
    }

    /**
     * Busca chamados por proprietário (admin)
     */
    public List<Chamado> findByProprietarioId(String proprietarioId, String status, String tipo, String prioridade) {
        try {
            Firestore db = getFirestore();
            Query query = db.collection(COLLECTION_NAME).whereEqualTo("proprietarioId", proprietarioId);

            ApiFuture<QuerySnapshot> future = query.get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            List<Chamado> chamados = new ArrayList<>();
            for (QueryDocumentSnapshot document : documents) {
                try {
                    Chamado chamado = document.toObject(Chamado.class);
                    if (chamado != null) {
                        chamado.setId(document.getId());

                        // Aplicar filtros em memória
                        boolean incluir = true;

                        if (status != null && !status.isEmpty() && !status.equalsIgnoreCase(chamado.getStatus())) {
                            incluir = false;
                        }

                        if (tipo != null && !tipo.isEmpty() && !tipo.equalsIgnoreCase(chamado.getTipo())) {
                            incluir = false;
                        }

                        if (prioridade != null && !prioridade.isEmpty() && !prioridade.equalsIgnoreCase(chamado.getPrioridade())) {
                            incluir = false;
                        }

                        if (incluir) {
                            chamados.add(chamado);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("❌ Erro ao deserializar documento de chamado " + document.getId() + ": " + e.getMessage());
                }
            }

            // Ordenar por dataHoraRegistro (mais recente primeiro)
            chamados.sort((c1, c2) -> {
                if (c1.getDataHoraRegistro() == null) return 1;
                if (c2.getDataHoraRegistro() == null) return -1;
                return c2.getDataHoraRegistro().compareTo(c1.getDataHoraRegistro());
            });

            return chamados;

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Repository: Erro ao buscar chamados do proprietário: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar chamados", e);
        }
    }

    /**
     * Busca todos os chamados (admin)
     */
    public List<Chamado> findAll(String status, String tipo, String prioridade) {
        try {
            Firestore db = getFirestore();
            ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME).get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            List<Chamado> chamados = new ArrayList<>();
            for (QueryDocumentSnapshot document : documents) {
                try {
                    Chamado chamado = document.toObject(Chamado.class);
                    if (chamado != null) {
                        chamado.setId(document.getId());

                        // Aplicar filtros em memória
                        boolean incluir = true;

                        if (status != null && !status.isEmpty() && !status.equalsIgnoreCase(chamado.getStatus())) {
                            incluir = false;
                        }

                        if (tipo != null && !tipo.isEmpty() && !tipo.equalsIgnoreCase(chamado.getTipo())) {
                            incluir = false;
                        }

                        if (prioridade != null && !prioridade.isEmpty() && !prioridade.equalsIgnoreCase(chamado.getPrioridade())) {
                            incluir = false;
                        }

                        if (incluir) {
                            chamados.add(chamado);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("❌ Erro ao deserializar documento de chamado " + document.getId() + ": " + e.getMessage());
                }
            }

            // Ordenar por dataHoraRegistro (mais recente primeiro)
            chamados.sort((c1, c2) -> {
                if (c1.getDataHoraRegistro() == null) return 1;
                if (c2.getDataHoraRegistro() == null) return -1;
                return c2.getDataHoraRegistro().compareTo(c1.getDataHoraRegistro());
            });

            return chamados;

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Repository: Erro ao buscar todos os chamados: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar chamados", e);
        }
    }

    /**
     * Atualiza um chamado
     */
    public void updateChamado(String id, Chamado chamado) {
        try {
            Firestore db = getFirestore();
            Map<String, Object> updates = new HashMap<>();
            
            if (chamado.getStatus() != null) updates.put("status", chamado.getStatus());
            if (chamado.getResponsavelId() != null) updates.put("responsavelId", chamado.getResponsavelId());
            if (chamado.getResponsavelNome() != null) updates.put("responsavelNome", chamado.getResponsavelNome());
            if (chamado.getPrioridade() != null) updates.put("prioridade", chamado.getPrioridade());
            if (chamado.getObservacoes() != null) updates.put("observacoes", chamado.getObservacoes());
            if (chamado.getFotos() != null) updates.put("fotos", chamado.getFotos());
            
            updates.put("ultimaAtualizacao", Timestamp.now());
            updates.put("updatedAt", Timestamp.now());

            db.collection(COLLECTION_NAME).document(id).update(updates).get();
            System.out.println("✅ Repository: Chamado atualizado - ID: " + id);

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Repository: Erro ao atualizar chamado: " + e.getMessage());
            throw new RuntimeException("Erro ao atualizar chamado", e);
        }
    }

    /**
     * Adiciona uma foto ao chamado
     */
    public void adicionarFoto(String id, String fotoUrl) {
        try {
            Firestore db = getFirestore();
            DocumentReference docRef = db.collection(COLLECTION_NAME).document(id);
            
            docRef.update(
                "fotos", FieldValue.arrayUnion(fotoUrl),
                "ultimaAtualizacao", Timestamp.now(),
                "updatedAt", Timestamp.now()
            ).get();

            System.out.println("✅ Repository: Foto adicionada ao chamado - ID: " + id);

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Repository: Erro ao adicionar foto: " + e.getMessage());
            throw new RuntimeException("Erro ao adicionar foto", e);
        }
    }

    /**
     * Deleta um chamado
     */
    public void deleteChamado(String id) {
        try {
            Firestore db = getFirestore();
            db.collection(COLLECTION_NAME).document(id).delete().get();
            System.out.println("✅ Repository: Chamado deletado - ID: " + id);

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Repository: Erro ao deletar chamado: " + e.getMessage());
            throw new RuntimeException("Erro ao deletar chamado", e);
        }
    }

    /**
     * Converte Chamado para Map (para Firestore)
     */
    private Map<String, Object> convertToMap(Chamado chamado) {
        Map<String, Object> map = new HashMap<>();
        
        if (chamado.getOperadorId() != null) map.put("operadorId", chamado.getOperadorId());
        if (chamado.getOperadorNome() != null) map.put("operadorNome", chamado.getOperadorNome());
        if (chamado.getUserId() != null) map.put("userId", chamado.getUserId());
        if (chamado.getTitulo() != null) map.put("titulo", chamado.getTitulo());
        if (chamado.getDescricao() != null) map.put("descricao", chamado.getDescricao());
        if (chamado.getTipo() != null) map.put("tipo", chamado.getTipo());
        if (chamado.getPrioridade() != null) map.put("prioridade", chamado.getPrioridade());
        if (chamado.getStatus() != null) map.put("status", chamado.getStatus());
        if (chamado.getDataHoraRegistro() != null) map.put("dataHoraRegistro", chamado.getDataHoraRegistro());
        if (chamado.getDataHoraEnvio() != null) map.put("dataHoraEnvio", chamado.getDataHoraEnvio());
        if (chamado.getProprietarioId() != null) map.put("proprietarioId", chamado.getProprietarioId());
        if (chamado.getLocalizacao() != null) map.put("localizacao", chamado.getLocalizacao());
        if (chamado.getFotos() != null) map.put("fotos", chamado.getFotos());
        if (chamado.getFazendaId() != null) map.put("fazendaId", chamado.getFazendaId());
        if (chamado.getFazendaNome() != null) map.put("fazendaNome", chamado.getFazendaNome());
        if (chamado.getTalhaoId() != null) map.put("talhaoId", chamado.getTalhaoId());
        if (chamado.getTalhaoNome() != null) map.put("talhaoNome", chamado.getTalhaoNome());
        if (chamado.getMaquinaId() != null) map.put("maquinaId", chamado.getMaquinaId());
        if (chamado.getMaquinaNome() != null) map.put("maquinaNome", chamado.getMaquinaNome());
        if (chamado.getResponsavelId() != null) map.put("responsavelId", chamado.getResponsavelId());
        if (chamado.getResponsavelNome() != null) map.put("responsavelNome", chamado.getResponsavelNome());
        if (chamado.getObservacoes() != null) map.put("observacoes", chamado.getObservacoes());
        if (chamado.getSincronizado() != null) map.put("sincronizado", chamado.getSincronizado());
        if (chamado.getDataCriacao() != null) map.put("dataCriacao", chamado.getDataCriacao());
        if (chamado.getUltimaAtualizacao() != null) map.put("ultimaAtualizacao", chamado.getUltimaAtualizacao());
        if (chamado.getUpdatedAt() != null) map.put("updatedAt", chamado.getUpdatedAt());
        
        return map;
    }

    /**
     * Remove campos nulos do Map
     */
    private void removeNullFields(Map<String, Object> map) {
        map.entrySet().removeIf(entry -> entry.getValue() == null);
    }
}

