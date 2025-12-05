import { useState, useEffect } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import toast from "react-hot-toast";
import dayjs from "dayjs";

import { FullscreenLoader } from "../../../app/components/feedback/FullscreenLoader";
import { UsuariosRepository, type UsuarioRequest, type UsuarioUpdateRequest } from "../services/UsuariosRepository";
import type { ApiUsuarioResponse } from "../../shared/types/backend";

interface StatCardProps {
  readonly title: string;
  readonly value: string;
  readonly description?: string;
  readonly tone?: "primary" | "success" | "warning" | "danger" | "info";
}

const StatCard = ({ title, value, description, tone = "primary" }: StatCardProps) => {
  const toneClasses = {
    primary: "border-primary bg-primary/10 text-primary",
    success: "border-success bg-success/10 text-success",
    warning: "border-warning bg-warning/10 text-warning",
    danger: "border-danger bg-danger/10 text-danger",
    info: "border-info bg-info/10 text-info",
  };

  return (
    <div className={`rounded-3xl border p-6 ${toneClasses[tone]}`}>
      <h3 className="text-sm font-medium opacity-80">{title}</h3>
      <p className="mt-2 text-3xl font-bold">{value}</p>
      {description && <p className="mt-1 text-xs opacity-70">{description}</p>}
    </div>
  );
};

export const UsuariosPage = () => {
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [selectedUsuarioId, setSelectedUsuarioId] = useState<number | null>(null);
  const [search, setSearch] = useState("");

  const { data: usuarios, isLoading } = useQuery({
    queryKey: ["usuarios"],
    queryFn: UsuariosRepository.getAll,
  });

  const queryClient = useQueryClient();

  const deleteMutation = useMutation({
    mutationFn: UsuariosRepository.delete,
    onSuccess: () => {
      toast.success("Usuario eliminado exitosamente");
      queryClient.invalidateQueries({ queryKey: ["usuarios"] });
    },
    onError: (error: Error) => {
      toast.error(error.message || "Error al eliminar el usuario");
    },
  });

  const usuariosFiltrados = usuarios?.filter(
    (usuario) =>
      search === "" ||
      usuario.nombre.toLowerCase().includes(search.toLowerCase()) ||
      usuario.apellido.toLowerCase().includes(search.toLowerCase()) ||
      usuario.username.toLowerCase().includes(search.toLowerCase()) ||
      usuario.correo.toLowerCase().includes(search.toLowerCase())
  ) || [];

  const stats = {
    total: usuarios?.length || 0,
    activos: usuarios?.filter((u) => u.activo).length || 0,
    veterinarios: usuarios?.filter((u) => u.rol?.nombre === "VETERINARIO").length || 0,
    secretarios: usuarios?.filter((u) => u.rol?.nombre === "SECRETARIO").length || 0,
  };

  if (isLoading) {
    return <FullscreenLoader />;
  }

  return (
    <div className="w-full space-y-6">
      <header className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-semibold text-secondary">Gestión de Usuarios</h2>
          <p className="text-sm text-gray-500">Administra los usuarios del sistema</p>
        </div>
        <button
          onClick={() => setIsCreateModalOpen(true)}
          className="rounded-2xl bg-primary px-4 py-2 text-sm font-semibold text-white shadow-soft transition-base hover:bg-primary-dark"
        >
          Nuevo Usuario
        </button>
      </header>

      <section className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <StatCard title="Total Usuarios" value={stats.total.toString()} description="Registrados" tone="primary" />
        <StatCard
          title="Activos"
          value={stats.activos.toString()}
          description="En el sistema"
          tone={stats.activos === stats.total ? "success" : "warning"}
        />
        <StatCard title="Veterinarios" value={stats.veterinarios.toString()} description="Activos" tone="info" />
        <StatCard title="Secretarios" value={stats.secretarios.toString()} description="Activos" tone="info" />
      </section>

      <section className="rounded-3xl bg-white p-6 shadow-soft">
        <div className="mb-6">
          <input
            type="text"
            placeholder="Buscar por nombre, apellido, username o correo..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full rounded-2xl border border-gray-200 bg-white px-4 py-2 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/30"
          />
        </div>

        {usuariosFiltrados.length === 0 ? (
          <div className="rounded-2xl border border-dashed border-gray-200 bg-gray-50 p-10 text-center text-sm text-gray-500">
            No se encontraron usuarios con los filtros seleccionados.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead>
                <tr className="border-b border-gray-200">
                  <th className="px-4 py-3 text-left text-xs font-semibold uppercase text-gray-500">Usuario</th>
                  <th className="px-4 py-3 text-left text-xs font-semibold uppercase text-gray-500">Correo</th>
                  <th className="px-4 py-3 text-left text-xs font-semibold uppercase text-gray-500">Rol</th>
                  <th className="px-4 py-3 text-left text-xs font-semibold uppercase text-gray-500">Estado</th>
                  <th className="px-4 py-3 text-left text-xs font-semibold uppercase text-gray-500">Último Acceso</th>
                  <th className="px-4 py-3 text-left text-xs font-semibold uppercase text-gray-500">Acciones</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200">
                {usuariosFiltrados.map((usuario) => (
                  <tr key={usuario.id} className="hover:bg-gray-50">
                    <td className="px-4 py-3">
                      <div>
                        <p className="text-sm font-semibold text-secondary">
                          {usuario.nombre} {usuario.apellido}
                        </p>
                        <p className="text-xs text-gray-500">@{usuario.username}</p>
                      </div>
                    </td>
                    <td className="px-4 py-3 text-sm text-gray-600">{usuario.correo}</td>
                    <td className="px-4 py-3">
                      {usuario.rol ? (
                        <span 
                          className={`rounded-full px-2 py-1 text-xs font-semibold ${
                            usuario.rol.nombre === 'ADMIN' 
                              ? 'bg-purple-100 text-purple-700'
                              : usuario.rol.nombre === 'VETERINARIO'
                              ? 'bg-blue-100 text-blue-700'
                              : usuario.rol.nombre === 'SECRETARIO'
                              ? 'bg-green-100 text-green-700'
                              : usuario.rol.nombre === 'CLIENTE'
                              ? 'bg-gray-100 text-gray-700'
                              : 'bg-primary/10 text-primary'
                          }`}
                        >
                          {usuario.rol.nombre}
                        </span>
                      ) : (
                        <span className="rounded-full bg-cyan-100 px-2 py-1 text-xs font-semibold text-cyan-700">
                          Sin rol
                        </span>
                      )}
                    </td>
                    <td className="px-4 py-3">
                      <span
                        className={`rounded-full px-2 py-1 text-xs font-semibold ${
                          usuario.activo ? "bg-success/10 text-success" : "bg-danger/10 text-danger"
                        }`}
                      >
                        {usuario.activo ? "Activo" : "Inactivo"}
                      </span>
                    </td>
                    <td className="px-4 py-3 text-xs text-gray-500">
                      {usuario.ultimoAcceso ? dayjs(usuario.ultimoAcceso).format("DD/MM/YYYY HH:mm") : "Nunca"}
                    </td>
                    <td className="px-4 py-3">
                      <div className="flex gap-2">
                        <button
                          onClick={() => {
                            setSelectedUsuarioId(usuario.id);
                            setIsEditModalOpen(true);
                          }}
                          className="rounded-lg border border-primary bg-primary/10 px-3 py-1 text-xs font-semibold text-primary transition-base hover:bg-primary hover:text-white"
                        >
                          Editar
                        </button>
                        <button
                          onClick={() => {
                            if (confirm(`¿Está seguro de eliminar al usuario ${usuario.nombre} ${usuario.apellido}?`)) {
                              deleteMutation.mutate(usuario.id);
                            }
                          }}
                          disabled={deleteMutation.isPending}
                          className="rounded-lg border border-danger bg-danger/10 px-3 py-1 text-xs font-semibold text-danger transition-base hover:bg-danger hover:text-white disabled:opacity-50"
                        >
                          Eliminar
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      {isCreateModalOpen && (
        <CreateUsuarioModal
          isOpen={isCreateModalOpen}
          onClose={() => setIsCreateModalOpen(false)}
        />
      )}

      {isEditModalOpen && selectedUsuarioId && (
        <EditUsuarioModal
          isOpen={isEditModalOpen}
          usuarioId={selectedUsuarioId}
          onClose={() => {
            setIsEditModalOpen(false);
            setSelectedUsuarioId(null);
          }}
        />
      )}
    </div>
  );
};

interface CreateUsuarioModalProps {
  readonly isOpen: boolean;
  readonly onClose: () => void;
}

const CreateUsuarioModal = ({ isOpen, onClose }: CreateUsuarioModalProps) => {
  const queryClient = useQueryClient();
  const [formData, setFormData] = useState<UsuarioRequest>({
    nombre: "",
    apellido: "",
    correo: "",
    telefono: "",
    direccion: "",
    username: "",
    password: "",
    rolId: 2, // VETERINARIO por defecto
    activo: true,
  });

  const createMutation = useMutation({
    mutationFn: UsuariosRepository.create,
    onSuccess: () => {
      toast.success("Usuario creado exitosamente");
      queryClient.invalidateQueries({ queryKey: ["usuarios"] });
      onClose();
      setFormData({
        nombre: "",
        apellido: "",
        correo: "",
        telefono: "",
        direccion: "",
        username: "",
        password: "",
        rolId: 2,
        activo: true,
      });
    },
    onError: (error: Error) => {
      toast.error(error.message || "Error al crear el usuario");
    },
  });

  if (!isOpen) return null;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    createMutation.mutate(formData);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
      <div className="w-full max-w-2xl max-h-[90vh] overflow-y-auto rounded-3xl bg-white p-6 shadow-soft">
        <div className="mb-4 flex items-center justify-between">
          <h3 className="text-lg font-semibold text-secondary">Crear Usuario</h3>
          <button onClick={onClose} className="text-gray-400 hover:text-gray-600">
            ✕
          </button>
        </div>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="mb-1 block text-sm font-medium text-gray-700">Nombre *</label>
              <input
                type="text"
                value={formData.nombre}
                onChange={(e) => setFormData({ ...formData, nombre: e.target.value })}
                required
                className="w-full rounded-2xl border border-gray-200 bg-white px-4 py-2 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/30"
              />
            </div>
            <div>
              <label className="mb-1 block text-sm font-medium text-gray-700">Apellido *</label>
              <input
                type="text"
                value={formData.apellido}
                onChange={(e) => setFormData({ ...formData, apellido: e.target.value })}
                required
                className="w-full rounded-2xl border border-gray-200 bg-white px-4 py-2 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/30"
              />
            </div>
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">Correo Electrónico *</label>
            <input
              type="email"
              value={formData.correo}
              onChange={(e) => setFormData({ ...formData, correo: e.target.value })}
              required
              className="w-full rounded-2xl border border-gray-200 bg-white px-4 py-2 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/30"
            />
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="mb-1 block text-sm font-medium text-gray-700">Teléfono</label>
              <input
                type="tel"
                value={formData.telefono}
                onChange={(e) => setFormData({ ...formData, telefono: e.target.value })}
                className="w-full rounded-2xl border border-gray-200 bg-white px-4 py-2 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/30"
              />
            </div>
            <div>
              <label className="mb-1 block text-sm font-medium text-gray-700">Dirección</label>
              <input
                type="text"
                value={formData.direccion}
                onChange={(e) => setFormData({ ...formData, direccion: e.target.value })}
                className="w-full rounded-2xl border border-gray-200 bg-white px-4 py-2 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/30"
              />
            </div>
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="mb-1 block text-sm font-medium text-gray-700">Username *</label>
              <input
                type="text"
                value={formData.username}
                onChange={(e) => setFormData({ ...formData, username: e.target.value })}
                required
                className="w-full rounded-2xl border border-gray-200 bg-white px-4 py-2 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/30"
              />
            </div>
            <div>
              <label className="mb-1 block text-sm font-medium text-gray-700">Contraseña *</label>
              <input
                type="password"
                value={formData.password}
                onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                required
                minLength={8}
                className="w-full rounded-2xl border border-gray-200 bg-white px-4 py-2 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/30"
              />
            </div>
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">Rol *</label>
            <select
              value={formData.rolId}
              onChange={(e) => setFormData({ ...formData, rolId: Number(e.target.value) })}
              required
              className="w-full rounded-2xl border border-gray-200 bg-white px-4 py-2 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/30"
            >
              <option value={1}>ADMIN</option>
              <option value={2}>VETERINARIO</option>
              <option value={3}>SECRETARIO</option>
              <option value={4}>CLIENTE</option>
            </select>
          </div>
          <div className="flex items-center gap-2">
            <input
              type="checkbox"
              checked={formData.activo}
              onChange={(e) => setFormData({ ...formData, activo: e.target.checked })}
              className="rounded border-gray-300 text-primary focus:ring-primary"
            />
            <label className="text-sm text-gray-700">Usuario activo</label>
          </div>
          <div className="flex gap-3 pt-4">
            <button
              type="button"
              onClick={onClose}
              className="flex-1 rounded-xl border border-gray-200 bg-white px-4 py-2 text-sm font-medium text-gray-700 transition-base hover:bg-gray-50"
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={createMutation.isPending}
              className="flex-1 rounded-xl bg-primary px-4 py-2 text-sm font-medium text-white transition-base hover:bg-primary-dark disabled:opacity-50"
            >
              {createMutation.isPending ? "Creando..." : "Crear Usuario"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

interface EditUsuarioModalProps {
  readonly isOpen: boolean;
  readonly usuarioId: number;
  readonly onClose: () => void;
}

const EditUsuarioModal = ({ isOpen, usuarioId, onClose }: EditUsuarioModalProps) => {
  const queryClient = useQueryClient();
  const { data: usuario } = useQuery({
    queryKey: ["usuario", usuarioId],
    queryFn: () => UsuariosRepository.getById(usuarioId),
    enabled: isOpen,
  });

  const [formData, setFormData] = useState<UsuarioUpdateRequest>({
    nombre: "",
    apellido: "",
    correo: "",
    telefono: "",
    direccion: "",
    username: "",
    rolId: undefined,
    activo: true,
  });

  // Actualizar formData cuando se carga el usuario
  useEffect(() => {
    if (usuario) {
      setFormData({
        nombre: usuario.nombre,
        apellido: usuario.apellido,
        correo: usuario.correo,
        telefono: usuario.telefono || "",
        direccion: usuario.direccion || "",
        username: usuario.username,
        rolId: usuario.rol?.id,
        activo: usuario.activo,
      });
    }
  }, [usuario]);

  const updateMutation = useMutation({
    mutationFn: (data: UsuarioUpdateRequest) => UsuariosRepository.update(usuarioId, data),
    onSuccess: () => {
      toast.success("Usuario actualizado exitosamente");
      queryClient.invalidateQueries({ queryKey: ["usuarios"] });
      queryClient.invalidateQueries({ queryKey: ["usuario", usuarioId] });
      onClose();
    },
    onError: (error: Error) => {
      toast.error(error.message || "Error al actualizar el usuario");
    },
  });

  if (!isOpen || !usuario) return null;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    updateMutation.mutate(formData);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
      <div className="w-full max-w-2xl max-h-[90vh] overflow-y-auto rounded-3xl bg-white p-6 shadow-soft">
        <div className="mb-4 flex items-center justify-between">
          <h3 className="text-lg font-semibold text-secondary">Editar Usuario</h3>
          <button onClick={onClose} className="text-gray-400 hover:text-gray-600">
            ✕
          </button>
        </div>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="mb-1 block text-sm font-medium text-gray-700">Nombre</label>
              <input
                type="text"
                value={formData.nombre}
                onChange={(e) => setFormData({ ...formData, nombre: e.target.value })}
                className="w-full rounded-2xl border border-gray-200 bg-white px-4 py-2 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/30"
              />
            </div>
            <div>
              <label className="mb-1 block text-sm font-medium text-gray-700">Apellido</label>
              <input
                type="text"
                value={formData.apellido}
                onChange={(e) => setFormData({ ...formData, apellido: e.target.value })}
                className="w-full rounded-2xl border border-gray-200 bg-white px-4 py-2 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/30"
              />
            </div>
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">Correo Electrónico</label>
            <input
              type="email"
              value={formData.correo}
              onChange={(e) => setFormData({ ...formData, correo: e.target.value })}
              className="w-full rounded-2xl border border-gray-200 bg-white px-4 py-2 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/30"
            />
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="mb-1 block text-sm font-medium text-gray-700">Teléfono</label>
              <input
                type="tel"
                value={formData.telefono}
                onChange={(e) => setFormData({ ...formData, telefono: e.target.value })}
                className="w-full rounded-2xl border border-gray-200 bg-white px-4 py-2 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/30"
              />
            </div>
            <div>
              <label className="mb-1 block text-sm font-medium text-gray-700">Dirección</label>
              <input
                type="text"
                value={formData.direccion}
                onChange={(e) => setFormData({ ...formData, direccion: e.target.value })}
                className="w-full rounded-2xl border border-gray-200 bg-white px-4 py-2 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/30"
              />
            </div>
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">Username</label>
            <input
              type="text"
              value={formData.username}
              onChange={(e) => setFormData({ ...formData, username: e.target.value })}
              className="w-full rounded-2xl border border-gray-200 bg-white px-4 py-2 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/30"
            />
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">Rol</label>
            <select
              value={formData.rolId || ""}
              onChange={(e) => setFormData({ ...formData, rolId: e.target.value ? Number(e.target.value) : undefined })}
              className="w-full rounded-2xl border border-gray-200 bg-white px-4 py-2 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/30"
            >
              <option value="">Seleccionar rol</option>
              <option value={1}>ADMIN</option>
              <option value={2}>VETERINARIO</option>
              <option value={3}>SECRETARIO</option>
              <option value={4}>CLIENTE</option>
            </select>
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">Nueva Contraseña (opcional)</label>
            <input
              type="password"
              value={formData.password || ""}
              onChange={(e) => setFormData({ ...formData, password: e.target.value || undefined })}
              minLength={8}
              className="w-full rounded-2xl border border-gray-200 bg-white px-4 py-2 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/30"
              placeholder="Dejar vacío para no cambiar"
            />
          </div>
          <div className="flex items-center gap-2">
            <input
              type="checkbox"
              checked={formData.activo}
              onChange={(e) => setFormData({ ...formData, activo: e.target.checked })}
              className="rounded border-gray-300 text-primary focus:ring-primary"
            />
            <label className="text-sm text-gray-700">Usuario activo</label>
          </div>
          <div className="flex gap-3 pt-4">
            <button
              type="button"
              onClick={onClose}
              className="flex-1 rounded-xl border border-gray-200 bg-white px-4 py-2 text-sm font-medium text-gray-700 transition-base hover:bg-gray-50"
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={updateMutation.isPending}
              className="flex-1 rounded-xl bg-primary px-4 py-2 text-sm font-medium text-white transition-base hover:bg-primary-dark disabled:opacity-50"
            >
              {updateMutation.isPending ? "Actualizando..." : "Actualizar Usuario"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

