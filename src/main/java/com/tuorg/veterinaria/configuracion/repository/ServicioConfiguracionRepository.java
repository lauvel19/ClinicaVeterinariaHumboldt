package com.tuorg.veterinaria.configuracion.repository;

import com.tuorg.veterinaria.configuracion.model.ServicioConfiguracion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para ServicioConfiguracion (catálogo de servicios).
 */
@Repository
public interface ServicioConfiguracionRepository extends JpaRepository<ServicioConfiguracion, Long> {

    /**
     * Obtiene todos los servicios activos.
     * 
     * @return Lista de servicios activos ordenados por nombre
     */
    @Query("SELECT sc FROM ServicioConfiguracion sc WHERE sc.activo = true ORDER BY sc.nombreServicio")
    List<ServicioConfiguracion> findAllActivosOrderByNombre();

    /**
     * Obtiene servicios por categoría.
     * 
     * @param categoria Categoría del servicio
     * @return Lista de servicios de esa categoría
     */
    @Query("SELECT sc FROM ServicioConfiguracion sc WHERE sc.categoria = :categoria AND sc.activo = true ORDER BY sc.nombreServicio")
    List<ServicioConfiguracion> findByCategoriaAndActivoTrue(@Param("categoria") String categoria);

    /**
     * Busca servicios por nombre (búsqueda parcial).
     * 
     * @param nombre Texto a buscar en el nombre
     * @return Lista de servicios que coinciden
     */
    @Query("SELECT sc FROM ServicioConfiguracion sc WHERE LOWER(sc.nombreServicio) LIKE LOWER(CONCAT('%', :nombre, '%')) AND sc.activo = true")
    List<ServicioConfiguracion> searchByNombre(@Param("nombre") String nombre);

    /**
     * Obtiene todas las categorías distintas de servicios activos.
     * 
     * @return Lista de categorías únicas
     */
    @Query("SELECT DISTINCT sc.categoria FROM ServicioConfiguracion sc WHERE sc.activo = true AND sc.categoria IS NOT NULL ORDER BY sc.categoria")
    List<String> findDistinctCategorias();
}
