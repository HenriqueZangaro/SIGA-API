package com.siga.service;

import com.siga.model.Talhao;
import com.siga.repository.TalhaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TalhaoService {

    private final TalhaoRepository talhaoRepository;

    @Autowired
    public TalhaoService(TalhaoRepository talhaoRepository) {
        this.talhaoRepository = talhaoRepository;
    }

    public List<Talhao> buscarTodas() {
        System.out.println("🔍 Service: Buscando todos os talhões...");
        return talhaoRepository.findAll();
    }

    public Talhao buscarPorId(String id) {
        System.out.println("🔍 Service: Buscando talhão por ID: " + id);
        
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID não pode ser vazio");
        }
        
        Talhao talhao = talhaoRepository.findById(id);
        
        if (talhao == null) {
            throw new RuntimeException("Talhão não encontrado com ID: " + id);
        }
        
        return talhao;
    }

    public List<Talhao> buscarPorFazendaId(String fazendaId) {
        System.out.println("🔍 Service: Buscando talhões da fazenda: " + fazendaId);
        
        if (fazendaId == null || fazendaId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID da fazenda não pode ser vazio");
        }
        
        List<Talhao> talhoes = talhaoRepository.findByFazendaId(fazendaId);
        
        System.out.println("✅ Service: Encontrados " + talhoes.size() + " talhões para fazenda " + fazendaId);
        
        return talhoes;
    }
}