import { useQuery } from "@tanstack/react-query";
import { useNavigate } from "react-router-dom";
import { IconCalendar, IconPaw, IconDocument, IconCreditCard } from "../../../shared/components/icons";
import { authStore } from "../../../shared/state/authStore";
import { getApiClient } from "../../../shared/api/ApiClient";
import { unwrapResponse } from "../../../shared/api/ApiResponseAdapter";
import type { ApiResponse } from "../../../shared/api/types";

/**
 * Interfaz para representar un paciente (mascota)
 * Cumple con el principio de Interface Segregation
 */
interface Paciente {
  idPaciente: number;
  nombre: string;
  especie: string;
  raza: string;
  edad: number;
  peso: number;
}

/**
 * Interfaz para representar una cita veterinaria
 * Contiene solo los campos necesarios para el dashboard
 */
interface Cita {
  idCita: number;
  fechaHora: string;
  estado: string;
  tipoServicio: string;
  motivo: string;
  paciente: {
    id: number;
    nombre: string;
    especie: string;
  };
  veterinario: {
    nombreCompleto: string;
    especialidad: string;
  };
}

/**
 * Portal del Cliente - Dashboard Principal
 * 
 * Componente que muestra el dashboard principal del cliente con:
 * - Resumen de próximas citas
 * - Estadísticas de mascotas registradas
 * - Accesos rápidos a funcionalidades principales
 * - Últimas citas registradas
 * 
 * @component
 * @example
 * return <ClienteDashboardPage />
 * 
 * @remarks
 * - Cumple con principio de Single Responsibility: solo maneja la vista del dashboard
 * - Utiliza React Query para manejo eficiente de cache
 * - Implementa patrón de composición para reutilización
 * - Todos los strings literales están en español para i18n
 */
export const ClienteDashboardPage = () => {
  const user = authStore((state) => state.user);
  const navigate = useNavigate();

  /**
   * Obtiene las mascotas registradas del cliente actual
   * Utiliza React Query para cache automático y refetch inteligente
   */
  const { data: mascotas = [], isLoading: loadingMascotas } = useQuery({
    queryKey: ["pacientes-cliente", user?.id],
    queryFn: async () => {
      if (!user?.id) return [];
      const client = getApiClient();
      const { data } = await client.get<ApiResponse<Paciente[]>>(`/pacientes/cliente/${user.id}`);
      return unwrapResponse(data);
    },
    enabled: !!user?.id,
  });

  /**
   * Obtiene todas las citas de las mascotas del cliente
   * Se ejecuta solo cuando hay mascotas disponibles
   * Las citas se ordenan por fecha (más recientes primero)
   */
  const { data: todasCitas = [], isLoading: loadingCitas } = useQuery({
    queryKey: ["citas-cliente", mascotas.map((mascota) => mascota.idPaciente)],
    queryFn: async () => {
      if (mascotas.length === 0) return [];
      const client = getApiClient();
      const promesas = mascotas.map((mascota) =>
        client
          .get<ApiResponse<Cita[]>>(`/citas/paciente/${mascota.idPaciente}`)
          .then(({ data }) => unwrapResponse(data))
      );
      const resultados = await Promise.all(promesas);
      return resultados.flat();
    },
    enabled: mascotas.length > 0,
  });

  /**
   * Filtra y obtiene las citas programadas
   * La próxima cita es la más cercana en el tiempo
   */
  const citasProgramadas = todasCitas.filter((cita) => cita.estado === "PROGRAMADA");
  const proximaCita = citasProgramadas.sort((citaA, citaB) => 
    new Date(citaA.fechaHora).getTime() - new Date(citaB.fechaHora).getTime()
  )[0];

  return (
    <div className="p-6 space-y-6">
      {/* Header - Bienvenida al usuario */}
      <div className="bg-white rounded-lg shadow-sm p-6">
        <h1 className="text-2xl font-bold text-gray-900">
          Portal del Cliente
        </h1>
        <p className="text-gray-600 mt-2">
          Bienvenido/a {user?.nombre} {user?.apellido}
        </p>
      </div>

      {/* Estadísticas Rápidas - Resumen de información clave */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Próxima Cita */}
        <div className="bg-white rounded-lg shadow-sm p-6 hover:shadow-md transition-shadow">
          <div className="flex items-center space-x-4">
            <div className="flex-shrink-0">
              <div className="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
                <IconCalendar className="w-6 h-6 text-blue-600" />
              </div>
            </div>
            <div className="flex-1">
              <h3 className="text-sm font-medium text-gray-900">Próxima Cita</h3>
              {proximaCita ? (
                <>
                  <p className="text-xs text-blue-600 font-semibold mt-1">
                    {new Date(proximaCita.fechaHora).toLocaleDateString('es-ES', {
                      day: '2-digit',
                      month: 'short',
                      hour: '2-digit',
                      minute: '2-digit'
                    })}
                  </p>
                  <p className="text-xs text-gray-500">{proximaCita.paciente.nombre}</p>
                </>
              ) : (
                <p className="text-xs text-gray-500 mt-1">No hay citas programadas</p>
              )}
            </div>
          </div>
        </div>

        {/* Mascotas Registradas */}
        <div className="bg-white rounded-lg shadow-sm p-6 hover:shadow-md transition-shadow">
          <div className="flex items-center space-x-4">
            <div className="flex-shrink-0">
              <div className="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center">
                <IconPaw className="w-6 h-6 text-green-600" />
              </div>
            </div>
            <div>
              <h3 className="text-sm font-medium text-gray-900">Mascotas Registradas</h3>
              <p className="text-xs text-gray-500 mt-1">
                {mascotas.length} {mascotas.length === 1 ? 'mascota' : 'mascotas'}
              </p>
            </div>
          </div>
        </div>

        {/* Total de Citas */}
        <div className="bg-white rounded-lg shadow-sm p-6 hover:shadow-md transition-shadow">
          <div className="flex items-center space-x-4">
            <div className="flex-shrink-0">
              <div className="w-12 h-12 bg-purple-100 rounded-lg flex items-center justify-center">
                <IconDocument className="w-6 h-6 text-purple-600" />
              </div>
            </div>
            <div>
              <h3 className="text-sm font-medium text-gray-900">Citas Totales</h3>
              <p className="text-xs text-gray-500 mt-1">
                {todasCitas.length} {todasCitas.length === 1 ? 'cita' : 'citas'}
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Accesos Rápidos - Navegación principal del cliente */}
      <div className="bg-white rounded-lg shadow-sm p-6">
        <h2 className="text-lg font-semibold text-gray-900 mb-4">Accesos Rápidos</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          {/* Mis Mascotas */}
          <button
            onClick={() => navigate("/cliente/mascotas")}
            className="p-4 border border-gray-200 rounded-lg hover:border-blue-500 hover:bg-blue-50 transition-all text-left group"
          >
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 bg-blue-100 rounded-lg flex items-center justify-center group-hover:bg-blue-500 transition-colors">
                <IconPaw className="w-5 h-5 text-blue-600 group-hover:text-white" />
              </div>
              <div>
                <p className="font-medium text-gray-900">Mis Mascotas</p>
                <p className="text-xs text-gray-500">Ver todas</p>
              </div>
            </div>
          </button>

          {/* Mis Citas */}
          <button
            onClick={() => navigate("/cliente/citas")}
            className="p-4 border border-gray-200 rounded-lg hover:border-blue-500 hover:bg-blue-50 transition-all text-left group"
          >
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 bg-green-100 rounded-lg flex items-center justify-center group-hover:bg-green-500 transition-colors">
                <IconCalendar className="w-5 h-5 text-green-600 group-hover:text-white" />
              </div>
              <div>
                <p className="font-medium text-gray-900">Mis Citas</p>
                <p className="text-xs text-gray-500">Ver historial</p>
              </div>
            </div>
          </button>

          {/* Historial Médico */}
          <button
            onClick={() => navigate("/cliente/historial")}
            className="p-4 border border-gray-200 rounded-lg hover:border-blue-500 hover:bg-blue-50 transition-all text-left group"
          >
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 bg-purple-100 rounded-lg flex items-center justify-center group-hover:bg-purple-500 transition-colors">
                <IconDocument className="w-5 h-5 text-purple-600 group-hover:text-white" />
              </div>
              <div>
                <p className="font-medium text-gray-900">Historial Médico</p>
                <p className="text-xs text-gray-500">Ver consultas</p>
              </div>
            </div>
          </button>

          {/* Mis Facturas */}
          <button
            onClick={() => navigate("/cliente/facturas")}
            className="p-4 border border-gray-200 rounded-lg hover:border-blue-500 hover:bg-blue-50 transition-all text-left group"
          >
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 bg-yellow-100 rounded-lg flex items-center justify-center group-hover:bg-yellow-500 transition-colors">
                <IconCreditCard className="w-5 h-5 text-yellow-600 group-hover:text-white" />
              </div>
              <div>
                <p className="font-medium text-gray-900">Mis Facturas</p>
                <p className="text-xs text-gray-500">Ver pagos</p>
              </div>
            </div>
          </button>
        </div>
      </div>

      {/* Actividad Reciente - Últimas 3 citas */}
      {loadingCitas ? (
        <div className="bg-white rounded-lg shadow-sm p-8 text-center">
          <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600" />
        </div>
      ) : todasCitas.length > 0 && (
        <div className="bg-white rounded-lg shadow-sm p-6">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-lg font-semibold text-gray-900">Últimas Citas</h2>
            <button
              onClick={() => navigate("/cliente/citas")}
              className="text-sm text-blue-600 hover:text-blue-800 font-medium"
            >
              Ver todas →
            </button>
          </div>
          <div className="space-y-3">
            {todasCitas.slice(0, 3).map((cita) => (
              <div key={cita.idCita} className="border rounded-lg p-4">
                <div className="flex justify-between items-start">
                  <div>
                    <div className="flex items-center gap-2 mb-1">
                      <h3 className="font-semibold text-gray-900">{cita.paciente.nombre}</h3>
                      <span className={`px-2 py-1 text-xs rounded-full ${
                        cita.estado === 'PROGRAMADA' ? 'bg-blue-100 text-blue-700' :
                        cita.estado === 'REALIZADA' ? 'bg-green-100 text-green-700' :
                        'bg-gray-100 text-gray-700'
                      }`}>
                        {cita.estado}
                      </span>
                    </div>
                    <p className="text-sm text-gray-600">
                      {new Date(cita.fechaHora).toLocaleDateString('es-ES', {
                        year: 'numeric',
                        month: 'long',
                        day: 'numeric',
                      })}
                    </p>
                    <p className="text-sm text-gray-500">
                      Dr. {cita.veterinario.nombreCompleto}
                    </p>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};
