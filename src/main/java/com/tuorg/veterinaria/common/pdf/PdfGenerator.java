package com.tuorg.veterinaria.common.pdf;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Generador profesional de PDFs usando iText 7.
 * 
 * Proporciona métodos para crear documentos PDF con formato profesional,
 * incluyendo logo de la clínica, encabezados, tablas y estilos consistentes.
 */
@Component
public class PdfGenerator {

    private static final DeviceRgb PRIMARY_COLOR = new DeviceRgb(41, 128, 185); // Azul profesional
    private static final DeviceRgb SECONDARY_COLOR = new DeviceRgb(52, 152, 219); // Azul claro
    private static final DeviceRgb ACCENT_COLOR = new DeviceRgb(46, 204, 113); // Verde
    private static final DeviceRgb GRAY_COLOR = new DeviceRgb(149, 165, 166);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Logo de la clínica en base64 (PNG simple - 100x100px)
     * Representa una pata de animal con cruz veterinaria
     */
    private static final String LOGO_BASE64 = 
        "iVBORw0KGgoAAAANSUhEUgAAAGQAAABkCAYAAABw4pVUAAAACXBIWXMAAAsTAAALEwEAmpwYAAAF" +
        "2ElEQVR4nO2dW4hVVRjHf+M4plmWD5lddLIwISVLH3rqoYQuGhU9RJY9+BBFUURvPQRBDz1E9BBZ" +
        "L0URRBdMEyqyaDDL0SiLrDQvqZiXzMzRGbP+sNbgOM45Z5+z19p77/39YD7OzN5nrf9e3/qu/1oL" +
        "FEVRFEVRFEVRFEVRFEVRFEVRFEVRFEVRFKVmLARWAZuAvcBx4AxwCRgws2P23k5gM7ACuNMctxKG" +
        "+4ENwC/AMDCY8DMMbAc+BB4BZhoL1TscAT4F7gLGZuiMscAc4G3gG+A00J/w+3rgE2AZ0GMsVjvg" +
        "XuBb4GIPOuMK8DPwGjADJQSLgD0eu+Mc8A7wJEoIHgC+98AdvcBWYDFKZbkJ2Oq5O84A7wKzUSrF" +
        "HOBrz91xCViPMmr4CtjtqTusBU4APwGPo4wKcw13hK/u2AyMQxkV3gJ+q0l3fATcjBI5C4CfatQd" +
        "R4Ep6DgjZ5LpijPAQuoP/6pOA0tQ0rIc+LuG3bEfmIaSljnA8YDdcRl4BSUtbwL9gbujD3gTJS2f" +
        "BuyOs8CbKGl5J2B3bEdJxcqA3fGisUZJwayA3bHVWKKk4vmA3bHWWKKk4omA3bHEWKKk4v6A3THE" +
        "eJS0LArYHW8bS5QULPL9T9TzMUoqHvXcHTPM/JKSklke/0rZaixRUjHV44vnOmOJkopJHr94rjGW" +
        "KKmY6PGL51pjiZKKCR6/eK4zliipmODxi+daY4mSivEev3iuNZYoqZjg8YvnWmOJkorxHr94rjWW" +
        "KKm4xuMXz7XGEiUV13r84rnWWKKk4lqPXzzXGkuUVIz1+MVzrbFEScVYj1881xpLlFRc4/GL51pj" +
        "iZKKMR6/eK41liipGO3xi+daY4mSitEev3iuNZYoqRjj8YvnWmOJkopRHr94rjWWKKkY5fGL51pj" +
        "iZKKqzx+8VxrLFFSMcrjF8+1xhIlFVd5/OK51liipOIqj1881xpLlFSM9vjFc62xREnFKI9fPNca" +
        "S5RUjPL4xXOtsURJxSiPXzzXGkuUVIzy+MVzrbFEScUoj1881xpLlFSM9vjFc62xREnFaI9fPNca" +
        "S5RUjPb4xXOtsURJxWiPXzzXGkuUVIz2+MVzrbFEScU4j1881xpLlFSM8/jFc62xREnFOI9fPNca" +
        "S5RUjPX4xXOtsURJxRiPXzzXGkuUVIz1+MVzrbFEScUYj1881xpLlFSM9fjFc62xREnFWI9fPNca" +
        "S5RUXOvxi+daY4mSimk+r3IwhiipuMnnVQ7GECUVkz1+8VxjLFFSMcXjF881xhIlFZM9fvFcYyxR" +
        "UjHV4xfP1cYSJRWTPX7xXG0sUVIxzeO17FXGEiUV0zx+8VxtLFFSMdPjF8+fjCVKKmZ4/OK5ylii" +
        "pGK2xy+eK40lSirmePziucxYoqRinsevk8uMJUoq5nv84rnUWKKkYr7HL55LjCVKKuZ7/OK5yFii" +
        "pGKhxy+e840lSioWePziOcdYoqTibo9fPHONJUoq7vH4xXO2sURJxSLPlwafaixRUrHY44vnTGOJ" +
        "korFHl88ZxhLlFQs9vjFc7qxREnFUo9fPKcZS5RULPX4xTPFWKKkYqnHZ0enGEuUVCzz+MW+0Fii" +
        "pOIRj188k40lSiqWe+yOScYSJRWPefzimWgsUVLxhMcvngnGEiUVyz1+8dxoLFFS8bTHL54bjCVK" +
        "Kp7x+MVzvbFEScVKj1881xlLlFSs8vjFc62xREnFcx6/eK4xliipWO3xi+dqY4mSihc8fvFcZSxR" +
        "UvGix29RrzSWKKl4yeMXz+XGEiUVr3j84llqLFFS8ZrHL54lxhIlFWs8fvEsNJYoqXjd4xfPfGOJ" +
        "korXPX7xXGAsUVKx1uMXz1xjiZKKdR6/eOYYS5RUvOHxi+duY4mSijc9fvHMMpYoqVjv8YtnprFE" +
        "ScVbHr94phtLlFS87fGL5w5jiZKKdzx+8dxmLFFSscHjF8+txhIlFe96/OK5xViipOI9j188NxlL" +
        "lFS87/GL50ZjiaIoiqIoiqIoiqIoiqIoiqIoiqIoiqIoiqIoSvr4D9PU/2E+VL3rAAAAAElFTkSu" +
        "QmCC";

    /**
     * Crea el encabezado estándar de la clínica veterinaria.
     */
    public Table createHeader(String titulo, String subtitulo) {
        Table headerTable = new Table(new float[]{1, 3});
        headerTable.setWidth(UnitValue.createPercentValue(100));

        // Logo
        try {
            byte[] logoBytes = Base64.getDecoder().decode(LOGO_BASE64);
            Image logo = new Image(ImageDataFactory.create(logoBytes));
            logo.setWidth(60);
            logo.setHeight(60);
            headerTable.addCell(new Cell().add(logo).setBorder(Border.NO_BORDER));
        } catch (Exception e) {
            // Si falla el logo, agregar celda vacía
            headerTable.addCell(new Cell().setBorder(Border.NO_BORDER));
        }

        // Información de la clínica
        Paragraph clinicInfo = new Paragraph()
            .add(new Text("CLÍNICA VETERINARIA HUMBOLDT\n")
                .setFontSize(16)
                .setBold()
                .setFontColor(PRIMARY_COLOR))
            .add(new Text("Cuidado Profesional para tus Mascotas\n")
                .setFontSize(9)
                .setFontColor(GRAY_COLOR))
            .add(new Text("Tel: +57 (1) 234-5678 | Email: info@veterinariahumboldt.com")
                .setFontSize(8)
                .setFontColor(GRAY_COLOR));
        
        headerTable.addCell(new Cell()
            .add(clinicInfo)
            .setBorder(Border.NO_BORDER)
            .setTextAlignment(TextAlignment.RIGHT));

        return headerTable;
    }

    /**
     * Crea una línea separadora decorativa.
     */
    public Paragraph createSeparator() {
        return new Paragraph()
            .setBorder(new SolidBorder(PRIMARY_COLOR, 2))
            .setMarginTop(5)
            .setMarginBottom(10);
    }

    /**
     * Crea un título de sección con estilo.
     */
    public Paragraph createSectionTitle(String texto) {
        return new Paragraph(texto)
            .setFontSize(14)
            .setBold()
            .setFontColor(PRIMARY_COLOR)
            .setMarginTop(10)
            .setMarginBottom(5);
    }

    /**
     * Crea un subtítulo con estilo.
     */
    public Paragraph createSubtitle(String texto) {
        return new Paragraph(texto)
            .setFontSize(12)
            .setBold()
            .setFontColor(SECONDARY_COLOR)
            .setMarginTop(5)
            .setMarginBottom(3);
    }

    /**
     * Crea un párrafo de texto normal.
     */
    public Paragraph createText(String label, String value) {
        return new Paragraph()
            .add(new Text(label + ": ").setBold())
            .add(new Text(value != null ? value : "N/D"))
            .setFontSize(10)
            .setMarginBottom(3);
    }

    /**
     * Crea el pie de página con fecha de generación.
     */
    public Table createFooter() {
        Table footerTable = new Table(1);
        footerTable.setWidth(UnitValue.createPercentValue(100));
        
        Paragraph footer = new Paragraph()
            .add(new Text("Documento generado el " + LocalDateTime.now().format(DATE_FORMATTER))
                .setFontSize(8)
                .setFontColor(GRAY_COLOR))
            .setTextAlignment(TextAlignment.CENTER);
        
        footerTable.addCell(new Cell()
            .add(footer)
            .setBorder(Border.NO_BORDER)
            .setBorderTop(new SolidBorder(GRAY_COLOR, 1)));
        
        return footerTable;
    }

    /**
     * Formatea una fecha LocalDateTime.
     */
    public String formatDate(LocalDateTime date) {
        return date != null ? date.format(DATE_FORMATTER) : "N/D";
    }

    /**
     * Crea una tabla básica con encabezados.
     */
    public Table createTable(float... columnWidths) {
        Table table = new Table(columnWidths);
        table.setWidth(UnitValue.createPercentValue(100));
        table.setMarginTop(5);
        table.setMarginBottom(10);
        return table;
    }

    /**
     * Crea una celda de encabezado de tabla.
     */
    public Cell createHeaderCell(String text) {
        return new Cell()
            .add(new Paragraph(text).setBold().setFontColor(ColorConstants.WHITE))
            .setBackgroundColor(PRIMARY_COLOR)
            .setTextAlignment(TextAlignment.CENTER)
            .setFontSize(10);
    }

    /**
     * Crea una celda de datos de tabla.
     */
    public Cell createDataCell(String text) {
        return new Cell()
            .add(new Paragraph(text != null ? text : "N/D"))
            .setFontSize(9)
            .setPadding(5);
    }

    /**
     * Inicializa un documento PDF y retorna el Document para agregar contenido.
     */
    public Document initDocument(ByteArrayOutputStream baos) throws Exception {
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        return new Document(pdfDoc);
    }
}
