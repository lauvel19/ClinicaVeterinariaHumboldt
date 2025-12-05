import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import dayjs from "dayjs";
import toast from "react-hot-toast";

import { PacientesRepository } from "../services/PacientesRepository";
import { HistoriasRepository } from "../../historias/services/HistoriasRepository";
import { EditPacienteModal } from "./EditPacienteModal";
import { CreateVacunacionModal } from "../../vacunaciones/components/CreateVacunacionModal";
import { FullscreenLoader } from "../../../app/components/feedback/FullscreenLoader";
import { authStore } from "../../../shared/state/authStore";

interface PacienteDetailModalProps {
  readonly isOpen: boolean;
  readonly pacienteId: number | null;
  readonly onClose: () => void;
}

export const PacienteDetailModal = ({ isOpen, pacienteId, onClose }: PacienteDetailModalProps) => {
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [isVacunacionModalOpen, setIsVacunacionModalOpen] = useState(false);
  const [isGenerandoResumen, setIsGenerandoResumen] = useState(false);
  const [activeTab, setActiveTab] = useState<"info" | "historia" | "vacunas">("info");
  const { user } = authStore.getState();
  const isVeterinario = user?.rol === "VETERINARIO";
  const { data: paciente, isLoading } = useQuery({
    queryKey: ["paciente", pacienteId],
    queryFn: () => PacientesRepository.getById(pacienteId!),
    enabled: isOpen && pacienteId !== null,
  });

  const { data: historia } = useQuery({
    queryKey: ["historia-clinica", pacienteId],
    queryFn: () => HistoriasRepository.getByPaciente(pacienteId!),
    enabled: isOpen && pacienteId !== null,
  });

  const handleExportarPDF = async () => {
    if (!historia) {
      toast.error("No se encontró la historia clínica del paciente");
      return;
    }
    try {
      const blob = await HistoriasRepository.exportarPDF(historia.id);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = `historia_clinica_${paciente?.nombre || pacienteId}_${dayjs().format("YYYY-MM-DD")}.pdf`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
      toast.success("Historia clínica exportada exitosamente");
    } catch (error) {
      toast.error("Error al exportar la historia clínica");
    }
  };

  const handleGenerarResumen = async () => {
    if (!pacienteId) return;
    setIsGenerandoResumen(true);
    try {
      const resumen = await PacientesRepository.generarResumen(pacienteId);
      // Mostrar resumen en un modal o alerta
      const modal = window.open("", "_blank");
      if (modal) {
        modal.document.write(`
          <!DOCTYPE html>
          <html>
            <head>
              <title>Resumen Clínico - ${paciente?.nombre || "Paciente"}</title>
              <style>
                body { font-family: Arial, sans-serif; padding: 20px; line-height: 1.6; }
                h1 { color: #1e40af; }
                pre { white-space: pre-wrap; background: #f5f5f5; padding: 15px; border-radius: 5px; }
              </style>
            </head>
            <body>
              <h1>Resumen Clínico</h1>
              <p><strong>Paciente:</strong> ${paciente?.nombre || "N/A"}</p>
              <p><strong>Fecha:</strong> ${dayjs().format("DD/MM/YYYY HH:mm")}</p>
              <hr>
              <pre>${resumen}</pre>
            </body>
          </html>
        `);
        modal.document.close();
      }
      toast.success("Resumen clínico generado exitosamente");
    } catch (error) {
      toast.error("Error al generar el resumen clínico");
    } finally {
      setIsGenerandoResumen(false);
    }
  };

  if (!isOpen || !pacienteId) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
      <div className="w-full max-w-3xl max-h-[90vh] overflow-y-auto rounded-2xl bg-white shadow-xl">
        <div className="sticky top-0 border-b border-gray-200 bg-white px-6 py-4">
          <div className="flex items-center justify-between">
            <div>
              <h2 className="text-xl font-semibold text-gray-900">Detalle del Paciente</h2>
              <p className="mt-1 text-sm text-gray-500">Información completa del paciente</p>
            </div>
            <button
              onClick={onClose}
              className="rounded-lg p-2 text-gray-400 transition-all hover:bg-gray-100 hover:text-gray-600"
            >
              ✕
            </button>
          </div>
        </div>

        <div className="p-0">
          {isLoading ? (
            <div className="p-6">
              <FullscreenLoader />
            </div>
          ) : paciente ? (
            <>
              {/* Header con avatar y datos principales */}
              <div className="bg-gradient-to-br from-primary/5 to-primary/10 p-6 border-b border-gray-200">
                <div className="flex flex-col sm:flex-row gap-6 items-start">
                  {/* Avatar grande */}
                  <div className="flex-shrink-0">
                    <div className="relative">
                      {paciente.fotoPerfil ? (
                        <img
                          src={`http://localhost:8080/api${paciente.fotoPerfil}`}
                          alt={paciente.nombre}
                          className="h-24 w-24 rounded-3xl object-cover shadow-lg border-4 border-white"
                        />
                      ) : (
                        <div className={`flex h-24 w-24 items-center justify-center rounded-3xl text-4xl text-white shadow-lg ${
                          paciente.especie?.toLowerCase().includes("gato")
                            ? "bg-gradient-to-br from-purple-500 to-purple-700"
                            : "bg-gradient-to-br from-blue-500 to-blue-700"
                        }`}>
                          {paciente.especie?.toLowerCase().includes("gato") ? "🐱" : "🐕"}
                        </div>
                      )}
                      {/* Indicator de estado */}
                      <div className={`absolute -bottom-1 -right-1 h-6 w-6 rounded-full border-4 border-white ${
                        paciente.estadoSalud?.toLowerCase().includes("sano") || paciente.estadoSalud?.toLowerCase().includes("bueno")
                          ? "bg-green-400"
                          : paciente.estadoSalud?.toLowerCase().includes("enfermo")
                          ? "bg-red-400"
                          : "bg-gray-400"
                      }`} />
                    </div>
                  </div>

                  {/* Información principal */}
                  <div className="flex-1 min-w-0">
                    <div className="flex items-start justify-between gap-4">
                      <div className="flex-1 min-w-0">
                        <h3 className="text-2xl font-bold text-gray-900">{paciente.nombre}</h3>
                        <p className="text-sm text-gray-600 capitalize mt-1">
                          {paciente.especie} {paciente.raza && `• ${paciente.raza}`} {paciente.sexo && `• ${paciente.sexo}`}
                        </p>
                      </div>
                      {paciente.estadoSalud && (
                        <span className={`rounded-full px-3 py-1.5 text-xs font-semibold whitespace-nowrap ${
                          paciente.estadoSalud?.toLowerCase().includes("sano") || paciente.estadoSalud?.toLowerCase().includes("bueno")
                            ? "bg-green-100 text-green-700"
                            : paciente.estadoSalud?.toLowerCase().includes("enfermo")
                            ? "bg-red-100 text-red-700"
                            : "bg-gray-100 text-gray-700"
                        }`}>
                          {paciente.estadoSalud}
                        </span>
                      )}
                    </div>

                    {/* Stats rápidos */}
                    <div className="grid grid-cols-3 gap-4 mt-4">
                      <div className="bg-white rounded-xl p-3 shadow-sm">
                        <p className="text-xs text-gray-500 font-medium">Edad</p>
                        <p className="text-lg font-bold text-gray-900 mt-1">
                          {paciente.fechaNacimiento
                            ? `${dayjs().diff(dayjs(paciente.fechaNacimiento), "year")} años`
                            : "N/A"}
                        </p>
                      </div>
                      <div className="bg-white rounded-xl p-3 shadow-sm">
                        <p className="text-xs text-gray-500 font-medium">Peso</p>
                        <p className="text-lg font-bold text-gray-900 mt-1">
                          {paciente.pesoKg ? `${paciente.pesoKg} kg` : "N/A"}
                        </p>
                      </div>
                      <div className="bg-white rounded-xl p-3 shadow-sm">
                        <p className="text-xs text-gray-500 font-medium">ID</p>
                        <p className="text-lg font-bold text-gray-900 mt-1">#{paciente.id}</p>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              {/* Tabs de navegación */}
              <div className="border-b border-gray-200 bg-white px-6">
                <nav className="flex space-x-6">
                  <button
                    onClick={() => setActiveTab("info")}
                    className={`py-4 px-1 border-b-2 font-medium text-sm transition-colors ${
                      activeTab === "info"
                        ? "border-primary text-primary"
                        : "border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300"
                    }`}
                  >
                    📋 Información
                  </button>
                  <button
                    onClick={() => setActiveTab("historia")}
                    className={`py-4 px-1 border-b-2 font-medium text-sm transition-colors ${
                      activeTab === "historia"
                        ? "border-primary text-primary"
                        : "border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300"
                    }`}
                  >
                    🩺 Historia Clínica
                  </button>
                  <button
                    onClick={() => setActiveTab("vacunas")}
                    className={`py-4 px-1 border-b-2 font-medium text-sm transition-colors ${
                      activeTab === "vacunas"
                        ? "border-primary text-primary"
                        : "border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300"
                    }`}
                  >
                    💉 Vacunaciones
                  </button>
                </nav>
              </div>

              {/* Contenido de tabs */}
              <div className="p-6">
                {activeTab === "info" && (
                  <div className="space-y-4">
                    {/* Datos del paciente */}
                    <section className="bg-gray-50 rounded-2xl p-4 space-y-3">
                      <h4 className="text-sm font-bold text-gray-900 uppercase tracking-wide">Datos del Paciente</h4>
                      <div className="grid grid-cols-2 gap-4">
                        <div>
                          <p className="text-xs text-gray-500 font-medium">Fecha de Nacimiento</p>
                          <p className="text-sm font-semibold text-gray-900 mt-1">
                            {paciente.fechaNacimiento ? dayjs(paciente.fechaNacimiento).format("DD/MM/YYYY") : "No registrada"}
                          </p>
                        </div>
                        <div>
                          <p className="text-xs text-gray-500 font-medium">Especie</p>
                          <p className="text-sm font-semibold text-gray-900 mt-1 capitalize">{paciente.especie || "No especificada"}</p>
                        </div>
                        <div>
                          <p className="text-xs text-gray-500 font-medium">Raza</p>
                          <p className="text-sm font-semibold text-gray-900 mt-1">{paciente.raza || "No especificada"}</p>
                        </div>
                        <div>
                          <p className="text-xs text-gray-500 font-medium">Sexo</p>
                          <p className="text-sm font-semibold text-gray-900 mt-1">{paciente.sexo || "No especificado"}</p>
                        </div>
                      </div>
                    </section>

                    {/* Propietario */}
                    {paciente.cliente && (
                      <section className="bg-blue-50 rounded-2xl p-4 space-y-3">
                        <h4 className="text-sm font-bold text-gray-900 uppercase tracking-wide">👤 Propietario</h4>
                        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                          <div>
                            <p className="text-xs text-gray-600 font-medium">Nombre Completo</p>
                            <p className="text-sm font-semibold text-gray-900 mt-1">
                              {paciente.cliente.nombre} {paciente.cliente.apellido}
                            </p>
                          </div>
                          <div>
                            <p className="text-xs text-gray-600 font-medium">Correo</p>
                            <p className="text-sm font-semibold text-gray-900 mt-1">{paciente.cliente.correo}</p>
                          </div>
                          {paciente.cliente.telefono && (
                            <div>
                              <p className="text-xs text-gray-600 font-medium">Teléfono</p>
                              <p className="text-sm font-semibold text-gray-900 mt-1">{paciente.cliente.telefono}</p>
                            </div>
                          )}
                          {paciente.cliente.direccion && (
                            <div>
                              <p className="text-xs text-gray-600 font-medium">Dirección</p>
                              <p className="text-sm font-semibold text-gray-900 mt-1">{paciente.cliente.direccion}</p>
                            </div>
                          )}
                        </div>
                      </section>
                    )}

                    {/* Acciones rápidas */}
                    <div className="flex flex-wrap gap-2 pt-2">
                      {!isVeterinario && (
                        <button
                          onClick={() => setIsEditModalOpen(true)}
                          className="flex-1 min-w-[140px] rounded-xl border-2 border-gray-300 bg-white px-4 py-2.5 text-sm font-bold text-gray-700 transition-all hover:border-primary hover:bg-primary hover:text-white"
                        >
                          ✏️ Editar
                        </button>
                      )}
                      {!isVeterinario && (
                        <button
                          onClick={() => setIsVacunacionModalOpen(true)}
                          className="flex-1 min-w-[140px] rounded-xl border-2 border-green-500 bg-green-500 px-4 py-2.5 text-sm font-bold text-white transition-all hover:bg-green-600"
                        >
                          💉 Vacunar
                        </button>
                      )}
                    </div>
                  </div>
                )}

                {activeTab === "historia" && (
                  <div className="space-y-4">
                    <div className="flex items-center justify-between">
                      <p className="text-sm text-gray-600">
                        {historia ? "Historia clínica disponible" : "No hay historia clínica registrada"}
                      </p>
                      <div className="flex gap-2">
                        <button
                          onClick={handleExportarPDF}
                          disabled={!historia}
                          className="rounded-xl border-2 border-primary bg-white px-4 py-2 text-sm font-bold text-primary transition-all hover:bg-primary hover:text-white disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                          📄 PDF
                        </button>
                        <button
                          onClick={handleGenerarResumen}
                          disabled={isGenerandoResumen}
                          className="rounded-xl border-2 border-blue-500 bg-blue-500 px-4 py-2 text-sm font-bold text-white transition-all hover:bg-blue-600 disabled:opacity-50"
                        >
                          {isGenerandoResumen ? "⏳" : "📋"} Resumen
                        </button>
                      </div>
                    </div>
                    {historia && (
                      <div className="bg-gray-50 rounded-2xl p-4">
                        <p className="text-xs text-gray-500 font-medium">Resumen</p>
                        <p className="text-sm text-gray-900 mt-2">{historia.resumen || "Sin resumen disponible"}</p>
                      </div>
                    )}
                  </div>
                )}

                {activeTab === "vacunas" && (
                  <div className="space-y-4">
                    <p className="text-sm text-gray-600">Historial de vacunaciones</p>
                    <div className="bg-gray-50 rounded-2xl p-6 text-center text-sm text-gray-500">
                      Funcionalidad en desarrollo
                    </div>
                  </div>
                )}
              </div>
            </>
          ) : (
            <div className="p-6">
              <div className="rounded-xl border border-dashed border-gray-200 bg-gray-50 p-6 text-center text-sm text-gray-500">
                No se pudo cargar la información del paciente.
              </div>
            </div>
          )}
        </div>
      </div>

      {!isVeterinario && (
        <EditPacienteModal
          isOpen={isEditModalOpen}
          pacienteId={pacienteId}
          onClose={() => setIsEditModalOpen(false)}
        />
      )}
      {!isVeterinario && (
        <CreateVacunacionModal
          isOpen={isVacunacionModalOpen}
          pacienteId={pacienteId || undefined}
          onClose={() => setIsVacunacionModalOpen(false)}
        />
      )}
    </div>
  );
};

