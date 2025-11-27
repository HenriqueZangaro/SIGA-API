package com.siga.service;

import com.siga.model.Safra;
import com.siga.repository.SafraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SafraService {

    private final SafraRepository safraRepository;

    @Autowired
    public SafraService(SafraRepository safraRepository) {
        this.safraRepository = safraRepository;
    }

    public List<Safra> buscarTodas() {
        System.out.println("🔍 Service: Buscando todas as safras...");
        return safraRepository.findAll();
    }

    public Safra buscarPorId(String id) {
        System.out.println("🔍 Service: Buscando safra por ID: " + id);
        
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID não pode ser vazio");
        }
        
        Safra safra = safraRepository.findById(id);
        
        if (safra == null) {
            throw new RuntimeException("Safra não encontrada com ID: " + id);
        }
        
        return safra;
    }

    public List<Safra> buscarPorFazendaId(String fazendaId) {
        System.out.println("🔍 Service: Buscando safras da fazenda: " + fazendaId);
        
        if (fazendaId == null || fazendaId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID da fazenda não pode ser vazio");
        }
        
        List<Safra> safras = safraRepository.findByFazendaId(fazendaId);
        
        System.out.println("✅ Service: Encontradas " + safras.size() + " safras para fazenda " + fazendaId);
        
        return safras;
    }

    /**
     * Busca safras por proprietarioId (filtro de segurança)
     */
    public List<Safra> buscarPorProprietarioId(String proprietarioId) {
        System.out.println("🔍 Service: Buscando safras do proprietário: " + proprietarioId);
        
        if (proprietarioId == null || proprietarioId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID do proprietário não pode ser vazio");
        }
        
        return safraRepository.findByProprietarioId(proprietarioId);
    }
}