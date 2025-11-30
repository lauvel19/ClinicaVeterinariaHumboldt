import { getApiClient } from "../../../shared/api/ApiClient";
import { unwrapResponse } from "../../../shared/api/ApiResponseAdapter";
import type { ApiResponse } from "../../../shared/api/types";

import type {
  PagoOnlineRequest,
  PagoOnlineResponse,
  PagoOnlineDetalle,
} from "../types/pagoOnline.types";

const BASE_PATH = "/pagos-online";

export const PagoOnlineRepository = {
  /**
   * Inicia un pago online para una factura
   * @param facturaId ID de la factura
   * @param request Datos del pagador
   * @returns Respuesta con URL de pago de ePayco
   */
  iniciarPago: async (
    facturaId: number,
    request: PagoOnlineRequest
  ): Promise<PagoOnlineResponse> => {
    const client = getApiClient();
    const { data } = await client.post<ApiResponse<PagoOnlineResponse>>(
      `/facturas/${facturaId}/iniciar-pago-online`,
      request
    );
    return unwrapResponse(data);
  },

  /**
   * Consulta el estado de un pago online en ePayco
   * @param pagoId ID del pago online
   * @returns Detalle del pago actualizado
   */
  consultarEstado: async (pagoId: number): Promise<PagoOnlineDetalle> => {
    const client = getApiClient();
    const { data } = await client.post<ApiResponse<PagoOnlineDetalle>>(
      `${BASE_PATH}/${pagoId}/consultar-estado`
    );
    return unwrapResponse(data);
  },

  /**
   * Obtiene los detalles de un pago online
   * @param pagoId ID del pago online
   * @returns Detalle del pago
   */
  obtenerDetalle: async (pagoId: number): Promise<PagoOnlineDetalle> => {
    const client = getApiClient();
    const { data } = await client.get<ApiResponse<PagoOnlineDetalle>>(
      `${BASE_PATH}/${pagoId}`
    );
    return unwrapResponse(data);
  },

  /**
   * Lista todos los pagos online de una factura
   * @param facturaId ID de la factura
   * @returns Lista de pagos online
   */
  listarPorFactura: async (facturaId: number): Promise<PagoOnlineDetalle[]> => {
    const client = getApiClient();
    const { data } = await client.get<ApiResponse<PagoOnlineDetalle[]>>(
      `/facturas/${facturaId}/pagos-online`
    );
    return unwrapResponse(data);
  },
};
