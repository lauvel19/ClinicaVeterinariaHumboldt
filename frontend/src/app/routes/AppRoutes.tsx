// Define las rutas principales de la aplicación.
// Se utiliza carga diferida (lazy) para cada página para mejorar el performance.
import { lazy, Suspense, ReactNode, useEffect, useState } from "react";
import { Navigate, Route, Routes } from "react-router-dom";

import { FullscreenLoader } from "../components/feedback/FullscreenLoader";
import { authStore } from "../../shared/state/authStore";
import { RoleGuard } from "../guards/RoleGuard";
import { SplashScreen } from "../components/feedback/SplashScreen";

interface AppRoutesProps {
  readonly renderDashboard: () => ReactNode;
}

// Páginas principales del veterinario.
const VeterinarianDashboardPage = lazy(() =>
  import("../../modules/dashboard/pages/VeterinarianDashboardPage").then((module) => ({
    default: module.VeterinarianDashboardPage,
  })),
);

const VeterinarianAgendaPage = lazy(() =>
  import("../../modules/agenda/pages/VeterinarianAgendaPage").then((module) => ({
    default: module.VeterinarianAgendaPage,
  })),
);

const VeterinarianPatientsPage = lazy(() =>
  import("../../modules/pacientes/pages/VeterinarianPatientsPage").then((module) => ({
    default: module.VeterinarianPatientsPage,
  })),
);

const VeterinarianHistoriesPage = lazy(() =>
  import("../../modules/historias/pages/VeterinarianHistoriesPage").then((module) => ({
    default: module.VeterinarianHistoriesPage,
  })),
);

const VeterinarianConsultationsPage = lazy(() =>
  import("../../modules/consultas/pages/VeterinarianConsultationsPage").then((module) => ({
    default: module.VeterinarianConsultationsPage,
  })),
);

const VeterinarianFollowUpsPage = lazy(() =>
  import("../../modules/seguimientos/pages/VeterinarianFollowUpsPage").then((module) => ({
    default: module.VeterinarianFollowUpsPage,
  })),
);

// Páginas del cliente
const ClienteDashboardPage = lazy(() =>
  import("../../modules/cliente/pages/ClienteDashboardPage").then((module) => ({
    default: module.ClienteDashboardPage,
  })),
);

// Páginas del secretario
const SecretaryDashboardPage = lazy(() =>
  import("../../modules/secretario/pages/SecretaryDashboardPage").then((module) => ({
    default: module.SecretaryDashboardPage,
  })),
);

const SecretaryCitasPage = lazy(() =>
  import("../../modules/secretario/pages/SecretaryCitasPage").then((module) => ({
    default: module.SecretaryCitasPage,
  })),
);

const SecretaryPatientsPage = lazy(() =>
  import("../../modules/secretario/pages/SecretaryPatientsPage").then((module) => ({
    default: module.SecretaryPatientsPage,
  })),
);

const SecretaryInventarioPage = lazy(() =>
  import("../../modules/secretario/pages/SecretaryInventarioPage").then((module) => ({
    default: module.SecretaryInventarioPage,
  })),
);

// Página de inventario para administrador (reutiliza la del secretario)
const AdminInventarioPage = lazy(() =>
  import("../../modules/secretario/pages/SecretaryInventarioPage").then((module) => ({
    default: module.SecretaryInventarioPage,
  })),
);

const FacturasPage = lazy(() =>
  import("../../modules/facturas/pages/FacturasPage").then((module) => ({
    default: module.FacturasPage,
  })),
);

const PagoResultadoPage = lazy(() =>
  import("../../modules/facturas/pages/PagoResultadoPage").then((module) => ({
    default: module.PagoResultadoPage,
  })),
);

const ReportesPage = lazy(() =>
  import("../../modules/reportes/pages/ReportesPage").then((module) => ({
    default: module.ReportesPage,
  })),
);

const NotificacionesPage = lazy(() =>
  import("../../modules/notificaciones/pages/NotificacionesPage").then((module) => ({
    default: module.NotificacionesPage,
  })),
);

const ConfiguracionPage = lazy(() =>
  import("../../modules/configuracion/pages/ConfiguracionPage").then((module) => ({
    default: module.ConfiguracionPage,
  })),
);

const UsuariosPage = lazy(() =>
  import("../../modules/usuarios/pages/UsuariosPage").then((module) => ({
    default: module.UsuariosPage,
  })),
);

const ClientesPage = lazy(() =>
  import("../../modules/clientes/pages/ClientesPage").then((module) => ({
    default: module.ClientesPage,
  })),
);

const ProveedoresPage = lazy(() =>
  import("../../modules/proveedores/pages/ProveedoresPage").then((module) => ({
    default: module.ProveedoresPage,
  })),
);

const AdminDashboardPage = lazy(() =>
  import("../../modules/dashboard/pages/AdminDashboardPage").then((module) => ({
    default: module.AdminDashboardPage,
  })),
);

const LoginPage = lazy(() =>
  import("../../modules/auth/pages/LoginPage").then((module) => ({ default: module.LoginPage })),
);

const NotFoundPage = lazy(() =>
  import("../../modules/core/pages/NotFoundPage").then((module) => ({ default: module.NotFoundPage })),
);

// Componente que redirige según el estado de autenticación y rol
const RootRedirect = () => {
  const token = authStore((state) => state.token);
  const user = authStore((state) => state.user);
  
  if (!token) {
    return <Navigate to="/auth/login" replace />;
  }
  
  // Redirigir según el rol
  const userRole = user?.rol?.toUpperCase() || "";
  if (userRole === "SECRETARIO") {
    return <Navigate to="/secretario/inicio" replace />;
  }
  if (userRole === "VETERINARIO") {
    return <Navigate to="/veterinario/inicio" replace />;
  }
  if (userRole === "ADMIN") {
    return <Navigate to="/admin/dashboard" replace />;
  }
  if (userRole === "CLIENTE") {
    return <Navigate to="/cliente/inicio" replace />;
  }
  
  // Por defecto, redirigir al login si el rol no está reconocido
  return <Navigate to="/auth/login" replace />;
};

// Componente que protege la ruta de login (redirige si ya está autenticado)
const LoginGuard = () => {
  const token = authStore((state) => state.token);
  const user = authStore((state) => state.user);
  
  if (token) {
    // Redirigir según el rol
    const userRole = user?.rol?.toUpperCase() || "";
    if (userRole === "SECRETARIO") {
      return <Navigate to="/secretario/inicio" replace />;
    }
    if (userRole === "CLIENTE") {
      return <Navigate to="/cliente/inicio" replace />;
    }
    if (userRole === "VETERINARIO") {
      return <Navigate to="/veterinario/inicio" replace />;
    }
    if (userRole === "ADMIN") {
      return <Navigate to="/admin/dashboard" replace />;
    }
  }
  
  return (
    <Suspense fallback={<FullscreenLoader />}>
      <LoginPage />
    </Suspense>
  );
};

export const AppRoutes = ({ renderDashboard }: AppRoutesProps) => {
  const [showSplash, setShowSplash] = useState(true);

  useEffect(() => {
    const timeout = setTimeout(() => setShowSplash(false), 800); // splash inicial
    return () => clearTimeout(timeout);
  }, []);

  if (showSplash) {
    return <SplashScreen />;
  }

  return (
    <Routes>
      {/* Ruta raíz: redirige según autenticación */}
      <Route path="/" element={<RootRedirect />} />

      {/* Ruta de login: si ya está autenticado, redirige al dashboard */}
      <Route path="/auth/login" element={<LoginGuard />} />

      {/* Ruta pública para resultado de pago de ePayco */}
      <Route
        path="/facturas/pago-resultado"
        element={
          <Suspense fallback={<FullscreenLoader />}>
            <PagoResultadoPage />
          </Suspense>
        }
      />

      {/* Rutas protegidas del dashboard */}
      <Route element={renderDashboard()}>
        <Route index element={<RootRedirect />} />
        
        {/* Rutas del veterinario */}
        <Route
          path="/veterinario/inicio"
          element={
            <RoleGuard allowedRoles={["VETERINARIO"]}>
              <VeterinarianDashboardPage />
            </RoleGuard>
          }
        />
        <Route
          path="/veterinario/agenda"
          element={
            <RoleGuard allowedRoles={["VETERINARIO"]}>
              <VeterinarianAgendaPage />
            </RoleGuard>
          }
        />
        <Route
          path="/veterinario/pacientes"
          element={
            <RoleGuard allowedRoles={["VETERINARIO"]}>
              <VeterinarianPatientsPage />
            </RoleGuard>
          }
        />
        <Route
          path="/veterinario/historias"
          element={
            <RoleGuard allowedRoles={["VETERINARIO"]}>
              <VeterinarianHistoriesPage />
            </RoleGuard>
          }
        />
        <Route
          path="/veterinario/consultas"
          element={
            <RoleGuard allowedRoles={["VETERINARIO"]}>
              <VeterinarianConsultationsPage />
            </RoleGuard>
          }
        />
        <Route
          path="/veterinario/seguimientos"
          element={
            <RoleGuard allowedRoles={["VETERINARIO"]}>
              <VeterinarianFollowUpsPage />
            </RoleGuard>
          }
        />
        
        {/* Rutas del cliente */}
        <Route
          path="/cliente/inicio"
          element={
            <RoleGuard allowedRoles={["CLIENTE"]}>
              <ClienteDashboardPage />
            </RoleGuard>
          }
        />
        
        {/* Rutas del secretario */}
        <Route
          path="/secretario/inicio"
          element={
            <RoleGuard allowedRoles={["SECRETARIO"]}>
              <SecretaryDashboardPage />
            </RoleGuard>
          }
        />
        <Route
          path="/secretario/citas"
          element={
            <RoleGuard allowedRoles={["SECRETARIO"]}>
              <SecretaryCitasPage />
            </RoleGuard>
          }
        />
        <Route
          path="/secretario/pacientes"
          element={
            <RoleGuard allowedRoles={["SECRETARIO"]}>
              <SecretaryPatientsPage />
            </RoleGuard>
          }
        />
        <Route
          path="/secretario/inventario"
          element={
            <RoleGuard allowedRoles={["SECRETARIO"]}>
              <SecretaryInventarioPage />
            </RoleGuard>
          }
        />
        <Route
          path="/secretario/facturas"
          element={
            <RoleGuard allowedRoles={["SECRETARIO"]}>
              <FacturasPage />
            </RoleGuard>
          }
        />
        
        {/* Rutas compartidas (Veterinario, Secretario y Administrador) */}
        <Route
          path="/reportes"
          element={
            <RoleGuard allowedRoles={["VETERINARIO", "SECRETARIO", "ADMIN"]}>
              <ReportesPage />
            </RoleGuard>
          }
        />
        <Route
          path="/notificaciones"
          element={
            <RoleGuard allowedRoles={["VETERINARIO", "SECRETARIO"]}>
              <NotificacionesPage />
            </RoleGuard>
          }
        />
        
              {/* Rutas solo para Administradores */}
              <Route
                path="/admin/dashboard"
                element={
                  <RoleGuard allowedRoles={["ADMIN"]}>
                    <AdminDashboardPage />
                  </RoleGuard>
                }
              />
              <Route
                path="/configuracion"
                element={
                  <RoleGuard allowedRoles={["ADMIN"]}>
                    <ConfiguracionPage />
                  </RoleGuard>
                }
              />
              <Route
                path="/usuarios"
                element={
                  <RoleGuard allowedRoles={["ADMIN"]}>
                    <UsuariosPage />
                  </RoleGuard>
                }
              />
              <Route
                path="/admin/inventario"
                element={
                  <RoleGuard allowedRoles={["ADMIN"]}>
                    <AdminInventarioPage />
                  </RoleGuard>
                }
              />
              {/* Ruta de Finanzas para administrador - Gestión de facturas e ingresos */}
              <Route
                path="/admin/finanzas"
                element={
                  <RoleGuard allowedRoles={["ADMIN"]}>
                    <FacturasPage />
                  </RoleGuard>
                }
              />
              <Route
                path="/clientes"
                element={
                  <RoleGuard allowedRoles={["SECRETARIO", "ADMIN"]}>
                    <ClientesPage />
                  </RoleGuard>
                }
              />
              <Route
                path="/proveedores"
                element={
                  <RoleGuard allowedRoles={["SECRETARIO", "ADMIN"]}>
                    <ProveedoresPage />
                  </RoleGuard>
                }
              />
      </Route>

      <Route
        path="*"
        element={
          <Suspense fallback={<FullscreenLoader />}>
            <NotFoundPage />
          </Suspense>
        }
      />
    </Routes>
  );
};


