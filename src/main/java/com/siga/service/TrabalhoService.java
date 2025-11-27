package com.siga.service;

import com.siga.model.Fazenda;
import com.siga.model.Trabalho;
import com.siga.repository.FazendaRepository;
import com.siga.repository.TrabalhoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class TrabalhoService {

    private final TrabalhoRepository trabalhoRepository;
    private final FazendaRepository fazendaRepository;

    @Autowired
    public TrabalhoService(TrabalhoRepository trabalhoRepository, FazendaRepository fazendaRepository) {
        this.trabalhoRepository = trabalhoRepository;
        this.fazendaRepository = fazendaRepository;
    }

    public List<Trabalho> buscarTodas() {
        System.out.println("🔍 Service: Buscando todos os trabalhos...");
        return trabalhoRepository.findAll();
    }

    public Trabalho buscarPorId(String id) {
        System.out.println("🔍 Service: Buscando trabalho por ID: " + id);
        
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID não pode ser vazio");
        }
        
        Trabalho trabalho = trabalhoRepository.findById(id);
        
        if (trabalho == null) {
            throw new RuntimeException("Trabalho não encontrado com ID: " + id);
        }
        
        return trabalho;
    }

    public List<Trabalho> buscarPorFazendaId(String fazendaId) {
        System.out.println("🔍 Service: Buscando trabalhos da fazenda: " + fazendaId);
        
        if (fazendaId == null || fazendaId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID da fazenda não pode ser vazio");
        }
        
        List<Trabalho> trabalhos = trabalhoRepository.findByFazendaId(fazendaId);
        
        System.out.println("✅ Service: Encontrados " + trabalhos.size() + " trabalhos para fazenda " + fazendaId);
        
        return trabalhos;
    }

    public List<Trabalho> buscarPorTalhaoId(String talhaoId) {
        System.out.println("🔍 Service: Buscando trabalhos do talhão: " + talhaoId);
        
        if (talhaoId == null || talhaoId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID do talhão não pode ser vazio");
        }
        
        List<Trabalho> trabalhos = trabalhoRepository.findByTalhaoId(talhaoId);
        
        System.out.println("✅ Service: Encontrados " + trabalhos.size() + " trabalhos para talhão " + talhaoId);
        
        return trabalhos;
    }

    public List<Trabalho> buscarPorMaquinaId(String maquinaId) {
        System.out.println("🔍 Service: Buscando trabalhos da máquina: " + maquinaId);
        
        if (maquinaId == null || maquinaId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID da máquina não pode ser vazio");
        }
        
        List<Trabalho> trabalhos = trabalhoRepository.findByMaquinaId(maquinaId);
        
        System.out.println("✅ Service: Encontrados " + trabalhos.size() + " trabalhos para máquina " + maquinaId);
        
        return trabalhos;
    }

    public List<Trabalho> buscarPorOperadorId(String operadorId) {
        System.out.println("🔍 Service: Buscando trabalhos do operador: " + operadorId);
        
        if (operadorId == null || operadorId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID do operador não pode ser vazio");
        }
        
        List<Trabalho> trabalhos = trabalhoRepository.findByOperadorId(operadorId);
        
        System.out.println("✅ Service: Encontrados " + trabalhos.size() + " trabalhos para operador " + operadorId);
        
        return trabalhos;
    }

    public List<Trabalho> buscarPorSafraId(String safraId) {
        System.out.println("🔍 Service: Buscando trabalhos da safra: " + safraId);
        
        if (safraId == null || safraId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID da safra não pode ser vazio");
        }
        
        List<Trabalho> trabalhos = trabalhoRepository.findBySafraId(safraId);
        
        System.out.println("✅ Service: Encontrados " + trabalhos.size() + " trabalhos para safra " + safraId);
        
        return trabalhos;
    }

    /**
     * Busca trabalhos por proprietarioId (filtro de segurança)
     * Como trabalhos estão relacionados a fazendas, busca através das fazendas do proprietário
     */
    public List<Trabalho> buscarPorProprietarioId(String proprietarioId) {
        System.out.println("🔍 Service: Buscando trabalhos do proprietário: " + proprietarioId);
        
        if (proprietarioId == null || proprietarioId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID do proprietário não pode ser vazio");
        }
        
        // 1. Buscar fazendas do proprietário
        List<Fazenda> fazendas = fazendaRepository.findByProprietarioId(proprietarioId);
        
        if (fazendas.isEmpty()) {
            System.out.println("⚠️ Service: Nenhuma fazenda encontrada para o proprietário " + proprietarioId);
            return Collections.emptyList();
        }
        
        // 2. Extrair IDs das fazendas
        List<String> fazendaIds = fazendas.stream()
                .map(Fazenda::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        
        System.out.println("   📋 Fazendas encontradas: " + fazendas.size());
        System.out.println("   📋 IDs: " + fazendaIds);
        
        // 3. Buscar trabalhos das fazendas
        System.out.println("🔍 Repository: Buscando trabalhos de " + fazendaIds.size() + " fazendas");
        List<Trabalho> trabalhos = trabalhoRepository.findByFazendaIdIn(fazendaIds);
        
        System.out.println("✅ Service: Encontrados " + trabalhos.size() + " trabalhos para o proprietário " + proprietarioId);
        return trabalhos;
    }
}