package com.tuorg.veterinaria.reportes.service;

import com.tuorg.veterinaria.common.constants.AppConstants;
import com.tuorg.veterinaria.gestionfacturacion.model.Factura;
import com.tuorg.veterinaria.gestionfacturacion.repository.FacturaRepository;
import com.tuorg.veterinaria.gestionpacientes.repository.DesparasitacionRepository;
import com.tuorg.veterinaria.gestionpacientes.repository.PacienteRepository;
import com.tuorg.veterinaria.gestionpacientes.repository.VacunacionRepository;
import com.tuorg.veterinaria.prestacioneservicios.model.Cita;
import com.tuorg.veterinaria.prestacioneservicios.repository.CitaRepository;
import com.tuorg.veterinaria.reportes.dto.ReporteActividadesResponse;
import com.tuorg.veterinaria.reportes.dto.ReporteCitasResponse;
import com.tuorg.veterinaria.reportes.dto.ReporteFacturacionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio para generación de reportes.
 * 
 * Proporciona métodos para consultar estadísticas y generar reportes
 * de diferentes aspectos de la clínica veterinaria.
 * 
 * @author Equipo de Desarrollo
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportesService {

        private final CitaRepository citaRepository;
        private final FacturaRepository facturaRepository;
        private final VacunacionRepository vacunacionRepository;
        private final DesparasitacionRepository desparasitacionRepository;
        private final PacienteRepository pacienteRepository;

        /**
         * Genera reporte de citas para un período específico.
         */
        @Transactional(readOnly = true)
        public ReporteCitasResponse reporteCitas(LocalDate fechaInicio, LocalDate fechaFin) {
                log.info("📊 Generando reporte de citas: {} a {}", fechaInicio, fechaFin);

                List<Cita> citas = citaRepository.findAll().stream()
                                .filter(c -> {
                                        LocalDateTime fechaCita = c.getFechaHora();
                                        return !fechaCita.isBefore(fechaInicio.atStartOfDay()) &&
                                                        !fechaCita.isAfter(fechaFin.plusDays(1).atStartOfDay());
                                })
                                .toList();

                long totalCitas = citas.size();
                long citasCompletadas = citas.stream()
                                .filter(c -> c.getEstado().equals(AppConstants.ESTADO_CITA_REALIZADA))
                                .count();
                long citasCanceladas = citas.stream()
                                .filter(c -> c.getEstado().equals(AppConstants.ESTADO_CITA_CANCELADA))
                                .count();
                long citasEnProceso = citas.stream()
                                .filter(c -> c.getEstado().equals(AppConstants.ESTADO_CITA_PROGRAMADA))
                                .count();

                double tasaCompletitud = totalCitas > 0 ? (double) citasCompletadas / totalCitas * 100 : 0;
                double tasaCancelacion = totalCitas > 0 ? (double) citasCanceladas / totalCitas * 100 : 0;

                long uniquePacientes = citas.stream()
                                .map(c -> c.getPaciente().getIdPaciente())
                                .distinct()
                                .count();
                long uniqueVeterinarios = citas.stream()
                                .map(c -> c.getVeterinario().getIdUsuario())
                                .distinct()
                                .count();

                List<Factura> facturasPeriodo = facturaRepository.findAll().stream()
                                .filter(f -> {
                                        LocalDateTime fechaFactura = f.getFechaEmision();
                                        return !fechaFactura.isBefore(fechaInicio.atStartOfDay()) &&
                                                        !fechaFactura.isAfter(fechaFin.atTime(23, 59, 59));
                                })
                                .toList();

                BigDecimal ingresosTotales = facturasPeriodo.stream()
                                .map(Factura::getTotal)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal ingresoPromedio = totalCitas > 0
                                ? ingresosTotales.divide(BigDecimal.valueOf(totalCitas), 2,
                                                java.math.RoundingMode.HALF_UP)
                                : BigDecimal.ZERO;

                return ReporteCitasResponse.builder()
                                .fechaInicio(fechaInicio)
                                .fechaFin(fechaFin)
                                .totalCitas(totalCitas)
                                .citasCompletadas(citasCompletadas)
                                .citasCanceladas(citasCanceladas)
                                .citasEnProceso(citasEnProceso)
                                .caudanoPacientes(uniquePacientes)
                                .totalVeterinarios(uniqueVeterinarios)
                                .tasaCompletitud(tasaCompletitud)
                                .tasaCancelacion(tasaCancelacion)
                                .ingresosTotales(ingresosTotales)
                                .ingresoPromedio(ingresoPromedio)
                                .build();
        }

        /**
         * Genera reporte de facturación para un período específico.
         */
        @Transactional(readOnly = true)
        public ReporteFacturacionResponse reporteFacturacion(LocalDate fechaInicio, LocalDate fechaFin) {
                log.info("💰 Generando reporte de facturación: {} a {}", fechaInicio, fechaFin);

                List<Factura> facturas = facturaRepository.findAll().stream()
                                .filter(f -> {
                                        LocalDateTime fechaFactura = f.getFechaEmision();
                                        return !fechaFactura.isBefore(fechaInicio.atStartOfDay()) &&
                                                        !fechaFactura.isAfter(fechaFin.atTime(23, 59, 59));
                                })
                                .toList();

                long totalFacturas = facturas.size();
                long facturasPagadas = facturas.stream()
                                .filter(f -> f.getEstado() != null && f.getEstado().equals("PAGADA"))
                                .count();
                long facturasPendientes = totalFacturas - facturasPagadas;

                BigDecimal ingresosBrutos = facturas.stream()
                                .map(Factura::getTotal)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal ingresosPagados = facturas.stream()
                                .filter(f -> f.getEstado() != null && f.getEstado().equals("PAGADA"))
                                .map(Factura::getTotal)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal ingresosPendientes = ingresosBrutos.subtract(ingresosPagados);

                double tasaPago = totalFacturas > 0 ? (double) facturasPagadas / totalFacturas * 100 : 0;

                long clientesUnicos = facturas.stream()
                                .map(f -> f.getCliente().getIdPersona())
                                .distinct()
                                .count();

                return ReporteFacturacionResponse.builder()
                                .fechaInicio(fechaInicio)
                                .fechaFin(fechaFin)
                                .totalFacturas(totalFacturas)
                                .facturasEmitidas(totalFacturas)
                                .facturasPagadas(facturasPagadas)
                                .facturasPendientes(facturasPendientes)
                                .ingresosBrutos(ingresosBrutos)
                                .ingresosPagados(ingresosPagados)
                                .ingresosPendientes(ingresosPendientes)
                                .tasaPago(tasaPago)
                                .totalClientes(clientesUnicos)
                                .clientesActivos(clientesUnicos)
                                .build();
        }

        /**
         * Genera reporte de actividades veterinarias.
         */
        @Transactional(readOnly = true)
        public ReporteActividadesResponse reporteActividades(LocalDate fechaInicio, LocalDate fechaFin) {
                log.info("🏥 Generando reporte de actividades: {} a {}", fechaInicio, fechaFin);

                long totalConsultas = citaRepository.findAll().stream()
                                .filter(c -> {
                                        LocalDateTime fechaCita = c.getFechaHora();
                                        return !fechaCita.isBefore(fechaInicio.atStartOfDay()) &&
                                                        !fechaCita.isAfter(fechaFin.plusDays(1).atStartOfDay()) &&
                                                        c.getEstado().equals(AppConstants.ESTADO_CITA_REALIZADA);
                                })
                                .count();

                long totalVacunaciones = vacunacionRepository.findAll().stream()
                                .filter(v -> {
                                        LocalDate fecha = v.getFechaAplicacion();
                                        return !fecha.isBefore(fechaInicio) && !fecha.isAfter(fechaFin);
                                })
                                .count();

                long totalDesparasitaciones = desparasitacionRepository.findAll().stream()
                                .filter(d -> {
                                        LocalDate fecha = d.getFechaAplicacion();
                                        return !fecha.isBefore(fechaInicio) && !fecha.isAfter(fechaFin);
                                })
                                .count();

                long pacientesAtendidos = pacienteRepository.findAll().size();

                return ReporteActividadesResponse.builder()
                                .fechaInicio(fechaInicio)
                                .fechaFin(fechaFin)
                                .totalConsultas(totalConsultas)
                                .totalVacunaciones(totalVacunaciones)
                                .totalDesparasitaciones(totalDesparasitaciones)
                                .totalInterenciones(totalDesparasitaciones + totalVacunaciones)
                                .pacientesAtendidos(pacientesAtendidos)
                                .serviciosPrestados(totalDesparasitaciones + totalVacunaciones + totalConsultas)
                                .serviciosCompletados(totalDesparasitaciones + totalVacunaciones + totalConsultas)
                                .build();
        }

        /**
         * Reporte últimos 30 días.
         */
        @Transactional(readOnly = true)
        public ReporteCitasResponse reporteUltimos30Dias() {
                log.info("📊 Generando reporte de últimos 30 días");
                LocalDate hoy = LocalDate.now();
                return reporteCitas(hoy.minusDays(30), hoy);
        }

        /**
         * Reporte del mes actual.
         */
        @Transactional(readOnly = true)
        public ReporteCitasResponse reporteMesActual() {
                log.info("📊 Generando reporte del mes actual");
                LocalDate hoy = LocalDate.now();
                return reporteCitas(hoy.withDayOfMonth(1), hoy);
        }

        /**
         * Reporte del año actual.
         */
        @Transactional(readOnly = true)
        public ReporteCitasResponse reporteAnoActual() {
                log.info("📊 Generando reporte del año actual");
                LocalDate hoy = LocalDate.now();
                return reporteCitas(hoy.withDayOfYear(1), hoy);
        }
}
