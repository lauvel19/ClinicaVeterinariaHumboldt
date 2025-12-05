// Sidebar principal del layout.
// Renderiza las secciones según el rol del usuario.
import { NavLink } from "react-router-dom";
import classNames from "classnames";

import { authStore } from "../../../shared/state/authStore";
// import { LogoCircular } from "../../../shared/components/LogoCircular";

interface NavigationItem {
  readonly label: string;
  readonly to: string;
  readonly icon?: string;
  readonly roles?: string[]; // Roles que pueden ver este item
}

const navigationItems: NavigationItem[] = [
  // Navegación para ADMIN - Orden: Dashboard, Usuarios, Clientes, Inventario, Finanzas, Reportes, Proveedores, Configuración
  { label: "Dashboard", to: "/admin/dashboard", icon: "dashboard", roles: ["ADMIN"] },
  { label: "Usuarios", to: "/usuarios", icon: "user", roles: ["ADMIN"] },
  { label: "Clientes", to: "/clientes", icon: "people", roles: ["ADMIN"] },
  { label: "Inventario", to: "/admin/inventario", icon: "box", roles: ["ADMIN"] },
  { label: "Finanzas", to: "/admin/finanzas", icon: "receipt", roles: ["ADMIN"] },
  { label: "Reportes", to: "/reportes", icon: "chart", roles: ["ADMIN"] },
  { label: "Proveedores", to: "/proveedores", icon: "truck", roles: ["ADMIN"] },
  { label: "Configuración", to: "/configuracion", icon: "settings", roles: ["ADMIN"] },

  // Navegación para VETERINARIO
  { label: "Inicio", to: "/veterinario/inicio", icon: "home", roles: ["VETERINARIO"] },
  { label: "Mi agenda", to: "/veterinario/agenda", icon: "calendar", roles: ["VETERINARIO"] },
  { label: "Pacientes", to: "/veterinario/pacientes", icon: "users", roles: ["VETERINARIO"] },
  { label: "Historias clínicas", to: "/veterinario/historias", icon: "folder", roles: ["VETERINARIO"] },
  { label: "Consultas", to: "/veterinario/consultas", icon: "stethoscope", roles: ["VETERINARIO"] },
  { label: "Seguimientos", to: "/veterinario/seguimientos", icon: "pulse", roles: ["VETERINARIO"] },
  { label: "Reportes", to: "/reportes", icon: "chart", roles: ["VETERINARIO"] },
  { label: "Notificaciones", to: "/notificaciones", icon: "bell", roles: ["VETERINARIO"] },

  // Navegación para SECRETARIO - Orden: Inicio, Clientes, Pacientes, Citas, Inventario, Proveedores, Reportes, Facturas, Notificaciones
  { label: "Inicio", to: "/secretario/inicio", icon: "home", roles: ["SECRETARIO"] },
  { label: "Clientes", to: "/clientes", icon: "people", roles: ["SECRETARIO"] },
  { label: "Pacientes", to: "/secretario/pacientes", icon: "users", roles: ["SECRETARIO"] },
  { label: "Citas", to: "/secretario/citas", icon: "calendar", roles: ["SECRETARIO"] },
  { label: "Inventario", to: "/secretario/inventario", icon: "box", roles: ["SECRETARIO"] },
  { label: "Proveedores", to: "/proveedores", icon: "truck", roles: ["SECRETARIO"] },
  { label: "Reportes", to: "/reportes", icon: "chart", roles: ["SECRETARIO"] },
  { label: "Facturas", to: "/secretario/facturas", icon: "receipt", roles: ["SECRETARIO"] },
  { label: "Notificaciones", to: "/notificaciones", icon: "bell", roles: ["SECRETARIO"] },

  // Navegación para CLIENTE
  { label: "Inicio", to: "/cliente/inicio", icon: "home", roles: ["CLIENTE"] },
  { label: "Mis Facturas", to: "/cliente/facturas", icon: "receipt", roles: ["CLIENTE"] },
  // Nota: Mascotas y Citas se ven en el dashboard principal (Inicio) con pestañas
];

const getIcon = (icon?: string) => {
  switch (icon) {
    case "home":
      return (
        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
        </svg>
      );
    case "calendar":
      return (
        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
        </svg>
      );
    case "users":
      return (
        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
        </svg>
      );
    case "folder":
      return (
        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-6l-2-2H5a2 2 0 00-2 2z" />
        </svg>
      );
    case "stethoscope":
      return (
        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4.5 17.5C4.5 19.985 6.515 22 9 22s4.5-2.015 4.5-4.5V15m0 0V9a4 4 0 018 0v.5m-8 5.5h8m0 0a2.5 2.5 0 105 0" />
        </svg>
      );
    case "pulse":
      return (
        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
        </svg>
      );
    case "box":
      return (
        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
        </svg>
      );
    case "receipt":
      return (
        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01" />
        </svg>
      );
    case "chart":
      return (
        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
        </svg>
      );
    case "bell":
      return (
        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
        </svg>
      );
    case "dashboard":
      return (
        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 5a1 1 0 011-1h4a1 1 0 011 1v7a1 1 0 01-1 1H5a1 1 0 01-1-1V5zM14 5a1 1 0 011-1h4a1 1 0 011 1v3a1 1 0 01-1 1h-4a1 1 0 01-1-1V5zM4 16a1 1 0 011-1h4a1 1 0 011 1v3a1 1 0 01-1 1H5a1 1 0 01-1-1v-3zM14 13a1 1 0 011-1h4a1 1 0 011 1v6a1 1 0 01-1 1h-4a1 1 0 01-1-1v-6z" />
        </svg>
      );
    case "settings":
      return (
        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
        </svg>
      );
    case "user":
      return (
        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
        </svg>
      );
    case "people":
      return (
        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
        </svg>
      );
    case "truck":
      return (
        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16V6a1 1 0 00-1-1H4a1 1 0 00-1 1v10a1 1 0 001 1h1m8-1a1 1 0 01-1 1H9m4-1V8a1 1 0 011-1h2.586a1 1 0 01.707.293l3.414 3.414a1 1 0 01.293.707V16a1 1 0 01-1 1h-1m-6-1a1 1 0 001 1h1M5 17a2 2 0 104 0m-4 0a2 2 0 114 0m6 0a2 2 0 104 0m-4 0a2 2 0 114 0" />
        </svg>
      );
    default:
      return (
        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
        </svg>
      );
  }
};

export const Sidebar = () => {
  const user = authStore((state) => state.user);
  const userRole = user?.rol?.toUpperCase() || "";

  // Filtrar navegación según el rol del usuario
  const navigation = navigationItems.filter((item) => {
    if (!item.roles || item.roles.length === 0) return true;
    return item.roles.includes(userRole);
  });

  return (
    <aside className="relative flex h-full w-72 flex-col overflow-hidden bg-gradient-to-br from-secondary via-secondary/95 to-secondary/90 text-white shadow-2xl lg:w-80">
      {/* Patrón de fondo sutil */}
      <div className="absolute inset-0 bg-[radial-gradient(circle_at_50%_50%,rgba(255,255,255,0.05)_0%,transparent_50%)]"></div>
      
      {/* Header del sidebar */}
      <div className="relative z-10 flex flex-col items-center gap-5 border-b border-white/10 p-8 pb-6">
        <div className="relative">
          <div className="absolute inset-0 rounded-full blur-2xl" style={{ background: "rgba(255,255,255,0.08)" }}></div>
          <div className="relative rounded-full p-2" style={{ border: "2px solid rgba(255,255,255,0.35)" }}>
            <div className="relative rounded-full p-2" style={{ border: "2px solid rgba(255,255,255,0.20)" }}>
              <img
                src="/LogoClinicaVeterinaria.png"
                alt="Logo Clínica Veterinaria Universitaria Humboldt"
                className="h-[110px] w-[110px] rounded-full object-cover bg-white/5"
              />
            </div>
          </div>
        </div>
        <div className="text-center">
          <p className="text-xs font-bold uppercase tracking-widest text-white/80">Clínica Veterinaria</p>
          <p className="mt-0.5 text-sm font-bold uppercase tracking-wider text-white/90">Universitaria</p>
          <p className="mt-1 text-xl font-black text-white drop-shadow-sm">Humboldt</p>
        </div>
        <p className="text-center text-xs leading-relaxed text-white/60">
          Sistema integral para gestionar pacientes, historias clínicas, inventario y más.
        </p>
      </div>

      {/* Navegación */}
      <nav className="relative z-10 mt-2 flex flex-1 flex-col gap-1.5 overflow-y-auto px-4 py-4 scrollbar-hide">
        {navigation.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            className={({ isActive }) =>
              classNames(
                "group relative flex items-center gap-3 rounded-xl px-4 py-3.5 text-sm font-semibold transition-all duration-200",
                isActive
                  ? "bg-white text-secondary shadow-lg shadow-primary/20"
                  : "text-white/80 hover:bg-white/10 hover:text-white hover:shadow-md",
              )
            }
          >
            {({ isActive }) => (
              <>
                <span className={`transition-transform ${isActive ? "scale-110" : "group-hover:scale-110"}`}>
                  {getIcon(item.icon)}
                </span>
                <span className="flex-1">{item.label}</span>
                {isActive && (
                  <div className="absolute right-2 h-2 w-2 rounded-full bg-primary"></div>
                )}
              </>
            )}
          </NavLink>
        ))}
      </nav>

      {/* Footer del sidebar */}
      <div className="relative z-10 border-t border-white/10 bg-secondary/50 p-5 text-center text-xs font-medium text-white/50 backdrop-blur-sm">
        © {new Date().getFullYear()} CVUH. Todos los derechos reservados.
      </div>
    </aside>
  );
};


