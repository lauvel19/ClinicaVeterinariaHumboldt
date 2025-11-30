export interface PagoOnlineRequest {
  readonly nombrePagador: string;
  readonly emailPagador: string;
  readonly telefonoPagador: string;
}

export interface PagoOnlineResponse {
  readonly idPagoOnline: number;
  readonly facturaId: number;
  readonly numeroFactura: string;
  readonly referenciaEpayco: string;
  readonly monto: string;
  readonly moneda: string;
  readonly estado: EstadoPago;
  readonly urlPago: string;
  readonly fechaCreacion: string;
}

export type EstadoPago = 
  | "PENDIENTE"
  | "PROCESANDO"
  | "APROBADA"
  | "RECHAZADA"
  | "FALLIDA"
  | "CANCELADA";

export interface PagoOnlineDetalle extends PagoOnlineResponse {
  readonly refPayco?: string;
  readonly nombrePagador?: string;
  readonly emailPagador?: string;
  readonly telefonoPagador?: string;
  readonly metodoPago?: string;
  readonly banco?: string;
  readonly codigoRespuesta?: string;
  readonly mensajeRespuesta?: string;
  readonly codigoAutorizacion?: string;
  readonly reciboPago?: string;
  readonly fechaTransaccion?: string;
  readonly fechaAprobacion?: string;
  readonly ipCliente?: string;
}
