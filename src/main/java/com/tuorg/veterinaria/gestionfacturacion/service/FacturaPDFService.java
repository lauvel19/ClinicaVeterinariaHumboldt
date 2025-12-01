package com.tuorg.veterinaria.gestionfacturacion.service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.tuorg.veterinaria.common.exception.BusinessException;
import com.tuorg.veterinaria.gestionfacturacion.model.Factura;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Servicio para generación de PDFs de facturas
 * 
 * @author Sistema Veterinaria
 * @version 1.0
 * @since 2024
 * 
 * @remarks
 * - Genera PDFs profesionales con iText7
 * - Diseño limpio y estructurado
 * - Incluye información completa de la clínica y cliente
 * - Cumple con principio de Single Responsibility
 * - Manejo robusto de errores
 */
@Service
@Slf4j
public class FacturaPDFService {

    private static final String CLINICA_NOMBRE = "Clínica Veterinaria Universitaria Humboldt";
    private static final String CLINICA_DIRECCION = "Calle Principal #123, Ciudad";
    private static final String CLINICA_TELEFONO = "Tel: (57) 300-123-4567";
    private static final String CLINICA_EMAIL = "contacto@veterinariahumboldt.com";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Colores corporativos
    private static final DeviceRgb COLOR_PRIMARIO = new DeviceRgb(59, 130, 246); // Azul
    private static final DeviceRgb COLOR_GRIS_CLARO = new DeviceRgb(243, 244, 246);
    private static final DeviceRgb COLOR_GRIS_OSCURO = new DeviceRgb(55, 65, 81);

    /**
     * Genera un PDF para una factura
     * 
     * @param factura La entidad Factura con todos los datos
     * @return Array de bytes con el PDF generado
     * @throws BusinessException Si ocurre un error en la generación
     */
    public byte[] generarPDFFactura(Factura factura) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Configuración del documento
            document.setMargins(40, 40, 40, 40);

            // Agregar contenido
            agregarEncabezado(document, factura);
            agregarInformacionCliente(document, factura);
            agregarDetalleServicios(document, factura);
            agregarTotales(document, factura);
            agregarPiePagina(document, factura);

            document.close();
            
            log.info("PDF generado exitosamente para factura {}", factura.getNumero());
            return baos.toByteArray();
            
        } catch (Exception e) {
            log.error("Error al generar PDF para factura {}: {}", 
                    factura.getNumero(), e.getMessage(), e);
            throw new BusinessException("Error al generar el PDF de la factura: " + e.getMessage());
        }
    }

    /**
     * Agrega el encabezado con logo e información de la clínica
     */
    private void agregarEncabezado(Document document, Factura factura) {
        // Tabla para el encabezado con 2 columnas
        Table headerTable = new Table(UnitValue.createPercentArray(new float[]{60, 40}))
                .useAllAvailableWidth();

        // Columna izquierda: Información de la clínica
        Cell leftCell = new Cell()
                .setBorder(null)
                .add(new Paragraph(CLINICA_NOMBRE)
                        .setFontSize(18)
                        .setBold()
                        .setFontColor(COLOR_PRIMARIO))
                .add(new Paragraph(CLINICA_DIRECCION)
                        .setFontSize(10)
                        .setMarginTop(5))
                .add(new Paragraph(CLINICA_TELEFONO)
                        .setFontSize(10))
                .add(new Paragraph(CLINICA_EMAIL)
                        .setFontSize(10)
                        .setFontColor(COLOR_PRIMARIO));

        // Columna derecha: Información de la factura
        Cell rightCell = new Cell()
                .setBorder(null)
                .setTextAlignment(TextAlignment.RIGHT)
                .add(new Paragraph("FACTURA")
                        .setFontSize(24)
                        .setBold()
                        .setFontColor(COLOR_PRIMARIO))
                .add(new Paragraph("N° " + factura.getNumero())
                        .setFontSize(14)
                        .setBold()
                        .setMarginTop(5))
                .add(new Paragraph("Fecha: " + factura.getFechaEmision().format(DATE_ONLY_FORMATTER))
                        .setFontSize(10)
                        .setMarginTop(10))
                .add(new Paragraph("Estado: " + factura.getEstado())
                        .setFontSize(10)
                        .setBold()
                        .setFontColor(getColorEstado(factura.getEstado())));

        headerTable.addCell(leftCell);
        headerTable.addCell(rightCell);

        document.add(headerTable);
        document.add(new Paragraph("\n"));
    }

    /**
     * Agrega información del cliente
     */
    private void agregarInformacionCliente(Document document, Factura factura) {
        // Título
        document.add(new Paragraph("Información del Cliente")
                .setFontSize(12)
                .setBold()
                .setFontColor(COLOR_GRIS_OSCURO)
                .setMarginBottom(10));

        // Tabla con información del cliente
        Table clienteTable = new Table(UnitValue.createPercentArray(new float[]{100}))
                .useAllAvailableWidth()
                .setBackgroundColor(COLOR_GRIS_CLARO)
                .setPadding(15);

        String nombreCompleto = factura.getCliente() != null 
                ? factura.getCliente().getNombre() + " " + factura.getCliente().getApellido()
                : "Cliente No Registrado";
        
        String correo = factura.getCliente() != null && factura.getCliente().getCorreo() != null
                ? factura.getCliente().getCorreo()
                : "N/A";
        
        String telefono = factura.getCliente() != null && factura.getCliente().getTelefono() != null
                ? factura.getCliente().getTelefono()
                : "N/A";

        Cell clienteCell = new Cell()
                .setBorder(null)
                .add(new Paragraph("Cliente: " + nombreCompleto).setBold().setFontSize(11))
                .add(new Paragraph("Correo: " + correo).setFontSize(10).setMarginTop(3))
                .add(new Paragraph("Teléfono: " + telefono).setFontSize(10).setMarginTop(3));

        clienteTable.addCell(clienteCell);
        document.add(clienteTable);
        document.add(new Paragraph("\n"));
    }

    /**
     * Agrega la tabla de servicios/productos
     */
    private void agregarDetalleServicios(Document document, Factura factura) {
        // Título
        document.add(new Paragraph("Detalle de Servicios")
                .setFontSize(12)
                .setBold()
                .setFontColor(COLOR_GRIS_OSCURO)
                .setMarginBottom(10));

        // Crear tabla con 4 columnas
        Table table = new Table(UnitValue.createPercentArray(new float[]{10, 40, 20, 30}))
                .useAllAvailableWidth();

        // Encabezados de tabla
        String[] headers = {"#", "Descripción", "Cantidad", "Precio"};
        for (String header : headers) {
            Cell headerCell = new Cell()
                    .setBackgroundColor(COLOR_PRIMARIO)
                    .setFontColor(ColorConstants.WHITE)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    .setPadding(10)
                    .add(new Paragraph(header));
            table.addHeaderCell(headerCell);
        }

        // Parsear contenido de la factura
        List<Map<String, Object>> items = parseContenidoFactura(factura.getContenido());
        
        int itemNumber = 1;
        for (Map<String, Object> item : items) {
            // Número
            table.addCell(new Cell()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPadding(8)
                    .add(new Paragraph(String.valueOf(itemNumber++))));

            // Descripción
            String descripcion = item.getOrDefault("descripcion", "Servicio").toString();
            table.addCell(new Cell()
                    .setPadding(8)
                    .add(new Paragraph(descripcion)));

            // Cantidad
            String cantidad = item.getOrDefault("cantidad", "1").toString();
            table.addCell(new Cell()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPadding(8)
                    .add(new Paragraph(cantidad)));

            // Precio
            String precio = formatCurrency(item.getOrDefault("precio", "0"));
            table.addCell(new Cell()
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setPadding(8)
                    .add(new Paragraph(precio)));
        }

        document.add(table);
        document.add(new Paragraph("\n"));
    }

    /**
     * Agrega los totales de la factura
     */
    private void agregarTotales(Document document, Factura factura) {
        // Tabla alineada a la derecha
        Table totalesTable = new Table(UnitValue.createPercentArray(new float[]{70, 30}))
                .useAllAvailableWidth();

        // Celda vacía a la izquierda
        totalesTable.addCell(new Cell().setBorder(null));

        // Celda con totales a la derecha
        Cell totalesCell = new Cell()
                .setBorder(new SolidBorder(COLOR_GRIS_CLARO, 1))
                .setPadding(15)
                .setBackgroundColor(COLOR_GRIS_CLARO);

        totalesCell.add(new Paragraph("TOTAL")
                .setFontSize(14)
                .setBold()
                .setTextAlignment(TextAlignment.RIGHT));
        
        totalesCell.add(new Paragraph("$ " + formatNumber(factura.getTotal()))
                .setFontSize(20)
                .setBold()
                .setFontColor(COLOR_PRIMARIO)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginTop(5));

        if (factura.getFormaPago() != null) {
            totalesCell.add(new Paragraph("Forma de Pago: " + factura.getFormaPago())
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginTop(10));
        }

        if (factura.getFechaPago() != null) {
            totalesCell.add(new Paragraph("Fecha de Pago: " + factura.getFechaPago().format(DATE_ONLY_FORMATTER))
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginTop(3));
        }

        totalesTable.addCell(totalesCell);
        document.add(totalesTable);
    }

    /**
     * Agrega el pie de página
     */
    private void agregarPiePagina(Document document, Factura factura) {
        document.add(new Paragraph("\n\n"));
        
        // Línea divisoria
        Table lineTable = new Table(1).useAllAvailableWidth();
        lineTable.addCell(new Cell()
                .setBorder(null)
                .setBorderTop(new SolidBorder(COLOR_GRIS_CLARO, 1))
                .add(new Paragraph("")));
        document.add(lineTable);

        // Texto del pie
        Paragraph footer = new Paragraph()
                .setFontSize(9)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.GRAY)
                .setMarginTop(10);

        footer.add("Gracias por confiar en nosotros para el cuidado de sus mascotas.\n");
        footer.add("Este documento es una representación impresa de una factura electrónica.\n");
        footer.add(CLINICA_NOMBRE + " - " + CLINICA_TELEFONO);

        document.add(footer);
    }

    /**
     * Parsea el contenido JSONB de la factura
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseContenidoFactura(String contenido) {
        try {
            if (contenido == null || contenido.trim().isEmpty()) {
                return List.of(Map.of(
                    "descripcion", "Servicios veterinarios",
                    "cantidad", "1",
                    "precio", "0"
                ));
            }

            // El contenido ya viene como String JSON desde la base de datos
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, Object> contenidoMap = mapper.readValue(contenido, Map.class);
            
            if (contenidoMap.containsKey("items")) {
                return (List<Map<String, Object>>) contenidoMap.get("items");
            }
            
            // Si no hay items, crear uno genérico
            return List.of(contenidoMap);
            
        } catch (Exception e) {
            log.warn("Error parseando contenido de factura: {}", e.getMessage());
            return List.of(Map.of(
                "descripcion", "Servicios veterinarios",
                "cantidad", "1",
                "precio", "0"
            ));
        }
    }

    /**
     * Formatea un número como moneda
     */
    private String formatCurrency(Object value) {
        try {
            double amount = value instanceof Number 
                    ? ((Number) value).doubleValue() 
                    : Double.parseDouble(value.toString());
            return "$ " + String.format("%,.2f", amount);
        } catch (Exception e) {
            return "$ 0.00";
        }
    }

    /**
     * Formatea un número con separadores de miles
     */
    private String formatNumber(Object value) {
        try {
            double amount = value instanceof Number 
                    ? ((Number) value).doubleValue() 
                    : Double.parseDouble(value.toString());
            return String.format("%,.2f", amount);
        } catch (Exception e) {
            return "0.00";
        }
    }

    /**
     * Obtiene el color según el estado de la factura
     */
    private DeviceRgb getColorEstado(String estado) {
        return switch (estado.toUpperCase()) {
            case "PAGADA" -> new DeviceRgb(34, 197, 94); // Verde
            case "PENDIENTE" -> new DeviceRgb(251, 146, 60); // Naranja
            case "ANULADA" -> new DeviceRgb(239, 68, 68); // Rojo
            default -> COLOR_GRIS_OSCURO;
        };
    }
}
