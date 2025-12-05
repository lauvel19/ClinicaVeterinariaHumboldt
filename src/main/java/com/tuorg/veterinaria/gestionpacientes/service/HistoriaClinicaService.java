package com.tuorg.veterinaria.gestionpacientes.service;

import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.tuorg.veterinaria.common.exception.BusinessException;
import com.tuorg.veterinaria.common.exception.ResourceNotFoundException;
import com.tuorg.veterinaria.common.pdf.PdfGenerator;
import com.tuorg.veterinaria.gestionpacientes.dto.HistoriaClinicaResponse;
import com.tuorg.veterinaria.gestionpacientes.dto.RegistroMedicoRequest;
import com.tuorg.veterinaria.gestionpacientes.dto.RegistroMedicoResponse;
import com.tuorg.veterinaria.gestionpacientes.dto.VacunacionResponse;
import com.tuorg.veterinaria.gestionpacientes.model.HistoriaClinica;
import com.tuorg.veterinaria.gestionpacientes.model.Paciente;
import com.tuorg.veterinaria.gestionpacientes.model.RegistroMedico;
import com.tuorg.veterinaria.gestionpacientes.repository.HistoriaClinicaRepository;
import com.tuorg.veterinaria.gestionpacientes.repository.RegistroMedicoRepository;
import com.tuorg.veterinaria.gestionusuarios.model.Usuario;
import com.tuorg.veterinaria.gestionusuarios.model.UsuarioVeterinario;
import com.tuorg.veterinaria.gestionusuarios.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

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

    private final HistoriaClinicaRepository historiaClinicaRepository;
    private final RegistroMedicoRepository registroMedicoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PdfGenerator pdfGenerator;

    @Autowired
    public HistoriaClinicaService(HistoriaClinicaRepository historiaClinicaRepository,
                                  RegistroMedicoRepository registroMedicoRepository,
                                  UsuarioRepository usuarioRepository,
                                  PdfGenerator pdfGenerator) {
        this.historiaClinicaRepository = historiaClinicaRepository;
        this.registroMedicoRepository = registroMedicoRepository;
        this.usuarioRepository = usuarioRepository;
        this.pdfGenerator = pdfGenerator;
    }

    /**
     * Obtiene la historia clínica de un paciente.
     * 
     * @param pacienteId ID del paciente
     * @return Historia clínica del paciente
     */
    @Transactional(readOnly = true)
    public HistoriaClinicaResponse obtenerPorPaciente(Long pacienteId) {
        HistoriaClinica historia = historiaClinicaRepository.findByPacienteId(pacienteId)
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
     * @param registro Registro médico a agregar
     * @return Registro médico creado
     */
    @Transactional
    public RegistroMedicoResponse agregarRegistro(Long historiaId, RegistroMedicoRequest request) {
        HistoriaClinica historia = historiaClinicaRepository.findById(historiaId)
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
            Usuario usuario = usuarioRepository.findById(request.getVeterinarioId())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", request.getVeterinarioId()));
            if (!(usuario instanceof UsuarioVeterinario)) {
                throw new BusinessException("El usuario indicado no corresponde a un veterinario");
            }
            registro.setVeterinario((UsuarioVeterinario) usuario);
        }

        RegistroMedico registroGuardado = registroMedicoRepository.save(registro);

        // Nota: El consumo de insumos del inventario se implementará en el servicio de inventario
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
        historiaClinicaRepository.findById(historiaId)
                .orElseThrow(() -> new ResourceNotFoundException(ENTIDAD_HISTORIA_CLINICA, "id", historiaId));
        return registroMedicoRepository.findByHistoriaId(historiaId)
                .stream()
                .map(this::mapRegistro)
                .toList();
    }

    /**
     * Actualiza un registro médico existente.
     * 
     * @param registroId ID del registro médico
     * @param request Datos actualizados
     * @return Registro médico actualizado
     */
    @Transactional
    public RegistroMedicoResponse actualizarRegistro(Long registroId, RegistroMedicoRequest request) {
        RegistroMedico registro = registroMedicoRepository.findById(registroId)
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
            Usuario usuario = usuarioRepository.findById(request.getVeterinarioId())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", request.getVeterinarioId()));
            if (!(usuario instanceof UsuarioVeterinario)) {
                throw new BusinessException("El usuario indicado no corresponde a un veterinario");
            }
            registro.setVeterinario((UsuarioVeterinario) usuario);
        }

        RegistroMedico registroActualizado = registroMedicoRepository.save(registro);
        return mapRegistro(registroActualizado);
    }

    /**
     * Exporta la historia clínica como PDF profesional usando iText.
     *
     * Genera un PDF completo con formato profesional, logo de la clínica,
     * todos los registros médicos, tratamientos, diagnósticos,
     * signos vitales e insumos utilizados.
     */
    @Transactional(readOnly = true)
    public byte[] exportarPDF(Long historiaId) {
        try {
            HistoriaClinica historia = historiaClinicaRepository.findById(historiaId)
                    .orElseThrow(() -> new ResourceNotFoundException(ENTIDAD_HISTORIA_CLINICA, "id", historiaId));

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = pdfGenerator.initDocument(baos);

            // ENCABEZADO CON LOGO
            document.add(pdfGenerator.createHeader("HISTORIA CLÍNICA", "Registro Médico Veterinario"));
            document.add(pdfGenerator.createSeparator());

            // INFORMACIÓN DEL PACIENTE
            document.add(pdfGenerator.createSectionTitle("INFORMACIÓN DEL PACIENTE"));
            
            Paciente paciente = historia.getPaciente();
            document.add(pdfGenerator.createText("ID Historia", String.valueOf(historia.getIdHistoria())));
            document.add(pdfGenerator.createText("Paciente", paciente.getNombre()));
            document.add(pdfGenerator.createText("Especie", paciente.getEspecie()));
            document.add(pdfGenerator.createText("Raza", paciente.getRaza()));
            if (paciente.getFechaNacimiento() != null) {
                document.add(pdfGenerator.createText("Fecha de Nacimiento", paciente.getFechaNacimiento().toString()));
            }
            document.add(pdfGenerator.createText("Sexo", paciente.getSexo()));
            if (paciente.getPesoKg() != null) {
                document.add(pdfGenerator.createText("Peso", paciente.getPesoKg() + " kg"));
            }
            
            if (paciente.getCliente() != null) {
                document.add(pdfGenerator.createText("Propietario", 
                    paciente.getCliente().getNombre() + " " + paciente.getCliente().getApellido()));
                document.add(pdfGenerator.createText("Teléfono", paciente.getCliente().getTelefono()));
            }
            
            document.add(pdfGenerator.createText("Fecha de Apertura", 
                pdfGenerator.formatDate(historia.getFechaApertura())));
            
            // RESUMEN DE LA HISTORIA
            if (historia.getResumen() != null && !historia.getResumen().isEmpty()) {
                document.add(pdfGenerator.createSubtitle("Resumen General"));
                document.add(new Paragraph(historia.getResumen()).setFontSize(10));
            }

            // REGISTROS MÉDICOS
            List<RegistroMedico> registros = registroMedicoRepository
                    .findByHistoriaIdHistoriaOrderByFechaDesc(historiaId);
            
            if (!registros.isEmpty()) {
                document.add(pdfGenerator.createSectionTitle("REGISTROS MÉDICOS"));
                
                for (RegistroMedico reg : registros) {
                    // Subtítulo de registro
                    document.add(pdfGenerator.createSubtitle(
                        "Registro #" + reg.getIdRegistro() + " - " + pdfGenerator.formatDate(reg.getFecha())));
                    
                    // Veterinario
                    if (reg.getVeterinario() != null) {
                        document.add(pdfGenerator.createText("Veterinario", 
                            reg.getVeterinario().getNombre() + " " + reg.getVeterinario().getApellido() +
                            (reg.getVeterinario().getEspecialidad() != null ? 
                                " - " + reg.getVeterinario().getEspecialidad() : "")));
                    }
                    
                    // Motivo
                    if (reg.getMotivo() != null && !reg.getMotivo().isEmpty()) {
                        document.add(pdfGenerator.createText("Motivo de Consulta", reg.getMotivo()));
                    }
                    
                    // Diagnóstico
                    if (reg.getDiagnostico() != null && !reg.getDiagnostico().isEmpty()) {
                        document.add(pdfGenerator.createText("Diagnóstico", reg.getDiagnostico()));
                    }
                    
                    // Tratamiento
                    if (reg.getTratamiento() != null && !reg.getTratamiento().isEmpty()) {
                        document.add(pdfGenerator.createText("Tratamiento", reg.getTratamiento()));
                    }
                    
                    // Signos Vitales (Tabla)
                    if (reg.getSignosVitales() != null && !reg.getSignosVitales().isEmpty()) {
                        document.add(new Paragraph("Signos Vitales:").setBold().setFontSize(10).setMarginTop(5));
                        Table signosTable = pdfGenerator.createTable(2, 2);
                        signosTable.addHeaderCell(pdfGenerator.createHeaderCell("Parámetro"));
                        signosTable.addHeaderCell(pdfGenerator.createHeaderCell("Valor"));
                        
                        for (Map.Entry<String, Object> entry : reg.getSignosVitales().entrySet()) {
                            signosTable.addCell(pdfGenerator.createDataCell(entry.getKey()));
                            signosTable.addCell(pdfGenerator.createDataCell(String.valueOf(entry.getValue())));
                        }
                        document.add(signosTable);
                    }
                    
                    // Insumos/Medicamentos Utilizados (Tabla)
                    if (reg.getInsumosUsados() != null && !reg.getInsumosUsados().isEmpty()) {
                        document.add(new Paragraph("Medicamentos/Insumos Utilizados:").setBold().setFontSize(10).setMarginTop(5));
                        Table insumosTable = pdfGenerator.createTable(3, 1, 1);
                        insumosTable.addHeaderCell(pdfGenerator.createHeaderCell("Nombre"));
                        insumosTable.addHeaderCell(pdfGenerator.createHeaderCell("Cantidad"));
                        insumosTable.addHeaderCell(pdfGenerator.createHeaderCell("Unidad"));
                        
                        for (Map<String, Object> insumo : reg.getInsumosUsados()) {
                            String nombre = insumo.containsKey("nombre") ? String.valueOf(insumo.get("nombre")) : "N/D";
                            String cantidad = insumo.containsKey("cantidad") ? String.valueOf(insumo.get("cantidad")) : "N/D";
                            String unidad = insumo.containsKey("unidad") ? String.valueOf(insumo.get("unidad")) : "N/D";
                            
                            insumosTable.addCell(pdfGenerator.createDataCell(nombre));
                            insumosTable.addCell(pdfGenerator.createDataCell(cantidad));
                            insumosTable.addCell(pdfGenerator.createDataCell(unidad));
                        }
                        document.add(insumosTable);
                    }
                    
                    // Separador entre registros
                    document.add(pdfGenerator.createSeparator());
                }
            } else {
                document.add(new Paragraph("No hay registros médicos disponibles.")
                    .setFontSize(10)
                    .setItalic()
                    .setMarginTop(10));
            }

            // PIE DE PÁGINA
            document.add(pdfGenerator.createFooter());

            document.close();
            return baos.toByteArray();
            
        } catch (Exception e) {
            throw new BusinessException("Error al generar PDF de historia clínica: " + e.getMessage());
        }
    }

    private HistoriaClinicaResponse mapHistoria(HistoriaClinica historia) {
        return new HistoriaClinicaResponse(
                historia.getIdHistoria(),
                new VacunacionResponse.PacienteSummary(
                        historia.getPaciente().getIdPaciente(),
                        historia.getPaciente().getNombre()
                ),
                historia.getFechaApertura(),
                historia.getResumen(),
                historia.getMetadatos()
        );
    }

    private RegistroMedicoResponse mapRegistro(RegistroMedico registro) {
        VacunacionResponse.VeterinarioSummary veterinarioSummary = null;
        if (registro.getVeterinario() != null) {
            UsuarioVeterinario vet = registro.getVeterinario();
            veterinarioSummary = new VacunacionResponse.VeterinarioSummary(
                    vet.getIdUsuario(),
                    vet.getNombre(),
                    vet.getApellido(),
                    vet.getEspecialidad()
            );
        }

        return new RegistroMedicoResponse(
                registro.getIdRegistro(),
                registro.getHistoria().getIdHistoria(),
                registro.getFecha(),
                registro.getMotivo(),
                registro.getDiagnostico(),
                registro.getSignosVitales(),
                registro.getTratamiento(),
                veterinarioSummary,
                registro.getInsumosUsados(),
                registro.getArchivos()
        );
    }
}


