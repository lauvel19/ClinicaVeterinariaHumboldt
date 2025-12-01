import { useState } from "react";
import { useForm } from "react-hook-form";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import toast from "react-hot-toast";

import { VacunacionesRepository, type VacunacionRequest } from "../services/VacunacionesRepository";
import { PacientesRepository } from "../../pacientes/services/PacientesRepository";
import { VeterinariosRepository } from "../../usuarios/services/VeterinariosRepository";

interface CreateVacunacionModalProps {
  readonly isOpen: boolean;
  readonly onClose: () => void;
  readonly pacienteId?: number;
}

interface FormData {
  pacienteId: string;
  tipoVacuna: string;
  fechaAplicacion: string;
  proximaDosis: string;
  lote: string;
  observaciones: string;
  veterinarioId: string;
}

export const CreateVacunacionModal = ({ isOpen, onClose, pacienteId: initialPacienteId }: CreateVacunacionModalProps) => {
  const queryClient = useQueryClient();
  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
  } = useForm<FormData>({
    defaultValues: {
      pacienteId: initialPacienteId?.toString() || "",
      fechaAplicacion: new Date().toISOString().split("T")[0],
    },
  });

  const { data: pacientes } = useQuery({
    queryKey: ["pacientes"],
    queryFn: PacientesRepository.getAll,
    enabled: isOpen,
  });

  const { data: veterinarios } = useQuery({
    queryKey: ["veterinarios"],
    queryFn: () => VeterinariosRepository.getAll(),
    enabled: isOpen,
  });

  const mutation = useMutation({
    mutationFn: (data: VacunacionRequest) => VacunacionesRepository.registrar(data),
    onSuccess: () => {
      toast.success("Vacunación registrada exitosamente");
      queryClient.invalidateQueries({ queryKey: ["vacunaciones"] });
      queryClient.invalidateQueries({ queryKey: ["vacunaciones-pendientes"] });
      reset();
      onClose();
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || "Error al registrar la vacunación");
    },
  });

  const onSubmit = (data: FormData) => {
    const request: VacunacionRequest = {
      pacienteId: parseInt(data.pacienteId, 10),
      tipoVacuna: data.tipoVacuna,
      fechaAplicacion: data.fechaAplicacion,
      proximaDosis: data.proximaDosis || undefined,
      veterinarioId: parseInt(data.veterinarioId, 10),
      lote: data.lote || undefined,
      observaciones: data.observaciones || undefined,
    };
    mutation.mutate(request);
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
      <div className="w-full max-w-2xl rounded-3xl bg-white p-6 shadow-soft max-h-[90vh] overflow-y-auto">
        <div className="mb-6 flex items-center justify-between">
          <h2 className="text-2xl font-semibold text-secondary">Registrar Vacunación</h2>
          <button
            onClick={onClose}
            className="rounded-full p-2 text-gray-400 transition-colors hover:bg-gray-100 hover:text-gray-600"
          >
            ✕
          </button>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div>
            <label className="mb-2 block text-sm font-semibold text-secondary">Paciente *</label>
            <select
              {...register("pacienteId", { required: "Debe seleccionar un paciente" })}
              disabled={!!initialPacienteId}
              className="w-full rounded-2xl border border-gray-200 bg-white px-4 py-2 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/30 disabled:bg-gray-50"
            >
              <option value="">Seleccionar paciente</option>
              {pacientes?.map((paciente) => (
                <option key={paciente.id} value={paciente.id}>
                  {paciente.nombre} ({paciente.especie}) - {paciente.cliente?.nombre} {paciente.cliente?.apellido}
                </option>
              ))}
            </select>
            {errors.pacienteId && <p className="mt-1 text-xs text-danger">{errors.pacienteId.message}</p>}
          </div>

          <div>
            <label className="mb-2 block text-sm font-semibold text-secondary">Veterinario *</label>
            <select
              {...register("veterinarioId", { required: "Debe seleccionar un veterinario" })}
              className="w-full rounded-2xl border border-gray-200 bg-white px-4 py-2 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/30"
            >
              <option value="">Seleccionar veterinario</option>
              {veterinarios?.map((vet) => (
                <option key={vet.id} value={vet.id}>
                  Dr. {vet.nombre} {vet.apellido} {vet.especialidad ? `- ${vet.especialidad}` : ""}
                </option>
              ))}
            </select>
            {errors.veterinarioId && <p className="mt-1 text-xs text-danger">{errors.veterinarioId.message}</p>}
          </div>

          <div>
            <label className="mb-2 block text-sm font-semibold text-secondary">Tipo de Vacuna *</label>
            <input
              {...register("tipoVacuna", { required: "El tipo de vacuna es obligatorio" })}
              className="w-full rounded-2xl border border-gray-200 bg-white px-4 py-2 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/30"
              placeholder="Ej: Rabia, Triple Felina, etc."
            />
            {errors.tipoVacuna && <p className="mt-1 text-xs text-danger">{errors.tipoVacuna.message}</p>}
          </div>

          <div className="grid gap-4 sm:grid-cols-2">
            <div>
              <label className="mb-2 block text-sm font-semibold text-secondary">Fecha de Aplicación *</label>
              <input
                type="date"
                {...register("fechaAplicacion", { required: "La fecha de aplicación es obligatoria" })}
                className="w-full rounded-2xl border border-gray-200 bg-white px-4 py-2 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/30"
              />
              {errors.fechaAplicacion && <p className="mt-1 text-xs text-danger">{errors.fechaAplicacion.message}</p>}
            </div>

            <div>
              <label className="mb-2 block text-sm font-semibold text-secondary">Próxima Dosis (opcional)</label>
              <input
                type="date"
                {...register("proximaDosis")}
                className="w-full rounded-2xl border border-gray-200 bg-white px-4 py-2 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/30"
              />
            </div>
          </div>

          <div>
            <label className="mb-2 block text-sm font-semibold text-secondary">Lote (opcional)</label>
            <input
              {...register("lote")}
              className="w-full rounded-2xl border border-gray-200 bg-white px-4 py-2 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/30"
              placeholder="Ej: LOTE-2025-001"
            />
          </div>

          <div>
            <label className="mb-2 block text-sm font-semibold text-secondary">Observaciones (opcional)</label>
            <textarea
              {...register("observaciones")}
              rows={3}
              className="w-full rounded-2xl border border-gray-200 bg-white px-4 py-2 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/30"
              placeholder="Observaciones sobre la vacunación..."
            />
          </div>

          <div className="flex justify-end gap-3 pt-4">
            <button
              type="button"
              onClick={onClose}
              className="rounded-2xl border border-gray-200 bg-white px-6 py-2 text-sm font-semibold text-secondary transition-base hover:bg-gray-50"
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={mutation.isPending}
              className="rounded-2xl bg-primary px-6 py-2 text-sm font-semibold text-white shadow-soft transition-base hover:bg-primary-dark disabled:opacity-50"
            >
              {mutation.isPending ? "Registrando..." : "Registrar Vacunación"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

