package com.tuorg.veterinaria.reportes.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuorg.veterinaria.common.exception.BusinessException;
import com.tuorg.veterinaria.reportes.dto.EstadisticaResponse;
import com.tuorg.veterinaria.reportes.dto.ReporteRequest;
import com.tuorg.veterinaria.reportes.dto.ReporteResponse;
import com.tuorg.veterinaria.reportes.model.Estadistica;
import com.tuorg.veterinaria.reportes.model.Reporte;
import com.tuorg.veterinaria.reportes.repository.ReporteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * Servicio para la gestión de reportes (Facade pattern).
 *
 * Coordina la generación de reportes combinando cálculos estadísticos y
 * persistencia.
 */
@SuppressWarnings("null")
@Service
public class ReporteService {

    private final ReporteRepository reporteRepository;
    private final EstadisticaService estadisticaService;
    private final ObjectMapper objectMapper;

    @Autowired
    public ReporteService(ReporteRepository reporteRepository,
            EstadisticaService estadisticaService,
            ObjectMapper objectMapper) {
        this.reporteRepository = reporteRepository;
        this.estadisticaService = estadisticaService;
        this.objectMapper = objectMapper;
    }

    /**
     * Genera un reporte y retorna un DTO listo para ser expuesto en la API.
     */
    @Transactional
    public ReporteResponse generar(ReporteRequest request) {
        Reporte reporte = new Reporte();
        reporte.setNombre(request.getNombre());
        reporte.setTipo(request.getTipo());
        reporte.setGeneradoPor(request.getGeneradoPor());
        reporte.setFechaGeneracion(LocalDateTime.now());
        reporte.setParametros(toJson(request.getParametros()));

        List<Estadistica> estadisticas = estadisticaService.calcularEstadisticasParaReporte(
                request.getTipo(),
                request.getParametros() != null ? request.getParametros() : Collections.emptyMap());

        Reporte guardado = reporteRepository.save(reporte);
        return mapToResponse(guardado, estadisticas);
    }

    /**
     * Exporta un reporte en formato de texto estructurado para descarga.
     */
    @Transactional(readOnly = true)
    public byte[] exportarPDF(Long reporteId) {
        Reporte reporte = reporteRepository.findById(reporteId)
                .orElseThrow(() -> new BusinessException("Reporte no encontrado para exportar a PDF"));

        List<Estadistica> estadisticas = estadisticaService.calcularEstadisticasParaReporte(
                reporte.getTipo(),
                toMap(reporte.getParametros()));

        StringBuilder contenido = new StringBuilder();
        contenido.append("CLINICA VETERINARIA HUMBOLDT\n");
        contenido.append("REPORTE: ").append(reporte.getNombre()).append("\n");
        contenido.append("TIPO: ").append(reporte.getTipo()).append("\n");
        contenido.append("FECHA GENERACION: ").append(reporte.getFechaGeneracion()).append("\n");
        contenido.append("GENERADO POR: ").append(reporte.getGeneradoPor()).append("\n\n");
        contenido.append("ESTADISTICAS\n");

        if (estadisticas.isEmpty()) {
            contenido.append("- Sin datos para el período solicitado\n");
        } else {
            for (Estadistica estadistica : estadisticas) {
                contenido.append("- ")
                        .append(estadistica.getNombre())
                        .append(": ")
                        .append(estadistica.getValor())
                        .append(" (")
                        .append(estadistica.getPeriodoInicio())
                        .append(" a ")
                        .append(estadistica.getPeriodoFin())
                        .append(")\n");
            }
        }

        return contenido.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Exporta un reporte en formato CSV para análisis en hojas de cálculo.
     */
    @Transactional(readOnly = true)
    public byte[] exportarExcel(Long reporteId) {
        Reporte reporte = reporteRepository.findById(reporteId)
                .orElseThrow(() -> new BusinessException("Reporte no encontrado para exportar a Excel"));

        List<Estadistica> estadisticas = estadisticaService.calcularEstadisticasParaReporte(
                reporte.getTipo(),
                toMap(reporte.getParametros()));

        StringJoiner csv = new StringJoiner("\n");
        csv.add("reporte,tipo,fecha_generacion,generado_por,estadistica,valor,periodo_inicio,periodo_fin");

        if (estadisticas.isEmpty()) {
            csv.add(String.format("\"%s\",\"%s\",\"%s\",%d,\"%s\",%s,%s,%s",
                    reporte.getNombre(),
                    reporte.getTipo(),
                    reporte.getFechaGeneracion(),
                    reporte.getGeneradoPor(),
                    "Sin datos",
                    "0",
                    "",
                    ""));
        } else {
            for (Estadistica estadistica : estadisticas) {
                csv.add(String.format("\"%s\",\"%s\",\"%s\",%d,\"%s\",%s,%s,%s",
                        reporte.getNombre(),
                        reporte.getTipo(),
                        reporte.getFechaGeneracion(),
                        reporte.getGeneradoPor(),
                        estadistica.getNombre(),
                        estadistica.getValor(),
                        estadistica.getPeriodoInicio(),
                        estadistica.getPeriodoFin()));
            }
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private ReporteResponse mapToResponse(Reporte reporte, List<Estadistica> estadisticas) {
        return ReporteResponse.builder()
                .id(reporte.getIdReporte())
                .nombre(reporte.getNombre())
                .tipo(reporte.getTipo())
                .fechaGeneracion(reporte.getFechaGeneracion())
                .generadoPor(reporte.getGeneradoPor())
                .parametros(toMap(reporte.getParametros()))
                .estadisticas(estadisticas.stream()
                        .map(this::mapEstadistica)
                        .collect(Collectors.toList()))
                .build();
    }

    private EstadisticaResponse mapEstadistica(Estadistica estadistica) {
        return EstadisticaResponse.builder()
                .id(estadistica.getIdEstadistica())
                .nombre(estadistica.getNombre())
                .valor(estadistica.getValor())
                .periodoInicio(estadistica.getPeriodoInicio())
                .periodoFin(estadistica.getPeriodoFin())
                .build();
    }

    private String toJson(Map<String, Object> parametros) {
        if (parametros == null || parametros.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(parametros);
        } catch (JsonProcessingException e) {
            throw new BusinessException("Los parámetros del reporte no tienen un formato JSON válido");
        }
    }

    private Map<String, Object> toMap(String parametrosJson) {
        if (parametrosJson == null || parametrosJson.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(parametrosJson, new TypeReference<Map<String, Object>>() {
            });
        } catch (JsonProcessingException e) {
            return Collections.emptyMap();
        }
    }
}
