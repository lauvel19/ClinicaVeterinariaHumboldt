package com.tuorg.veterinaria.dashboard.service;

import com.tuorg.veterinaria.common.exception.BusinessException;
import com.tuorg.veterinaria.dashboard.dto.AdminDashboardResponse;
import com.tuorg.veterinaria.dashboard.dto.AdminDashboardResponse.*;
import com.tuorg.veterinaria.gestionfacturacion.repository.FacturaRepository;
import com.tuorg.veterinaria.gestioninventario.repository.ProductoRepository;
import com.tuorg.veterinaria.gestionpacientes.repository.DesparasitacionRepository;
import com.tuorg.veterinaria.gestionpacientes.repository.PacienteRepository;
import com.tuorg.veterinaria.gestionpacientes.repository.VacunacionRepository;
import com.tuorg.veterinaria.gestionusuarios.repository.ClienteRepository;
import com.tuorg.veterinaria.gestionusuarios.repository.UsuarioRepository;
import com.tuorg.veterinaria.prestacioneservicios.repository.CitaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para generar el dashboard del administrador con datos reales de la BD.
 */
@Service
public class AdminDashboardService {

    private final PacienteRepository pacienteRepository;
    private final ClienteRepository clienteRepository;
    private final CitaRepository citaRepository;
    private final FacturaRepository facturaRepository;
    private final ProductoRepository productoRepository;
    private final VacunacionRepository vacunacionRepository;
    private final DesparasitacionRepository desparasitacionRepository;
    private final UsuarioRepository usuarioRepository;

    @Autowired
    public AdminDashboardService(
            PacienteRepository pacienteRepository,
            ClienteRepository clienteRepository,
            CitaRepository citaRepository,
            FacturaRepository facturaRepository,
            ProductoRepository productoRepository,
            VacunacionRepository vacunacionRepository,
            DesparasitacionRepository desparasitacionRepository,
            UsuarioRepository usuarioRepository) {
        this.pacienteRepository = pacienteRepository;
        this.clienteRepository = clienteRepository;
        this.citaRepository = citaRepository;
        this.facturaRepository = facturaRepository;
        this.productoRepository = productoRepository;
        this.vacunacionRepository = vacunacionRepository;
        this.desparasitacionRepository = desparasitacionRepository;
        this.usuarioRepository = usuarioRepository;
    }

    private static final Logger logger = LoggerFactory.getLogger(AdminDashboardService.class);

    @Transactional(readOnly = true)
    public AdminDashboardResponse obtenerDashboard() {
        try {
            logger.info("📊 Iniciando construcción del dashboard...");
            LocalDate hoy = LocalDate.now();
            LocalDate inicioMes = hoy.withDayOfMonth(1);
            LocalDate finMes = hoy.withDayOfMonth(hoy.lengthOfMonth());
            LocalDate inicioMesAnterior = inicioMes.minusMonths(1);
            LocalDate finMesAnterior = inicioMes.minusDays(1);

            logger.info("✓ Construyendo resumen general...");
            ResumenGeneral resumen = construirResumenGeneral();
            
            logger.info("✓ Construyendo estadísticas financieras...");
            EstadisticasFinancieras finanzas = construirEstadisticasFinancieras(inicioMes, finMes, inicioMesAnterior, finMesAnterior);
            
            logger.info("✓ Construyendo métricas de pacientes...");
            MetricasPacientes pacientes = construirMetricasPacientes(inicioMes, finMes);
            
            logger.info("✓ Construyendo estado de inventario...");
            EstadoInventario inventario = construirEstadoInventario();
            
            logger.info("✓ Construyendo rendimiento del personal...");
            RendimientoPersonal personal = construirRendimientoPersonal();
            
            logger.info("✓ Construyendo datos para gráficos...");
            DatosGraficos graficos = construirDatosGraficos();

            logger.info("✅ Dashboard construido exitosamente");
            return AdminDashboardResponse.builder()
                    .resumenGeneral(resumen)
                    .finanzas(finanzas)
                    .pacientes(pacientes)
                    .inventario(inventario)
                    .personal(personal)
                    .graficos(graficos)
                    .build();
        } catch (Exception e) {
            logger.error("❌ Error al construir dashboard: {}", e.getMessage(), e);
            throw new BusinessException("Error al construir el dashboard: " + e.getMessage(), e);
        }
    }

    private ResumenGeneral construirResumenGeneral() {
        Long totalPacientes = pacienteRepository.count();
        Long totalClientes = clienteRepository.count();
        Long totalCitas = citaRepository.count();
        Long totalFacturas = facturaRepository.count();
        
        // INGRESOS TOTALES: Solo facturas PAGADAS (ingresos reales confirmados)
        BigDecimal ingresosTotales = facturaRepository.findAll().stream()
                .filter(f -> "PAGADA".equals(f.getEstado())) // Solo facturas pagadas
                .map(f -> f.getTotal() != null ? f.getTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        Long citasPendientes = citaRepository.findAll().stream()
                .filter(c -> "PROGRAMADA".equals(c.getEstado()))
                .count();

        return ResumenGeneral.builder()
                .totalPacientes(totalPacientes)
                .totalClientes(totalClientes)
                .totalCitas(totalCitas)
                .totalFacturas(totalFacturas)
                .ingresosTotales(ingresosTotales)
                .citasPendientes(citasPendientes)
                .build();
    }

    private EstadisticasFinancieras construirEstadisticasFinancieras(
            LocalDate inicioMes, LocalDate finMes,
            LocalDate inicioMesAnterior, LocalDate finMesAnterior) {
        
        BigDecimal ingresosMesActual = facturaRepository.findAll().stream()
                .filter(f -> f.getFechaEmision() != null && 
                            !f.getFechaEmision().isBefore(inicioMes.atStartOfDay()) &&
                            !f.getFechaEmision().isAfter(finMes.atTime(23, 59, 59)))
                .map(f -> f.getTotal() != null ? f.getTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal ingresosMesAnterior = facturaRepository.findAll().stream()
                .filter(f -> f.getFechaEmision() != null &&
                            !f.getFechaEmision().isBefore(inicioMesAnterior.atStartOfDay()) &&
                            !f.getFechaEmision().isAfter(finMesAnterior.atTime(23, 59, 59)))
                .map(f -> f.getTotal() != null ? f.getTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal porcentajeCrecimiento = BigDecimal.ZERO;
        if (ingresosMesAnterior.compareTo(BigDecimal.ZERO) > 0) {
            porcentajeCrecimiento = ingresosMesActual
                    .subtract(ingresosMesAnterior)
                    .divide(ingresosMesAnterior, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        Long facturasPendientes = facturaRepository.findAll().stream()
                .filter(f -> "PENDIENTE".equals(f.getEstado()))
                .count();

        BigDecimal montoFacturasPendientes = facturaRepository.findAll().stream()
                .filter(f -> "PENDIENTE".equals(f.getEstado()))
                .map(f -> f.getTotal() != null ? f.getTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Long facturasRealizadas = facturaRepository.findAll().stream()
                .filter(f -> "PAGADA".equals(f.getEstado()))
                .count();

        Long citasRealizadas = citaRepository.findAll().stream()
                .filter(c -> "REALIZADA".equals(c.getEstado()))
                .count();

        BigDecimal promedioIngresoPorCita = BigDecimal.ZERO;
        if (citasRealizadas > 0 && ingresosMesActual.compareTo(BigDecimal.ZERO) > 0) {
            promedioIngresoPorCita = ingresosMesActual.divide(
                    BigDecimal.valueOf(citasRealizadas), 2, RoundingMode.HALF_UP);
        }

        return EstadisticasFinancieras.builder()
                .ingresosMesActual(ingresosMesActual)
                .ingresosMesAnterior(ingresosMesAnterior)
                .porcentajeCrecimiento(porcentajeCrecimiento)
                .facturasPendientes(facturasPendientes)
                .montoFacturasPendientes(montoFacturasPendientes)
                .facturasRealizadas(facturasRealizadas)
                .promedioIngresoPorCita(promedioIngresoPorCita)
                .build();
    }

    private MetricasPacientes construirMetricasPacientes(LocalDate inicioMes, LocalDate finMes) {
        Long pacientesActivos = pacienteRepository.count();
        
        Long pacientesNuevosEsteMes = 0L; // No hay campo fechaRegistro en Paciente

        Long citasRealizadasEsteMes = citaRepository.findAll().stream()
                .filter(c -> c.getFechaHora() != null &&
                            c.getFechaHora().toLocalDate().compareTo(inicioMes) >= 0 &&
                            c.getFechaHora().toLocalDate().compareTo(finMes) <= 0 &&
                            "REALIZADA".equals(c.getEstado()))
                .count();

        Long citasPendientes = citaRepository.findAll().stream()
                .filter(c -> "PROGRAMADA".equals(c.getEstado()))
                .count();

        Long citasCanceladasEsteMes = citaRepository.findAll().stream()
                .filter(c -> c.getFechaHora() != null &&
                            c.getFechaHora().toLocalDate().compareTo(inicioMes) >= 0 &&
                            c.getFechaHora().toLocalDate().compareTo(finMes) <= 0 &&
                            "CANCELADA".equals(c.getEstado()))
                .count();

        Long vacunacionesPendientes = vacunacionRepository.findAll().stream()
                .filter(v -> v.getProximaDosis() != null && 
                            v.getProximaDosis().isAfter(LocalDate.now()))
                .count();

        Long desparasitacionesPendientes = desparasitacionRepository.findAll().stream()
                .filter(d -> d.getProximaAplicacion() != null && 
                            d.getProximaAplicacion().isAfter(LocalDate.now()))
                .count();

        return MetricasPacientes.builder()
                .pacientesActivos(pacientesActivos)
                .pacientesNuevosEsteMes(pacientesNuevosEsteMes)
                .citasRealizadasEsteMes(citasRealizadasEsteMes)
                .citasPendientes(citasPendientes)
                .citasCanceladasEsteMes(citasCanceladasEsteMes)
                .vacunacionesPendientes(vacunacionesPendientes)
                .desparasitacionesPendientes(desparasitacionesPendientes)
                .build();
    }

    private EstadoInventario construirEstadoInventario() {
        Long totalProductos = productoRepository.count();
        
        Long productosStockBajo = productoRepository.findAll().stream()
                .filter(p -> p.getStock() != null && p.getStock() < 10 && p.getStock() > 0)
                .count();

        Long productosAgotados = productoRepository.findAll().stream()
                .filter(p -> p.getStock() != null && p.getStock() == 0)
                .count();

        BigDecimal valorTotalInventario = productoRepository.findAll().stream()
                .map(p -> {
                    BigDecimal precio = p.getPrecioUnitario() != null ? p.getPrecioUnitario() : BigDecimal.ZERO;
                    Integer stock = p.getStock() != null ? p.getStock() : 0;
                    return precio.multiply(BigDecimal.valueOf(stock));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return EstadoInventario.builder()
                .totalProductos(totalProductos)
                .productosStockBajo(productosStockBajo)
                .productosAgotados(productosAgotados)
                .valorTotalInventario(valorTotalInventario)
                .build();
    }

    private RendimientoPersonal construirRendimientoPersonal() {
        Long totalVeterinarios = usuarioRepository.findAll().stream()
                .filter(u -> u.getRol() != null && "VETERINARIO".equals(u.getRol().getNombreRol()))
                .count();

        Long totalSecretarios = usuarioRepository.findAll().stream()
                .filter(u -> u.getRol() != null && "SECRETARIO".equals(u.getRol().getNombreRol()))
                .count();

        Map<String, Long> citasPorVeterinario = citaRepository.findAll().stream()
                .filter(c -> c.getVeterinario() != null && c.getVeterinario().getNombre() != null)
                .collect(Collectors.groupingBy(
                        c -> c.getVeterinario().getNombre() + " " + 
                             (c.getVeterinario().getApellido() != null ? c.getVeterinario().getApellido() : ""),
                        Collectors.counting()
                ));

        return RendimientoPersonal.builder()
                .totalVeterinarios(totalVeterinarios)
                .totalSecretarios(totalSecretarios)
                .citasPorVeterinario(citasPorVeterinario)
                .build();
    }

    private DatosGraficos construirDatosGraficos() {
        List<DatoMensual> ingresosPorMes = calcularIngresosMensuales();
        List<DatoMensual> citasPorMes = calcularCitasMensuales();
        List<DistribucionPorTipo> distribucionServicios = calcularDistribucionServicios();
        List<TendenciaClientes> tendenciaClientes = calcularTendenciaClientes();

        return DatosGraficos.builder()
                .ingresosPorMes(ingresosPorMes)
                .citasPorMes(citasPorMes)
                .distribucionServicios(distribucionServicios)
                .tendenciaClientes(tendenciaClientes)
                .build();
    }

    private List<DatoMensual> calcularIngresosMensuales() {
        LocalDate inicioAnio = LocalDate.now().withDayOfYear(1);
        List<DatoMensual> datos = new ArrayList<>();

        for (int i = 0; i < 12; i++) {
            LocalDate mes = inicioAnio.plusMonths(i);
            LocalDate inicioMes = mes.withDayOfMonth(1);
            LocalDate finMes = mes.withDayOfMonth(mes.lengthOfMonth());

            BigDecimal ingresos = facturaRepository.findAll().stream()
                    .filter(f -> f.getFechaEmision() != null &&
                                !f.getFechaEmision().isBefore(inicioMes.atStartOfDay()) &&
                                !f.getFechaEmision().isAfter(finMes.atTime(23, 59, 59)))
                    .map(f -> f.getTotal() != null ? f.getTotal() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Long cantidadFacturas = facturaRepository.findAll().stream()
                    .filter(f -> f.getFechaEmision() != null &&
                                !f.getFechaEmision().isBefore(inicioMes.atStartOfDay()) &&
                                !f.getFechaEmision().isAfter(finMes.atTime(23, 59, 59)))
                    .count();

            datos.add(DatoMensual.builder()
                    .mes(mes.getMonth().getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("es")))
                    .anio(mes.getYear())
                    .valor(ingresos)
                    .cantidad(cantidadFacturas)
                    .build());
        }

        return datos;
    }

    private List<DatoMensual> calcularCitasMensuales() {
        LocalDate inicioAnio = LocalDate.now().withDayOfYear(1);
        List<DatoMensual> datos = new ArrayList<>();

        for (int i = 0; i < 12; i++) {
            LocalDate mes = inicioAnio.plusMonths(i);
            LocalDate inicioMes = mes.withDayOfMonth(1);
            LocalDate finMes = mes.withDayOfMonth(mes.lengthOfMonth());

            Long cantidadCitas = citaRepository.findAll().stream()
                    .filter(c -> c.getFechaHora() != null &&
                                c.getFechaHora().toLocalDate().compareTo(inicioMes) >= 0 &&
                                c.getFechaHora().toLocalDate().compareTo(finMes) <= 0)
                    .count();

            datos.add(DatoMensual.builder()
                    .mes(mes.getMonth().getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("es")))
                    .anio(mes.getYear())
                    .cantidad(cantidadCitas)
                    .valor(BigDecimal.valueOf(cantidadCitas))
                    .build());
        }

        return datos;
    }

    private List<DistribucionPorTipo> calcularDistribucionServicios() {
        Long totalCitas = citaRepository.count();
        if (totalCitas == 0) {
            return Collections.emptyList();
        }

        Map<String, Long> distribucion = citaRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        c -> c.getTipoServicio() != null ? c.getTipoServicio() : "Sin tipo",
                        Collectors.counting()
                ));

        return distribucion.entrySet().stream()
                .map(e -> DistribucionPorTipo.builder()
                        .tipo(e.getKey())
                        .cantidad(e.getValue())
                        .porcentaje(BigDecimal.valueOf(e.getValue())
                                .multiply(BigDecimal.valueOf(100))
                                .divide(BigDecimal.valueOf(totalCitas), 2, RoundingMode.HALF_UP))
                        .build())
                .sorted(Comparator.comparing(DistribucionPorTipo::getCantidad).reversed())
                .collect(Collectors.toList());
    }

    private List<TendenciaClientes> calcularTendenciaClientes() {
        LocalDate hoy = LocalDate.now();
        List<TendenciaClientes> tendencia = new ArrayList<>();

        for (int i = 5; i >= 0; i--) {
            LocalDate mes = hoy.minusMonths(i);
            LocalDate inicioMes = mes.withDayOfMonth(1);
            LocalDate finMes = mes.withDayOfMonth(mes.lengthOfMonth());

            Long nuevos = clienteRepository.findAll().stream()
                    .filter(c -> c.getFechaRegistro() != null &&
                                !c.getFechaRegistro().isBefore(inicioMes.atStartOfDay()) &&
                                !c.getFechaRegistro().isAfter(finMes.atTime(23, 59, 59)))
                    .count();

            Long activos = clienteRepository.count();

            tendencia.add(TendenciaClientes.builder()
                    .periodo(mes.getMonth().getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("es")))
                    .nuevos(nuevos)
                    .activos(activos)
                    .build());
        }

        return tendencia;
    }
}
