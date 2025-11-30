import { useState } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import toast from "react-hot-toast";

import { PagoOnlineRepository } from "../services/PagoOnlineRepository";
import type { PagoOnlineRequest } from "../types/pagoOnline.types";

interface PagoOnlineModalProps {
  readonly isOpen: boolean;
  readonly facturaId: number;
  readonly numeroFactura: string;
  readonly montoTotal: string;
  readonly nombreCliente?: string;
  readonly emailCliente?: string;
  readonly telefonoCliente?: string;
  readonly onClose: () => void;
}

export const PagoOnlineModal = ({
  isOpen,
  facturaId,
  numeroFactura,
  montoTotal,
  nombreCliente = "",
  emailCliente = "",
  telefonoCliente = "",
  onClose,
}: PagoOnlineModalProps) => {
  const queryClient = useQueryClient();
  const [formData, setFormData] = useState<PagoOnlineRequest>({
    nombrePagador: nombreCliente,
    emailPagador: emailCliente,
    telefonoPagador: telefonoCliente,
  });

  const [errors, setErrors] = useState<Partial<Record<keyof PagoOnlineRequest, string>>>({});

  const pagoMutation = useMutation({
    mutationFn: (request: PagoOnlineRequest) =>
      PagoOnlineRepository.iniciarPago(facturaId, request),
    onSuccess: (response) => {
      toast.success("Redirigiendo a ePayco...");
      queryClient.invalidateQueries({ queryKey: ["factura", facturaId] });
      
      // Redirigir a la URL de pago de ePayco
      window.location.href = response.urlPago;
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || "Error al iniciar el pago");
    },
  });

  const validateForm = (): boolean => {
    const newErrors: Partial<Record<keyof PagoOnlineRequest, string>> = {};

    if (!formData.nombrePagador.trim()) {
      newErrors.nombrePagador = "El nombre es requerido";
    }

    if (!formData.emailPagador.trim()) {
      newErrors.emailPagador = "El email es requerido";
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.emailPagador)) {
      newErrors.emailPagador = "Email inválido";
    }

    if (!formData.telefonoPagador.trim()) {
      newErrors.telefonoPagador = "El teléfono es requerido";
    } else if (!/^\d{10,15}$/.test(formData.telefonoPagador.replace(/\s/g, ""))) {
      newErrors.telefonoPagador = "Teléfono inválido (10-15 dígitos)";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!validateForm()) {
      return;
    }

    pagoMutation.mutate(formData);
  };

  const handleClose = () => {
    if (pagoMutation.isPending) return;
    setFormData({
      nombrePagador: nombreCliente,
      emailPagador: emailCliente,
      telefonoPagador: telefonoCliente,
    });
    setErrors({});
    onClose();
  };

  if (!isOpen) return null;

  const monto = parseFloat(montoTotal);

  return (
    <div className="fixed inset-0 z-60 flex items-center justify-center bg-black/60 p-4">
      <div className="w-full max-w-md rounded-3xl bg-white p-6 shadow-soft">
        <div className="mb-6 flex items-center justify-between">
          <h3 className="text-xl font-semibold text-secondary">Pago Online - ePayco</h3>
          <button
            onClick={handleClose}
            disabled={pagoMutation.isPending}
            className="rounded-full p-2 text-gray-400 transition-colors hover:bg-gray-100 hover:text-gray-600 disabled:opacity-50"
          >
            ✕
          </button>
        </div>

        <div className="mb-6 rounded-2xl border border-gray-200 bg-gray-50 p-4">
          <p className="text-sm font-semibold text-gray-500">Resumen de Pago</p>
          <p className="mt-1 text-sm text-gray-600">Factura: {numeroFactura}</p>
          <p className="mt-2 text-2xl font-semibold text-secondary">
            {monto.toLocaleString("es-CO", { style: "currency", currency: "COP" })}
          </p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="mb-2 block text-sm font-semibold text-secondary">
              Nombre completo *
            </label>
            <input
              type="text"
              value={formData.nombrePagador}
              onChange={(e) =>
                setFormData({ ...formData, nombrePagador: e.target.value })
              }
              disabled={pagoMutation.isPending}
              className={`w-full rounded-2xl border ${
                errors.nombrePagador ? "border-danger" : "border-gray-200"
              } bg-white px-4 py-2 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/30 disabled:bg-gray-50`}
              placeholder="Ej: Juan Pérez"
            />
            {errors.nombrePagador && (
              <p className="mt-1 text-xs text-danger">{errors.nombrePagador}</p>
            )}
          </div>

          <div>
            <label className="mb-2 block text-sm font-semibold text-secondary">
              Correo electrónico *
            </label>
            <input
              type="email"
              value={formData.emailPagador}
              onChange={(e) =>
                setFormData({ ...formData, emailPagador: e.target.value })
              }
              disabled={pagoMutation.isPending}
              className={`w-full rounded-2xl border ${
                errors.emailPagador ? "border-danger" : "border-gray-200"
              } bg-white px-4 py-2 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/30 disabled:bg-gray-50`}
              placeholder="Ej: juan@example.com"
            />
            {errors.emailPagador && (
              <p className="mt-1 text-xs text-danger">{errors.emailPagador}</p>
            )}
          </div>

          <div>
            <label className="mb-2 block text-sm font-semibold text-secondary">
              Teléfono *
            </label>
            <input
              type="tel"
              value={formData.telefonoPagador}
              onChange={(e) =>
                setFormData({ ...formData, telefonoPagador: e.target.value })
              }
              disabled={pagoMutation.isPending}
              className={`w-full rounded-2xl border ${
                errors.telefonoPagador ? "border-danger" : "border-gray-200"
              } bg-white px-4 py-2 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/30 disabled:bg-gray-50`}
              placeholder="Ej: 3001234567"
            />
            {errors.telefonoPagador && (
              <p className="mt-1 text-xs text-danger">{errors.telefonoPagador}</p>
            )}
            <p className="mt-1 text-xs text-gray-500">
              Solo números, 10-15 dígitos
            </p>
          </div>

          <div className="rounded-2xl border border-blue-200 bg-blue-50 p-4">
            <p className="text-xs text-blue-600">
              <strong>Nota:</strong> Serás redirigido a la plataforma segura de ePayco
              para completar el pago. Puedes pagar con tarjeta de crédito, débito o PSE.
            </p>
          </div>

          <div className="flex justify-end gap-3 pt-2">
            <button
              type="button"
              onClick={handleClose}
              disabled={pagoMutation.isPending}
              className="rounded-2xl border border-gray-200 bg-white px-4 py-2 text-sm font-semibold text-secondary transition-base hover:bg-gray-50 disabled:opacity-50"
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={pagoMutation.isPending}
              className="rounded-2xl bg-primary px-6 py-2 text-sm font-semibold text-white shadow-soft transition-base hover:bg-primary-dark disabled:opacity-50"
            >
              {pagoMutation.isPending ? "Procesando..." : "Ir a ePayco"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
