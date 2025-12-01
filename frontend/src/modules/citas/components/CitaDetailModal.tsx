import { useState } from "react";
import { useForm } from "react-hook-form";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import dayjs from "dayjs";
import toast from "react-hot-toast";

import { CitasRepository, type CitaReprogramarRequest, type CitaCancelarRequest } from "../services/CitasRepository";
import { CreateServicioPrestadoModal } from "../../consultas/components/CreateServicioPrestadoModal";
import { CreateFacturaModal } from "../../facturas/components/CreateFacturaModal";
import { ConsultasRepository } from "../../consultas/services/ConsultasRepository";
import { PacientesRepository } from "../../pacientes/services/PacientesRepository";
import type { ApiCitaResponse } from "../../shared/types/backend";
import { authStore } from "../../../shared/state/authStore";

interface CitaDetailModalProps {
  isOpen: boolean;
  cita: ApiCitaResponse | null;
  onClose: () => void;
}

interface ReprogramarFormData { nuevaFechaHora: string; }
interface CancelarFormData { motivoCancelacion: string; }

export const CitaDetailModal = ({ isOpen, cita, onClose }: CitaDetailModalProps) => {
  const queryClient = useQueryClient();
  const [action, setAction] = useState<"view" | "reprogramar" | "cancelar">("view");
  const [isCreateServicioModalOpen, setIsCreateServicioModalOpen] = useState(false);
  const [isCreateFacturaModalOpen, setIsCreateFacturaModalOpen] = useState(false);
  const [isGenerandoResumen, setIsGenerandoResumen] = useState(false);
  const { user } = authStore.getState();
  const isVeterinario = user?.rol === "VETERINARIO";

  const fecha = cita ? dayjs(cita.fechaHora) : dayjs();
  const estado = cita?.estado ?? "PROGRAMADA";

  const { data: serviciosPrestados } = useQuery({
    queryKey: ["servicios-prestados", cita?.idCita],
    queryFn: () => (cita ? ConsultasRepository.getByCita(cita.idCita) : []),
    enabled: Boolean(isOpen && cita && estado === "REALIZADA"),
  });

  const { data: paciente } = useQuery({
    queryKey: ["paciente", cita?.paciente?.id],
    queryFn: () => (cita?.paciente?.id ? PacientesRepository.getById(cita.paciente.id) : null),
    enabled: Boolean(isOpen && cita?.paciente?.id && estado === "REALIZADA"),
  });

  const totalServicios = serviciosPrestados?.reduce((sum, s) => sum + Number.parseFloat(s.costoTotal), 0) ?? 0;

  // Generar resumen
  const handleGenerarResumen = async (servicioId: number) => {
    setIsGenerandoResumen(true);
    try {
      const resumen = await ConsultasRepository.generarResumen(servicioId);
      const modal = window.open("", "_blank", "width=800,height=600");
      if (modal) {
        modal.document.open();
        modal.document.write(`
          <!DOCTYPE html>
          <html lang="es">
            <head><meta charset="UTF-8"><title>Resumen de Servicio</title></head>
            <body><h1>Resumen de Servicio Prestado</h1><pre>${resumen}</pre></body>
          </html>
        `);
        modal.document.close();
      }
      toast.success("Resumen generado exitosamente");
    } catch {
      toast.error("Error al generar el resumen");
    } finally {
      setIsGenerandoResumen(false);
    }
  };

  const { register: registerReprogramar, handleSubmit: handleSubmitReprogramar, reset: resetReprogramar } = useForm<ReprogramarFormData>({
    defaultValues: { nuevaFechaHora: cita ? dayjs(cita.fechaHora).format("YYYY-MM-DDTHH:mm") : "" },
  });

  const { register: registerCancelar, handleSubmit: handleSubmitCancelar, reset: resetCancelar } = useForm<CancelarFormData>();

  // Mutations
  const completarMutation = useMutation((citaId: number) => CitasRepository.completar(citaId), {
    onSuccess: () => {
      toast.success("Cita completada exitosamente");
      queryClient.invalidateQueries(["citas-veterinario", "veterinarian-dashboard"]);
      onClose();
    },
    onError: (error: any) => toast.error(error.response?.data?.message ?? "Error al completar la cita"),
  });

  const reprogramarMutation = useMutation(({ citaId, request }: { citaId: number; request: CitaReprogramarRequest }) =>
      CitasRepository.reprogramar(citaId, request), {
    onSuccess: () => {
      toast.success("Cita reprogramada exitosamente");
      queryClient.invalidateQueries(["citas-veterinario", "veterinarian-dashboard"]);
      resetReprogramar();
      setAction("view");
    },
    onError: (error: any) => toast.error(error.response?.data?.message ?? "Error al reprogramar la cita"),
  });

  const cancelarMutation = useMutation(({ citaId, request }: { citaId: number; request: CitaCancelarRequest }) =>
      CitasRepository.cancelar(citaId, request), {
    onSuccess: () => {
      toast.success("Cita cancelada exitosamente");
      queryClient.invalidateQueries(["citas-veterinario", "veterinarian-dashboard"]);
      resetCancelar();
      setAction("view");
      onClose();
    },
    onError: (error: any) => toast.error(error.response?.data?.message ?? "Error al cancelar la cita"),
  });

  const handleCompletar = () => {
    if (!cita) return;
    if (confirm("¿Está seguro de que desea marcar esta cita como completada?")) {
      completarMutation.mutate(cita.idCita);
    }
  };

  const onReprogramar = (data: ReprogramarFormData) => {
    if (!cita) return;
    reprogramarMutation.mutate({ citaId: cita.idCita, request: { nuevaFechaHora: data.nuevaFechaHora } });
  };

  const onCancelar = (data: CancelarFormData) => {
    if (!cita) return;
    cancelarMutation.mutate({ citaId: cita.idCita, request: { motivo: data.motivoCancelacion } });
  };

  const getEstadoTone = () => estado === "REALIZADA" ? "bg-success/20 text-success"
      : estado === "CANCELADA" ? "bg-danger/20 text-danger"
          : "bg-warning/20 text-warning";

  const getEstadoLabel = () => estado === "REALIZADA" ? "Completada"
      : estado === "CANCELADA" ? "Cancelada"
          : "Programada";

  if (!isOpen || !cita) return null;

  return (
      <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
        <div className="w-full max-w-2xl rounded-2xl bg-white shadow-xl">
          {/* Header */}
          <div className="border-b border-gray-200 px-6 py-4 flex items-center justify-between">
            <div>
              <h2 className="text-xl font-semibold text-gray-900">Detalle de la Cita</h2>
              <p className="mt-1 text-sm text-gray-500">Información y acciones disponibles</p>
            </div>
            <button onClick={() => { setAction("view"); onClose(); }} className="rounded-lg p-2 text-gray-400 hover:bg-gray-100">✕</button>
          </div>

          <div className="p-6">
            {action === "view" && (
                <>
                  <div className="flex items-center justify-between rounded-xl border border-gray-200 bg-gray-50 p-4">
                    <div>
                      <p className="text-xs font-medium text-gray-500">Estado</p>
                      <span className={`mt-1 inline-block rounded-full px-3 py-1 text-xs font-semibold ${getEstadoTone()}`}>
                    {getEstadoLabel()}
                  </span>
                    </div>
                    <div className="text-right">
                      <p className="text-xs font-medium text-gray-500">Fecha y Hora</p>
                      <p className="mt-1 text-sm font-semibold text-gray-900">{fecha.format("DD/MM/YYYY HH:mm")}</p>
                    </div>
                  </div>

                  {/* Botones */}
                  <div className="flex flex-wrap gap-3 border-t border-gray-200 pt-4">
                    {isVeterinario ? (
                        estado === "PROGRAMADA" ? (
                            <button
                                onClick={handleCompletar}
                                disabled={completarMutation.isPending}
                                className="w-full rounded-lg bg-success px-4 py-2 text-sm font-medium text-white disabled:opacity-50"
                            >
                              {completarMutation.isPending ? "Marcando..." : "Marcar como atendida"}
                            </button>
                        ) : estado === "REALIZADA" ? (
                            <p className="w-full text-center text-sm text-gray-500">Esta cita ya fue marcada como atendida.</p>
                        ) : (
                            <p className="w-full text-center text-sm text-gray-500">Esta cita fue cancelada.</p>
                        )
                    ) : (
                        <>
                          {/* Staff buttons */}
                          {estado === "PROGRAMADA" && (
                              <>
                                <button onClick={() => setIsCreateServicioModalOpen(true)}>Registrar Consulta</button>
                                <button onClick={handleCompletar}>Completar Cita</button>
                                <button onClick={() => setAction("reprogramar")}>Reprogramar</button>
                                <button onClick={() => setAction("cancelar")}>Cancelar</button>
                              </>
                          )}
                          {estado === "REALIZADA" && (
                              <>
                                <button onClick={() => setIsCreateServicioModalOpen(true)}>Agregar Servicio</button>
                                {cita.paciente?.id && <button onClick={() => setIsCreateFacturaModalOpen(true)}>Crear Factura</button>}
                                <p>Esta cita ya fue completada</p>
                              </>
                          )}
                          {estado === "CANCELADA" && <p>Esta cita fue cancelada</p>}
                        </>
                    )}
                  </div>
                </>
            )}
            {action === "reprogramar" && (
                <form onSubmit={handleSubmitReprogramar(onReprogramar)}>
                  <input type="datetime-local" {...registerReprogramar("nuevaFechaHora", { required: true })} />
                  <button type="submit" disabled={reprogramarMutation.isPending}>Confirmar</button>
                  <button type="button" onClick={() => setAction("view")}>Cancelar</button>
                </form>
            )}
            {action === "cancelar" && (
                <form onSubmit={handleSubmitCancelar(onCancelar)}>
                  <textarea {...registerCancelar("motivoCancelacion", { required: true })} />
                  <button type="submit" disabled={cancelarMutation.isPending}>Confirmar</button>
                  <button type="button" onClick={() => setAction("view")}>Cancelar</button>
                </form>
            )}
          </div>

          <CreateServicioPrestadoModal isOpen={isCreateServicioModalOpen} cita={cita} onClose={() => setIsCreateServicioModalOpen(false)} />
          <CreateFacturaModal
              isOpen={isCreateFacturaModalOpen}
              clienteId={paciente?.cliente?.id}
              totalInicial={totalServicios || undefined}
              contenidoInicial={serviciosPrestados?.length ? {
                citaId: cita.idCita,
                servicios: serviciosPrestados.map((s) => ({ nombre: s.servicio?.nombre ?? "Servicio", costo: s.costoTotal })),
              } : undefined}
              onClose={() => setIsCreateFacturaModalOpen(false)}
          />
        </div>
      </div>
  );
};
