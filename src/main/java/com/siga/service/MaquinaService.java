package com.siga.service;

import com.siga.model.Fazenda;
import com.siga.model.Maquina;
import com.siga.repository.FazendaRepository;
import com.siga.repository.MaquinaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class MaquinaService {

    private final MaquinaRepository maquinaRepository;
    private final FazendaRepository fazendaRepository;

    @Autowired
    public MaquinaService(MaquinaRepository maquinaRepository, FazendaRepository fazendaRepository) {
        this.maquinaRepository = maquinaRepository;
        this.fazendaRepository = fazendaRepository;
    }

    public List<Maquina> buscarTodas() {
        System.out.println("🔍 Service: Buscando todas as máquinas...");
        return maquinaRepository.findAll();
    }

    public Maquina buscarPorId(String id) {
        System.out.println("🔍 Service: Buscando máquina por ID: " + id);
        
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID não pode ser vazio");
        }
        
        Maquina maquina = maquinaRepository.findById(id);
        
        if (maquina == null) {
            throw new RuntimeException("Máquina não encontrada com ID: " + id);
        }
        
        return maquina;
    }

    public List<Maquina> buscarPorFazendaId(String fazendaId) {
        System.out.println("🔍 Service: Buscando máquinas da fazenda: " + fazendaId);
        
        if (fazendaId == null || fazendaId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID da fazenda não pode ser vazio");
        }
        
        List<Maquina> maquinas = maquinaRepository.findByFazendaId(fazendaId);
        
        System.out.println("✅ Service: Encontradas " + maquinas.size() + " máquinas para fazenda " + fazendaId);
        
        return maquinas;
    }

    /**
     * Busca máquinas por proprietarioId (filtro de segurança)
     * Filtra através das fazendas do proprietário, pois máquinas não têm proprietarioId direto
     * Máquinas podem ter múltiplas fazendas (fazendaIds[]), retorna máquinas que pertencem a
     * pelo menos uma fazenda do proprietário
     */
    public List<Maquina> buscarPorProprietarioId(String proprietarioId) {
        System.out.println("🔍 Service: Buscando máquinas do proprietário: " + proprietarioId);
        
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
        
        // 3. Buscar máquinas que pertencem a pelo menos uma dessas fazendas
        // Máquinas podem ter múltiplas fazendas (fazendaIds[])
        List<Maquina> maquinas = maquinaRepository.findByFazendaIdsContainingAny(fazendaIds);
        
        System.out.println("✅ Máquinas encontradas: " + maquinas.size());
        
        return maquinas;
    }
}