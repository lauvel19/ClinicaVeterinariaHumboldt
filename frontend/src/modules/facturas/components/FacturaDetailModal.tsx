import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import dayjs from "dayjs";
import "dayjs/locale/es";
import toast from "react-hot-toast";

import { FacturasRepository, type FacturaPagoRequest } from "../services/FacturasRepository";
import { PagoOnlineModal } from "./PagoOnlineModal";
import type { ApiFacturaResponse } from "../../shared/types/backend";

dayjs.locale("es");

interface FacturaDetailModalProps {
  readonly isOpen: boolean;
  readonly facturaId: number | null;
  readonly onClose: () => void;
}

export const FacturaDetailModal = ({ isOpen, facturaId, onClose }: FacturaDetailModalProps) => {
  const queryClient = useQueryClient();
  const [isPagoModalOpen, setIsPagoModalOpen] = useState(false);
  const [isPagoOnlineModalOpen, setIsPagoOnlineModalOpen] = useState(false);
  const [formaPago, setFormaPago] = useState("");

  const { data: factura, isLoading } = useQuery({
    queryKey: ["factura", facturaId],
    queryFn: () => (facturaId ? FacturasRepository.getById(facturaId) : null),
    enabled: isOpen && facturaId !== null,
  });

  const pagoMutation = useMutation({
    mutationFn: (request: FacturaPagoRequest) =>
      facturaId ? FacturasRepository.registrarPago(facturaId, request) : Promise.reject(),
    onSuccess: () => {
      toast.success("Pago registrado exitosamente");
      queryClient.invalidateQueries({ queryKey: ["factura", facturaId] });
      queryClient.invalidateQueries({ queryKey: ["facturas"] });
      setIsPagoModalOpen(false);
      setFormaPago("");
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || "Error al registrar el pago");
    },
  });

  const anularMutation = useMutation({
    mutationFn: () => (facturaId ? FacturasRepository.anular(facturaId) : Promise.reject()),
    onSuccess: () => {
      toast.success("Factura anulada exitosamente");
      queryClient.invalidateQueries({ queryKey: ["factura", facturaId] });
      queryClient.invalidateQueries({ queryKey: ["facturas"] });
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || "Error al anular la factura");
    },
  });

  const handleGenerarPDF = async () => {
    if (!facturaId) return;
    try {
      const blob = await FacturasRepository.generarPDF(facturaId);
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.download = `factura_${factura?.numero || facturaId}.pdf`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
      toast.success("PDF generado exitosamente");
    } catch (error: any) {
      toast.error(error.response?.data?.message || "Error al generar el PDF");
    }
  };

  const handleRegistrarPago = () => {
    if (!formaPago) {
      toast.error("Debe seleccionar una forma de pago");
      return;
    }
    pagoMutation.mutate({ formaPago });
  };

  if (!isOpen || !facturaId) return null;

  if (isLoading) {
    return (
      <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50">
        <div className="rounded-3xl bg-white p-8">
          <p className="text-secondary">Cargando factura...</p>
        </div>
      </div>
    );
  }

  if (!factura) return null;

  const fecha = dayjs(factura.fechaEmision);
  const total = parseFloat(factura.total);
  const estadoTone =
    factura.estado === "PAGADA"
      ? "bg-success/20 text-success"
      : factura.estado === "ANULADA"
        ? "bg-danger/20 text-danger"
        : "bg-warning/20 text-warning";

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
      <div className="w-full max-w-2xl rounded-3xl bg-white p-6 shadow-soft max-h-[90vh] overflow-y-auto">
        <div className="mb-6 flex items-center justify-between">
          <h2 className="text-2xl font-semibold text-secondary">Detalle de Factura</h2>
          <button
            onClick={onClose}
            className="rounded-full p-2 text-gray-400 transition-colors hover:bg-gray-100 hover:text-gray-600"
          >
            ✕
          </button>
        </div>

        <div className="space-y-6">
          <div className="grid gap-4 sm:grid-cols-2">
            <div>
              <p className="text-sm font-semibold text-gray-500">Número de Factura</p>
              <p className="text-lg font-semibold text-secondary">{factura.numero}</p>
            </div>
            <div>
              <p className="text-sm font-semibold text-gray-500">Estado</p>
              <span className={`mt-1 inline-block rounded-full px-3 py-1 text-xs font-semibold ${estadoTone}`}>
                {factura.estado}
              </span>
            </div>
            <div>
              <p className="text-sm font-semibold text-gray-500">Fecha de Emisión</p>
              <p className="text-sm text-gray-600">{fecha.format("DD/MM/YYYY HH:mm")}</p>
            </div>
            <div>
              <p className="text-sm font-semibold text-gray-500">Total</p>
              <p className="text-lg font-semibold text-secondary">
                {total.toLocaleString("es-CO", { style: "currency", currency: "COP" })}
              </p>
            </div>
          </div>

          {factura.cliente && (
            <div className="rounded-2xl border border-gray-200 bg-gray-50 p-4">
              <p className="mb-2 text-sm font-semibold text-secondary">Cliente</p>
              <p className="text-sm text-gray-600">{factura.cliente.nombreCompleto}</p>
              <p className="text-xs text-gray-500">{factura.cliente.correo}</p>
              {factura.cliente.telefono && <p className="text-xs text-gray-500">{factura.cliente.telefono}</p>}
            </div>
          )}

          {factura.formaPago && (
            <div>
              <p className="text-sm font-semibold text-gray-500">Forma de Pago</p>
              <p className="text-sm text-gray-600">{factura.formaPago}</p>
            </div>
          )}

          {factura.contenido && (
            <div className="rounded-2xl border border-gray-200 bg-gray-50 p-4">
              <p className="mb-2 text-sm font-semibold text-secondary">Contenido</p>
              <pre className="text-xs text-gray-600 overflow-auto">
                {JSON.stringify(factura.contenido, null, 2)}
              </pre>
            </div>
          )}

          <div className="flex flex-wrap gap-3 pt-4">
            <button
              onClick={handleGenerarPDF}
              className="rounded-2xl border border-primary bg-white px-4 py-2 text-sm font-semibold text-primary transition-base hover:bg-primary hover:text-white"
            >
              Generar PDF
            </button>
            {factura.estado === "PENDIENTE" && (
              <>
                <button
                  onClick={() => setIsPagoOnlineModalOpen(true)}
                  className="rounded-2xl bg-primary px-4 py-2 text-sm font-semibold text-white shadow-soft transition-base hover:bg-primary-dark"
                >
                  💳 Pagar Online
                </button>
                <button
                  onClick={() => setIsPagoModalOpen(true)}
                  className="rounded-2xl bg-success px-4 py-2 text-sm font-semibold text-white shadow-soft transition-base hover:bg-success/90"
                >
                  Registrar Pago
                </button>
                <button
                  onClick={() => {
                    if (confirm("¿Está seguro de anular esta factura?")) {
                      anularMutation.mutate();
                    }
                  }}
                  disabled={anularMutation.isPending}
                  className="rounded-2xl border border-danger bg-danger/10 px-4 py-2 text-sm font-semibold text-danger transition-base hover:bg-danger/20 disabled:opacity-50"
                >
                  {anularMutation.isPending ? "Anulando..." : "Anular Factura"}
                </button>
              </>
            )}
          </div>
        </div>

        {/* Modal de pago online */}
        {factura && (
          <PagoOnlineModal
            isOpen={isPagoOnlineModalOpen}
            facturaId={factura.idFactura}
            numeroFactura={factura.numero}
            montoTotal={factura.total}
            nombreCliente={factura.cliente?.nombreCompleto}
            emailCliente={factura.cliente?.correo}
            telefonoCliente={factura.cliente?.telefono}
            onClose={() => setIsPagoOnlineModalOpen(false)}
          />
        )}

        {/* Modal de pago */}
        {isPagoModalOpen && (
          <div className="fixed inset-0 z-60 flex items-center justify-center bg-black/60">
            <div className="w-full max-w-md rounded-3xl bg-white p-6 shadow-soft">
              <h3 className="mb-4 text-xl font-semibold text-secondary">Registrar Pago</h3>
              <div className="space-y-4">
                <div>
                  <label className="mb-2 block text-sm font-semibold text-secondary">Forma de Pago *</label>
                  <select
                    value={formaPago}
                    onChange={(e) => setFormaPago(e.target.value)}
                    className="w-full rounded-2xl border border-gray-200 bg-white px-4 py-2 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/30"
                  >
                    <option value="">Seleccionar forma de pago</option>
                    <option value="EFECTIVO">Efectivo</option>
                    <option value="TARJETA">Tarjeta</option>
                    <option value="TRANSFERENCIA">Transferencia</option>
                  </select>
                </div>
                <div className="flex justify-end gap-3">
                  <button
                    onClick={() => {
                      setIsPagoModalOpen(false);
                      setFormaPago("");
                    }}
                    className="rounded-2xl border border-gray-200 bg-white px-4 py-2 text-sm font-semibold text-secondary transition-base hover:bg-gray-50"
                  >
                    Cancelar
                  </button>
                  <button
                    onClick={handleRegistrarPago}
                    disabled={pagoMutation.isPending || !formaPago}
                    className="rounded-2xl bg-success px-4 py-2 text-sm font-semibold text-white shadow-soft transition-base hover:bg-success/90 disabled:opacity-50"
                  >
                    {pagoMutation.isPending ? "Registrando..." : "Registrar Pago"}
                  </button>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

