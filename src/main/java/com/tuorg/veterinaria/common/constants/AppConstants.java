package com.tuorg.veterinaria.common.constants;

/**
 * Clase de constantes de la aplicación.
 * 
 * Esta clase centraliza todas las constantes utilizadas en la aplicación,
 * incluyendo mensajes, códigos de error, valores por defecto, etc.
 * 
 * @author Equipo de Desarrollo
 * @version 1.0.0
 */
public final class AppConstants {

    /**
     * Constructor privado para prevenir instanciación.
     */
    private AppConstants() {
        throw new UnsupportedOperationException("Esta es una clase de constantes y no debe instanciarse");
    }

    // ==================== Mensajes de Respuesta ====================

    /**
     * Mensaje de éxito genérico.
     */
    public static final String MSG_SUCCESS = "Operación realizada exitosamente";

    /**
     * Mensaje de error genérico.
     */
    public static final String MSG_ERROR = "Ocurrió un error al procesar la solicitud";

    /**
     * Mensaje de recurso no encontrado.
     */
    public static final String MSG_RESOURCE_NOT_FOUND = "Recurso no encontrado";

    // ==================== Especies de Animales ====================

    /**
     * Especie: Perro.
     */
    public static final String ESPECIE_PERRO = "perro";

    /**
     * Especie: Gato.
     */
    public static final String ESPECIE_GATO = "gato";

    // ==================== Estados de Citas ====================

    /**
     * Estado de cita: Reservada (solicitada por cliente, pendiente de confirmación).
     */
    public static final String ESTADO_CITA_RESERVADA = "RESERVADA";

    /**
     * Estado de cita: Programada (confirmada por secretario).
     */
    public static final String ESTADO_CITA_PROGRAMADA = "PROGRAMADA";

    /**
     * Estado de cita: Realizada.
     */
    public static final String ESTADO_CITA_REALIZADA = "REALIZADA";

    /**
     * Estado de cita: Cancelada.
     */
    public static final String ESTADO_CITA_CANCELADA = "CANCELADA";

    // ==================== Estados de Solicitud de Cita ====================

    /**
     * Estado de solicitud: Pendiente.
     */
    public static final String ESTADO_SOLICITUD_PENDIENTE = "PENDIENTE";

    /**
     * Estado de solicitud: Aprobada.
     */
    public static final String ESTADO_SOLICITUD_APROBADA = "APROBADA";

    /**
     * Estado de solicitud: Rechazada.
     */
    public static final String ESTADO_SOLICITUD_RECHAZADA = "RECHAZADA";

    /**
     * Estado de solicitud: Cancelada.
     */
    public static final String ESTADO_SOLICITUD_CANCELADA = "CANCELADA";

    /**
     * Duración estándar de una cita en minutos.
     */
    public static final int DURACION_CITA_MINUTOS = 30;

    /**
     * Tiempo mínimo de anticipación para agendar/cancelar una cita (en horas).
     */
    public static final int ANTICIPACION_MINIMA_HORAS = 2;

    /**
     * Tiempo máximo de anticipación para agendar una cita (en días).
     */
    public static final int ANTICIPACION_MAXIMA_DIAS = 90;

    /**
     * Intervalo de minutos permitido para agendar citas (ej: 30 = solo :00 y :30).
     */
    public static final int INTERVALO_CITAS_MINUTOS = 30;

    /**
     * Hora de inicio del horario laboral - mañana (formato 24 horas).
     */
    public static final int HORARIO_MANANA_INICIO = 8;

    /**
     * Hora de fin del horario laboral - mañana (formato 24 horas).
     */
    public static final int HORARIO_MANANA_FIN = 12;

    /**
     * Hora de inicio del horario laboral - tarde (formato 24 horas).
     */
    public static final int HORARIO_TARDE_INICIO = 14;

    /**
     * Hora de fin del horario laboral - tarde (formato 24 horas).
     */
    public static final int HORARIO_TARDE_FIN = 18;

    /**
     * Máximo de citas que un cliente puede agendar por día.
     */
    public static final int MAX_CITAS_POR_DIA_CLIENTE = 3;

    // ==================== Tipos de Movimiento de Inventario ====================

    /**
     * Tipo de movimiento: Entrada (IN).
     */
    public static final String TIPO_MOVIMIENTO_ENTRADA = "IN";

    /**
     * Tipo de movimiento: Salida (OUT).
     */
    public static final String TIPO_MOVIMIENTO_SALIDA = "OUT";

    /**
     * Tipo de movimiento: Ajuste (AJUSTE).
     */
    public static final String TIPO_MOVIMIENTO_AJUSTE = "AJUSTE";

    // ==================== Estados de Factura ====================

    /**
     * Estado de factura: Pendiente.
     */
    public static final String ESTADO_FACTURA_PENDIENTE = "PENDIENTE";

    /**
     * Estado de factura: Pagada.
     */
    public static final String ESTADO_FACTURA_PAGADA = "PAGADA";

    /**
     * Estado de factura: Anulada.
     */
    public static final String ESTADO_FACTURA_ANULADA = "ANULADA";

    // ==================== Estados de Notificación ====================

    /**
     * Estado de notificación: Pendiente.
     */
    public static final String ESTADO_NOTIFICACION_PENDIENTE = "PENDIENTE";

    /**
     * Estado de notificación: Enviada.
     */
    public static final String ESTADO_NOTIFICACION_ENVIADA = "ENVIADA";

    /**
     * Estado de notificación: Fallida.
     */
    public static final String ESTADO_NOTIFICACION_FALLIDA = "FALLIDA";

    // ==================== Canales de Notificación ====================

    /**
     * Canal de notificación: Email.
     */
    public static final String CANAL_EMAIL = "EMAIL";

    /**
     * Canal de notificación: App.
     */
    public static final String CANAL_APP = "APP";

    // ==================== Roles del Sistema ====================

    /**
     * Rol: Administrador.
     */
    public static final String ROL_ADMIN = "ADMIN";

    /**
     * Rol: Veterinario.
     */
    public static final String ROL_VETERINARIO = "VETERINARIO";

    /**
     * Rol: Secretario.
     */
    public static final String ROL_SECRETARIO = "SECRETARIO";

    /**
     * Rol: Cliente.
     */
    public static final String ROL_CLIENTE = "CLIENTE";

    // ==================== Configuración de Archivos ====================

    /**
     * Tamaño máximo de archivo en bytes (10MB).
     */
    public static final long MAX_FILE_SIZE = 10485760L;

    /**
     * Directorio de carga de archivos por defecto.
     */
    public static final String DEFAULT_UPLOAD_DIR = "./uploads";

    // ==================== Validaciones ====================

    /**
     * Longitud mínima de contraseña.
     */
    public static final int MIN_PASSWORD_LENGTH = 8;

    /**
     * Longitud máxima de nombre de usuario.
     */
    public static final int MAX_USERNAME_LENGTH = 60;

    /**
     * Longitud mínima de nombre de usuario.
     */
    public static final int MIN_USERNAME_LENGTH = 3;
}

