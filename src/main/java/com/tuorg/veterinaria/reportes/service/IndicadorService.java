package com.tuorg.veterinaria.reportes.service;

import com.tuorg.veterinaria.common.exception.BusinessException;
import com.tuorg.veterinaria.reportes.model.Estadistica;
import com.tuorg.veterinaria.reportes.model.Indicador;
import com.tuorg.veterinaria.reportes.repository.EstadisticaRepository;
import com.tuorg.veterinaria.reportes.repository.IndicadorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Servicio para la gestión de indicadores.
 * 
 * Este servicio proporciona métodos para evaluar tendencias
 * y gestionar indicadores clave de rendimiento (KPIs).
 * 
 * @author Equipo de Desarrollo
 * @version 1.0.0
 */
@Service
public class IndicadorService {

    /**
     * Repositorio de indicadores.
     */
    private final IndicadorRepository indicadorRepository;

    /**
     * Repositorio de estadísticas para análisis histórico.
     */
    private final EstadisticaRepository estadisticaRepository;

    /**
     * Constructor con inyección de dependencias.
     * 
     * @param indicadorRepository Repositorio de indicadores
     */
    @Autowired
    public IndicadorService(IndicadorRepository indicadorRepository,
                            EstadisticaRepository estadisticaRepository) {
        this.indicadorRepository = indicadorRepository;
        this.estadisticaRepository = estadisticaRepository;
    }

    /**
     * Evalúa la tendencia de un indicador.
     * 
     * @param indicadorId ID del indicador
     * @return String con la tendencia (creciente, decreciente, estable)
     */
    @Transactional(readOnly = true)
    public String evaluarTendencia(Long indicadorId) {
        Indicador indicador = indicadorRepository.findById(indicadorId)
            .orElseThrow(() -> new BusinessException("Indicador no encontrado"));

        BigDecimal valorActual = indicador.getValorActual() != null ? indicador.getValorActual() : BigDecimal.ZERO;
        List<Estadistica> historicas = estadisticaRepository.findByNombre(indicador.getNombre());

        if (historicas.isEmpty()) {
            return "estable";
        }

        BigDecimal promedioHistorico = historicas.stream()
                .map(Estadistica::getValor)
                .filter(valor -> valor != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(historicas.size()), 4, java.math.RoundingMode.HALF_UP);

        int comparacion = valorActual.compareTo(promedioHistorico);
        if (comparacion > 0) {
            return "creciente";
        }
        if (comparacion < 0) {
            return "decreciente";
        }
        return "estable";
    }

    /**
     * Obtiene todos los indicadores.
     * 
     * @return Lista de indicadores
     */
    @Transactional(readOnly = true)
    public List<Indicador> obtenerTodos() {
        return indicadorRepository.findAll();
    }
}

