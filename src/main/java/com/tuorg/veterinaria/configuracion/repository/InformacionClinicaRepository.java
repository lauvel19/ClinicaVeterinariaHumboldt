package com.tuorg.veterinaria.configuracion.repository;

import com.tuorg.veterinaria.configuracion.model.InformacionClinica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para InformacionClinica (patrón Singleton).
 * 
 * Solo debe existir un registro activo en la base de datos.
 */
@Repository
public interface InformacionClinicaRepository extends JpaRepository<InformacionClinica, Long> {

    /**
     * Obtiene la información de la clínica activa (Singleton).
     * 
     * @return Información de la clínica activa
     */
    @Query("SELECT ic FROM InformacionClinica ic WHERE ic.activo = true")
    Optional<InformacionClinica> findActivaClinica();

    /**
     * Verifica si ya existe una clínica activa.
     * 
     * @return true si existe una clínica activa
     */
    boolean existsByActivoTrue();
}
