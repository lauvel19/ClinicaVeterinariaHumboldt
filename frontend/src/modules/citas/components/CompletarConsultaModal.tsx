import { useState } from "react";
import { useForm } from "react-hook-form";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import dayjs from "dayjs";
import toast from "react-hot-toast";

import { CitasRepository } from "../services/CitasRepository";
import type { ApiCitaResponse } from "../../shared/types/backend";

interface CompletarConsultaModalProps {
  readonly isOpen: boolean;
  readonly cita: ApiCitaResponse | null;
  readonly onClose: () => void;
}

interface FormData {
  diagnostico: string;
  tratamiento: string;
  peso: string;
  temperatura: string;
  frecuenciaCardiaca: string;
  frecuenciaRespiratoria: string;
  observaciones: string;
}

interface Medicamento {
  nombre: string;
  dosis: string;
  frecuencia: string;
  duracionDias: number;
  costo: number;
}

export const CompletarConsultaModal = ({ isOpen, cita, onClose }: CompletarConsultaModalProps) => {
  const queryClient = useQueryClient();
  const { register, handleSubmit, formState: { errors }, reset } = useForm<FormData>();
  const [medicamentos, setMedicamentos] = useState<Medicamento[]>([]);

  const agregarMedicamento = () => {
    setMedicamentos([...medicamentos, { nombre: "", dosis: "", frecuencia: "", duracionDias: 0, costo: 0 }]);
  };

  const eliminarMedicamento = (index: number) => {
    setMedicamentos(medicamentos.filter((_, i) => i !== index));
  };

  const actualizarMedicamento = (index: number, field: keyof Medicamento, value: string | number) => {
    const nuevos = [...medicamentos];
    nuevos[index] = { ...nuevos[index], [field]: value };
    setMedicamentos(nuevos);
  };

  const mutation = useMutation({
    mutationFn: async (data: FormData) => {
      if (!cita) throw new Error("No hay cita seleccionada");
      
      // Completar la cita con los datos médicos
      return await CitasRepository.completar(cita.idCita, {
        diagnostico: data.diagnostico,
        tratamiento: data.tratamiento,
        signosVitales: {
          peso: data.peso ? parseFloat(data.peso) : undefined,
          temperatura: data.temperatura ? parseFloat(data.temperatura) : undefined,
          frecuenciaCardiaca: data.frecuenciaCardiaca ? parseInt(data.frecuenciaCardiaca) : undefined,
          frecuenciaRespiratoria: data.frecuenciaRespiratoria ? parseInt(data.frecuenciaRespiratoria) : undefined,
        },
        observaciones: data.observaciones,
        medicamentos: medicamentos.filter(m => m.nombre.trim() !== ""),
      });
    },
    onSuccess: () => {
      toast.success("Consulta completada exitosamente. Se ha generado la factura automáticamente.");
      queryClient.invalidateQueries({ queryKey: ["citas-veterinario"] });
      queryClient.invalidateQueries({ queryKey: ["todas-las-citas"] });
      reset();
      setMedicamentos([]);
      onClose();
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || "Error al completar la consulta");
    },
  });

  const onSubmit = (data: FormData) => {
    mutation.mutate(data);
  };

  if (!isOpen || !cita) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-start sm:items-center justify-center bg-black/50 p-2 sm:p-4 overflow-y-auto">
      <div className="w-full max-w-3xl rounded-lg sm:rounded-2xl bg-white shadow-xl my-2 sm:my-8 max-h-[98vh] overflow-y-auto">
        {/* Header */}
        <div className="sticky top-0 z-10 bg-gradient-to-r from-blue-50 to-cyan-50 border-b border-gray-200 px-4 sm:px-6 py-3 sm:py-4 rounded-t-lg sm:rounded-t-2xl">
          <div className="flex items-center justify-between">
            <div>
              <h2 className="text-lg sm:text-xl font-semibold text-gray-900 flex items-center gap-2">
                🏥 Completar Consulta Médica
              </h2>
              <p className="mt-1 text-xs sm:text-sm text-gray-600">
                <span className="font-medium">Paciente:</span> {cita.paciente?.nombre || "Sin nombre"} • 
                <span className="ml-2"><span className="font-medium">Fecha:</span> {dayjs(cita.fechaHora).format("DD/MM/YYYY HH:mm")}</span>
              </p>
            </div>
            <button
              onClick={onClose}
              className="rounded-lg p-2 text-gray-400 transition-all hover:bg-white hover:text-gray-600"
            >
              ✕
            </button>
          </div>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} className="p-4 sm:p-6">
          <div className="space-y-5">
            {/* Alerta informativa */}
            <div className="rounded-lg bg-blue-50 border border-blue-200 p-3 sm:p-4">
              <div className="flex gap-3">
                <div className="flex-shrink-0">
                  <svg className="h-5 w-5 text-blue-600" fill="currentColor" viewBox="0 0 20 20">
                    <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clipRule="evenodd" />
                  </svg>
                </div>
                <div>
                  <p className="text-sm font-medium text-blue-900">Información médica</p>
                  <p className="mt-1 text-xs text-blue-700">
                    Esta información se guardará en la historia clínica del paciente y generará automáticamente la factura basada en el tipo de servicio de la cita.
                  </p>
                </div>
              </div>
            </div>

            {/* Diagnóstico */}
            <div>
              <label className="mb-2 block text-sm font-medium text-gray-700">
                🩺 Diagnóstico <span className="text-red-500">*</span>
              </label>
              <textarea
                {...register("diagnostico", { required: "El diagnóstico es obligatorio" })}
                rows={3}
                className="w-full rounded-lg border border-gray-300 px-3 sm:px-4 py-2 sm:py-2.5 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/20"
                placeholder="Ej: Dermatitis alérgica leve en región dorsal..."
              />
              {errors.diagnostico && (
                <p className="mt-1.5 text-xs text-red-600 flex items-center gap-1">
                  <svg className="h-4 w-4" fill="currentColor" viewBox="0 0 20 20">
                    <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
                  </svg>
                  {errors.diagnostico.message}
                </p>
              )}
            </div>

            {/* Tratamiento */}
            <div>
              <label className="mb-2 block text-sm font-medium text-gray-700">
                💊 Tratamiento Prescrito <span className="text-red-500">*</span>
              </label>
              <textarea
                {...register("tratamiento", { required: "El tratamiento es obligatorio" })}
                rows={3}
                className="w-full rounded-lg border border-gray-300 px-3 sm:px-4 py-2 sm:py-2.5 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/20"
                placeholder="Ej: Antihistamínico oral (Cetirizina) 10mg cada 12 horas por 7 días..."
              />
              {errors.tratamiento && (
                <p className="mt-1.5 text-xs text-red-600 flex items-center gap-1">
                  <svg className="h-4 w-4" fill="currentColor" viewBox="0 0 20 20">
                    <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
                  </svg>
                  {errors.tratamiento.message}
                </p>
              )}
            </div>

            {/* Signos Vitales */}
            <div>
              <label className="mb-3 block text-sm font-medium text-gray-700">
                📊 Signos Vitales
              </label>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 sm:gap-4">
                <div>
                  <label className="mb-1 block text-xs font-medium text-gray-600">Peso (kg)</label>
                  <input
                    type="number"
                    step="0.01"
                    {...register("peso")}
                    className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/20"
                    placeholder="Ej: 15.5"
                  />
                </div>
                <div>
                  <label className="mb-1 block text-xs font-medium text-gray-600">Temperatura (°C)</label>
                  <input
                    type="number"
                    step="0.1"
                    {...register("temperatura")}
                    className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/20"
                    placeholder="Ej: 38.5"
                  />
                </div>
                <div>
                  <label className="mb-1 block text-xs font-medium text-gray-600">Frecuencia Cardíaca (lpm)</label>
                  <input
                    type="number"
                    {...register("frecuenciaCardiaca")}
                    className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/20"
                    placeholder="Ej: 120"
                  />
                </div>
                <div>
                  <label className="mb-1 block text-xs font-medium text-gray-600">Frecuencia Respiratoria (rpm)</label>
                  <input
                    type="number"
                    {...register("frecuenciaRespiratoria")}
                    className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/20"
                    placeholder="Ej: 30"
                  />
                </div>
              </div>
              <p className="mt-1.5 text-xs text-gray-500">Campos opcionales: Completa los que sean relevantes</p>
            </div>

            {/* Medicamentos */}
            <div>
              <div className="mb-3 flex items-center justify-between">
                <label className="block text-sm font-medium text-gray-700">
                  💊 Medicamentos Recetados
                </label>
                <button
                  type="button"
                  onClick={agregarMedicamento}
                  className="rounded-lg bg-green-600 px-3 py-1.5 text-xs font-medium text-white transition-all hover:bg-green-700 flex items-center gap-1"
                >
                  ➕ Agregar Medicamento
                </button>
              </div>
              
              {medicamentos.length === 0 ? (
                <div className="rounded-lg border border-dashed border-gray-300 bg-gray-50 p-4 text-center text-sm text-gray-500">
                  No hay medicamentos agregados. Haz clic en "Agregar Medicamento" para añadir.
                </div>
              ) : (
                <div className="space-y-3">
                  {medicamentos.map((med, index) => (
                    <div key={index} className="rounded-lg border border-gray-300 bg-gray-50 p-3 sm:p-4">
                      <div className="mb-3 flex items-center justify-between">
                        <span className="text-xs font-semibold text-gray-700">Medicamento #{index + 1}</span>
                        <button
                          type="button"
                          onClick={() => eliminarMedicamento(index)}
                          className="rounded bg-red-100 px-2 py-1 text-xs font-medium text-red-700 transition-all hover:bg-red-200"
                        >
                          🗑️ Eliminar
                        </button>
                      </div>
                      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                        <div className="sm:col-span-2">
                          <label className="mb-1 block text-xs font-medium text-gray-600">Nombre del Medicamento *</label>
                          <input
                            type="text"
                            value={med.nombre}
                            onChange={(e) => actualizarMedicamento(index, "nombre", e.target.value)}
                            className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/20"
                            placeholder="Ej: Amoxicilina"
                            required
                          />
                        </div>
                        <div>
                          <label className="mb-1 block text-xs font-medium text-gray-600">Dosis *</label>
                          <input
                            type="text"
                            value={med.dosis}
                            onChange={(e) => actualizarMedicamento(index, "dosis", e.target.value)}
                            className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/20"
                            placeholder="Ej: 500mg"
                            required
                          />
                        </div>
                        <div>
                          <label className="mb-1 block text-xs font-medium text-gray-600">Frecuencia *</label>
                          <input
                            type="text"
                            value={med.frecuencia}
                            onChange={(e) => actualizarMedicamento(index, "frecuencia", e.target.value)}
                            className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/20"
                            placeholder="Ej: 12 horas"
                            required
                          />
                        </div>
                        <div>
                          <label className="mb-1 block text-xs font-medium text-gray-600">Duración (días) *</label>
                          <input
                            type="number"
                            value={med.duracionDias}
                            onChange={(e) => actualizarMedicamento(index, "duracionDias", parseInt(e.target.value) || 0)}
                            className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/20"
                            placeholder="Ej: 7"
                            min="1"
                            required
                          />
                        </div>
                        <div>
                          <label className="mb-1 block text-xs font-medium text-gray-600">Costo ($) *</label>
                          <input
                            type="number"
                            step="0.01"
                            value={med.costo}
                            onChange={(e) => actualizarMedicamento(index, "costo", parseFloat(e.target.value) || 0)}
                            className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/20"
                            placeholder="Ej: 25.00"
                            min="0"
                            required
                          />
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              )}
              <p className="mt-2 text-xs text-gray-500">
                Opcional: Los medicamentos se incluirán en el costo total de la factura
              </p>
            </div>

            {/* Observaciones */}
            <div>
              <label className="mb-2 block text-sm font-medium text-gray-700">
                📝 Observaciones Adicionales
              </label>
              <textarea
                {...register("observaciones")}
                rows={3}
                className="w-full rounded-lg border border-gray-300 px-3 sm:px-4 py-2 sm:py-2.5 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/20"
                placeholder="Ej: Propietario menciona que el animal ha estado rascándose frecuentemente. Recomendado baño medicado..."
              />
              <p className="mt-1.5 text-xs text-gray-500">Opcional: Cualquier información adicional relevante</p>
            </div>
          </div>

          {/* Botones */}
          <div className="mt-6 flex flex-col-reverse sm:flex-row gap-3 sm:justify-end border-t border-gray-200 pt-4">
            <button
              type="button"
              onClick={onClose}
              className="w-full sm:w-auto rounded-lg border border-gray-300 bg-white px-5 py-2.5 text-sm font-medium text-gray-700 transition-all hover:bg-gray-50"
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={mutation.isPending}
              className="w-full sm:w-auto rounded-lg bg-primary px-5 py-2.5 text-sm font-medium text-white transition-all hover:bg-primary/90 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
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
                  Completar Consulta
                </>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
