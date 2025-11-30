import { useEffect, useState } from "react";
import { useSearchParams, useNavigate } from "react-router-dom";
import dayjs from "dayjs";
import "dayjs/locale/es";

dayjs.locale("es");

type EstadoPagoEpayco = 
  | "Aceptada"
  | "Rechazada"
  | "Pendiente"
  | "Fallida";

interface ResultadoPago {
  readonly refPayco: string;
  readonly factura: string;
  readonly estado: EstadoPagoEpayco;
  readonly monto: string;
  readonly moneda: string;
  readonly banco?: string;
  readonly descripcion?: string;
  readonly fecha: string;
}

export const PagoResultadoPage = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [resultado, setResultado] = useState<ResultadoPago | null>(null);

  useEffect(() => {
    // Extraer parámetros de ePayco de la URL
    const refPayco = searchParams.get("ref_payco") || searchParams.get("x_ref_payco") || "";
    const factura = searchParams.get("x_id_invoice") || searchParams.get("x_extra1") || "";
    const estado = searchParams.get("x_response") as EstadoPagoEpayco || "Pendiente";
    const monto = searchParams.get("x_amount") || "0";
    const moneda = searchParams.get("x_currency_code") || "COP";
    const banco = searchParams.get("x_bank_name") || undefined;
    const descripcion = searchParams.get("x_response_reason_text") || undefined;
    const fecha = searchParams.get("x_transaction_date") || new Date().toISOString();

    if (refPayco || factura) {
      setResultado({
        refPayco,
        factura,
        estado,
        monto,
        moneda,
        banco,
        descripcion,
        fecha,
      });
    }
  }, [searchParams]);

  const handleVolverFacturas = () => {
    navigate("/facturas");
  };

  if (!resultado) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-gray-50 p-4">
        <div className="w-full max-w-md rounded-3xl bg-white p-8 shadow-soft text-center">
          <div className="mb-4 flex justify-center">
            <div className="rounded-full bg-gray-100 p-4">
              <svg
                className="h-12 w-12 text-gray-400"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
                />
              </svg>
            </div>
          </div>
          <h2 className="mb-2 text-2xl font-semibold text-secondary">
            No hay información de pago
          </h2>
          <p className="mb-6 text-sm text-gray-500">
            No se encontraron datos de la transacción en la URL.
          </p>
          <button
            onClick={handleVolverFacturas}
            className="rounded-2xl bg-primary px-6 py-3 text-sm font-semibold text-white shadow-soft transition-base hover:bg-primary-dark"
          >
            Volver a Facturas
          </button>
        </div>
      </div>
    );
  }

  const esExitoso = resultado.estado === "Aceptada";
  const esPendiente = resultado.estado === "Pendiente";
  const esRechazado = resultado.estado === "Rechazada" || resultado.estado === "Fallida";

  const iconColor = esExitoso
    ? "text-success bg-success/10"
    : esPendiente
      ? "text-warning bg-warning/10"
      : "text-danger bg-danger/10";

  const titulo = esExitoso
    ? "¡Pago Exitoso!"
    : esPendiente
      ? "Pago Pendiente"
      : "Pago Rechazado";

  const mensaje = esExitoso
    ? "Tu pago ha sido procesado correctamente. Recibirás un correo de confirmación."
    : esPendiente
      ? "Tu pago está siendo procesado. Te notificaremos cuando se complete."
      : "El pago no pudo ser procesado. Por favor, intenta nuevamente o contacta con soporte.";

  const monto = Number.parseFloat(resultado.monto);
  const fecha = dayjs(resultado.fecha);

  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-50 p-4">
      <div className="w-full max-w-md rounded-3xl bg-white p-8 shadow-soft">
        {/* Icono de estado */}
        <div className="mb-6 flex justify-center">
          <div className={`rounded-full p-4 ${iconColor}`}>
            {esExitoso ? (
              <svg
                className="h-12 w-12"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M5 13l4 4L19 7"
                />
              </svg>
            ) : esPendiente ? (
              <svg
                className="h-12 w-12"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"
                />
              </svg>
            ) : (
              <svg
                className="h-12 w-12"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M6 18L18 6M6 6l12 12"
                />
              </svg>
            )}
          </div>
        </div>

        {/* Título y mensaje */}
        <div className="mb-6 text-center">
          <h2 className="mb-2 text-2xl font-semibold text-secondary">{titulo}</h2>
          <p className="text-sm text-gray-500">{mensaje}</p>
        </div>

        {/* Detalles del pago */}
        <div className="space-y-4 rounded-2xl border border-gray-200 bg-gray-50 p-4">
          {resultado.factura && (
            <div className="flex justify-between">
              <span className="text-sm font-semibold text-gray-500">Factura:</span>
              <span className="text-sm text-gray-600">{resultado.factura}</span>
            </div>
          )}
          
          <div className="flex justify-between">
            <span className="text-sm font-semibold text-gray-500">Monto:</span>
            <span className="text-sm font-semibold text-secondary">
              {monto.toLocaleString("es-CO", {
                style: "currency",
                currency: resultado.moneda,
              })}
            </span>
          </div>

          {resultado.refPayco && (
            <div className="flex justify-between">
              <span className="text-sm font-semibold text-gray-500">Referencia:</span>
              <span className="text-xs text-gray-600 font-mono">{resultado.refPayco}</span>
            </div>
          )}

          <div className="flex justify-between">
            <span className="text-sm font-semibold text-gray-500">Fecha:</span>
            <span className="text-sm text-gray-600">
              {fecha.format("DD/MM/YYYY HH:mm")}
            </span>
          </div>

          {resultado.banco && (
            <div className="flex justify-between">
              <span className="text-sm font-semibold text-gray-500">Banco:</span>
              <span className="text-sm text-gray-600">{resultado.banco}</span>
            </div>
          )}

          {resultado.descripcion && (
            <div className="pt-2 border-t border-gray-200">
              <p className="text-xs text-gray-500">{resultado.descripcion}</p>
            </div>
          )}
        </div>

        {/* Acciones */}
        <div className="mt-6 flex flex-col gap-3">
          <button
            onClick={handleVolverFacturas}
            className="w-full rounded-2xl bg-primary px-6 py-3 text-sm font-semibold text-white shadow-soft transition-base hover:bg-primary-dark"
          >
            Volver a Facturas
          </button>
          
          {esRechazado && resultado.factura && (
            <button
              onClick={() => navigate(`/facturas`)}
              className="w-full rounded-2xl border border-primary bg-white px-6 py-3 text-sm font-semibold text-primary transition-base hover:bg-primary hover:text-white"
            >
              Intentar nuevamente
            </button>
          )}
        </div>

        {/* Nota informativa */}
        <div className="mt-6 rounded-2xl border border-blue-200 bg-blue-50 p-4">
          <p className="text-xs text-blue-600">
            <strong>Nota:</strong> El estado de tu pago puede tardar unos minutos en 
            actualizarse. Si tienes dudas, contacta con soporte.
          </p>
        </div>
      </div>
    </div>
  );
};
