// Página de gestión de pacientes para el secretario
import { useMemo, useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import dayjs from "dayjs";
import "dayjs/locale/es";
import toast from "react-hot-toast";

import { FullscreenLoader } from "../../../app/components/feedback/FullscreenLoader";
import { PacientesRepository } from "../../pacientes/services/PacientesRepository";
import { PacienteDetailModal } from "../../pacientes/components/PacienteDetailModal";
import type { ApiPacienteResponse } from "../../shared/types/backend";

dayjs.locale("es");

type SpeciesFilter = "TODOS" | "perro" | "gato" | "otros";
type OrderOption = "nombre-asc" | "nombre-desc";

const normalizeSpecies = (especie: string): SpeciesFilter => {
  const normalized = especie.toLowerCase();
  if (normalized.includes("perro")) return "perro";
  if (normalized.includes("gato")) return "gato";
  return "otros";
};

export const SecretaryPatientsPage = () => {
  const [search, setSearch] = useState("");
  const [speciesFilter, setSpeciesFilter] = useState<SpeciesFilter>("TODOS");
  const [order, setOrder] = useState<OrderOption>("nombre-asc");
  const [selectedPacienteId, setSelectedPacienteId] = useState<number | null>(null);
  const [showCreateModal, setShowCreateModal] = useState(false);
  
  const queryClient = useQueryClient();

  const { data, isLoading } = useQuery({
    queryKey: ["pacientes"],
    queryFn: PacientesRepository.getAll,
  });

  const filteredPatients = useMemo(() => {
    if (!data) return [];

    let filtered = data.filter((paciente) => {
      const matchesSearch =
        search === "" ||
        paciente.nombre.toLowerCase().includes(search.toLowerCase()) ||
        paciente.cliente?.nombre.toLowerCase().includes(search.toLowerCase()) ||
        paciente.cliente?.apellido.toLowerCase().includes(search.toLowerCase());

      const matchesSpecies = speciesFilter === "TODOS" || normalizeSpecies(paciente.especie) === speciesFilter;

      return matchesSearch && matchesSpecies;
    });

    filtered.sort((a, b) => {
      if (order === "nombre-asc") {
        return a.nombre.localeCompare(b.nombre);
      } else {
        return b.nombre.localeCompare(a.nombre);
      }
    });

    return filtered;
  }, [data, search, speciesFilter, order]);

  const stats = useMemo(() => buildStats(data || []), [data]);

  if (isLoading) {
    return <FullscreenLoader />;
  }

  return (
    <div className="w-full space-y-6">
      <header className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-semibold text-secondary">Pacientes</h2>
          <p className="text-sm text-gray-500">Gestiona los pacientes registrados en el sistema</p>
        </div>
        <button
          onClick={() => setShowCreateModal(true)}
          className="rounded-2xl bg-primary px-6 py-2.5 text-sm font-semibold text-white shadow-md transition-all hover:bg-primary-dark hover:shadow-lg"
        >
          + Nuevo Paciente
        </button>
      </header>

      <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        <StatCard title="Total Pacientes" value={stats.total.toString()} description="Registrados" />
        <StatCard title="Perros" value={stats.perros.toString()} description="Registrados" tone="primary" />
        <StatCard title="Gatos" value={stats.gatos.toString()} description="Registrados" tone="success" />
        <StatCard title="Otros" value={stats.otros.toString()} description="Registrados" tone="info" />
      </section>

      <section className="rounded-3xl bg-white p-6 shadow-soft">
        <div className="mb-6 flex flex-wrap items-center gap-4">
          <input
            type="text"
            placeholder="Buscar por nombre o propietario..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="flex-1 min-w-[200px] rounded-2xl border border-gray-200 bg-white px-4 py-2 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/30"
          />
          <select
            value={speciesFilter}
            onChange={(e) => setSpeciesFilter(e.target.value as SpeciesFilter)}
            className="rounded-2xl border border-gray-200 bg-white px-4 py-2 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/30"
          >
            <option value="TODOS">Todas las especies</option>
            <option value="perro">Perros</option>
            <option value="gato">Gatos</option>
            <option value="otros">Otros</option>
          </select>
          <select
            value={order}
            onChange={(e) => setOrder(e.target.value as OrderOption)}
            className="rounded-2xl border border-gray-200 bg-white px-4 py-2 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/30"
          >
            <option value="nombre-asc">Ordenar: A-Z</option>
            <option value="nombre-desc">Ordenar: Z-A</option>
          </select>
        </div>

        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {filteredPatients.length === 0 ? (
            <div className="col-span-full rounded-3xl border border-dashed border-gray-200 bg-gray-50 p-10 text-center text-sm text-gray-500">
              No se encontraron pacientes con los criterios seleccionados.
            </div>
          ) : (
            filteredPatients.map((paciente) => (
              <PatientCard
                key={paciente.id}
                paciente={paciente}
                onViewDetail={() => setSelectedPacienteId(paciente.id)}
              />
            ))
          )}
        </div>
      </section>

      <PacienteDetailModal
        isOpen={selectedPacienteId !== null}
        pacienteId={selectedPacienteId}
        onClose={() => setSelectedPacienteId(null)}
      />
      
      <CreatePacienteModal
        isOpen={showCreateModal}
        onClose={() => setShowCreateModal(false)}
        onSuccess={() => {
          queryClient.invalidateQueries({ queryKey: ["pacientes"] });
          setShowCreateModal(false);
        }}
      />
    </div>
  );
};

const buildStats = (pacientes: ApiPacienteResponse[]) => {
  const total = pacientes.length;
  const perros = pacientes.filter((paciente) => normalizeSpecies(paciente.especie) === "perro").length;
  const gatos = pacientes.filter((paciente) => normalizeSpecies(paciente.especie) === "gato").length;
  const otros = total - perros - gatos;
  return { total, perros, gatos, otros };
};

interface StatCardProps {
  readonly title: string;
  readonly value: string;
  readonly description: string;
  readonly tone?: "primary" | "success" | "warning" | "danger" | "info";
}

const StatCard = ({ title, value, description, tone = "primary" }: StatCardProps) => {
  const toneClasses = {
    primary: "bg-primary/10 text-primary border-primary/20",
    success: "bg-success/10 text-success border-success/20",
    warning: "bg-warning/10 text-warning border-warning/20",
    danger: "bg-danger/10 text-danger border-danger/20",
    info: "bg-blue-100 text-blue-600 border-blue-200",
  };

  return (
    <article className="rounded-2xl border bg-white p-6 shadow-soft">
      <p className="text-sm font-medium text-gray-500">{title}</p>
      <p className="mt-2 text-3xl font-semibold text-secondary">{value}</p>
      <span className={`mt-3 inline-flex rounded-full border px-3 py-1 text-xs font-semibold ${toneClasses[tone]}`}>
        {description}
      </span>
    </article>
  );
};

interface PatientCardProps {
  readonly paciente: ApiPacienteResponse;
  readonly onViewDetail: () => void;
}

const PatientCard = ({ paciente, onViewDetail }: PatientCardProps) => {
  const initials = `${paciente.nombre.charAt(0)}`;
  const ownerFullName = paciente.cliente ? `${paciente.cliente.nombre} ${paciente.cliente.apellido}` : "Sin asignar";
  const speciesBadge =
    normalizeSpecies(paciente.especie) === "perro"
      ? "bg-primary/10 text-primary"
      : normalizeSpecies(paciente.especie) === "gato"
        ? "bg-success/10 text-success"
        : "bg-info/10 text-info";

  return (
    <article className="flex flex-col rounded-2xl border border-gray-100 bg-gray-50 p-4 shadow-sm">
      <div className="flex items-start gap-3">
        <div className="flex h-12 w-12 items-center justify-center rounded-full bg-primary text-lg font-semibold text-white">
          {initials}
        </div>
        <div className="flex-1">
          <div className="flex items-center justify-between">
            <h3 className="text-sm font-semibold text-secondary">{paciente.nombre}</h3>
            <span className={`rounded-full px-2 py-0.5 text-xs font-semibold ${speciesBadge}`}>
              {paciente.especie}
            </span>
          </div>
          <p className="mt-1 text-xs text-gray-500">{paciente.raza || "Raza no especificada"}</p>
        </div>
      </div>

      <dl className="mt-3 space-y-1 text-xs">
        <div className="flex justify-between">
          <dt className="font-semibold text-secondary">Propietario</dt>
          <dd className="text-gray-600">{ownerFullName}</dd>
        </div>
        {paciente.fechaNacimiento && (
          <div className="flex justify-between">
            <dt className="font-semibold text-secondary">Edad</dt>
            <dd>{dayjs().diff(dayjs(paciente.fechaNacimiento), "year")} años</dd>
          </div>
        )}
        <div className="flex justify-between">
          <dt className="font-semibold text-secondary">Estado</dt>
          <dd>{paciente.estadoSalud ?? "Sin registrar"}</dd>
        </div>
      </dl>

      <div className="mt-auto mt-3">
        <button
          className="w-full rounded-2xl border border-gray-200 px-4 py-2 text-xs font-semibold text-secondary transition-base hover:border-primary hover:text-primary"
          onClick={onViewDetail}
        >
          Ver detalles
        </button>
      </div>
    </article>
  );
};

interface CreatePacienteModalProps {
  readonly isOpen: boolean;
  readonly onClose: () => void;
  readonly onSuccess: () => void;
}

const CreatePacienteModal = ({ isOpen, onClose, onSuccess }: CreatePacienteModalProps) => {
  const queryClient = useQueryClient();
  const [formData, setFormData] = useState({
    nombre: "",
    especie: "",
    raza: "",
    fechaNacimiento: "",
    sexo: "",
    peso: "",
    estadoSalud: "",
    clienteId: "",
  });
  const [selectedImage, setSelectedImage] = useState<File | null>(null);
  const [imagePreview, setImagePreview] = useState<string | null>(null);

  const { data: clientes } = useQuery({
    queryKey: ["clientes"],
    queryFn: async () => {
      const client = await import("../../../shared/api/ApiClient").then((m) => m.getApiClient());
      const { data } = await client.get("/clientes");
      return data.data || [];
    },
    enabled: isOpen,
  });

  const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      if (file.size > 5 * 1024 * 1024) {
        toast.error("La imagen no debe superar los 5MB");
        return;
      }
      
      if (!file.type.startsWith("image/")) {
        toast.error("El archivo debe ser una imagen");
        return;
      }
      
      setSelectedImage(file);
      
      const reader = new FileReader();
      reader.onloadend = () => {
        setImagePreview(reader.result as string);
      };
      reader.readAsDataURL(file);
    }
  };

  const removeImage = () => {
    setSelectedImage(null);
    setImagePreview(null);
  };

  const createMutation = useMutation({
    mutationFn: async (data: typeof formData) => {
      const paciente = await PacientesRepository.create({
        nombre: data.nombre,
        especie: data.especie,
        raza: data.raza,
        fechaNacimiento: data.fechaNacimiento,
        sexo: data.sexo,
        pesoKg: parseFloat(data.peso),
        estadoSalud: data.estadoSalud || undefined,
        clienteId: parseInt(data.clienteId),
      });
      
      // Si hay una imagen seleccionada, subirla
      if (selectedImage) {
        try {
          await PacientesRepository.subirFoto(paciente.id, selectedImage);
        } catch (error) {
          console.error("Error al subir la foto:", error);
          // No fallar todo el proceso si la foto falla
        }
      }
      
      return paciente;
    },
    onSuccess: () => {
      toast.success("Paciente creado exitosamente");
      queryClient.invalidateQueries({ queryKey: ["pacientes"] });
      onSuccess();
      resetForm();
    },
    onError: (error: any) => {
      toast.error(error?.response?.data?.message || "Error al crear paciente");
    },
  });

  const resetForm = () => {
    setFormData({
      nombre: "",
      especie: "",
      raza: "",
      fechaNacimiento: "",
      sexo: "",
      peso: "",
      estadoSalud: "",
      clienteId: "",
    });
    setSelectedImage(null);
    setImagePreview(null);
  };

  const handleClose = () => {
    resetForm();
    onClose();
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!formData.nombre || !formData.especie || !formData.clienteId) {
      toast.error("Completa los campos requeridos: Nombre, Especie y Propietario");
      return;
    }
    createMutation.mutate(formData);
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-2 sm:p-4 overflow-y-auto">
      <div className="w-full max-w-2xl rounded-3xl bg-white p-4 sm:p-6 shadow-xl my-4 max-h-[95vh] overflow-y-auto">
        <div className="mb-4 sm:mb-6 flex items-center justify-between sticky top-0 bg-white pb-3 border-b border-gray-100">
          <h2 className="text-xl sm:text-2xl font-semibold text-secondary">Nuevo Paciente</h2>
          <button
            onClick={handleClose}
            className="rounded-full p-2 hover:bg-gray-100 transition-colors"
          >
            ✕
          </button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-3 sm:space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Nombre * <span className="text-red-500">*</span>
              </label>
              <input
                type="text"
                value={formData.nombre}
                onChange={(e) => setFormData({ ...formData, nombre: e.target.value })}
                className="w-full rounded-xl border border-gray-200 px-4 py-2 focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/30"
                required
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Propietario <span className="text-red-500">*</span>
              </label>
              <select
                value={formData.clienteId}
                onChange={(e) => setFormData({ ...formData, clienteId: e.target.value })}
                className="w-full rounded-xl border border-gray-200 px-4 py-2 focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/30"
                required
              >
                <option value="">Seleccionar cliente</option>
                {clientes?.map((cliente: any) => (
                  <option key={cliente.id} value={cliente.id}>
                    {cliente.nombre} {cliente.apellido}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Especie <span className="text-red-500">*</span>
              </label>
              <select
                value={formData.especie}
                onChange={(e) => setFormData({ ...formData, especie: e.target.value })}
                className="w-full rounded-xl border border-gray-200 px-4 py-2 focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/30"
                required
              >
                <option value="">Seleccionar especie</option>
                <option value="perro">Perro</option>
                <option value="gato">Gato</option>
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Raza</label>
              <input
                type="text"
                value={formData.raza}
                onChange={(e) => setFormData({ ...formData, raza: e.target.value })}
                className="w-full rounded-xl border border-gray-200 px-4 py-2 focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/30"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Fecha de Nacimiento</label>
              <input
                type="date"
                value={formData.fechaNacimiento}
                onChange={(e) => setFormData({ ...formData, fechaNacimiento: e.target.value })}
                className="w-full rounded-xl border border-gray-200 px-4 py-2 focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/30"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Sexo</label>
              <select
                value={formData.sexo}
                onChange={(e) => setFormData({ ...formData, sexo: e.target.value })}
                className="w-full rounded-xl border border-gray-200 px-4 py-2 focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/30"
              >
                <option value="">Seleccionar</option>
                <option value="Macho">Macho</option>
                <option value="Hembra">Hembra</option>
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Peso (kg)</label>
              <input
                type="number"
                step="0.1"
                value={formData.peso}
                onChange={(e) => setFormData({ ...formData, peso: e.target.value })}
                className="w-full rounded-xl border border-gray-200 px-4 py-2 focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/30"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Estado de Salud</label>
              <select
                value={formData.estadoSalud}
                onChange={(e) => setFormData({ ...formData, estadoSalud: e.target.value })}
                className="w-full rounded-xl border border-gray-200 px-4 py-2 focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/30"
              >
                <option value="">Seleccionar</option>
                <option value="Saludable">Saludable</option>
                <option value="En tratamiento">En tratamiento</option>
                <option value="Crítico">Crítico</option>
              </select>
            </div>

            {/* Foto de Perfil */}
            <div className="col-span-1 md:col-span-2">
              <label className="block text-sm font-medium text-gray-700 mb-2">
                📸 Foto del Paciente
              </label>
              <div className="flex flex-col sm:flex-row items-center sm:items-start gap-3 sm:gap-4">
                {imagePreview ? (
                  <div className="relative flex-shrink-0">
                    <img
                      src={imagePreview}
                      alt="Preview"
                      className="h-24 w-24 sm:h-32 sm:w-32 rounded-full object-cover border-4 border-primary/20"
                    />
                    <button
                      type="button"
                      onClick={removeImage}
                      className="absolute -top-1 -right-1 sm:-top-2 sm:-right-2 rounded-full bg-red-500 p-1.5 text-white transition-all hover:bg-red-600"
                    >
                      <svg className="h-3 w-3 sm:h-4 sm:w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                      </svg>
                    </button>
                  </div>
                ) : (
                  <div className="h-24 w-24 sm:h-32 sm:w-32 flex-shrink-0 rounded-full bg-blue-100 flex items-center justify-center border-4 border-blue-200">
                    <svg className="h-12 w-12 sm:h-16 sm:w-16 text-blue-400" fill="currentColor" viewBox="0 0 24 24">
                      <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
                    </svg>
                  </div>
                )}
                <div className="flex-1 w-full text-center sm:text-left">
                  <label
                    htmlFor="foto-upload-secretary"
                    className="inline-flex cursor-pointer items-center justify-center gap-2 rounded-xl bg-primary px-4 py-2 text-sm font-medium text-white transition-all hover:bg-primary/90 w-full sm:w-auto"
                  >
                    <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                    </svg>
                    {imagePreview ? "Cambiar foto" : "Subir foto"}
                  </label>
                  <input
                    id="foto-upload-secretary"
                    type="file"
                    accept="image/*"
                    onChange={handleImageChange}
                    className="hidden"
                  />
                  <p className="mt-2 text-xs text-gray-500">
                    JPG, PNG, GIF o WEBP (máx. 5MB). Opcional.
                  </p>
                </div>
              </div>
            </div>
          </div>

          <div className="flex flex-col sm:flex-row justify-end gap-2 sm:gap-3 pt-4 sticky bottom-0 bg-white border-t border-gray-100 mt-4 pt-4">
            <button
              type="button"
              onClick={handleClose}
              className="w-full sm:w-auto rounded-xl border border-gray-200 px-6 py-2.5 text-sm font-semibold text-gray-700 hover:bg-gray-50 transition-colors"
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={createMutation.isPending}
              className="w-full sm:w-auto rounded-xl bg-primary px-6 py-2.5 text-sm font-semibold text-white hover:bg-primary-dark transition-colors disabled:opacity-50"
            >
              {createMutation.isPending ? "Creando..." : "Crear Paciente"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

