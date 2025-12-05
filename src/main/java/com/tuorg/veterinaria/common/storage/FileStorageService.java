package com.tuorg.veterinaria.common.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Servicio para almacenamiento de archivos en el sistema de archivos local.
 * Maneja la carga y eliminación de imágenes de perfil de pacientes.
 */
@Service
@Slf4j
public class FileStorageService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String[] ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".webp"};

    /**
     * Guarda una imagen de perfil de paciente.
     * 
     * @param file Archivo multipart a guardar
     * @param pacienteId ID del paciente
     * @return Path relativo del archivo guardado
     * @throws IOException Si hay error al guardar el archivo
     * @throws IllegalArgumentException Si el archivo no es válido
     */
    public String guardarFotoPaciente(MultipartFile file, Long pacienteId) throws IOException {
        validarArchivo(file);

        // Crear directorio si no existe
        Path uploadPath = Paths.get(uploadDir, "pacientes");
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generar nombre único para el archivo
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
            ? originalFilename.substring(originalFilename.lastIndexOf("."))
            : ".jpg";
        
        String filename = "paciente_" + pacienteId + "_" + UUID.randomUUID().toString() + extension;
        Path filePath = uploadPath.resolve(filename);

        // Guardar archivo
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        log.info("Imagen guardada exitosamente: {}", filePath);

        // Retornar path relativo para la URL
        return "/uploads/pacientes/" + filename;
    }

    /**
     * Elimina un archivo de foto de perfil.
     * 
     * @param relativePath Path relativo del archivo a eliminar
     */
    public void eliminarFoto(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return;
        }

        try {
            // Convertir path relativo a absoluto
            String filename = relativePath.substring(relativePath.lastIndexOf("/") + 1);
            Path filePath = Paths.get(uploadDir, "pacientes", filename);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("Imagen eliminada exitosamente: {}", filePath);
            }
        } catch (IOException e) {
            log.error("Error al eliminar imagen: {}", relativePath, e);
        }
    }

    /**
     * Valida que el archivo sea una imagen válida.
     * 
     * @param file Archivo a validar
     * @throws IllegalArgumentException Si el archivo no es válido
     */
    private void validarArchivo(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("El archivo excede el tamaño máximo permitido de 5MB");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("El nombre del archivo es inválido");
        }

        boolean extensionValida = false;
        for (String ext : ALLOWED_EXTENSIONS) {
            if (originalFilename.toLowerCase().endsWith(ext)) {
                extensionValida = true;
                break;
            }
        }

        if (!extensionValida) {
            throw new IllegalArgumentException("Formato de archivo no permitido. Use: jpg, jpeg, png, gif, webp");
        }

        // Validar content type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("El archivo debe ser una imagen");
        }
    }
}
