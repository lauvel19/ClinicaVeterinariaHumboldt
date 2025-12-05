import { useEffect, useMemo, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import dayjs from "dayjs";
import "dayjs/locale/es";
import toast from "react-hot-toast";

import { FullscreenLoader } from "../../../app/components/feedback/FullscreenLoader";
import { PacientesRepository } from "../../pacientes/services/PacientesRepository";
import { HistoriasRepository } from "../services/HistoriasRepository";
import { RegistroMedicoDetailModal } from "../components/RegistroMedicoDetailModal";
import type { ApiPacienteResponse } from "../../shared/types/backend";
import type { ApiRegistroMedicoResponse } from "../../shared/types/backend";

dayjs.locale("es");

export const VeterinarianHistoriesPage = () => {
  const { data: pacientes, isLoading: loadingPacientes } = useQuery({
    queryKey: ["pacientes"],
    queryFn: PacientesRepository.getAll,
  });

  const [selectedPatientId, setSelectedPatientId] = useState<number | null>(null);
  const [selectedRegistro, setSelectedRegistro] = useState<ApiRegistroMedicoResponse | null>(null);

  useEffect(() => {
    if (!selectedPatientId && pacientes?.length) {
      setSelectedPatientId(pacientes[0].id);
    }
  }, [pacientes, selectedPatientId]);

  const patient = useMemo(
    () => pacientes?.find((p) => p.id === selectedPatientId) ?? null,
    [pacientes, selectedPatientId],
  );

  const { data: historia, isLoading: loadingHistoria } = useQuery({
    queryKey: ["historia-clinica", selectedPatientId],
    enabled: selectedPatientId != null,
    queryFn: () => HistoriasRepository.getByPaciente(selectedPatientId!),
  });

  const { data: registros, isLoading: loadingRegistros } = useQuery({
    queryKey: ["historia-clinica-registros", historia?.id],
    enabled: Boolean(historia?.id),
    queryFn: () => HistoriasRepository.getRegistros(historia!.id),
  });

  const sortedRegistros = useMemo(() => {
    if (!registros) return [];
    return registros.slice().sort((a, b) => dayjs(b.fecha).valueOf() - dayjs(a.fecha).valueOf());
  }, [registros]);

  if (loadingPacientes || !selectedPatientId) {
    return <FullscreenLoader />;
  }

  return (
    <div className="space-y-6">
      <header className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-semibold text-secondary">Historias Clínicas</h2>
          <p className="text-sm text-gray-500">Consulta el expediente médico completo de cada paciente.</p>
        </div>
        <select
          className="rounded-2xl border border-gray-200 bg-white px-4 py-2 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/30"
          value={selectedPatientId ?? undefined}
          onChange={(event) => setSelectedPatientId(Number(event.target.value))}
        >
          {pacientes?.map((paciente) => (
            <option key={paciente.id} value={paciente.id}>
              {paciente.nombre} ({paciente.especie ?? "Sin especie"})
            </option>
          ))}
        </select>
      </header>

      <section className="rounded-3xl bg-white p-6 shadow-soft">
        {patient ? (
          <PatientSummary paciente={patient} historia={historia} />
        ) : (
          <div className="rounded-2xl border border-dashed border-gray-200 bg-gray-50 p-6 text-center text-sm text-gray-500">
            Selecciona un paciente para visualizar su historia clínica.
          </div>
        )}

        <div className="mt-6 border-t border-gray-100 pt-6">
          <div className="flex flex-wrap items-center justify-between gap-4">
            <div>
              <h3 className="text-lg font-semibold text-secondary">Historial de atenciones</h3>
              <p className="text-xs text-gray-500">
                Registros médicos ordenados cronológicamente. Puedes abrir cada registro para ver más detalles.
              </p>
            </div>
            <button
              className="rounded-2xl border border-primary px-4 py-2 text-xs font-semibold text-primary transition-base hover:bg-primary hover:text-white"
              onClick={async () => {
                if (!historia?.id) {
                  toast.error("No hay historia clínica para exportar");
                  return;
                }
                try {
                  const blob = await HistoriasRepository.exportarPDF(historia.id);
                  const url = window.URL.createObjectURL(blob);
                  const link = document.createElement("a");
                  link.href = url;
                  link.download = `historia_clinica_${paciente?.nombre || "paciente"}_${historia.id}.pdf`;
                  document.body.appendChild(link);
                  link.click();
                  document.body.removeChild(link);
                  window.URL.revokeObjectURL(url);
                  toast.success("PDF descargado exitosamente");
                } catch (error: any) {
                  toast.error(error.response?.data?.message || "Error al exportar el PDF");
                }
              }}
            >
              Exportar historial
            </button>
          </div>

          {loadingHistoria || loadingRegistros ? (
            <div className="mt-6">
              <FullscreenLoader />
            </div>
          ) : sortedRegistros.length === 0 ? (
            <div className="mt-6 rounded-2xl border border-dashed border-gray-200 bg-gray-50 p-6 text-center text-sm text-gray-500">
              Aún no hay registros médicos para este paciente.
            </div>
          ) : (
            <div className="mt-6 space-y-6">
              {sortedRegistros.map((registro, index) => (
                <TimelineItem 
                  key={registro.id} 
                  registro={registro} 
                  index={index} 
                  onViewDetail={() => setSelectedRegistro(registro)}
                />
              ))}
            </div>
          )}
        </div>
      </section>

      <RegistroMedicoDetailModal
        isOpen={selectedRegistro !== null}
        registro={selectedRegistro}
        onClose={() => setSelectedRegistro(null)}
      />
    </div>
  );
};

interface PatientSummaryProps {
  readonly paciente: ApiPacienteResponse | null;
  readonly historia: { resumen: string; fechaApertura: string } | undefined;
}

const PatientSummary = ({ paciente, historia }: PatientSummaryProps) => {
  if (!paciente) return null;

  const owner = paciente.cliente ? `${paciente.cliente.nombre} ${paciente.cliente.apellido}` : "Sin asignar";

  return (
    <div className="flex flex-col gap-6 md:flex-row md:items-start md:justify-between">
      <div>
        <p className="text-lg font-semibold text-secondary">{paciente.nombre}</p>
        <p className="text-xs text-gray-500 capitalize">
          {paciente.especie ?? "Especie no definida"} • {paciente.raza ?? "Raza no registrada"}
        </p>
        <p className="mt-2 text-xs text-gray-500">Historia abierta: {historia ? dayjs(historia.fechaApertura).format("DD MMM YYYY") : "—"}</p>
      </div>
      <div className="grid gap-2 text-sm text-gray-500 md:text-right">
        <p>
          <span className="font-semibold text-secondary">Propietario:</span> {owner}
        </p>
        {paciente.cliente?.telefono ? (
          <p>
            <span className="font-semibold text-secondary">Teléfono:</span> {paciente.cliente.telefono}
          </p>
        ) : null}
        {paciente.cliente?.correo ? (
          <p>
            <span className="font-semibold text-secondary">Correo:</span> {paciente.cliente.correo}
          </p>
        ) : null}
        {historia?.resumen ? (
          <p className="text-xs text-gray-500">Resumen: {historia.resumen}</p>
        ) : null}
      </div>
    </div>
  );
};

interface TimelineItemProps {
  readonly registro: ApiRegistroMedicoResponse;
  readonly index: number;
  readonly onViewDetail: () => void;
}

const TimelineItem = ({ registro, index, onViewDetail }: TimelineItemProps) => {
  const fecha = dayjs(registro.fecha);
  return (
    <div className="flex gap-4">
      <div className="flex flex-col items-center">
        <span className="relative z-10 flex h-8 w-8 items-center justify-center rounded-full bg-primary text-xs font-semibold text-white">
          {index + 1}
        </span>
        <span className="mt-2 h-full w-1 rounded-full bg-primary/20" />
      </div>
      <div className="flex-1 rounded-2xl border border-gray-100 bg-gray-50 p-4 shadow-sm">
        <header className="flex flex-wrap items-center justify-between gap-2">
          <div>
            <p className="text-sm font-semibold text-secondary">{registro.motivo ?? "Consulta médica"}</p>
            <p className="text-xs text-gray-500">{fecha.format("dddd, D [de] MMMM YYYY • HH:mm")}</p>
          </div>
          {registro.veterinario ? (
            <span className="rounded-full bg-primary/10 px-3 py-1 text-xs font-semibold text-primary">
              {registro.veterinario.nombre} {registro.veterinario.apellido}
            </span>
          ) : null}
        </header>

        {registro.diagnostico ? (
          <p className="mt-3 text-xs text-gray-600">
            <span className="font-semibold text-secondary">Diagnóstico:</span> {registro.diagnostico}
          </p>
        ) : null}

        {registro.tratamiento ? (
          <p className="mt-2 text-xs text-gray-600">
            <span className="font-semibold text-secondary">Tratamiento:</span> {registro.tratamiento}
          </p>
        ) : null}

        {registro.signosVitales ? (
          <div className="mt-3 grid grid-cols-2 gap-2 rounded-2xl bg-white/60 p-3 text-[11px] text-gray-500">
            {Object.entries(registro.signosVitales).map(([key, value]) => (
              <div key={key}>
                <span className="font-semibold text-secondary">{key}:</span> {String(value)}
              </div>
            ))}
          </div>
        ) : null}

        <div className="mt-3 flex gap-2 text-xs">
          <button
            className="rounded-2xl border border-primary px-3 py-1 font-semibold text-primary transition-base hover:bg-primary hover:text-white"
            onClick={onViewDetail}
          >
            👁️ Ver detalle completo
          </button>
        </div>
      </div>
    </div>
  );
};

export default VeterinarianHistoriesPage;

