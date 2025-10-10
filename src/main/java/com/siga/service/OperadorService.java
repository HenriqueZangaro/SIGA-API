package com.siga.service;

import com.siga.model.Operador;
import com.siga.repository.OperadorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OperadorService {

    private final OperadorRepository operadorRepository;

    @Autowired
    public OperadorService(OperadorRepository operadorRepository) {
        this.operadorRepository = operadorRepository;
    }

    public List<Operador> buscarTodas() {
        System.out.println("🔍 Service: Buscando todos os operadores...");
        return operadorRepository.findAll();
    }

    public Operador buscarPorId(String id) {
        System.out.println("🔍 Service: Buscando operador por ID: " + id);
        
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID não pode ser vazio");
        }
        
        Operador operador = operadorRepository.findById(id);
        
        if (operador == null) {
            throw new RuntimeException("Operador não encontrado com ID: " + id);
        }
        
        return operador;
    }

    public List<Operador> buscarPorFazendaId(String fazendaId) {
        System.out.println("🔍 Service: Buscando operadores da fazenda: " + fazendaId);
        
        if (fazendaId == null || fazendaId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID da fazenda não pode ser vazio");
        }
        
        List<Operador> operadores = operadorRepository.findByFazendaId(fazendaId);
        
        System.out.println("✅ Service: Encontrados " + operadores.size() + " operadores para fazenda " + fazendaId);
        
        return operadores;
    }
}