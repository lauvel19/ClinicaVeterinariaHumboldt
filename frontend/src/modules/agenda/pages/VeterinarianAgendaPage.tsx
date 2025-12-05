// Vista de agenda del veterinario conectada al backend real.
import { useMemo, useState } from "react";
import dayjs from "dayjs";
import localizedFormat from "dayjs/plugin/localizedFormat";
import isoWeek from "dayjs/plugin/isoWeek";
import "dayjs/locale/es";
import toast from "react-hot-toast";

import { FullscreenLoader } from "../../../app/components/feedback/FullscreenLoader";
import { useCitasVeterinario } from "../../citas/hooks/useCitasVeterinario";
import { CreateCitaModal } from "../../citas/components/CreateCitaModal";
import { CitaDetailModal } from "../../citas/components/CitaDetailModal";
import type { ApiCitaResponse } from "../../shared/types/backend";
import { authStore } from "../../../shared/state/authStore";

dayjs.extend(localizedFormat);
dayjs.extend(isoWeek);
dayjs.locale("es");

type AgendaView = "mes" | "semana" | "dia";

interface AgendaEvent {
  readonly id: number;
  readonly fecha: dayjs.Dayjs;
  readonly paciente: string;
  readonly tipo: string;
  readonly propietario: string;
  readonly estado: string;
}

const STATUS_COLORS: Record<string, string> = {
  REALIZADA: "bg-success/10 text-success border-success/20",
  PROGRAMADA: "bg-primary/10 text-primary border-primary/20",
  CANCELADA: "bg-danger/10 text-danger border-danger/20",
};

export const VeterinarianAgendaPage = () => {
  const [view, setView] = useState<AgendaView>("mes");
  const [currentDate, setCurrentDate] = useState(() => dayjs());
  const [isCreateCitaModalOpen, setIsCreateCitaModalOpen] = useState(false);
  const [selectedDate, setSelectedDate] = useState<dayjs.Dayjs | undefined>();
  const [selectedCita, setSelectedCita] = useState<ApiCitaResponse | null>(null);
  const { data, isLoading } = useCitasVeterinario();
  const { user } = authStore.getState();
  const isVeterinario = user?.rol === "VETERINARIO";

  const events: AgendaEvent[] = useMemo(() => {
    if (!data) return [];
    return data.map(mapCitaToEvent).sort((a, b) => a.fecha.valueOf() - b.fecha.valueOf());
  }, [data]);

  const visibleEvents = useMemo(() => {
    switch (view) {
      case "dia":
        return events.filter((event) => event.fecha.isSame(currentDate, "day"));
      case "semana": {
        const start = currentDate.startOf("week");
        const end = currentDate.endOf("week");
        return events.filter((event) => event.fecha.isAfter(start.subtract(1, "day")) && event.fecha.isBefore(end.add(1, "day")));
      }
      case "mes":
      default:
        return events.filter((event) => event.fecha.isSame(currentDate, "month"));
    }
  }, [events, view, currentDate]);

  const handlePrev = () => {
    const unit = view === "mes" ? "month" : view === "semana" ? "week" : "day";
    setCurrentDate((prev) => prev.subtract(1, unit));
  };

  const handleNext = () => {
    const unit = view === "mes" ? "month" : view === "semana" ? "week" : "day";
    setCurrentDate((prev) => prev.add(1, unit));
  };

  const handleToday = () => setCurrentDate(dayjs());

  if (isLoading) {
    return <FullscreenLoader />;
  }

  return (
    <div className="space-y-4 sm:space-y-6">
      <header className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h2 className="text-xl font-semibold text-secondary sm:text-2xl">Mi Agenda</h2>
          <p className="mt-1 text-xs text-gray-500 sm:text-sm">Consulta y gestiona las citas programadas para tus pacientes.</p>
        </div>
        <div className="flex flex-wrap gap-2 sm:gap-3">
          {/* Oculto para rol VETERINARIO: no puede agendar citas */}
          {!isVeterinario && (
            <button
              className="flex-1 rounded-xl bg-primary px-3 py-2 text-xs font-semibold text-white shadow-soft transition-base hover:bg-primary-dark sm:flex-none sm:rounded-2xl sm:px-4 sm:text-sm"
              onClick={() => {
                setSelectedDate(undefined);
                setIsCreateCitaModalOpen(true);
              }}
            >
              Agendar cita
            </button>
          )}
          <button
            className="flex-1 rounded-xl border border-gray-200 px-3 py-2 text-xs font-semibold text-secondary transition-base hover:border-primary hover:text-primary sm:flex-none sm:rounded-2xl sm:px-4 sm:text-sm"
            onClick={() => toast("Exportar a PDF está en desarrollo", { icon: "📄" })}
          >
            <span className="hidden sm:inline">Exportar PDF</span>
            <span className="sm:hidden">PDF</span>
          </button>
        </div>
      </header>

      <div className="flex flex-col gap-3 sm:flex-row sm:flex-wrap sm:items-center">
        <div className="flex gap-1 rounded-xl border border-gray-200 bg-white p-1 sm:rounded-2xl sm:gap-2">
          {(["mes", "semana", "dia"] as const).map((option) => (
            <button
              key={option}
              className={`flex-1 rounded-xl px-2 py-1.5 text-xs font-semibold capitalize transition-base sm:flex-none sm:rounded-2xl sm:px-4 sm:py-2 sm:text-sm ${
                option === view ? "bg-primary text-white shadow-soft" : "text-secondary hover:bg-gray-100"
              }`}
              onClick={() => {
                setView(option);
                setCurrentDate(dayjs());
              }}
            >
              {option === "mes" ? "Mes" : option === "semana" ? "Semana" : "Día"}
            </button>
          ))}
        </div>

        <div className="flex flex-wrap items-center gap-2">
          <div className="flex items-center gap-1 sm:gap-2">
            <button
              className="rounded-full border border-gray-200 p-1.5 text-xs text-secondary transition-base hover:border-primary hover:text-primary sm:p-2 sm:text-sm"
              onClick={handlePrev}
              aria-label="Anterior"
            >
              ◀
            </button>
            <button
              className="rounded-full border border-gray-200 px-2.5 py-1.5 text-xs font-medium text-secondary transition-base hover:border-primary hover:text-primary sm:px-3 sm:py-2 sm:text-sm"
              onClick={handleToday}
            >
              Hoy
            </button>
            <button
              className="rounded-full border border-gray-200 p-1.5 text-xs text-secondary transition-base hover:border-primary hover:text-primary sm:p-2 sm:text-sm"
              onClick={handleNext}
              aria-label="Siguiente"
            >
              ▶
            </button>
          </div>
          <p className="flex-1 text-xs font-semibold text-secondary sm:text-sm">
            {view === "mes" && (
              <>
                <span className="hidden sm:inline">{currentDate.format("MMMM [de] YYYY")}</span>
                <span className="sm:hidden">{currentDate.format("MMM YYYY")}</span>
              </>
            )}
            {view === "semana" && (
              <>
                <span className="hidden sm:inline">
                  Semana del {currentDate.startOf("week").format("D MMM")} al {currentDate.endOf("week").format("D MMM")}
                </span>
                <span className="sm:hidden">
                  {currentDate.startOf("week").format("D MMM")} - {currentDate.endOf("week").format("D MMM")}
                </span>
              </>
            )}
            {view === "dia" && (
              <>
                <span className="hidden sm:inline">{currentDate.format("dddd D [de] MMMM YYYY")}</span>
                <span className="sm:hidden">{currentDate.format("D MMM YYYY")}</span>
              </>
            )}
          </p>
        </div>
      </div>

      {view === "mes" ? (
        <MonthCalendar
          events={events}
          currentDate={currentDate}
          onSelectDay={setCurrentDate}
          onSelectEvent={(event) => {
            // Buscar la cita original en las citas
            const citaCompleta = data?.find((c) => c.idCita === event.id);
            if (citaCompleta) {
              setSelectedCita(citaCompleta);
            }
          }}
          isVeterinario={isVeterinario}
          setSelectedDate={setSelectedDate}
          setIsCreateCitaModalOpen={setIsCreateCitaModalOpen}
        />
      ) : null}

      <section className="rounded-2xl bg-white p-4 shadow-soft sm:rounded-3xl sm:p-6">
        <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
          <h3 className="text-base font-semibold text-secondary sm:text-lg">
            {view === "dia" && (
              <>
                <span className="hidden sm:inline">Citas del {currentDate.format("dddd D [de] MMMM")}</span>
                <span className="sm:hidden">Citas del día</span>
              </>
            )}
            {view === "semana" && "Citas de la semana"}
            {view === "mes" && "Citas del mes seleccionado"}
          </h3>
          <span className="text-xs text-gray-500 sm:text-sm">{visibleEvents.length} registros</span>
        </div>

        <div className="mt-4 space-y-3 sm:mt-6 sm:space-y-4">
          {visibleEvents.length === 0 ? (
            <div className="rounded-xl border border-dashed border-gray-200 bg-gray-50 p-4 text-center text-xs text-gray-500 sm:rounded-2xl sm:p-6 sm:text-sm">
              No hay citas para el rango seleccionado.
            </div>
          ) : (
            visibleEvents.map((event) => (
              <article
                key={event.id}
                className="flex flex-col gap-3 rounded-xl border border-gray-100 bg-gray-50 p-4 transition-base hover:border-primary/40 hover:bg-white sm:flex-row sm:items-center sm:gap-4 sm:rounded-2xl sm:px-6 sm:py-4"
              >
                <div className="flex w-full items-center justify-between sm:w-24 sm:flex-col">
                  <span className="text-base font-semibold text-secondary sm:text-lg">{event.fecha.format("HH:mm")}</span>
                  <span className="text-xs text-gray-500 sm:mt-0">{event.fecha.format("DD MMM")}</span>
                </div>
                <div className="flex-1 min-w-0">
                  <p className="truncate text-sm font-semibold text-secondary">{event.paciente}</p>
                  <p className="mt-0.5 text-xs text-gray-500">{event.tipo}</p>
                  <p className="mt-0.5 text-xs text-gray-400">
                    <span className="hidden sm:inline">Propietario: </span>
                    {event.propietario}
                  </p>
                </div>
                <div className="flex items-center gap-2 sm:flex-col sm:items-end sm:gap-3">
                  <span
                    className={`whitespace-nowrap rounded-full border px-2.5 py-1 text-xs font-semibold sm:px-3 ${
                      STATUS_COLORS[event.estado] ?? "bg-gray-100 text-secondary"
                    }`}
                  >
                    {event.estado === "REALIZADA" ? "Completada" : event.estado === "CANCELADA" ? "Cancelada" : "Programada"}
                  </span>
                  <button
                    className="flex-1 rounded-xl border border-primary px-3 py-1.5 text-xs font-semibold text-primary transition-base hover:bg-primary hover:text-white sm:flex-none sm:rounded-2xl sm:px-4 sm:py-2"
                    onClick={() => {
                      const citaOriginal = data?.find((c) => c.idCita === event.id);
                      if (citaOriginal) setSelectedCita(citaOriginal);
                    }}
                  >
                    Ver detalle
                  </button>
                </div>
              </article>
            ))
          )}
        </div>
      </section>

      {/* Oculto para rol VETERINARIO: el modal de creación solo para secretario/admin */}
      {!isVeterinario && (
        <CreateCitaModal
          isOpen={isCreateCitaModalOpen}
          onClose={() => setIsCreateCitaModalOpen(false)}
          initialDate={selectedDate}
        />
      )}
      <CitaDetailModal isOpen={selectedCita !== null} cita={selectedCita} onClose={() => setSelectedCita(null)} />
    </div>
  );
};

const mapCitaToEvent = (cita: ApiCitaResponse): AgendaEvent => ({
  id: cita.idCita,
  fecha: dayjs(cita.fechaHora),
  paciente: `${cita.paciente?.nombre ?? "Paciente"} ${cita.paciente?.especie ? `• ${cita.paciente?.especie}` : ""}`.trim(),
  tipo: cita.tipoServicio ?? cita.motivo ?? "Consulta",
  propietario: cita.paciente?.propietario ?? "No asignado",
  estado: cita.estado,
});

interface MonthCalendarProps {
  readonly events: AgendaEvent[];
  readonly currentDate: dayjs.Dayjs;
  readonly onSelectDay: (date: dayjs.Dayjs) => void;
  readonly onSelectEvent?: (event: AgendaEvent) => void;
  readonly isVeterinario?: boolean;
  readonly setSelectedDate?: (date: dayjs.Dayjs) => void;
  readonly setIsCreateCitaModalOpen?: (open: boolean) => void;
}

const MonthCalendar = ({ events, currentDate, onSelectDay, onSelectEvent, isVeterinario, setSelectedDate, setIsCreateCitaModalOpen }: MonthCalendarProps) => {
  const startOfMonth = currentDate.startOf("month");
  const startCalendar = startOfMonth.startOf("week"); // arranca el lunes
  const days = Array.from({ length: 42 }, (_, index) => startCalendar.add(index, "day"));

  return (
    <section className="rounded-2xl bg-white p-3 shadow-soft sm:rounded-3xl sm:p-6">
      <div className="grid grid-cols-7 gap-1 text-center text-[10px] font-semibold uppercase text-gray-400 sm:gap-2 sm:text-xs">
        {["Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"].map((day) => (
          <span key={day} className="hidden sm:inline">
            {day}
          </span>
        ))}
        {["L", "M", "X", "J", "V", "S", "D"].map((day, index) => (
          <span key={`${day}-${index}`} className="sm:hidden">
            {day}
          </span>
        ))}
      </div>

      <div className="mt-2 grid grid-cols-7 gap-1 sm:mt-3 sm:gap-2">
        {days.map((day) => {
          const dayEvents = events.filter((event) => event.fecha.isSame(day, "day"));
          const isCurrentMonth = day.isSame(currentDate, "month");
          const isToday = day.isSame(dayjs(), "day");

          return (
            <button
              key={day.toString()}
              onClick={() => {
                onSelectDay(day);
                // Para veterinarios, solo cambiar el día seleccionado para filtrar la lista
                // Para secretarios/admin, abrir modal de creación de cita
                if (!isVeterinario && setSelectedDate && setIsCreateCitaModalOpen) {
                  setSelectedDate(day);
                  setIsCreateCitaModalOpen(true);
                }
              }}
              className={`flex min-h-[60px] flex-col rounded-lg border p-1.5 text-left transition-base sm:min-h-[90px] sm:rounded-2xl sm:p-3 ${
                isCurrentMonth ? "border-gray-100 bg-gray-50 hover:border-primary/40 hover:bg-white" : "border-gray-50 bg-white/60 text-gray-300"
              } ${isToday ? "border-primary bg-primary/10" : ""}`}
            >
              <span className="text-[10px] font-semibold text-secondary sm:text-xs">{day.format("D")}</span>
              <div className="mt-1 flex flex-col gap-0.5 sm:mt-2 sm:gap-1">
                {dayEvents.slice(0, 2).map((event) => (
                  <button
                    key={event.id}
                    onClick={(e) => {
                      e.stopPropagation();
                      if (isVeterinario && onSelectEvent) {
                        onSelectEvent(event);
                      }
                    }}
                    className="hidden items-center gap-1 rounded-full bg-primary/10 px-1.5 py-0.5 text-[9px] font-medium text-primary hover:bg-primary/20 sm:inline-flex sm:px-2 sm:text-[10px]"
                  >
                    <span className="hidden sm:inline">{event.fecha.format("HH:mm")} · </span>
                    <span className="truncate">{event.paciente.split("•")[0]}</span>
                  </button>
                ))}
                {dayEvents.length > 0 && dayEvents.length <= 2 && (
                  <span className="hidden text-[9px] font-medium text-primary sm:inline sm:text-[10px]">
                    {dayEvents[0]?.fecha.format("HH:mm")}
                  </span>
                )}
                {dayEvents.length > 2 && (
                  <span className="text-[9px] font-medium text-gray-500 sm:text-[10px]">+{dayEvents.length - 2}</span>
                )}
              </div>
            </button>
          );
        })}
      </div>
    </section>
  );
};

export default VeterinarianAgendaPage;
