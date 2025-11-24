package com.siga.service;

import com.google.cloud.Timestamp;
import com.siga.dto.EstatisticasPontosResponse;
import com.siga.dto.StatusOperadorResponse;
import com.siga.model.Ponto;
import com.siga.repository.PontoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PontoService {

    private final PontoRepository pontoRepository;

    @Autowired
    public PontoService(PontoRepository pontoRepository) {
        this.pontoRepository = pontoRepository;
    }

    /**
     * Registra um ponto (entrada ou saída)
     * Calcula duração automaticamente se for saída
     */
    public String registrarPonto(Ponto ponto) {
        System.out.println("🔍 Service: Registrando ponto - Tipo: " + ponto.getTipo() + ", Operador: " + ponto.getOperadorNome());
        
        // Se for saída, calcular duração
        if ("saida".equalsIgnoreCase(ponto.getTipo())) {
            Ponto pontoAberto = verificarPontoAberto(ponto.getOperadorId());
            
            if (pontoAberto == null) {
                throw new RuntimeException("Não há ponto de entrada aberto para registrar saída");
            }
            
            // Vincular à entrada
            ponto.setPontoEntradaId(pontoAberto.getId());
            
            // Calcular duração em minutos
            Timestamp entrada = pontoAberto.getDataHora();
            Timestamp saida = ponto.getDataHora() != null ? ponto.getDataHora() : Timestamp.now();
            
            long duracaoMs = saida.toDate().getTime() - entrada.toDate().getTime();
            int duracaoMinutos = (int) (duracaoMs / 60000);
            
            ponto.setDuracaoMinutos(duracaoMinutos);
            
            System.out.println("✅ Saída vinculada à entrada " + pontoAberto.getId() + " - Duração: " + duracaoMinutos + " minutos");
        }
        
        String pontoId = pontoRepository.registrarPonto(ponto);
        System.out.println("✅ Service: Ponto registrado com ID: " + pontoId);
        
        return pontoId;
    }

    /**
     * Verifica se operador tem ponto aberto (entrada sem saída)
     */
    public Ponto verificarPontoAberto(String operadorId) {
        Ponto ultimoPonto = pontoRepository.findUltimoPontoByOperadorId(operadorId);
        
        if (ultimoPonto != null && "entrada".equalsIgnoreCase(ultimoPonto.getTipo())) {
            System.out.println("✅ Service: Ponto aberto encontrado - ID: " + ultimoPonto.getId());
            return ultimoPonto;
        }
        
        System.out.println("ℹ️ Service: Nenhum ponto aberto para operador " + operadorId);
        return null;
    }

    /**
     * Busca pontos de hoje de um operador
     */
    public List<Ponto> getPontosHoje(String operadorId) {
        Calendar hoje = Calendar.getInstance();
        hoje.set(Calendar.HOUR_OF_DAY, 0);
        hoje.set(Calendar.MINUTE, 0);
        hoje.set(Calendar.SECOND, 0);
        hoje.set(Calendar.MILLISECOND, 0);
        
        Calendar amanha = Calendar.getInstance();
        amanha.setTime(hoje.getTime());
        amanha.add(Calendar.DAY_OF_MONTH, 1);
        
        return pontoRepository.findByOperadorId(operadorId, hoje.getTime(), amanha.getTime());
    }

    /**
     * Busca pontos de um operador com filtro de data
     */
    public List<Ponto> getPontosByOperador(String operadorId, Date dataInicio, Date dataFim) {
        System.out.println("🔍 Service: Buscando pontos do operador: " + operadorId);
        List<Ponto> pontos = pontoRepository.findByOperadorId(operadorId, dataInicio, dataFim);
        System.out.println("✅ Service: Encontrados " + pontos.size() + " pontos");
        return pontos;
    }

    /**
     * Busca TODOS os pontos (para admin)
     */
    public List<Ponto> getTodosPontos(Date dataInicio, Date dataFim) {
        System.out.println("🔍 Service: Buscando TODOS os pontos (admin)");
        List<Ponto> pontos = pontoRepository.findAll(dataInicio, dataFim);
        System.out.println("✅ Service: Encontrados " + pontos.size() + " pontos no total");
        return pontos;
    }

    /**
     * Busca pontos de um proprietário (admin)
     */
    public List<Ponto> getPontosByProprietario(String proprietarioId, Date dataInicio, Date dataFim) {
        System.out.println("🔍 Service: Buscando pontos do proprietário: " + proprietarioId);
        List<Ponto> pontos = pontoRepository.findByProprietarioId(proprietarioId, dataInicio, dataFim);
        System.out.println("✅ Service: Encontrados " + pontos.size() + " pontos");
        return pontos;
    }

    /**
     * Calcula horas trabalhadas hoje
     */
    public double calcularHorasTrabalhadasHoje(String operadorId) {
        List<Ponto> pontosHoje = getPontosHoje(operadorId);
        
        int totalMinutos = 0;
        
        for (Ponto ponto : pontosHoje) {
            if ("saida".equalsIgnoreCase(ponto.getTipo()) && ponto.getDuracaoMinutos() != null) {
                totalMinutos += ponto.getDuracaoMinutos();
            }
        }
        
        return totalMinutos / 60.0;
    }

    /**
     * Retorna status completo do operador
     */
    public StatusOperadorResponse getStatusOperador(String operadorId) {
        System.out.println("🔍 Service: Buscando status do operador: " + operadorId);
        
        Ponto pontoAberto = verificarPontoAberto(operadorId);
        List<Ponto> pontosHoje = getPontosHoje(operadorId);
        double horasTrabalhadasHoje = calcularHorasTrabalhadasHoje(operadorId);
        Ponto ultimoPonto = pontoRepository.findUltimoPontoByOperadorId(operadorId);
        
        StatusOperadorResponse status = new StatusOperadorResponse();
        status.setPontoAberto(pontoAberto);
        status.setPodeRegistrarEntrada(pontoAberto == null);
        status.setPodeRegistrarSaida(pontoAberto != null);
        status.setPontosHoje(pontosHoje);
        status.setHorasTrabalhadasHoje(horasTrabalhadasHoje);
        status.setTotalRegistrosHoje(pontosHoje.size());
        status.setUltimoPonto(ultimoPonto);
        
        System.out.println("✅ Service: Status calculado - Ponto aberto: " + (pontoAberto != null));
        
        return status;
    }

    /**
     * Busca estatísticas de pontos de um operador
     */
    public EstatisticasPontosResponse getEstatisticas(String operadorId, Date dataInicio, Date dataFim) {
        System.out.println("🔍 Service: Calculando estatísticas do operador: " + operadorId);
        
        List<Ponto> pontos = getPontosByOperador(operadorId, dataInicio, dataFim);
        
        int totalPontos = pontos.size();
        int totalEntradas = 0;
        int totalSaidas = 0;
        int totalMinutos = 0;
        Set<String> diasUnicos = new HashSet<>();
        
        for (Ponto ponto : pontos) {
            if ("entrada".equalsIgnoreCase(ponto.getTipo())) {
                totalEntradas++;
            } else if ("saida".equalsIgnoreCase(ponto.getTipo())) {
                totalSaidas++;
                if (ponto.getDuracaoMinutos() != null) {
                    totalMinutos += ponto.getDuracaoMinutos();
                }
            }
            
            // Adicionar dia único
            if (ponto.getDataHora() != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(ponto.getDataHora().toDate());
                String dia = String.format("%04d-%02d-%02d", 
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH) + 1,
                    cal.get(Calendar.DAY_OF_MONTH)
                );
                diasUnicos.add(dia);
            }
        }
        
        double horasTrabalhadas = totalMinutos / 60.0;
        int diasTrabalhados = diasUnicos.size();
        double mediaHorasDia = diasTrabalhados > 0 ? horasTrabalhadas / diasTrabalhados : 0;
        
        EstatisticasPontosResponse estatisticas = new EstatisticasPontosResponse();
        estatisticas.setTotalPontos(totalPontos);
        estatisticas.setTotalEntradas(totalEntradas);
        estatisticas.setTotalSaidas(totalSaidas);
        estatisticas.setHorasTrabalhadas(Math.round(horasTrabalhadas * 100.0) / 100.0);
        estatisticas.setDiasTrabalhados(diasTrabalhados);
        estatisticas.setMediaHorasDia(Math.round(mediaHorasDia * 100.0) / 100.0);
        
        System.out.println("✅ Service: Estatísticas calculadas - " + totalPontos + " pontos, " + horasTrabalhadas + "h trabalhadas");
        
        return estatisticas;
    }

    /**
     * Busca ponto por ID
     */
    public Ponto getPontoById(String id) {
        Ponto ponto = pontoRepository.findById(id);
        
        if (ponto == null) {
            throw new RuntimeException("Ponto não encontrado com ID: " + id);
        }
        
        return ponto;
    }

    /**
     * Atualiza um ponto (admin)
     */
    public void updatePonto(String id, Ponto ponto) {
        System.out.println("🔍 Service: Atualizando ponto: " + id);
        pontoRepository.updatePonto(id, ponto);
        System.out.println("✅ Service: Ponto atualizado");
    }

    /**
     * Deleta um ponto (admin)
     */
    public void deletePonto(String id) {
        System.out.println("🔍 Service: Deletando ponto: " + id);
        pontoRepository.deletePonto(id);
        System.out.println("✅ Service: Ponto deletado");
    }
}

