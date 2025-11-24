package com.siga.repository;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.siga.model.Ponto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Repository
public class PontoRepository {

    private final Firestore firestore;
    private static final String COLLECTION_NAME = "pontos";

    @Autowired
    public PontoRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    /**
     * Registra um ponto (entrada ou saída)
     */
    public String registrarPonto(Ponto ponto) {
        try {
            // Remover campos null para evitar problemas no Firestore
            Map<String, Object> pontoData = new HashMap<>();
            
            // Campos obrigatórios
            pontoData.put("operadorId", ponto.getOperadorId());
            pontoData.put("operadorNome", ponto.getOperadorNome());
            pontoData.put("userId", ponto.getUserId());
            pontoData.put("tipo", ponto.getTipo());
            pontoData.put("dataHora", ponto.getDataHora() != null ? ponto.getDataHora() : Timestamp.now());
            pontoData.put("proprietarioId", ponto.getProprietarioId());
            pontoData.put("dataCriacao", Timestamp.now());
            
            // Campos opcionais
            if (ponto.getLocalizacao() != null) {
                Map<String, Object> localizacao = new HashMap<>();
                localizacao.put("latitude", ponto.getLocalizacao().getLatitude());
                localizacao.put("longitude", ponto.getLocalizacao().getLongitude());
                if (ponto.getLocalizacao().getAccuracy() != null) {
                    localizacao.put("accuracy", ponto.getLocalizacao().getAccuracy());
                }
                if (ponto.getLocalizacao().getTimestamp() != null) {
                    localizacao.put("timestamp", ponto.getLocalizacao().getTimestamp());
                }
                pontoData.put("localizacao", localizacao);
            }
            
            if (ponto.getFazendaId() != null) {
                pontoData.put("fazendaId", ponto.getFazendaId());
            }
            if (ponto.getFazendaNome() != null) {
                pontoData.put("fazendaNome", ponto.getFazendaNome());
            }
            if (ponto.getObservacao() != null) {
                pontoData.put("observacao", ponto.getObservacao());
            }
            if (ponto.getPontoEntradaId() != null) {
                pontoData.put("pontoEntradaId", ponto.getPontoEntradaId());
            }
            if (ponto.getDuracaoMinutos() != null) {
                pontoData.put("duracaoMinutos", ponto.getDuracaoMinutos());
            }
            if (ponto.getDispositivo() != null) {
                pontoData.put("dispositivo", ponto.getDispositivo());
            }
            if (ponto.getVersaoApp() != null) {
                pontoData.put("versaoApp", ponto.getVersaoApp());
            }
            
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document();
            docRef.set(pontoData).get();
            
            System.out.println("✅ Ponto registrado: " + docRef.getId() + " - Tipo: " + ponto.getTipo());
            return docRef.getId();
            
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Erro ao registrar ponto: " + e.getMessage());
            throw new RuntimeException("Erro ao registrar ponto", e);
        }
    }

    /**
     * Busca ponto por ID
     */
    public Ponto findById(String id) {
        try {
            DocumentSnapshot document = firestore.collection(COLLECTION_NAME)
                    .document(id)
                    .get()
                    .get();
            
            if (document.exists()) {
                try {
                    Ponto ponto = document.toObject(Ponto.class);
                    if (ponto != null) {
                        ponto.setId(document.getId());
                        return ponto;
                    }
                } catch (Exception e) {
                    System.err.println("⚠️ Erro ao deserializar ponto " + id + ": " + e.getMessage());
                    throw new RuntimeException("Erro ao deserializar ponto: " + e.getMessage(), e);
                }
            }
            
            return null;
            
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Erro ao buscar ponto por ID: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar ponto por ID", e);
        }
    }

    /**
     * Busca último ponto de um operador (ordenado por dataHora desc)
     */
    public Ponto findUltimoPontoByOperadorId(String operadorId) {
        try {
            Query query = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("operadorId", operadorId)
                    .orderBy("dataHora", Query.Direction.DESCENDING)
                    .limit(1);
            
            List<QueryDocumentSnapshot> documents = query.get().get().getDocuments();
            
            if (!documents.isEmpty()) {
                try {
                    Ponto ponto = documents.get(0).toObject(Ponto.class);
                    if (ponto != null) {
                        ponto.setId(documents.get(0).getId());
                        return ponto;
                    }
                } catch (Exception e) {
                    System.err.println("⚠️ Erro ao deserializar último ponto: " + e.getMessage());
                    return null;
                }
            }
            
            return null;
            
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Erro ao buscar último ponto: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar último ponto", e);
        }
    }

    /**
     * Busca TODOS os pontos (para admin)
     */
    public List<Ponto> findAll(Date dataInicio, Date dataFim) {
        try {
            Query query = firestore.collection(COLLECTION_NAME);
            
            List<QueryDocumentSnapshot> documents = query.get().get().getDocuments();
            List<Ponto> pontos = new ArrayList<>();
            
            for (QueryDocumentSnapshot document : documents) {
                try {
                    Ponto ponto = document.toObject(Ponto.class);
                    if (ponto != null) {
                        ponto.setId(document.getId());
                        
                        // Filtrar por data em memória (evita necessidade de índice composto)
                        if (dataInicio != null || dataFim != null) {
                            if (ponto.getDataHora() != null) {
                                Date dataPonto = ponto.getDataHora().toDate();
                                
                                if (dataInicio != null && dataPonto.before(dataInicio)) {
                                    continue;
                                }
                                
                                if (dataFim != null) {
                                    Date fimCompleto = new Date(dataFim.getTime() + 86400000); // +1 dia
                                    if (dataPonto.after(fimCompleto)) {
                                        continue;
                                    }
                                }
                            } else {
                                continue;
                            }
                        }
                        
                        pontos.add(ponto);
                    }
                } catch (Exception e) {
                    System.err.println("⚠️ Erro ao deserializar ponto " + document.getId() + ": " + e.getMessage());
                    // Continua para o próximo documento
                    continue;
                }
            }
            
            // Ordenar por dataHora (mais recente primeiro)
            pontos.sort((a, b) -> {
                if (a.getDataHora() == null || b.getDataHora() == null) return 0;
                return b.getDataHora().compareTo(a.getDataHora());
            });
            
            return pontos;
            
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Erro ao buscar todos os pontos: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar todos os pontos", e);
        }
    }

    /**
     * Busca pontos de um operador (todos ou com filtro de data)
     */
    public List<Ponto> findByOperadorId(String operadorId, Date dataInicio, Date dataFim) {
        try {
            Query query = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("operadorId", operadorId);
            
            List<QueryDocumentSnapshot> documents = query.get().get().getDocuments();
            List<Ponto> pontos = new ArrayList<>();
            
            for (QueryDocumentSnapshot document : documents) {
                try {
                    Ponto ponto = document.toObject(Ponto.class);
                    if (ponto != null) {
                        ponto.setId(document.getId());
                        
                        // Filtrar por data em memória (evita necessidade de índice composto)
                        if (dataInicio != null || dataFim != null) {
                            if (ponto.getDataHora() != null) {
                                Date dataPonto = ponto.getDataHora().toDate();
                                
                                if (dataInicio != null && dataPonto.before(dataInicio)) {
                                    continue;
                                }
                                
                                if (dataFim != null) {
                                    Date fimCompleto = new Date(dataFim.getTime() + 86400000); // +1 dia
                                    if (dataPonto.after(fimCompleto)) {
                                        continue;
                                    }
                                }
                            } else {
                                continue;
                            }
                        }
                        
                        pontos.add(ponto);
                    }
                } catch (Exception e) {
                    System.err.println("⚠️ Erro ao deserializar ponto " + document.getId() + ": " + e.getMessage());
                    // Continua para o próximo documento
                    continue;
                }
            }
            
            // Ordenar por dataHora (mais recente primeiro)
            pontos.sort((a, b) -> {
                if (a.getDataHora() == null || b.getDataHora() == null) return 0;
                return b.getDataHora().compareTo(a.getDataHora());
            });
            
            return pontos;
            
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Erro ao buscar pontos do operador: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar pontos do operador", e);
        }
    }

    /**
     * Busca pontos de um proprietário (todos os operadores)
     */
    public List<Ponto> findByProprietarioId(String proprietarioId, Date dataInicio, Date dataFim) {
        try {
            Query query = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("proprietarioId", proprietarioId);
            
            List<QueryDocumentSnapshot> documents = query.get().get().getDocuments();
            List<Ponto> pontos = new ArrayList<>();
            
            for (QueryDocumentSnapshot document : documents) {
                try {
                    Ponto ponto = document.toObject(Ponto.class);
                    if (ponto != null) {
                        ponto.setId(document.getId());
                        
                        // Filtrar por data em memória
                        if (dataInicio != null || dataFim != null) {
                            if (ponto.getDataHora() != null) {
                                Date dataPonto = ponto.getDataHora().toDate();
                                
                                if (dataInicio != null && dataPonto.before(dataInicio)) {
                                    continue;
                                }
                                
                                if (dataFim != null) {
                                    Date fimCompleto = new Date(dataFim.getTime() + 86400000); // +1 dia
                                    if (dataPonto.after(fimCompleto)) {
                                        continue;
                                    }
                                }
                            } else {
                                continue;
                            }
                        }
                        
                        pontos.add(ponto);
                    }
                } catch (Exception e) {
                    System.err.println("⚠️ Erro ao deserializar ponto " + document.getId() + ": " + e.getMessage());
                    // Continua para o próximo documento
                    continue;
                }
            }
            
            // Ordenar por dataHora (mais recente primeiro)
            pontos.sort((a, b) -> {
                if (a.getDataHora() == null || b.getDataHora() == null) return 0;
                return b.getDataHora().compareTo(a.getDataHora());
            });
            
            return pontos;
            
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Erro ao buscar pontos do proprietário: " + e.getMessage());
            throw new RuntimeException("Erro ao buscar pontos do proprietário", e);
        }
    }

    /**
     * Atualiza um ponto
     */
    public void updatePonto(String id, Ponto ponto) {
        try {
            Map<String, Object> updates = new HashMap<>();
            
            if (ponto.getObservacao() != null) {
                updates.put("observacao", ponto.getObservacao());
            }
            if (ponto.getFazendaId() != null) {
                updates.put("fazendaId", ponto.getFazendaId());
            }
            if (ponto.getFazendaNome() != null) {
                updates.put("fazendaNome", ponto.getFazendaNome());
            }
            
            if (!updates.isEmpty()) {
                firestore.collection(COLLECTION_NAME)
                        .document(id)
                        .update(updates)
                        .get();
                
                System.out.println("✅ Ponto atualizado: " + id);
            }
            
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Erro ao atualizar ponto: " + e.getMessage());
            throw new RuntimeException("Erro ao atualizar ponto", e);
        }
    }

    /**
     * Deleta um ponto
     */
    public void deletePonto(String id) {
        try {
            firestore.collection(COLLECTION_NAME)
                    .document(id)
                    .delete()
                    .get();
            
            System.out.println("✅ Ponto deletado: " + id);
            
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Erro ao deletar ponto: " + e.getMessage());
            throw new RuntimeException("Erro ao deletar ponto", e);
        }
    }
}

