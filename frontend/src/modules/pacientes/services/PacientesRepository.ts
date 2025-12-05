import { getApiClient } from "../../../shared/api/ApiClient";
import { unwrapResponse } from "../../../shared/api/ApiResponseAdapter";
import type { ApiResponse } from "../../../shared/api/types";

import type { ApiPacienteResponse } from "../../shared/types/backend";

const BASE_PATH = "/pacientes";

export interface PacienteRequest {
  readonly nombre: string;
  readonly especie: string;
  readonly raza?: string;
  readonly fechaNacimiento?: string;
  readonly sexo?: string;
  readonly pesoKg?: number;
  readonly estadoSalud?: string;
  readonly fotoPerfil?: string;
  readonly clienteId: number;
  readonly identificadorExterno?: string;
}

export interface PacienteUpdateRequest {
  readonly nombre?: string;
  readonly especie?: string;
  readonly raza?: string;
  readonly fechaNacimiento?: string;
  readonly sexo?: string;
  readonly pesoKg?: number;
  readonly estadoSalud?: string;
  readonly fotoPerfil?: string;
}

export const PacientesRepository = {
  getAll: async (): Promise<ApiPacienteResponse[]> => {
    const client = getApiClient();
    const { data } = await client.get<ApiResponse<ApiPacienteResponse[]>>(BASE_PATH);
    return unwrapResponse(data);
  },

  getById: async (id: number): Promise<ApiPacienteResponse> => {
    const client = getApiClient();
    const { data } = await client.get<ApiResponse<ApiPacienteResponse>>(`${BASE_PATH}/${id}`);
    return unwrapResponse(data);
  },

  getByCliente: async (clienteId: number): Promise<ApiPacienteResponse[]> => {
    const client = getApiClient();
    const { data } = await client.get<ApiResponse<ApiPacienteResponse[]>>(`${BASE_PATH}/cliente/${clienteId}`);
    return unwrapResponse(data);
  },

  create: async (request: PacienteRequest): Promise<ApiPacienteResponse> => {
    const client = getApiClient();
    const { data } = await client.post<ApiResponse<ApiPacienteResponse>>(BASE_PATH, request);
    return unwrapResponse(data);
  },

  update: async (id: number, request: PacienteUpdateRequest): Promise<ApiPacienteResponse> => {
    const client = getApiClient();
    const { data } = await client.put<ApiResponse<ApiPacienteResponse>>(`${BASE_PATH}/${id}`, request);
    return unwrapResponse(data);
  },

  generarResumen: async (id: number): Promise<string> => {
    const client = getApiClient();
    const { data} = await client.get<ApiResponse<string>>(`${BASE_PATH}/${id}/resumen`);
    return unwrapResponse(data);
  },

  subirFoto: async (id: number, file: File): Promise<string> => {
    const client = getApiClient();
    const formData = new FormData();
    formData.append("file", file);
    const { data } = await client.post<ApiResponse<string>>(`${BASE_PATH}/${id}/foto`, formData, {
      headers: { "Content-Type": "multipart/form-data" },
    });
    return unwrapResponse(data);
  },
};


