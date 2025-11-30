package com.tuorg.veterinaria.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración para integración con ePayco.
 * 
 * Lee las credenciales desde application.yml y las expone
 * para ser utilizadas por el servicio de pagos online.
 * 
 * Para ambiente de pruebas usar credenciales de testing de ePayco.
 * 
 * @author Equipo de Desarrollo
 * @version 1.0.0
 */
@Configuration
@ConfigurationProperties(prefix = "app.epayco")
@Getter
@Setter
public class EpaycoConfig {

    /**
     * Public Key de ePayco (obligatorio).
     * En testing: obtener de https://dashboard.epayco.co
     */
    private String publicKey;

    /**
     * Private Key de ePayco (obligatorio).
     * Nunca exponer en frontend, solo backend.
     */
    private String privateKey;

    /**
     * P_CUST_ID_CLIENTE - ID del cliente en ePayco.
     */
    private String customerId;

    /**
     * Indica si está en modo prueba (true) o producción (false).
     */
    private boolean testMode = true;

    /**
     * URL base de la API de ePayco.
     * Testing: https://secure.epayco.co
     * Producción: https://secure.epayco.co
     */
    private String apiUrl = "https://secure.epayco.co";

    /**
     * URL de confirmación (webhook) donde ePayco notificará el resultado.
     * Debe ser accesible públicamente (usar ngrok en desarrollo).
     */
    private String confirmationUrl;

    /**
     * URL de respuesta donde se redirige al usuario después del pago.
     */
    private String responseUrl;

    /**
     * Moneda por defecto (COP para Colombia).
     */
    private String currency = "COP";

    /**
     * País (CO para Colombia).
     */
    private String country = "CO";

    /**
     * Idioma de la pasarela (ES para español).
     */
    private String lang = "ES";
}
