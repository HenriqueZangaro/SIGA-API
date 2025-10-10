package com.siga.service;

import com.siga.model.Proprietario;
import com.siga.repository.ProprietarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProprietarioService {

    private final ProprietarioRepository proprietarioRepository;

    @Autowired
    public ProprietarioService(ProprietarioRepository proprietarioRepository) {
        this.proprietarioRepository = proprietarioRepository;
    }

    public List<Proprietario> buscarTodos() {
        System.out.println("🔍 Service: Buscando todos os proprietários...");
        return proprietarioRepository.findAll();
    }

    public Proprietario buscarPorId(String id) {
        System.out.println("🔍 Service: Buscando proprietário por ID: " + id);
        
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID não pode ser vazio");
        }
        
        Proprietario proprietario = proprietarioRepository.findById(id);
        
        if (proprietario == null) {
            throw new RuntimeException("Proprietário não encontrado com ID: " + id);
        }
        
        return proprietario;
    }

    public Proprietario buscarPorDocumento(String documento) {
        System.out.println("🔍 Service: Buscando proprietário por documento: " + documento);
        
        if (documento == null || documento.trim().isEmpty()) {
            throw new IllegalArgumentException("Documento não pode ser vazio");
        }
        
        Proprietario proprietario = proprietarioRepository.findByDocumento(documento);
        
        if (proprietario == null) {
            throw new RuntimeException("Proprietário não encontrado com documento: " + documento);
        }
        
        return proprietario;
    }
}