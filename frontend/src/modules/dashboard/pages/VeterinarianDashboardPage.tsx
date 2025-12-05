// Página principal del veterinario que replica la vista de dashboard del prototipo.
// Genera tarjetas de resumen, lista de citas del día y accesos rápidos.
import { Fragment, useState } from "react";
import { useNavigate } from "react-router-dom";
import classNames from "classnames";

import { useVeterinarianDashboard } from "../hooks/useVeterinarianDashboard";
import { FullscreenLoader } from "../../../app/components/feedback/FullscreenLoader";
import { CreateCitaModal } from "../../citas/components/CreateCitaModal";
import { CitaDetailModal } from "../../citas/components/CitaDetailModal";
import type { ApiCitaResponse } from "../../shared/types/backend";

const statusLabels: Record<string, { label: string; tone: string }> = {
  COMPLETADA: { label: "Completada", tone: "bg-success/20 text-success" },
  PENDIENTE: { label: "Pendiente", tone: "bg-warning/20 text-warning" },
  URGENTE: { label: "Urgente", tone: "bg-danger/20 text-danger" },
  EN_CURSO: { label: "En progreso", tone: "bg-primary/20 text-primary" },
  CANCELADA: { label: "Cancelada", tone: "bg-danger/20 text-danger" },
};

export const VeterinarianDashboardPage = () => {
  const navigate = useNavigate();
  const { data, isLoading, isError } = useVeterinarianDashboard();
  const [isCreateCitaModalOpen, setIsCreateCitaModalOpen] = useState(false);
  const [selectedCita, setSelectedCita] = useState<ApiCitaResponse | null>(null);

  if (isLoading) {
    return <FullscreenLoader />;
  }

  if (isError || !data) {
    return (
      <div className="rounded-3xl border border-danger/40 bg-white p-10 text-center shadow-soft">
        <h2 className="text-xl font-semibold text-danger">No se pudo cargar el dashboard</h2>
        <p className="mt-2 text-sm text-gray-500">
          Verifica la conexión con el backend. Se requiere que el endpoint `/prestaciones/veterinarios/[id]/dashboard`
          esté implementado.
        </p>
      </div>
    );
  }

  const {
    resumen: { citasHoy, citasCompletadasHoy, pacientesAsignados, pendientes, proximaCita },
    citasDelDia,
    accesosRapidos,
    citasOriginales,
  } = data;

  return (
    <div className="w-full space-y-4 sm:space-y-6">
      {/* Tarjetas de resumen superior */}
      <section className="grid w-full gap-3 sm:gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <SummaryCard title="Citas Hoy" value={citasHoy} subtitle={`${citasCompletadasHoy} completadas`} />
        <SummaryCard title="Pacientes" value={pacientesAsignados} subtitle="Bajo tu cuidado" />
        <SummaryCard title="Pendientes" value={pendientes} subtitle="Seguimientos activos" tone="warning" />
        <SummaryCard
          title="Próxima cita"
          value={proximaCita?.hora ?? "--:--"}
          subtitle={proximaCita?.descripcion ?? "Sin citas próximas"}
        />
      </section>

      {/* Sección de citas de hoy */}
      <section className="w-full overflow-hidden rounded-2xl border border-gray-200/80 bg-white shadow-lg sm:rounded-3xl">
        <div className="border-b border-gray-100 bg-gradient-to-r from-white to-gray-50/50 px-4 py-4 sm:px-6 sm:py-5">
          <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between sm:gap-4">
            <div className="min-w-0 flex-1">
              <div className="flex items-center gap-2 sm:gap-3">
                <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-primary/10 sm:h-10 sm:w-10 sm:rounded-xl">
                  <span className="text-base sm:text-xl">📅</span>
                </div>
                <div className="min-w-0 flex-1">
                  <h2 className="text-base font-bold text-secondary sm:text-xl">Citas de hoy</h2>
                  <p className="mt-0.5 hidden text-sm font-medium text-gray-500 sm:block">Gestiona tus citas programadas para el día</p>
                </div>
              </div>
            </div>
            <div className="flex flex-wrap gap-2">
              <button 
                onClick={() => navigate('/veterinario/pacientes')}
                className="flex-1 rounded-lg border-2 border-gray-200 bg-white px-3 py-2 text-xs font-semibold text-gray-700 shadow-sm transition-all hover:border-primary hover:bg-primary/5 hover:text-primary hover:shadow-md sm:flex-none sm:rounded-xl sm:px-4 sm:py-2.5 sm:text-sm"
              >
                🔍 <span className="hidden sm:inline">Buscar paciente</span><span className="sm:hidden">Buscar</span>
              </button>
              <button 
                onClick={() => navigate('/veterinario/agenda')}
                className="flex-1 rounded-lg border-2 border-gray-200 bg-white px-3 py-2 text-xs font-semibold text-gray-700 shadow-sm transition-all hover:border-primary hover:bg-primary/5 hover:text-primary hover:shadow-md sm:flex-none sm:rounded-xl sm:px-4 sm:py-2.5 sm:text-sm"
              >
                📆 <span className="hidden sm:inline">Ver mi agenda</span><span className="sm:hidden">Agenda</span>
              </button>
            </div>
          </div>
        </div>

        <div className="p-4 sm:p-6">
          {citasDelDia.length === 0 ? (
            <div className="rounded-xl border-2 border-dashed border-gray-200 bg-gradient-to-br from-gray-50 to-white p-8 text-center sm:rounded-2xl sm:p-16">
              <div className="mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-xl bg-gradient-to-br from-primary/10 to-primary/5 shadow-inner sm:mb-6 sm:h-20 sm:w-20 sm:rounded-2xl">
                <span className="text-2xl sm:text-4xl">📅</span>
              </div>
              <p className="text-base font-bold text-gray-700 sm:text-lg">No hay citas programadas</p>
              <p className="mt-2 text-xs font-medium text-gray-500 sm:text-sm">No hay citas programadas para el día de hoy.</p>
            </div>
          ) : (
            <div className="space-y-2 sm:space-y-3">
              {citasDelDia.map((cita) => {
                const statusTone = statusLabels[cita.estado] ?? statusLabels.PENDIENTE;
                return (
                  <article
                    key={cita.idCita}
                    className="group flex flex-col gap-3 rounded-xl border-2 border-gray-200/80 bg-white p-4 shadow-sm transition-all hover:border-primary/40 hover:shadow-lg sm:flex-row sm:items-center sm:gap-5 sm:rounded-2xl sm:p-5"
                  >
                    <div className="flex min-w-[70px] flex-col items-center justify-center rounded-lg bg-gradient-to-br from-primary/10 to-primary/5 py-2 px-3 shadow-inner sm:min-w-[90px] sm:rounded-xl sm:py-3 sm:px-4">
                      <span className="text-base font-bold text-primary sm:text-xl">{cita.hora}</span>
                      <span className="text-[9px] font-bold uppercase tracking-wider text-gray-500 sm:text-[10px]">Hora</span>
                    </div>
                    <div className="flex-1 min-w-0">
                      <div className="flex flex-col gap-2 sm:flex-row sm:items-start sm:justify-between sm:gap-4">
                        <div className="min-w-0 flex-1">
                          <p className="truncate text-sm font-bold text-secondary sm:text-base">
                            {cita.paciente} <span className="font-normal text-gray-500">- {cita.especie}</span>
                          </p>
                          <p className="mt-1 text-xs font-medium text-gray-600 sm:mt-1.5 sm:text-sm">{cita.motivo}</p>
                          <p className="mt-1 text-[10px] font-medium text-gray-500 sm:mt-1.5 sm:text-xs">
                            Propietario: <span className="font-semibold text-gray-700">{cita.propietario}</span>
                          </p>
                        </div>
                        <span className={classNames("self-start whitespace-nowrap rounded-lg px-2.5 py-1.5 text-[10px] font-bold shadow-sm sm:rounded-xl sm:px-3.5 sm:py-2 sm:text-xs", statusTone.tone)}>
                          {statusTone.label}
                        </span>
                      </div>
                    </div>
                    <button
                      onClick={() => {
                        const citaOriginal = citasOriginales?.find((c) => c.idCita === cita.idCita);
                        if (citaOriginal) setSelectedCita(citaOriginal);
                      }}
                      className="w-full rounded-lg border-2 border-primary/30 bg-white px-4 py-2 text-xs font-bold text-primary shadow-sm transition-all hover:border-primary hover:bg-primary hover:text-white hover:shadow-md sm:w-auto sm:rounded-xl sm:px-5 sm:py-2.5 sm:text-sm"
                    >
                      {cita.estado === "PENDIENTE" ? "▶ Iniciar" : "👁 Ver"}
                    </button>
                  </article>
                );
              })}
            </div>
          )}
        </div>
      </section>

      {/* Tarjetas de accesos rápidos */}
      <section className="grid w-full gap-3 sm:gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <ShortcutCard
          title="Seguimientos activos"
          description="Tratamientos en progreso"
          value={`${accesosRapidos.seguimientosActivos} activos`}
          onClick={() => navigate('/veterinario/seguimientos')}
        />
        <ShortcutCard
          title="Vacunas pendientes"
          description="Próximas aplicaciones"
          value={`${accesosRapidos.vacunasPendientes} pendientes`}
          onClick={() => navigate('/veterinario/pacientes')}
        />
        <ShortcutCard
          title="Historias clínicas"
          description="Buscar registros médicos"
          value={`${accesosRapidos.historiasClinicas} registros`}
          onClick={() => navigate('/veterinario/historias')}
        />
        <ShortcutCard
          title="Pacientes nuevos"
          description="Registrados esta semana"
          value={`${accesosRapidos.pacientesNuevosSemana} nuevos`}
          onClick={() => navigate('/veterinario/pacientes')}
        />
      </section>

      <CreateCitaModal isOpen={isCreateCitaModalOpen} onClose={() => setIsCreateCitaModalOpen(false)} />
      <CitaDetailModal isOpen={selectedCita !== null} cita={selectedCita} onClose={() => setSelectedCita(null)} />
    </div>
  );
};

interface SummaryCardProps {
  readonly title: string;
  readonly value: number | string;
  readonly subtitle: string;
  readonly tone?: "default" | "warning";
}

const SummaryCard = ({ title, value, subtitle, tone = "default" }: SummaryCardProps) => {
  const toneClasses =
    tone === "warning"
      ? "bg-gradient-to-br from-amber-50 to-amber-100/50 text-amber-700 border-amber-200/60"
      : "bg-gradient-to-br from-blue-50 to-primary/5 text-blue-700 border-blue-200/60";

  const iconMap: Record<string, string> = {
    "Citas Hoy": "📅",
    "Pacientes": "👥",
    "Pendientes": "⏰",
    "Próxima cita": "🕐",
  };

  return (
    <div className="group relative overflow-hidden rounded-2xl border border-gray-200/80 bg-white p-6 shadow-sm transition-all hover:border-primary/30 hover:shadow-lg">
      <div className="absolute top-0 right-0 h-20 w-20 -translate-y-8 translate-x-8 rounded-full bg-gradient-to-br from-primary/5 to-transparent opacity-50 transition-transform group-hover:scale-150"></div>
      <div className="relative">
        <div className="mb-3 flex items-center justify-between">
          <p className="text-xs font-bold uppercase tracking-wider text-gray-500">{title}</p>
          <span className="text-xl opacity-60">{iconMap[title] || "📊"}</span>
        </div>
        <p className="mb-4 text-4xl font-bold text-secondary">{value}</p>
        <div>
          <span className={classNames("inline-flex rounded-lg border px-3 py-1.5 text-xs font-bold shadow-sm", toneClasses)}>
            {subtitle}
          </span>
        </div>
      </div>
    </div>
  );
};

interface ShortcutCardProps {
  readonly title: string;
  readonly description: string;
  readonly value: string;
  readonly onClick?: () => void;
}

const ShortcutCard = ({ title, description, value, onClick }: ShortcutCardProps) => {
  const iconMap: Record<string, { icon: string; gradient: string }> = {
    "Seguimientos activos": { icon: "📋", gradient: "from-purple-50 to-purple-100/30" },
    "Vacunas pendientes": { icon: "💉", gradient: "from-green-50 to-green-100/30" },
    "Historias clínicas": { icon: "📁", gradient: "from-blue-50 to-blue-100/30" },
    "Pacientes nuevos": { icon: "✨", gradient: "from-orange-50 to-orange-100/30" },
  };

  const cardConfig = iconMap[title] || { icon: "📊", gradient: "from-gray-50 to-gray-100/30" };

  return (
    <article className="group relative flex flex-col justify-between overflow-hidden rounded-2xl border border-gray-200/80 bg-white p-6 shadow-sm transition-all hover:border-primary/40 hover:shadow-lg">
      <div className={`absolute inset-0 bg-gradient-to-br ${cardConfig.gradient} opacity-0 transition-opacity group-hover:opacity-100`}></div>
      <header className="relative z-10">
        <div className="mb-3 flex items-center justify-between">
          <p className="text-xs font-bold uppercase tracking-wider text-primary">{title}</p>
          <span className="text-2xl">{cardConfig.icon}</span>
        </div>
        <p className="text-sm font-medium text-gray-600">{description}</p>
      </header>
      <div className="relative z-10 mt-4">
        <p className="text-3xl font-bold text-secondary">{value}</p>
      </div>
      <button 
        onClick={onClick}
        className="relative z-10 mt-6 w-fit rounded-xl border-2 border-primary/20 bg-white px-4 py-2 text-xs font-bold text-primary transition-all hover:border-primary hover:bg-primary hover:text-white hover:shadow-md"
      >
        Ver detalle →
      </button>
    </article>
  );
};

export default VeterinarianDashboardPage;


