package com.siga.service;

import com.siga.model.Fazenda;
import com.siga.repository.FazendaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FazendaService {

    private final FazendaRepository fazendaRepository;

    @Autowired
    public FazendaService(FazendaRepository fazendaRepository) {
        this.fazendaRepository = fazendaRepository;
    }

    public List<Fazenda> buscarTodas() {
        System.out.println("🔍 Service: Buscando todas as fazendas...");
        return fazendaRepository.findAll();
    }

    public Fazenda buscarPorId(String id) {
        System.out.println("🔍 Service: Buscando fazenda por ID: " + id);
        
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID não pode ser vazio");
        }
        
        Fazenda fazenda = fazendaRepository.findById(id);
        
        if (fazenda == null) {
            throw new RuntimeException("Fazenda não encontrada com ID: " + id);
        }
        
        return fazenda;
    }
}

