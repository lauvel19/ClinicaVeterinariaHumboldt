// Tipos que reflejan las respuestas expuestas por el backend (DTOs Java).
// Se utilizan en los repositorios para mantener tipado fuerte en toda la app.

export interface ApiCitaResponse {
  readonly idCita: number;
  readonly fechaHora: string;
  readonly estado: string;
  readonly tipoServicio: string | null;
  readonly motivo: string | null;
  readonly triageNivel: string | null;
  readonly paciente: {
    readonly id: number | null;
    readonly nombre: string | null;
    readonly especie: string | null;
    readonly propietario: string | null;
  } | null;
  readonly veterinario: {
    readonly id: number | null;
    readonly nombreCompleto: string | null;
    readonly especialidad: string | null;
  } | null;
}

export interface ApiPacienteResponse {
  readonly id: number;
  readonly nombre: string;
  readonly especie: string;
  readonly raza: string | null;
  readonly fechaNacimiento: string | null;
  readonly sexo: string | null;
  readonly pesoKg: string | null;
  readonly estadoSalud: string | null;
  readonly fotoPerfil: string | null;
  readonly cliente: {
    readonly id: number;
    readonly nombre: string;
    readonly apellido: string;
    readonly correo: string;
    readonly telefono: string | null;
  } | null;
  readonly identificadorExterno: string | null;
}

export interface ApiVacunacionResponse {
  readonly id: number;
  readonly paciente: {
    readonly id: number;
    readonly nombre: string;
  };
  readonly tipoVacuna: string;
  readonly fechaAplicacion: string;
  readonly proximaDosis: string | null;
  readonly veterinario: {
    readonly id: number;
    readonly nombre: string;
    readonly apellido: string;
    readonly especialidad: string | null;
  } | null;
}

export interface ApiHistoriaClinicaResponse {
  readonly id: number;
  readonly paciente: {
    readonly id: number;
    readonly nombre: string;
  };
  readonly fechaApertura: string;
  readonly resumen: string;
  readonly metadatos: Record<string, unknown> | null;
}

export interface ApiRegistroMedicoResponse {
  readonly id: number;
  readonly historiaId: number;
  readonly fecha: string;
  readonly motivo: string | null;
  readonly diagnostico: string | null;
  readonly signosVitales: Record<string, unknown> | null;
  readonly tratamiento: string | null;
  readonly veterinario: {
    readonly id: number;
    readonly nombre: string;
    readonly apellido: string;
    readonly especialidad: string | null;
  } | null;
  readonly insumosUsados: Array<Record<string, unknown>> | null;
  readonly archivos: string[] | null;
}

export interface ApiServicioPrestadoResponse {
  readonly idPrestado: number;
  readonly fechaEjecucion: string;
  readonly observaciones: string | null;
  readonly costoTotal: string;
  readonly insumos: Array<{
    readonly productoId: number | null;
    readonly cantidad: string | null;
    readonly precioUnitario: string | null;
  }>;
  readonly cita: ApiCitaResponse | null;
  readonly servicio: {
    readonly id: number | null;
    readonly nombre: string | null;
    readonly precioBase: string | null;
  } | null;
}

export interface ApiFacturaResponse {
  readonly idFactura: number;
  readonly numero: string;
  readonly fechaEmision: string;
  readonly total: string;
  readonly formaPago: string | null;
  readonly estado: string;
  readonly contenido: Record<string, unknown> | null;
  readonly cliente: {
    readonly id: number;
    readonly nombreCompleto: string;
    readonly correo: string;
    readonly telefono: string | null;
  } | null;
}

export interface ApiUsuarioResponse {
  readonly id: number;
  readonly nombre: string;
  readonly apellido: string;
  readonly correo: string;
  readonly telefono: string | null;
  readonly direccion: string | null;
  readonly username: string;
  readonly activo: boolean;
  readonly ultimoAcceso: string | null;
  readonly rol: {
    readonly id: number;
    readonly nombre: string;
    readonly descripcion: string | null;
    readonly permisos: Array<{
      readonly id: number;
      readonly nombre: string;
      readonly descripcion: string | null;
    }>;
  } | null;
}

export interface ApiProductoResponse {
  readonly id: number;
  readonly sku: string;
  readonly nombre: string;
  readonly descripcion: string | null;
  readonly tipo: string | null;
  readonly stock: number;
  readonly precioUnitario: string;
  readonly um: string | null;
  readonly metadatos: Record<string, unknown> | null;
}

export interface ApiMovimientoInventarioResponse {
  readonly id: number;
  readonly tipoMovimiento: string;
  readonly cantidad: number;
  readonly fecha: string;
  readonly referencia: string | null;
  readonly producto: {
    readonly id: number;
    readonly sku: string;
    readonly nombre: string;
  } | null;
  readonly proveedor: {
    readonly id: number;
    readonly nombre: string;
  } | null;
  readonly usuario: {
    readonly id: number;
    readonly username: string;
    readonly nombre: string;
    readonly apellido: string;
  } | null;
  readonly stockResultante: number;
}

export interface ApiClienteResponse {
  readonly id: number;
  readonly nombre: string;
  readonly apellido: string;
  readonly correo: string;
  readonly telefono: string | null;
  readonly direccion: string | null;
  readonly identificacion: string | null;
}

export interface ApiNotificacionResponse {
  readonly id: number;
  readonly tipo: string;
  readonly mensaje: string;
  readonly estado: string;
  readonly fechaEnvioProgramada: string | null;
  readonly fechaEnvioReal: string | null;
  readonly datos: Record<string, unknown> | null;
}

export interface ApiEstadisticaResponse {
  readonly id: number;
  readonly nombre: string;
  readonly valor: string;
  readonly periodoInicio: string | null;
  readonly periodoFin: string | null;
}

export interface ApiReporteResponse {
  readonly id: number;
  readonly nombre: string;
  readonly tipo: string | null;
  readonly fechaGeneracion: string;
  readonly generadoPor: number | null;
  readonly parametros: Record<string, unknown> | null;
  readonly estadisticas: ApiEstadisticaResponse[] | null;
}

export interface ApiProveedorResponse {
  readonly idProveedor: number;
  readonly nombre: string;
  readonly contacto: string | null;
  readonly telefono: string | null;
  readonly direccion: string | null;
  readonly correo: string | null;
}

export interface ApiDesparasitacionResponse {
  readonly idDesparasitacion: number;
  readonly paciente: {
    readonly id: number;
    readonly nombre: string;
  };
  readonly productoUsado: string;
  readonly fechaAplicacion: string;
  readonly proximaAplicacion: string | null;
}


