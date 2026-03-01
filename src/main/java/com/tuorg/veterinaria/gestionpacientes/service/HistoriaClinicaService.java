package com.tuorg.veterinaria.gestionpacientes.service;

import com.tuorg.veterinaria.common.exception.BusinessException;
import com.tuorg.veterinaria.common.exception.ResourceNotFoundException;
import com.tuorg.veterinaria.gestionpacientes.dto.HistoriaClinicaResponse;
import com.tuorg.veterinaria.gestionpacientes.dto.RegistroMedicoRequest;
import com.tuorg.veterinaria.gestionpacientes.dto.RegistroMedicoResponse;
import com.tuorg.veterinaria.gestionpacientes.dto.VacunacionResponse;
import com.tuorg.veterinaria.gestionpacientes.model.HistoriaClinica;
import com.tuorg.veterinaria.gestionpacientes.model.RegistroMedico;
import com.tuorg.veterinaria.gestionpacientes.repository.HistoriaClinicaRepository;
import com.tuorg.veterinaria.gestionpacientes.repository.RegistroMedicoRepository;
import com.tuorg.veterinaria.gestionusuarios.model.Usuario;
import com.tuorg.veterinaria.gestionusuarios.model.UsuarioVeterinario;
import com.tuorg.veterinaria.gestionusuarios.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Servicio para la gestión de historias clínicas.
 * 
 * Este servicio proporciona métodos para agregar registros médicos
 * a las historias clínicas y exportar información.
 * 
 * @author Equipo de Desarrollo
 * @version 1.0.0
 */
@Service
public class HistoriaClinicaService {

    private static final String ENTIDAD_HISTORIA_CLINICA = "HistoriaClinica";
    private static final String PDF_TEXT_END = ") Tj";
    private static final String PDF_XREF_FORMAT = "%010d %05d n";
    private static final String PDF_NEWLINE = "\n";

    /**
     * Repositorio de historias clínicas.
     */
    private final HistoriaClinicaRepository historiaClinicaRepository;

    /**
     * Repositorio de registros médicos.
     */
    private final RegistroMedicoRepository registroMedicoRepository;
    private final UsuarioRepository usuarioRepository;

    /**
     * Constructor con inyección de dependencias.
     * 
     * @param historiaClinicaRepository Repositorio de historias clínicas
     * @param registroMedicoRepository  Repositorio de registros médicos
     */
    @Autowired
    public HistoriaClinicaService(HistoriaClinicaRepository historiaClinicaRepository,
            RegistroMedicoRepository registroMedicoRepository,
            UsuarioRepository usuarioRepository) {
        this.historiaClinicaRepository = historiaClinicaRepository;
        this.registroMedicoRepository = registroMedicoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Obtiene la historia clínica de un paciente.
     * 
     * @param pacienteId ID del paciente
     * @return Historia clínica del paciente
     */
    @Transactional(readOnly = true)
    public HistoriaClinicaResponse obtenerPorPaciente(Long pacienteId) {
        HistoriaClinica historia = historiaClinicaRepository.findByPacienteId(Objects.requireNonNull(pacienteId))
                .orElseThrow(() -> new ResourceNotFoundException(ENTIDAD_HISTORIA_CLINICA, "paciente_id", pacienteId));
        return mapHistoria(historia);
    }

    /**
     * Agrega un registro médico a una historia clínica.
     * 
     * Esta operación es transaccional y también debe consumir insumos
     * del inventario según lo especificado en el registro.
     * 
     * @param historiaId ID de la historia clínica
     * @param registro   Registro médico a agregar
     * @return Registro médico creado
     */
    @Transactional
    public RegistroMedicoResponse agregarRegistro(Long historiaId, RegistroMedicoRequest request) {
        HistoriaClinica historia = historiaClinicaRepository.findById(Objects.requireNonNull(historiaId))
                .orElseThrow(() -> new ResourceNotFoundException(ENTIDAD_HISTORIA_CLINICA, "id", historiaId));

        RegistroMedico registro = new RegistroMedico();
        registro.setHistoria(historia);
        registro.setFecha(request.getFecha());
        registro.setMotivo(request.getMotivo());
        registro.setDiagnostico(request.getDiagnostico());
        registro.setSignosVitales(request.getSignosVitales());
        registro.setTratamiento(request.getTratamiento());
        registro.setInsumosUsados(request.getInsumosUsados());
        registro.setArchivos(request.getArchivos());

        if (request.getVeterinarioId() != null) {
            Usuario usuario = usuarioRepository.findById(Objects.requireNonNull(request.getVeterinarioId()))
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", request.getVeterinarioId()));
            if (!(usuario instanceof UsuarioVeterinario)) {
                throw new BusinessException("El usuario indicado no corresponde a un veterinario");
            }
            registro.setVeterinario((UsuarioVeterinario) usuario);
        }

        RegistroMedico registroGuardado = registroMedicoRepository.save(Objects.requireNonNull(registro));

        // Nota: El consumo de insumos del inventario se implementará en el servicio de
        // inventario
        // cuando se requiera la funcionalidad completa de gestión de inventario

        return mapRegistro(registroGuardado);
    }

    /**
     * Obtiene todos los registros médicos de una historia clínica.
     * 
     * @param historiaId ID de la historia clínica
     * @return Lista de registros médicos
     */
    @Transactional(readOnly = true)
    public List<RegistroMedicoResponse> obtenerRegistros(Long historiaId) {
        // Verificar que la historia clínica existe
        historiaClinicaRepository.findById(Objects.requireNonNull(historiaId))
                .orElseThrow(() -> new ResourceNotFoundException(ENTIDAD_HISTORIA_CLINICA, "id", historiaId));
        return registroMedicoRepository.findByHistoriaId(Objects.requireNonNull(historiaId))
                .stream()
                .map(this::mapRegistro)
                .toList();
    }

    /**
     * Actualiza un registro médico existente.
     * 
     * @param registroId ID del registro médico
     * @param request    Datos actualizados
     * @return Registro médico actualizado
     */
    @Transactional
    public RegistroMedicoResponse actualizarRegistro(Long registroId, RegistroMedicoRequest request) {
        RegistroMedico registro = registroMedicoRepository.findById(Objects.requireNonNull(registroId))
                .orElseThrow(() -> new ResourceNotFoundException("RegistroMedico", "id", registroId));

        // Actualizar campos
        if (request.getFecha() != null) {
            registro.setFecha(request.getFecha());
        }
        if (request.getMotivo() != null) {
            registro.setMotivo(request.getMotivo());
        }
        if (request.getDiagnostico() != null) {
            registro.setDiagnostico(request.getDiagnostico());
        }
        if (request.getSignosVitales() != null) {
            registro.setSignosVitales(request.getSignosVitales());
        }
        if (request.getTratamiento() != null) {
            registro.setTratamiento(request.getTratamiento());
        }
        if (request.getInsumosUsados() != null) {
            registro.setInsumosUsados(request.getInsumosUsados());
        }
        if (request.getArchivos() != null) {
            registro.setArchivos(request.getArchivos());
        }
        if (request.getVeterinarioId() != null) {
            Usuario usuario = usuarioRepository.findById(Objects.requireNonNull(request.getVeterinarioId()))
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", request.getVeterinarioId()));
            if (!(usuario instanceof UsuarioVeterinario)) {
                throw new BusinessException("El usuario indicado no corresponde a un veterinario");
            }
            registro.setVeterinario((UsuarioVeterinario) usuario);
        }

        RegistroMedico registroActualizado = registroMedicoRepository.save(Objects.requireNonNull(registro));
        return mapRegistro(registroActualizado);
    }

    /**
     * Exporta la historia clínica como PDF sin dependencias externas (PDF mínimo).
     *
     * Genera un PDF de una página con texto básico usando sintaxis PDF directa.
     */
    @Transactional(readOnly = true)
    public byte[] exportarPDF(Long historiaId) {
        HistoriaClinica historia = historiaClinicaRepository.findById(Objects.requireNonNull(historiaId))
                .orElseThrow(() -> new ResourceNotFoundException(ENTIDAD_HISTORIA_CLINICA, "id", historiaId));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String titulo = "Historia Clinica";
        String linea1 = "ID Historia: " + historia.getIdHistoria();
        String linea2 = "Paciente: " + normalize(historia.getPaciente().getNombre()) + " (ID "
                + historia.getPaciente().getIdPaciente() + ")";
        String linea3 = "Apertura: "
                + (historia.getFechaApertura() != null ? historia.getFechaApertura().format(formatter) : "N/D");
        String resumenTitulo = "Resumen:";
        String resumen = historia.getResumen() != null ? normalize(historia.getResumen()) : "Sin resumen registrado.";

        // Contenido del stream (texto)
        StringBuilder content = new StringBuilder();
        content.append("BT\n");
        content.append("/F1 18 Tf 50 750 Td (").append(escapePdf(titulo)).append(PDF_TEXT_END).append("\n");
        content.append("/F1 12 Tf 0 -30 Td (").append(escapePdf(linea1)).append(PDF_TEXT_END).append("\n");
        content.append("0 -18 Td (").append(escapePdf(linea2)).append(PDF_TEXT_END).append("\n");
        content.append("0 -18 Td (").append(escapePdf(linea3)).append(PDF_TEXT_END).append("\n");
        content.append("/F1 14 Tf 0 -24 Td (").append(escapePdf(resumenTitulo)).append(PDF_TEXT_END).append("\n");
        // Resumen en líneas simples de 90 chars
        int wrap = 90;
        int idx = 0;
        while (idx < resumen.length()) {
            int end = Math.min(idx + wrap, resumen.length());
            String slice = resumen.substring(idx, end);
            content.append("/F1 12 Tf 0 -16 Td (").append(escapePdf(slice)).append(PDF_TEXT_END).append("\n");
            idx = end;
        }
        content.append("ET\n");

        byte[] streamBytes = content.toString().getBytes(StandardCharsets.US_ASCII);
        StringBuilder pdf = new StringBuilder();
        pdf.append("%PDF-1.4\n");

        // Objetos y offsets
        int xref1 = pdf.length();
        pdf.append("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");
        int xref2 = pdf.length();
        pdf.append("2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n");
        int xref3 = pdf.length();
        pdf.append(
                "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>\nendobj\n");
        int xref4 = pdf.length();
        pdf.append("4 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n");
        int xref5 = pdf.length();
        pdf.append("5 0 obj\n<< /Length ").append(streamBytes.length).append(" >>\nstream\n");
        pdf.append(content);
        pdf.append("endstream\nendobj\n");

        // xref table
        int xrefStart = pdf.length();
        pdf.append("xref").append(PDF_NEWLINE);
        pdf.append("0 6").append(PDF_NEWLINE);
        pdf.append(String.format("%010d %05d f ", 0, 65535)).append(PDF_NEWLINE);
        pdf.append(String.format(PDF_XREF_FORMAT, xref1, 00000)).append(" ").append(PDF_NEWLINE);
        pdf.append(String.format(PDF_XREF_FORMAT, xref2, 00000)).append(" ").append(PDF_NEWLINE);
        pdf.append(String.format(PDF_XREF_FORMAT, xref3, 00000)).append(" ").append(PDF_NEWLINE);
        pdf.append(String.format(PDF_XREF_FORMAT, xref4, 00000)).append(" ").append(PDF_NEWLINE);
        pdf.append(String.format(PDF_XREF_FORMAT, xref5, 00000)).append(" ").append(PDF_NEWLINE);

        pdf.append("trailer\n<< /Size 6 /Root 1 0 R >>\n");
        pdf.append("startxref\n").append(xrefStart).append("\n%%EOF");

        return pdf.toString().getBytes(StandardCharsets.US_ASCII);
    }

    private String escapePdf(String text) {
        return text.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)").replace("\r", " ").replace("\n", " ");
    }

    private String normalize(String text) {
        if (text == null)
            return "";
        // Remover acentos para evitar caracteres no representables en el PDF mínimo
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        // Reemplazos comunes adicionales
        normalized = normalized.replace("ñ", "n").replace("Ñ", "N");
        return normalized;
    }

    private HistoriaClinicaResponse mapHistoria(HistoriaClinica historia) {
        return new HistoriaClinicaResponse(
                Objects.requireNonNull(historia.getIdHistoria()),
                new VacunacionResponse.PacienteSummary(
                        Objects.requireNonNull(historia.getPaciente().getIdPaciente()),
                        historia.getPaciente().getNombre()),
                historia.getFechaApertura(),
                historia.getResumen(),
                historia.getMetadatos());
    }

    private RegistroMedicoResponse mapRegistro(RegistroMedico registro) {
        VacunacionResponse.VeterinarioSummary veterinarioSummary = null;
        if (registro.getVeterinario() != null) {
            UsuarioVeterinario vet = registro.getVeterinario();
            veterinarioSummary = new VacunacionResponse.VeterinarioSummary(
                    Objects.requireNonNull(vet.getIdUsuario()),
                    vet.getNombre(),
                    vet.getApellido(),
                    vet.getEspecialidad());
        }

        return new RegistroMedicoResponse(
                Objects.requireNonNull(registro.getIdRegistro()),
                Objects.requireNonNull(registro.getHistoria().getIdHistoria()),
                registro.getFecha(),
                registro.getMotivo(),
                registro.getDiagnostico(),
                registro.getSignosVitales(),
                registro.getTratamiento(),
                veterinarioSummary,
                registro.getInsumosUsados(),
                registro.getArchivos());
    }
}
