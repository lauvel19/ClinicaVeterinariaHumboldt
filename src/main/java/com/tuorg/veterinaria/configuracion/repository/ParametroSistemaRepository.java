package com.tuorg.veterinaria.configuracion.repository;

import com.tuorg.veterinaria.configuracion.model.ParametroSistema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad ParametroSistema.
 * 
 * Proporciona operaciones de base de datos para gestionar
 * los parámetros de configuración del sistema.
 */
@Repository
public interface ParametroSistemaRepository extends JpaRepository<ParametroSistema, Long> {

    /**
     * Busca un parámetro por su clave.
     * 
     * @param clave Clave del parámetro
     * @return Optional con el parámetro si existe
     */
    Optional<ParametroSistema> findByClave(String clave);

    /**
     * Busca parámetros por categoría.
     * 
     * @param categoria Categoría de los parámetros
     * @return Lista de parámetros de la categoría especificada
     */
    List<ParametroSistema> findByCategoria(String categoria);

    /**
     * Busca parámetros activos por categoría.
     * 
     * @param categoria Categoría de los parámetros
     * @param activo Estado activo
     * @return Lista de parámetros activos de la categoría especificada
     */
    List<ParametroSistema> findByCategoriaAndActivo(String categoria, Boolean activo);

    /**
     * Busca todos los parámetros editables.
     * 
     * @param editable Estado editable
     * @return Lista de parámetros editables
     */
    List<ParametroSistema> findByEditable(Boolean editable);

    /**
     * Busca todos los parámetros activos.
     * 
     * @return Lista de parámetros activos
     */
    @Query("SELECT p FROM ParametroSistema p WHERE p.activo = true ORDER BY p.categoria, p.clave")
    List<ParametroSistema> findAllActivos();

    /**
     * Verifica si existe un parámetro con la clave especificada.
     * 
     * @param clave Clave del parámetro
     * @return true si existe, false en caso contrario
     */
    boolean existsByClave(String clave);
}
