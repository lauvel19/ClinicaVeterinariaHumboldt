package com.tuorg.veterinaria.gestionpagos.repository;

import com.tuorg.veterinaria.gestionpagos.model.PagoOnline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad PagoOnline.
 * 
 * Proporciona métodos de acceso a datos para pagos online
 * realizados a través de ePayco.
 * 
 * @author Equipo de Desarrollo
 * @version 1.0.0
 */
@Repository
public interface PagoOnlineRepository extends JpaRepository<PagoOnline, Long> {

    /**
     * Busca un pago por su referencia de ePayco.
     * 
     * @param referenciaEpayco Referencia única en ePayco
     * @return Optional con el pago si existe
     */
    Optional<PagoOnline> findByReferenciaEpayco(String referenciaEpayco);

    /**
     * Busca un pago por la referencia devuelta por ePayco.
     * 
     * @param refPayco Referencia de ePayco
     * @return Optional con el pago si existe
     */
    Optional<PagoOnline> findByRefPayco(String refPayco);

    /**
     * Busca todos los pagos de una factura.
     * 
     * @param facturaId ID de la factura
     * @return Lista de pagos online
     */
    @Query("SELECT p FROM PagoOnline p WHERE p.factura.idFactura = :facturaId ORDER BY p.createdAt DESC")
    List<PagoOnline> findByFacturaId(@Param("facturaId") Long facturaId);

    /**
     * Busca pagos por estado.
     * 
     * @param estado Estado del pago
     * @return Lista de pagos con ese estado
     */
    List<PagoOnline> findByEstado(String estado);

    /**
     * Verifica si existe un pago aprobado para una factura.
     * 
     * @param facturaId ID de la factura
     * @return true si existe un pago aprobado
     */
    @Query("SELECT COUNT(p) > 0 FROM PagoOnline p WHERE p.factura.idFactura = :facturaId AND p.estado = 'APROBADA'")
    boolean existePagoAprobadoParaFactura(@Param("facturaId") Long facturaId);
}
