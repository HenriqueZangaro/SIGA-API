package com.siga.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.google.cloud.Timestamp;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Trabalho {
    @JsonProperty("id")
    private String id;
    @JsonProperty("horarioInicio")
    private String horarioInicio;
    @JsonProperty("horarioFim")
    private String horarioFim;
    @JsonProperty("paradas")
    private List<Object> paradas;
    @JsonProperty("ultimaAtualizacao")
    private Timestamp ultimaAtualizacao;
    @JsonProperty("horasJanta")
    private Long horasJanta;
    @JsonProperty("dataFim")
    private Timestamp dataFim;
    @JsonProperty("unidadeDose")
    private String unidadeDose;
    @JsonProperty("safraId")
    private String safraId;
    @JsonProperty("horasTrabalhadasInformadas")
    private Double horasTrabalhadasInformadas;
    @JsonProperty("horarioAlmocoFim")
    private String horarioAlmocoFim;
    @JsonProperty("fazendaNome")
    private String fazendaNome;
    @JsonProperty("safraNome")
    private String safraNome;
    @JsonProperty("horasParadaComMaquina")
    private Long horasParadaComMaquina;
    @JsonProperty("operadorNome")
    private String operadorNome;
    @JsonProperty("talhaoNome")
    private String talhaoNome;
    @JsonProperty("horarioJantaFim")
    private String horarioJantaFim;
    @JsonProperty("dataInicio")
    private Timestamp dataInicio;
    @JsonProperty("horasTrabalhadas")
    private Double horasTrabalhadas;
    @JsonProperty("dataCadastro")
    private Timestamp dataCadastro;
    @JsonProperty("horasAlmoco")
    private Long horasAlmoco;
    @JsonProperty("areaOperada")
    private Long areaOperada;
    @JsonProperty("produtoTipo")
    private String produtoTipo;
    @JsonProperty("horasOperador")
    private Double horasOperador;
    @JsonProperty("horarioAlmocoInicio")
    private String horarioAlmocoInicio;
    @JsonProperty("descricaoOutro")
    private String descricaoOutro;
    @JsonProperty("produtoId")
    private String produtoId;
    @JsonProperty("produtoNome")
    private String produtoNome;
    @JsonProperty("fazendaId")
    private String fazendaId;
    @JsonProperty("proprietarioId")
    private String proprietarioId;
    @JsonProperty("talhaoId")
    private String talhaoId;
    @JsonProperty("maquinaId")
    private String maquinaId;
    @JsonProperty("maquinaNome")
    private String maquinaNome;
    @JsonProperty("operadorId")
    private String operadorId;
    @JsonProperty("horasParadaSemMaquina")
    private Long horasParadaSemMaquina;
    @JsonProperty("doseAplicada")
    private String doseAplicada;
    @JsonProperty("produtoAplicado")
    private String produtoAplicado;
    @JsonProperty("quantidadeTotal")
    private Long quantidadeTotal;
    @JsonProperty("horasOperadorBruto")
    private Double horasOperadorBruto;
    @JsonProperty("tipoTrabalho")
    private String tipoTrabalho;
    @JsonProperty("status")
    private String status;
    @JsonProperty("horarioJantaInicio")
    private String horarioJantaInicio;
}