package com.tuorg.veterinaria.reportes.service;

import com.tuorg.veterinaria.reportes.model.Estadistica;
import com.tuorg.veterinaria.reportes.repository.EstadisticaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Servicio para la gestión de estadísticas.
 * 
 * Este servicio proporciona métodos para calcular y actualizar
 * estadísticas del sistema.
 * 
 * @author Equipo de Desarrollo
 * @version 1.0.0
 */
@Service
public class EstadisticaService {

    /**
     * Repositorio de estadísticas.
     */
    private final EstadisticaRepository estadisticaRepository;

    /**
     * Constructor con inyección de dependencias.
     * 
     * @param estadisticaRepository Repositorio de estadísticas
     */
    @Autowired
    public EstadisticaService(EstadisticaRepository estadisticaRepository) {
        this.estadisticaRepository = estadisticaRepository;
    }

    /**
     * Calcula una estadística.
     * 
     * @param nombre Nombre de la estadística
     * @param periodoInicio Fecha de inicio del período
     * @param periodoFin Fecha de fin del período
     * @return Estadistica calculada
     */
    @Transactional
    public Estadistica calcular(String nombre, LocalDate periodoInicio, LocalDate periodoFin) {
        BigDecimal valorCalculado = calcularValorSegunNombre(nombre, periodoInicio, periodoFin);

        Estadistica estadistica = new Estadistica();
        estadistica.setNombre(nombre);
        estadistica.setValor(valorCalculado);
        estadistica.setPeriodoInicio(periodoInicio);
        estadistica.setPeriodoFin(periodoFin);
        
        return estadisticaRepository.save(estadistica);
    }

    /**
     * Calcula estadísticas para un reporte (usado por Facade).
     * 
     * @param tipoReporte Tipo de reporte
     * @param parametros Parámetros del reporte
     * @return Lista de estadísticas calculadas
     */
    @Transactional
    public List<Estadistica> calcularEstadisticasParaReporte(String tipoReporte, Map<String, Object> parametros) {
        List<Estadistica> estadisticas = new ArrayList<>();

        LocalDate periodoInicio = extraerFecha(parametros, "fechaInicio");
        LocalDate periodoFin = extraerFecha(parametros, "fechaFin");

        if (periodoInicio == null) {
            periodoInicio = LocalDate.now().minusDays(30);
        }
        if (periodoFin == null) {
            periodoFin = LocalDate.now();
        }

        Estadistica diasPeriodo = new Estadistica();
        diasPeriodo.setNombre("DIAS_PERIODO");
        diasPeriodo.setValor(BigDecimal.valueOf(Math.max(0, ChronoUnit.DAYS.between(periodoInicio, periodoFin) + 1)));
        diasPeriodo.setPeriodoInicio(periodoInicio);
        diasPeriodo.setPeriodoFin(periodoFin);
        estadisticas.add(diasPeriodo);

        List<Estadistica> historicasTipo = estadisticaRepository.findByNombre(tipoReporte);
        BigDecimal acumulado = historicasTipo.stream()
                .map(Estadistica::getValor)
                .filter(valor -> valor != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Estadistica acumuladoHistorico = new Estadistica();
        acumuladoHistorico.setNombre("ACUMULADO_HISTORICO_" + tipoReporte);
        acumuladoHistorico.setValor(acumulado);
        acumuladoHistorico.setPeriodoInicio(periodoInicio);
        acumuladoHistorico.setPeriodoFin(periodoFin);
        estadisticas.add(acumuladoHistorico);

        return estadisticas;
    }

    private BigDecimal calcularValorSegunNombre(String nombre, LocalDate periodoInicio, LocalDate periodoFin) {
        if (periodoInicio == null || periodoFin == null || periodoFin.isBefore(periodoInicio)) {
            return BigDecimal.ZERO;
        }

        if ("DIAS_PERIODO".equalsIgnoreCase(nombre)) {
            return BigDecimal.valueOf(ChronoUnit.DAYS.between(periodoInicio, periodoFin) + 1);
        }

        List<Estadistica> historicas = estadisticaRepository.findByNombre(nombre);
        if (historicas.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return historicas.stream()
                .map(Estadistica::getValor)
                .filter(valor -> valor != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private LocalDate extraerFecha(Map<String, Object> parametros, String clave) {
        if (parametros == null || !parametros.containsKey(clave) || parametros.get(clave) == null) {
            return null;
        }
        try {
            return LocalDate.parse(String.valueOf(parametros.get(clave)));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Actualiza una estadística existente.
     * 
     * @param estadisticaId ID de la estadística
     * @param nuevoValor Nuevo valor de la estadística
     * @return Estadistica actualizada
     */
    @Transactional
    public Estadistica actualizar(Long estadisticaId, BigDecimal nuevoValor) {
        Estadistica estadistica = estadisticaRepository.findById(estadisticaId)
                .orElseThrow(() -> new RuntimeException("Estadística no encontrada"));
        
        estadistica.setValor(nuevoValor);
        return estadisticaRepository.save(estadistica);
    }

    /**
     * Obtiene todas las estadísticas.
     * 
     * @return Lista de estadísticas
     */
    @Transactional(readOnly = true)
    public List<Estadistica> obtenerTodas() {
        return estadisticaRepository.findAll();
    }
}

