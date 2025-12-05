package com.tuorg.veterinaria.prestacioneservicios.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * DTO para recibir los datos médicos al completar una consulta
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompletarConsultaRequest {
    
    private String diagnostico;
    private String tratamiento;
    private SignosVitalesDTO signosVitales;
    private String observaciones;
    private List<MedicamentoDTO> medicamentos;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SignosVitalesDTO {
        private Double peso;
        private Double temperatura;
        private Integer frecuenciaCardiaca;
        private Integer frecuenciaRespiratoria;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MedicamentoDTO {
        private String nombre;
        private String dosis;
        private String frecuencia;
        private Integer duracionDias;
        private java.math.BigDecimal costo;
    }
}
