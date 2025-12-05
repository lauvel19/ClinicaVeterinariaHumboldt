import { getApiClient } from "../../../shared/api/ApiClient";
import { unwrapResponse } from "../../../shared/api/ApiResponseAdapter";
import type { ApiResponse } from "../../../shared/api/types";

import type { ApiCitaResponse } from "../../shared/types/backend";

const BASE_PATH = "/citas";
const SOLICITUDES_BASE_PATH = "/solicitudes-citas";

export interface CitaRequest {
  readonly pacienteId: number;
  readonly veterinarioId: number;
  readonly fechaHora: string;
  readonly tipoServicio?: string;
  readonly motivo?: string;
  readonly triageNivel?: string;
}

export interface CitaReprogramarRequest {
  readonly nuevaFechaHora: string;
}

export interface CitaCancelarRequest {
  readonly motivo: string;
}

export interface SolicitudCitaRequest {
  readonly clienteId: number;
  readonly pacienteId: number;
  readonly fechaSolicitada: string; // YYYY-MM-DD
  readonly horaSolicitada: string;  // HH:mm
  readonly tipoServicio: string;
  readonly motivo: string;
  readonly observaciones?: string;
}

export interface SolicitudCitaResponse {
  readonly idSolicitud: number;
  readonly clienteId: number;
  readonly pacienteId: number;
  readonly fechaSolicitada: string;
  readonly horaSolicitada: string;
  readonly tipoServicio: string;
  readonly motivo: string;
  readonly estado: 'PENDIENTE' | 'APROBADA' | 'RECHAZADA' | 'CANCELADA';
  readonly motivoRechazo?: string;
  readonly citaId?: number;
  readonly observaciones?: string;
  readonly createdAt: string;
  readonly updatedAt: string;
  readonly createdBy?: string;
  readonly updatedBy?: string;
  // Audit trail
  readonly aprobadoPor?: number;
  readonly aprobadoEn?: string;
  readonly rechazadoPor?: number;
  readonly rechazadoEn?: string;
  readonly canceladoPor?: number;
  readonly canceladoEn?: string;
}

export const CitasRepository = {
  getAll: async (): Promise<ApiCitaResponse[]> => {
    const client = getApiClient();
    const { data } = await client.get<ApiResponse<ApiCitaResponse[]>>(BASE_PATH);
    return unwrapResponse(data);
  },

  getByVeterinario: async (veterinarioId: number): Promise<ApiCitaResponse[]> => {
    const client = getApiClient();
    const { data } = await client.get<ApiResponse<ApiCitaResponse[]>>(`${BASE_PATH}/veterinario/${veterinarioId}`);
    return unwrapResponse(data);
  },

  getByVeterinarioPaginado: async (veterinarioId: number, page: number = 0, size: number = 10): Promise<{ content: ApiCitaResponse[]; totalElements: number; totalPages: number }> => {
    const client = getApiClient();
    const { data } = await client.get<ApiResponse<{ content: ApiCitaResponse[]; totalElements: number; totalPages: number }>>(
      `${BASE_PATH}/veterinario/${veterinarioId}/paginado?page=${page}&size=${size}`
    );
    return unwrapResponse(data);
  },

  getByPaciente: async (pacienteId: number): Promise<ApiCitaResponse[]> => {
    const client = getApiClient();
    const { data } = await client.get<ApiResponse<ApiCitaResponse[]>>(`${BASE_PATH}/paciente/${pacienteId}`);
    return unwrapResponse(data);
  },

  getByPacientePaginado: async (pacienteId: number, page: number = 0, size: number = 10): Promise<{ content: ApiCitaResponse[]; totalElements: number; totalPages: number }> => {
    const client = getApiClient();
    const { data } = await client.get<ApiResponse<{ content: ApiCitaResponse[]; totalElements: number; totalPages: number }>>(
      `${BASE_PATH}/paciente/${pacienteId}/paginado?page=${page}&size=${size}`
    );
    return unwrapResponse(data);
  },

  create: async (request: CitaRequest): Promise<ApiCitaResponse> => {
    const client = getApiClient();
    const { data } = await client.post<ApiResponse<ApiCitaResponse>>(BASE_PATH, request);
    return unwrapResponse(data);
  },

  reprogramar: async (citaId: number, request: CitaReprogramarRequest): Promise<ApiCitaResponse> => {
    const client = getApiClient();
    const { data } = await client.put<ApiResponse<ApiCitaResponse>>(`${BASE_PATH}/${citaId}/reprogramar`, request);
    return unwrapResponse(data);
  },

  cancelar: async (citaId: number, request: CitaCancelarRequest): Promise<ApiCitaResponse> => {
    const client = getApiClient();
    const { data } = await client.put<ApiResponse<ApiCitaResponse>>(`${BASE_PATH}/${citaId}/cancelar`, request);
    return unwrapResponse(data);
  },

  completar: async (citaId: number, datosConsulta?: {
    diagnostico: string;
    tratamiento: string;
    signosVitales?: {
      peso?: number;
      temperatura?: number;
      frecuenciaCardiaca?: number;
      frecuenciaRespiratoria?: number;
    };
    observaciones?: string;
  }): Promise<ApiCitaResponse> => {
    const client = getApiClient();
    const { data } = await client.put<ApiResponse<ApiCitaResponse>>(`${BASE_PATH}/${citaId}/completar`, datosConsulta || {});
    return unwrapResponse(data);
  },

  verificarDisponibilidad: async (veterinarioId: number, fechaHora: string): Promise<boolean> => {
    const client = getApiClient();
    const { data } = await client.get<ApiResponse<boolean>>(
      `${BASE_PATH}/disponibilidad?veterinarioId=${veterinarioId}&fechaHora=${encodeURIComponent(fechaHora)}`
    );
    return unwrapResponse(data);
  },

  // ============ SOLICITUDES DE CITA (PORTAL DEL CLIENTE) ============

  crearSolicitud: async (request: SolicitudCitaRequest): Promise<SolicitudCitaResponse> => {
    const client = getApiClient();
    const { data } = await client.post<ApiResponse<SolicitudCitaResponse>>(
      SOLICITUDES_BASE_PATH,
      request
    );
    return unwrapResponse(data);
  },

  obtenerMisSolicitudes: async (): Promise<SolicitudCitaResponse[]> => {
    const client = getApiClient();
    const { data } = await client.get<ApiResponse<SolicitudCitaResponse[]>>(SOLICITUDES_BASE_PATH);
    return unwrapResponse(data);
  },

  obtenerSolicitud: async (id: number): Promise<SolicitudCitaResponse> => {
    const client = getApiClient();
    const { data } = await client.get<ApiResponse<SolicitudCitaResponse>>(`${SOLICITUDES_BASE_PATH}/${id}`);
    return unwrapResponse(data);
  },

  obtenerSolicitudesPendientes: async (): Promise<SolicitudCitaResponse[]> => {
    const client = getApiClient();
    const { data } = await client.get<ApiResponse<SolicitudCitaResponse[]>>(
      `${SOLICITUDES_BASE_PATH}/pendientes`
    );
    return unwrapResponse(data);
  },

  aprobarSolicitud: async (id: number, veterinarioId: number): Promise<SolicitudCitaResponse> => {
    const client = getApiClient();
    const { data } = await client.put<ApiResponse<SolicitudCitaResponse>>(
      `${SOLICITUDES_BASE_PATH}/${id}/aprobar?veterinarioId=${veterinarioId}`,
      {}
    );
    return unwrapResponse(data);
  },

  rechazarSolicitud: async (id: number, motivo: string): Promise<SolicitudCitaResponse> => {
    const client = getApiClient();
    const { data } = await client.put<ApiResponse<SolicitudCitaResponse>>(
      `${SOLICITUDES_BASE_PATH}/${id}/rechazar?motivo=${encodeURIComponent(motivo)}`,
      {}
    );
    return unwrapResponse(data);
  },

  cancelarSolicitud: async (id: number): Promise<SolicitudCitaResponse> => {
    const client = getApiClient();
    const { data } = await client.put<ApiResponse<SolicitudCitaResponse>>(
      `${SOLICITUDES_BASE_PATH}/${id}/cancelar`,
      {}
    );
    return unwrapResponse(data);
  },

  obtenerEstadisticasPendientes: async (): Promise<{ total: number }> => {
    const client = getApiClient();
    const { data } = await client.get<ApiResponse<{ total: number }>>(
      `${SOLICITUDES_BASE_PATH}/estadisticas/pendientes`
    );
    return unwrapResponse(data);
  },

  // ============ HORARIOS DISPONIBLES ============

  obtenerHorariosDelDia: async (veterinarioId: number, fecha: string): Promise<HorarioDisponibilidad[]> => {
    const client = getApiClient();
    const { data } = await client.get<ApiResponse<HorarioDisponibilidad[]>>(
      `${BASE_PATH}/horarios-disponibles?veterinarioId=${veterinarioId}&fecha=${fecha}`
    );
    return unwrapResponse(data);
  },
};

export interface HorarioDisponibilidad {
  readonly fechaHora: string;
  readonly disponible: boolean;
  readonly estado: "DISPONIBLE" | "OCUPADO" | "FUERA_HORARIO" | "BLOQUEADO";
  readonly duracionMinutos: number;
  readonly citaId?: number;
  readonly nombrePaciente?: string;
}



