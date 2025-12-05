import { useState } from "react";
import { useForm } from "react-hook-form";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import dayjs from "dayjs";
import toast from "react-hot-toast";

import { ConsultasRepository, type ServicioPrestadoRequest } from "../services/ConsultasRepository";
import type { ApiCitaResponse } from "../../shared/types/backend";

interface CreateServicioPrestadoModalProps {
  readonly isOpen: boolean;
  readonly cita: ApiCitaResponse | null;
  readonly onClose: () => void;
}

interface FormData {
  servicioId: string;
  fechaEjecucion: string;
  observaciones: string;
  costoTotal: string;
}

export const CreateServicioPrestadoModal = ({ isOpen, cita, onClose }: CreateServicioPrestadoModalProps) => {
  const queryClient = useQueryClient();
  const { register, handleSubmit, formState: { errors }, reset } = useForm<FormData>({
    defaultValues: {
      fechaEjecucion: dayjs().format("YYYY-MM-DDTHH:mm"),
      costoTotal: "0",
    },
  });

  const mutation = useMutation({
    mutationFn: (data: ServicioPrestadoRequest) => ConsultasRepository.create(data),
    onSuccess: () => {
      toast.success("Servicio prestado registrado exitosamente");
      if (cita) {
        queryClient.invalidateQueries({ queryKey: ["servicios-prestados", cita.idCita] });
      }
      reset();
      onClose();
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || "Error al registrar el servicio prestado");
    },
  });

  const onSubmit = (data: FormData) => {
    if (!cita) {
      toast.error("No se pudo obtener la información de la cita");
      return;
    }

    const request: ServicioPrestadoRequest = {
      citaId: cita.idCita,
      servicioId: parseInt(data.servicioId),
      fechaEjecucion: data.fechaEjecucion || undefined,
      observaciones: data.observaciones || undefined,
      costoTotal: parseFloat(data.costoTotal),
    };
    mutation.mutate(request);
  };

  if (!isOpen || !cita) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
      <div className="w-full max-w-2xl rounded-2xl bg-white shadow-xl">
        <div className="border-b border-gray-200 px-6 py-4 bg-gradient-to-r from-blue-50 to-cyan-50">
          <div className="flex items-center justify-between">
            <div>
              <h2 className="text-xl font-semibold text-gray-900">📋 Registrar Servicio Médico</h2>
              <p className="mt-1 text-sm text-gray-600">
                <span className="font-medium">Paciente:</span> {cita.paciente?.nombre || "Sin nombre"} • 
                <span className="ml-2"><span className="font-medium">Cita:</span> {dayjs(cita.fechaHora).format("DD/MM/YYYY HH:mm")}</span>
              </p>
            </div>
            <button
              onClick={onClose}
              className="rounded-lg p-2 text-gray-400 transition-all hover:bg-gray-100 hover:text-gray-600"
            >
              ✕
            </button>
          </div>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} className="p-6">
          <div className="space-y-5">
            {/* Alerta informativa */}
            <div className="rounded-lg bg-blue-50 border border-blue-200 p-4">
              <div className="flex gap-3">
                <div className="flex-shrink-0">
                  <svg className="h-5 w-5 text-blue-600" fill="currentColor" viewBox="0 0 20 20">
                    <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clipRule="evenodd" />
                  </svg>
                </div>
                <div>
                  <p className="text-sm font-medium text-blue-900">Información importante</p>
                  <p className="mt-1 text-xs text-blue-700">
                    Registra el tipo de servicio médico realizado. Esto se incluirá en la factura del cliente.
                  </p>
                </div>
              </div>
            </div>

            <div>
              <label className="mb-2 block text-sm font-medium text-gray-700">
                🏥 Tipo de Servicio Médico <span className="text-red-500">*</span>
              </label>
              <input
                type="number"
                {...register("servicioId", {
                  required: "Debes seleccionar el tipo de servicio",
                  min: { value: 1, message: "ID inválido" },
                })}
                className="w-full rounded-lg border border-gray-300 px-4 py-2.5 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/20"
                placeholder="Ej: 1 (Consulta General), 2 (Vacunación), 3 (Cirugía)..."
              />
              <p className="mt-1.5 text-xs text-gray-500 flex items-start gap-1">
                <svg className="h-4 w-4 text-gray-400 flex-shrink-0 mt-0.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                <span>
                  <strong>Códigos comunes:</strong> 1 = Consulta General, 2 = Vacunación, 3 = Desparasitación, 4 = Cirugía, 5 = Exámenes de Laboratorio
                  <br />
                  <em className="text-gray-400">Consulta con administración para más códigos</em>
                </span>
              </p>
              {errors.servicioId && <p className="mt-1.5 text-xs text-red-600 flex items-center gap-1">
                <svg className="h-4 w-4" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
                </svg>
                {errors.servicioId.message}
              </p>}
            </div>

            <div>
              <label className="mb-2 block text-sm font-medium text-gray-700">
                📅 Fecha y Hora del Servicio
              </label>
              <input
                type="datetime-local"
                {...register("fechaEjecucion")}
                className="w-full rounded-lg border border-gray-300 px-4 py-2.5 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/20"
              />
              <p className="mt-1.5 text-xs text-gray-500">Por defecto se usa la fecha y hora actual</p>
            </div>

            <div>
              <label className="mb-2 block text-sm font-medium text-gray-700">
                💰 Costo del Servicio (COP) <span className="text-red-500">*</span>
              </label>
              <div className="relative">
                <span className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-500">$</span>
                <input
                  type="number"
                  step="0.01"
                  min="0.01"
                  {...register("costoTotal", {
                    required: "El costo es obligatorio",
                    min: { value: 0.01, message: "El costo debe ser mayor a 0" },
                  })}
                  className="w-full rounded-lg border border-gray-300 pl-8 pr-4 py-2.5 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/20"
                  placeholder="85000.00"
                />
              </div>
              <p className="mt-1.5 text-xs text-gray-500">Valor en pesos colombianos que se cobrará al cliente</p>
              {errors.costoTotal && <p className="mt-1.5 text-xs text-red-600 flex items-center gap-1">
                <svg className="h-4 w-4" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
                </svg>
                {errors.costoTotal.message}
              </p>}
            </div>

            <div>
              <label className="mb-2 block text-sm font-medium text-gray-700">
                📝 Notas u Observaciones
              </label>
              <textarea
                {...register("observaciones")}
                rows={3}
                className="w-full rounded-lg border border-gray-300 px-4 py-2.5 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/20"
                placeholder="Ej: Paciente respondió bien al tratamiento, se recomienda control en 15 días..."
              />
              <p className="mt-1.5 text-xs text-gray-500">Opcional: Agrega cualquier detalle relevante del servicio</p>
            </div>
          </div>

          <div className="mt-6 flex gap-3 justify-end border-t border-gray-200 pt-4">
            <button
              type="button"
              onClick={onClose}
              className="rounded-lg border border-gray-300 bg-white px-5 py-2.5 text-sm font-medium text-gray-700 transition-all hover:bg-gray-50"
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={mutation.isPending}
              className="rounded-lg bg-primary px-5 py-2.5 text-sm font-medium text-white transition-all hover:bg-primary/90 disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
            >
              {mutation.isPending ? (
                <>
                  <svg className="animate-spin h-4 w-4" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
                  </svg>
                  Guardando...
                </>
              ) : (
                <>
                  <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                  </svg>
                  Registrar Servicio
                </>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

