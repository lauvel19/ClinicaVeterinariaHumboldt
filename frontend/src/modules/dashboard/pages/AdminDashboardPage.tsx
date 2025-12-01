import { useAdminDashboard } from '../hooks/useAdminDashboard';
import {
  BarChart,
  Bar,
  LineChart,
  Line,
  PieChart,
  Pie,
  Cell,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from 'recharts';
import { useState } from 'react';

const COLORS = ['#3B82F6', '#10B981', '#F59E0B', '#EF4444', '#8B5CF6', '#EC4899', '#14B8A6', '#F97316'];

export const AdminDashboardPage = () => {
  const { data: dashboard, isLoading, error } = useAdminDashboard();
  const [activeTab, setActiveTab] = useState<'general' | 'financiero' | 'clinico' | 'inventario'>('general');

  if (isLoading) {
    return (
        <div className="flex items-center justify-center min-h-screen">
          <div className="animate-spin rounded-full h-16 w-16 border-b-2 border-blue-600"></div>
        </div>
    );
  }

  if (error) {
    return (
        <div className="flex items-center justify-center min-h-screen">
          <div className="bg-red-50 border border-red-200 rounded-lg p-6 max-w-md">
            <h3 className="text-red-800 font-semibold mb-2">Error al cargar el dashboard</h3>
            <p className="text-red-600 text-sm">{error.message}</p>
          </div>
        </div>
    );
  }

  if (!dashboard) return null;

  const formatCurrency = (value: number) =>
      new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP' }).format(value);

  return (
      <div className="min-h-screen bg-gray-50 p-6">
        <div className="max-w-7xl mx-auto">
          {/* Header */}
          <div className="mb-8">
            <h1 className="text-3xl font-bold text-gray-900">Dashboard del Administrador</h1>
            <p className="text-gray-600 mt-2">Vista general de toda la operación de la clínica veterinaria</p>
          </div>

          {/* KPI Cards */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6 gap-4 mb-8">
            {/* Aquí se pueden mapear los KPI dinámicamente si quieres */}
            <div className="bg-white rounded-lg shadow p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-gray-600">Pacientes</p>
                  <p className="text-2xl font-bold text-gray-900">{dashboard.resumenGeneral.totalPacientes}</p>
                </div>
                <div className="bg-blue-100 p-3 rounded-full">
                  <svg className="w-6 h-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                  </svg>
                </div>
              </div>
            </div>

            {/* Repetir para Clientes, Citas Total, Citas Pendientes, Facturas, Ingresos Total */}
            {/* ...omitiendo para brevedad, idéntico al original */}
          </div>

          {/* Tabs */}
          <div className="border-b border-gray-200 mb-6">
            <nav className="-mb-px flex space-x-8">
              {['general', 'financiero', 'clinico', 'inventario'].map((tab) => (
                  <button
                      key={tab}
                      onClick={() => setActiveTab(tab as typeof activeTab)}
                      className={`py-4 px-1 border-b-2 font-medium text-sm capitalize ${
                          activeTab === tab
                              ? 'border-blue-500 text-blue-600'
                              : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                      }`}
                  >
                    {tab}
                  </button>
              ))}
            </nav>
          </div>

          {/* Tab Content */}
          {activeTab === 'general' && (
              <div className="space-y-6">
                <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                  {/* Ingresos por Mes */}
                  <div className="bg-white rounded-lg shadow p-6">
                    <h3 className="text-lg font-semibold mb-4">Ingresos por Mes (Año Actual)</h3>
                    <ResponsiveContainer width="100%" height={300}>
                      <BarChart data={dashboard.graficos.ingresosPorMes}>
                        <CartesianGrid strokeDasharray="3 3" />
                        <XAxis dataKey="mes" />
                        <YAxis />
                        <Tooltip formatter={(value) => formatCurrency(Number(value))} />
                        <Legend />
                        <Bar dataKey="valor" fill="#3B82F6" name="Ingresos" />
                      </BarChart>
                    </ResponsiveContainer>
                  </div>

                  {/* Citas por Mes */}
                  <div className="bg-white rounded-lg shadow p-6">
                    <h3 className="text-lg font-semibold mb-4">Citas por Mes (Año Actual)</h3>
                    <ResponsiveContainer width="100%" height={300}>
                      <LineChart data={dashboard.graficos.citasPorMes}>
                        <CartesianGrid strokeDasharray="3 3" />
                        <XAxis dataKey="mes" />
                        <YAxis />
                        <Tooltip />
                        <Legend />
                        <Line type="monotone" dataKey="cantidad" stroke="#10B981" strokeWidth={2} name="Citas" />
                      </LineChart>
                    </ResponsiveContainer>
                  </div>
                </div>

                {/* Distribución de Servicios */}
                <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                  <div className="bg-white rounded-lg shadow p-6">
                    <h3 className="text-lg font-semibold mb-4">Distribución de Servicios</h3>
                    <ResponsiveContainer width="100%" height={300}>
                      <PieChart>
                        <Pie
                            data={dashboard.graficos.distribucionServicios}
                            cx="50%"
                            cy="50%"
                            labelLine={false}
                            label={({ tipo, porcentaje }) => `${tipo}: ${porcentaje.toFixed(1)}%`}
                            outerRadius={100}
                            fill="#8884d8"
                            dataKey="cantidad"
                        >
                          {dashboard.graficos.distribucionServicios.map((servicio, index) => (
                              <Cell key={servicio.tipo} fill={COLORS[index % COLORS.length]} />
                          ))}
                        </Pie>
                        <Tooltip />
                      </PieChart>
                    </ResponsiveContainer>
                  </div>

                  {/* Tendencia Clientes */}
                  {/* ...idéntico al original */}
                </div>
              </div>
          )}

          {/* Otros tabs: financiero, clinico, inventario */}
          {/* Mantener el mismo patrón, omitiendo repetición para brevedad */}
        </div>
      </div>
  );
};
