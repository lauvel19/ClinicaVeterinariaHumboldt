import { useState } from "react";
import { useForm } from "react-hook-form";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import dayjs from "dayjs";
import toast from "react-hot-toast";

import { CitasRepository, type CitaReprogramarRequest, type CitaCancelarRequest } from "../services/CitasRepository";
import { CompletarConsultaModal } from "./CompletarConsultaModal";
import { CreateFacturaModal } from "../../facturas/components/CreateFacturaModal";
import { ConsultasRepository } from "../../consultas/services/ConsultasRepository";
import { PacientesRepository } from "../../pacientes/services/PacientesRepository";
import type { ApiCitaResponse } from "../../shared/types/backend";
import { authStore } from "../../../shared/state/authStore";

interface CitaDetailModalProps {
  readonly isOpen: boolean;
  readonly cita: ApiCitaResponse | null;
  readonly onClose: () => void;
}

interface ReprogramarFormData {
  nuevaFechaHora: string;
}

interface CancelarFormData {
  motivoCancelacion: string;
}

export const CitaDetailModal = ({ isOpen, cita, onClose }: CitaDetailModalProps) => {
  const queryClient = useQueryClient();
  const [action, setAction] = useState<"view" | "reprogramar" | "cancelar">("view");
  const [isCreateServicioModalOpen, setIsCreateServicioModalOpen] = useState(false);
  const [isCreateFacturaModalOpen, setIsCreateFacturaModalOpen] = useState(false);
  const [isGenerandoResumen, setIsGenerandoResumen] = useState(false);
  const { user } = authStore.getState();
  const isVeterinario = user?.rol === "VETERINARIO";

  // Obtener servicios prestados para calcular el total de la factura
  const { data: serviciosPrestados } = useQuery({
    queryKey: ["servicios-prestados", cita?.idCita],
    queryFn: () => (cita ? ConsultasRepository.getByCita(cita.idCita) : []),
    enabled: isOpen && cita !== null && cita.estado === "REALIZADA",
  });

  // Obtener información del paciente para obtener el clienteId
  const { data: paciente } = useQuery({
    queryKey: ["paciente", cita?.paciente?.id],
    queryFn: () => (cita?.paciente?.id ? PacientesRepository.getById(cita.paciente.id) : null),
    enabled: isOpen && cita !== null && cita.paciente?.id !== null && cita.estado === "REALIZADA",
  });

  // Calcular el total de los servicios prestados
  const totalServicios = serviciosPrestados?.reduce(
    (sum, servicio) => sum + Number.parseFloat(servicio.costoTotal),
    0
  ) || 0;

  const handleGenerarResumen = async (servicioId: number) => {
    setIsGenerandoResumen(true);
    try {
      const resumen = await ConsultasRepository.generarResumen(servicioId);
      const modal = globalThis.open("", "_blank");
      if (modal) {
        const htmlContent = `
          <!DOCTYPE html>
          <html lang="es">
            <head>
              <title>Resumen de Servicio</title>
              <style>
                body { font-family: Arial, sans-serif; padding: 20px; line-height: 1.6; }
                h1 { color: #1e40af; }
                pre { white-space: pre-wrap; background: #f5f5f5; padding: 15px; border-radius: 5px; }
              </style>
            </head>
            <body>
              <h1>Resumen de Servicio Prestado</h1>
              <p><strong>Fecha:</strong> ${dayjs().format("DD/MM/YYYY HH:mm")}</p>
              <hr>
              <pre>${resumen}</pre>
            </body>
          </html>
        `;
        modal.document.open();
        modal.document.write(htmlContent);
        modal.document.close();
      }
      toast.success("Resumen generado exitosamente");
    } catch (error) {
      console.error("Error al generar resumen:", error);
      toast.error("Error al generar el resumen");
    } finally {
      setIsGenerandoResumen(false);
    }
  };

  const { register: registerReprogramar, handleSubmit: handleSubmitReprogramar, reset: resetReprogramar } =
    useForm<ReprogramarFormData>({
      defaultValues: {
        nuevaFechaHora: cita ? dayjs(cita.fechaHora).format("YYYY-MM-DDTHH:mm") : "",
      },
    });

  const { register: registerCancelar, handleSubmit: handleSubmitCancelar, reset: resetCancelar } =
    useForm<CancelarFormData>();

  const reprogramarMutation = useMutation({
    mutationFn: ({ citaId, request }: { citaId: number; request: CitaReprogramarRequest }) =>
      CitasRepository.reprogramar(citaId, request),
    onSuccess: () => {
      toast.success("Cita reprogramada exitosamente");
      queryClient.invalidateQueries({ queryKey: ["citas-veterinario"] });
      queryClient.invalidateQueries({ queryKey: ["veterinarian-dashboard"] });
      resetReprogramar();
      setAction("view");
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || "Error al reprogramar la cita");
    },
  });

  const cancelarMutation = useMutation({
    mutationFn: ({ citaId, request }: { citaId: number; request: CitaCancelarRequest }) =>
      CitasRepository.cancelar(citaId, request),
    onSuccess: () => {
      toast.success("Cita cancelada exitosamente");
      queryClient.invalidateQueries({ queryKey: ["citas-veterinario"] });
      queryClient.invalidateQueries({ queryKey: ["veterinarian-dashboard"] });
      resetCancelar();
      setAction("view");
      onClose();
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || "Error al cancelar la cita");
    },
  });

  const onReprogramar = (data: ReprogramarFormData) => {
    if (!cita) return;
    reprogramarMutation.mutate({
      citaId: cita.idCita,
      request: { nuevaFechaHora: data.nuevaFechaHora },
    });
  };

  const onCancelar = (data: CancelarFormData) => {
    if (!cita) return;
    cancelarMutation.mutate({
      citaId: cita.idCita,
      request: { motivo: data.motivoCancelacion },
    });
  };

  const getEstadoTone = (estado: string) => {
    if (estado === "REALIZADA") return "bg-success/20 text-success";
    if (estado === "CANCELADA") return "bg-danger/20 text-danger";
    return "bg-warning/20 text-warning";
  };

  const getEstadoTexto = (estado: string) => {
    if (estado === "REALIZADA") return "Completada";
    if (estado === "CANCELADA") return "Cancelada";
    return "Programada";
  };

  if (!isOpen || !cita) return null;

  const fecha = dayjs(cita.fechaHora);
  const estadoTone = getEstadoTone(cita.estado);

  // Helper para renderizar el contenido según el estado de la cita
  const renderEstadoMessage = () => {
    if (cita.estado === "PROGRAMADA") return null;

    const message = cita.estado === "REALIZADA"
      ? "Esta cita ya fue marcada como atendida."
      : "Esta cita fue cancelada.";

    return <p className="w-full text-center text-sm text-gray-500">{message}</p>;
  };

  // Helper para renderizar el formulario según la acción
  const renderFormulario = () => {
    if (action === "reprogramar") {
      return (
        <form onSubmit={handleSubmitReprogramar(onReprogramar)} className="space-y-4">
          <div>
            <label htmlFor="nuevaFechaHora" className="mb-1 block text-sm font-medium text-gray-700">
              Nueva Fecha y Hora <span className="text-red-500">*</span>
            </label>
            <input
              id="nuevaFechaHora"
              type="datetime-local"
              {...registerReprogramar("nuevaFechaHora", { required: "La nueva fecha es obligatoria" })}
              min={dayjs().format("YYYY-MM-DDTHH:mm")}
              className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/20"
            />
          </div>
          <div className="flex gap-3">
            <button
              type="button"
              onClick={() => setAction("view")}
              className="flex-1 rounded-lg border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 transition-all hover:bg-gray-50"
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={reprogramarMutation.isPending}
              className="flex-1 rounded-lg bg-primary px-4 py-2 text-sm font-medium text-white transition-all hover:bg-primary/90 disabled:opacity-50"
            >
              {reprogramarMutation.isPending ? "Reprogramando..." : "Confirmar Reprogramación"}
            </button>
          </div>
        </form>
      );
    }

    if (action === "cancelar") {
      return (
        <form onSubmit={handleSubmitCancelar(onCancelar)} className="space-y-4">
          <div>
            <label htmlFor="motivoCancelacion" className="mb-1 block text-sm font-medium text-gray-700">
              Motivo de Cancelación <span className="text-red-500">*</span>
            </label>
            <textarea
              id="motivoCancelacion"
              {...registerCancelar("motivoCancelacion", { required: "El motivo es obligatorio" })}
              rows={4}
              className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/20"
              placeholder="Describe el motivo de la cancelación..."
            />
          </div>
          <div className="flex gap-3">
            <button
              type="button"
              onClick={() => setAction("view")}
              className="flex-1 rounded-lg border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 transition-all hover:bg-gray-50"
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={cancelarMutation.isPending}
              className="flex-1 rounded-lg bg-red-600 px-4 py-2 text-sm font-medium text-white transition-all hover:bg-red-700 disabled:opacity-50"
            >
              {cancelarMutation.isPending ? "Cancelando..." : "Confirmar Cancelación"}
            </button>
          </div>
        </form>
      );
    }

    return null;
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
      <div className="w-full max-w-2xl rounded-2xl bg-white shadow-xl">
        <div className="border-b border-gray-200 px-6 py-4">
          <div className="flex items-center justify-between">
            <div>
              <h2 className="text-xl font-semibold text-gray-900">Detalle de la Cita</h2>
              <p className="mt-1 text-sm text-gray-500">Información y acciones disponibles</p>
            </div>
            <button
              onClick={() => {
                setAction("view");
                onClose();
              }}
              className="rounded-lg p-2 text-gray-400 transition-colors hover:bg-gray-100 hover:text-gray-600"
            >
              <svg className="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>
        </div>

        <div className="p-6">
          {action === "view" ? (
            <div className="space-y-6">
              {/* Información de la cita */}
              <div className="space-y-4">
                <div className="flex items-center justify-between rounded-xl border border-gray-200 bg-gray-50 p-4">
                  <div>
                    <p className="text-xs font-medium text-gray-500">Estado</p>
                    <span className={`mt-1 inline-block rounded-full px-3 py-1 text-xs font-semibold ${estadoTone}`}>
                      {getEstadoTexto(cita.estado)}
                    </span>
                  </div>
                  <div className="text-right">
                    <p className="text-xs font-medium text-gray-500">Fecha y Hora</p>
                    <p className="mt-1 text-sm font-semibold text-gray-900">{fecha.format("DD/MM/YYYY HH:mm")}</p>
                  </div>
                </div>

                <div className="grid gap-4 sm:grid-cols-2">
                  <div>
                    <p className="text-xs font-medium text-gray-500">Paciente</p>
                    <p className="mt-1 text-sm font-semibold text-gray-900">
                      {cita.paciente?.nombre || "Sin nombre"} ({cita.paciente?.especie || "Sin especie"})
                    </p>
                  </div>
                  <div>
                    <p className="text-xs font-medium text-gray-500">Propietario</p>
                    <p className="mt-1 text-sm font-semibold text-gray-900">{cita.paciente?.propietario || "No asignado"}</p>
                  </div>
                  <div>
                    <p className="text-xs font-medium text-gray-500">Tipo de Servicio</p>
                    <p className="mt-1 text-sm font-semibold text-gray-900">{cita.tipoServicio || "No especificado"}</p>
                  </div>
                  <div>
                    <p className="text-xs font-medium text-gray-500">Nivel de Prioridad</p>
                    <p className="mt-1 text-sm font-semibold text-gray-900">{cita.triageNivel || "No especificado"}</p>
                  </div>
                </div>

                {cita.motivo && (
                  <div>
                    <p className="text-xs font-medium text-gray-500">Motivo</p>
                    <p className="mt-1 text-sm text-gray-900">{cita.motivo}</p>
                  </div>
                )}

                {cita.veterinario && (
                  <div>
                    <p className="text-xs font-medium text-gray-500">Veterinario Asignado</p>
                    <p className="mt-1 text-sm font-semibold text-gray-900">{cita.veterinario.nombreCompleto}</p>
                    {cita.veterinario.especialidad && (
                      <p className="mt-0.5 text-xs text-gray-500">{cita.veterinario.especialidad}</p>
                    )}
                  </div>
                )}
              </div>

              {/* Servicios prestados section */}
              {cita.estado === "REALIZADA" && serviciosPrestados && serviciosPrestados.length > 0 && (
                <div className="rounded-xl border border-gray-200 bg-gray-50 p-4">
                  <h3 className="mb-3 text-sm font-semibold text-gray-900">Servicios Prestados</h3>
                  <div className="space-y-2">
                    {serviciosPrestados.map((servicio) => (
                      <div
                        key={servicio.idPrestado}
                        className="flex items-center justify-between rounded-lg border border-gray-200 bg-white p-3"
                      >
                        <div className="flex-1">
                          <p className="text-sm font-semibold text-gray-900">
                            {servicio.servicio?.nombre || "Servicio"}
                          </p>
                          <p className="text-xs text-gray-500">
                            {dayjs(servicio.fechaEjecucion).format("DD/MM/YYYY HH:mm")} •{" "}
                            {Number.parseFloat(servicio.costoTotal).toLocaleString("es-CO", {
                              style: "currency",
                              currency: "COP",
                            })}
                          </p>
                          {servicio.observaciones && (
                            <p className="mt-1 text-xs text-gray-600">{servicio.observaciones}</p>
                          )}
                        </div>
                        <button
                          onClick={() => handleGenerarResumen(servicio.idPrestado)}
                          disabled={isGenerandoResumen}
                          className="ml-3 rounded-lg border border-info bg-info/10 px-3 py-1.5 text-xs font-semibold text-info transition-all hover:bg-info hover:text-white disabled:opacity-50"
                        >
                          {isGenerandoResumen ? "..." : "📋 Resumen"}
                        </button>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {/* Botones de acción */}
              <div className="flex gap-3 border-t border-gray-200 pt-4">
                {isVeterinario ? (
                  <>
                    {cita.estado === "PROGRAMADA" && (
                      <button
                        onClick={() => setIsCreateServicioModalOpen(true)}
                        disabled={cita.estado === "REALIZADA"}
                        className={`flex-1 rounded-lg px-4 py-2.5 text-sm font-medium text-white transition-all flex items-center justify-center gap-2 ${
                          cita.estado === "REALIZADA"
                            ? "bg-gray-400 cursor-not-allowed"
                            : "bg-green-600 hover:bg-green-700"
                        }`}
                      >
                        <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                        </svg>
                        {cita.estado === "REALIZADA" ? "Consulta Completada" : "Completar Consulta"}
                      </button>
                    )}
                    {renderEstadoMessage()}
                  </>
                ) : (
                  <>
                    {cita.estado === "PROGRAMADA" && (
                      <>
                        <button
                          onClick={() => setAction("reprogramar")}
                          className="flex-1 rounded-lg bg-primary px-4 py-2 text-sm font-medium text-white transition-all hover:bg-primary/90"
                        >
                          Reprogramar
                        </button>
                        <button
                          onClick={() => setAction("cancelar")}
                          className="flex-1 rounded-lg bg-red-600 px-4 py-2 text-sm font-medium text-white transition-all hover:bg-red-700"
                        >
                          Cancelar Cita
                        </button>
                      </>
                    )}
                    {renderEstadoMessage()}
                  </>
                )}
              </div>
            </div>
          ) : (
            renderFormulario()
          )}
        </div>
      </div>

      <CompletarConsultaModal
        isOpen={isCreateServicioModalOpen}
        cita={cita}
        onClose={() => setIsCreateServicioModalOpen(false)}
      />
      <CreateFacturaModal
        isOpen={isCreateFacturaModalOpen}
        clienteId={paciente?.cliente?.id}
        totalInicial={totalServicios > 0 ? totalServicios : undefined}
        contenidoInicial={
          serviciosPrestados && serviciosPrestados.length > 0
            ? {
                citaId: cita?.idCita,
                servicios: serviciosPrestados.map((s) => ({
                  nombre: s.servicio?.nombre || "Servicio",
                  costo: s.costoTotal,
                })),
              }
            : undefined
        }
        onClose={() => setIsCreateFacturaModalOpen(false)}
      />
    </div>
  );
};

