package com.siga.service;

import com.siga.model.Maquina;
import com.siga.repository.MaquinaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MaquinaService {

    private final MaquinaRepository maquinaRepository;

    @Autowired
    public MaquinaService(MaquinaRepository maquinaRepository) {
        this.maquinaRepository = maquinaRepository;
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
}