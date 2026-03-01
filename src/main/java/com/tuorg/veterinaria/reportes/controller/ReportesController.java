package com.tuorg.veterinaria.reportes.controller;

import com.tuorg.veterinaria.common.dto.ApiResponse;
import com.tuorg.veterinaria.reportes.dto.ReporteActividadesResponse;
import com.tuorg.veterinaria.reportes.dto.ReporteCitasResponse;
import com.tuorg.veterinaria.reportes.dto.ReporteFacturacionResponse;
import com.tuorg.veterinaria.reportes.service.ReportesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Controlador REST para la generación de reportes.
 * 
 * Expone endpoints para generar reportes de diferentes aspectos
 * de la clínica veterinaria: citas, facturación y actividades.
 * 
 * @author Equipo de Desarrollo
 * @version 1.0.0
 */
@RestController
@RequestMapping("/reportes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reportes", description = "API para generación de reportes y estadísticas")
public class ReportesController {

    private final ReportesService reportesService;

    /**
     * Genera reporte de citas para un período específico.
     * 
     * @param fechaInicio Fecha de inicio (formato: yyyy-MM-dd)
     * @param fechaFin Fecha de fin (formato: yyyy-MM-dd)
     * @return Reporte con estadísticas de citas
     */
    @GetMapping("/citas")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIO')")
    @Operation(summary = "Reporte de citas", description = "Genera estadísticas de citas para un período")
    public ResponseEntity<ApiResponse<ReporteCitasResponse>> reporteCitas(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaInicio,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaFin) {

        log.info("📊 Generando reporte de citas: {} a {}", fechaInicio, fechaFin);

        ReporteCitasResponse reporte = reportesService.reporteCitas(fechaInicio, fechaFin);

        return ResponseEntity.ok(ApiResponse.success("Reporte de citas generado exitosamente", reporte));
    }

    /**
     * Genera reporte de facturación para un período específico.
     * 
     * @param fechaInicio Fecha de inicio (formato: yyyy-MM-dd)
     * @param fechaFin Fecha de fin (formato: yyyy-MM-dd)
     * @return Reporte con estadísticas de facturación
     */
    @GetMapping("/facturacion")
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARIO')")
    @Operation(summary = "Reporte de facturación", description = "Genera estadísticas de ingresos y facturas")
    public ResponseEntity<ApiResponse<ReporteFacturacionResponse>> reporteFacturacion(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaInicio,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaFin) {

        log.info("💰 Generando reporte de facturación: {} a {}", fechaInicio, fechaFin);

        ReporteFacturacionResponse reporte = reportesService.reporteFacturacion(fechaInicio, fechaFin);

        return ResponseEntity.ok(ApiResponse.success("Reporte de facturación generado exitosamente", reporte));
    }

    /**
     * Genera reporte de actividades veterinarias para un período específico.
     * 
     * @param fechaInicio Fecha de inicio (formato: yyyy-MM-dd)
     * @param fechaFin Fecha de fin (formato: yyyy-MM-dd)
     * @return Reporte con estadísticas de actividades
     */
    @GetMapping("/actividades")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIO')")
    @Operation(summary = "Reporte de actividades", description = "Genera estadísticas de servicios realizados")
    public ResponseEntity<ApiResponse<ReporteActividadesResponse>> reporteActividades(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaInicio,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaFin) {

        log.info("🏥 Generando reporte de actividades: {} a {}", fechaInicio, fechaFin);

        ReporteActividadesResponse reporte = reportesService.reporteActividades(fechaInicio, fechaFin);

        return ResponseEntity.ok(ApiResponse.success("Reporte de actividades generado exitosamente", reporte));
    }

    /**
     * Obtiene resumen rápido de los últimos 30 días.
     * 
     * @return Reporte del período últimos 30 días
     */
    @GetMapping("/citas/resumen/ultimos-30-dias")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIO')")
    @Operation(summary = "Resumen últimos 30 días", description = "Obtiene estadísticas rápidas de los últimos 30 días")
    public ResponseEntity<ApiResponse<ReporteCitasResponse>> reporteUltimos30Dias() {

        log.info("📊 Generando reporte de últimos 30 días");

        ReporteCitasResponse reporte = reportesService.reporteUltimos30Dias();

        return ResponseEntity.ok(ApiResponse.success("Reporte de últimos 30 días generado exitosamente", reporte));
    }

    /**
     * Obtiene resumen rápido del mes actual.
     * 
     * @return Reporte del mes en curso
     */
    @GetMapping("/citas/resumen/mes-actual")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIO')")
    @Operation(summary = "Resumen del mes actual", description = "Obtiene estadísticas del mes en curso")
    public ResponseEntity<ApiResponse<ReporteCitasResponse>> reporteMesActual() {

        log.info("📊 Generando reporte del mes actual");

        ReporteCitasResponse reporte = reportesService.reporteMesActual();

        return ResponseEntity.ok(ApiResponse.success("Reporte del mes actual generado exitosamente", reporte));
    }

    /**
     * Obtiene resumen rápido del año actual.
     * 
     * @return Reporte del año en curso
     */
    @GetMapping("/citas/resumen/ano-actual")
    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIO')")
    @Operation(summary = "Resumen del año actual", description = "Obtiene estadísticas del año en curso")
    public ResponseEntity<ApiResponse<ReporteCitasResponse>> reporteAnoActual() {

        log.info("📊 Generando reporte del año actual");

        ReporteCitasResponse reporte = reportesService.reporteAnoActual();

        return ResponseEntity.ok(ApiResponse.success("Reporte del año actual generado exitosamente", reporte));
    }
}
