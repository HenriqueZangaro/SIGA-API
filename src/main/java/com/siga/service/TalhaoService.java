package com.siga.service;

import com.siga.model.Fazenda;
import com.siga.model.Talhao;
import com.siga.repository.FazendaRepository;
import com.siga.repository.TalhaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class TalhaoService {

    private final TalhaoRepository talhaoRepository;
    private final FazendaRepository fazendaRepository;

    @Autowired
    public TalhaoService(TalhaoRepository talhaoRepository, FazendaRepository fazendaRepository) {
        this.talhaoRepository = talhaoRepository;
        this.fazendaRepository = fazendaRepository;
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

    /**
     * Busca talhões por proprietarioId (filtro de segurança)
     * Filtra através das fazendas do proprietário, pois talhões não têm proprietarioId direto
     */
    public List<Talhao> buscarPorProprietarioId(String proprietarioId) {
        System.out.println("🔍 Service: Buscando talhões do proprietário: " + proprietarioId);
        
        if (proprietarioId == null || proprietarioId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID do proprietário não pode ser vazio");
        }
        
        // 1. Buscar todas as fazendas do proprietário
        List<Fazenda> fazendas = fazendaRepository.findByProprietarioId(proprietarioId);
        
        if (fazendas.isEmpty()) {
            System.out.println("⚠️ Nenhuma fazenda encontrada para o proprietário");
            return Collections.emptyList();
        }
        
        // 2. Extrair IDs das fazendas
        List<String> fazendaIds = fazendas.stream()
                .map(Fazenda::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        
        System.out.println("✅ Fazendas encontradas: " + fazendaIds.size());
        System.out.println("   IDs: " + fazendaIds);
        
        // 3. Buscar talhões que pertencem a essas fazendas
        List<Talhao> talhoes = talhaoRepository.findByFazendaIdIn(fazendaIds);
        
        System.out.println("✅ Talhões encontrados: " + talhoes.size());
        
        return talhoes;
    }
}