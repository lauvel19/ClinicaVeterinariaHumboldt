// Página de inicio de sesión basada en el prototipo de Figma.
// Implementa tabs para seleccionar el rol y un formulario validado con React Hook Form.
import { useState } from "react";
import { useForm } from "react-hook-form";
import { useLocation, useNavigate } from "react-router-dom";
import type { Location } from "react-router-dom";
import toast from "react-hot-toast";

import { AuthService } from "../services/AuthService";
import { AuthRepository, type RegisterRequest } from "../services/AuthRepository";
import type { LoginRequestDTO } from "../types";
// import { LogoCircular } from "../../../shared/components/LogoCircular";
import { ForgotPasswordModal } from "../components/ForgotPasswordModal";
import { ResetPasswordModal } from "../components/ResetPasswordModal";

interface LoginFormValues {
  readonly username: string;
  readonly password: string;
}

export const LoginPage = () => {
  const [isRegisterMode, setIsRegisterMode] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
    reset,
  } = useForm<LoginFormValues>({
    defaultValues: {
      username: "",
      password: "",
    },
  });

  const [registerData, setRegisterData] = useState<RegisterRequest>({
    username: "",
    password: "",
    email: "",
    nombre: "",
    apellido: "",
  });
  const [isRegistering, setIsRegistering] = useState(false);
  const [isForgotPasswordModalOpen, setIsForgotPasswordModalOpen] = useState(false);
  const [isResetPasswordModalOpen, setIsResetPasswordModalOpen] = useState(false);
  const [resetToken, setResetToken] = useState<string | undefined>(undefined);

  const onSubmit = handleSubmit(async (values) => {
    try {
      // El backend espera `username`; si en el futuro se habilita login por correo, se debe ajustar aquí.
      // Asegurar que los valores no estén vacíos
      if (!values.username?.trim() || !values.password?.trim()) {
        toast.error("Por favor, completa todos los campos");
        return;
      }

      await AuthService.login({
        username: values.username.trim(),
        password: values.password.trim(),
      } satisfies LoginRequestDTO);
      toast.success("Sesión iniciada correctamente");

      // La redirección se maneja automáticamente por RootRedirect según el rol del usuario
      // Si hay una ruta previa, redirigir a ella, sino el RootRedirect se encargará
      const redirectTo = (location.state as { from?: Location })?.from?.pathname;
      if (redirectTo) {
        navigate(redirectTo, { replace: true });
      } else {
        // El RootRedirect manejará la redirección según el rol
        navigate("/", { replace: true });
      }
    } catch (error: any) {
      console.error("Fallo de autenticación:", error);
      // Mostrar mensaje de error más específico si está disponible
      const errorMessage = error.response?.data?.message || error.message || "Credenciales inválidas o error al iniciar sesión";
      toast.error(errorMessage);
    }
  });

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!registerData.username.trim() || !registerData.password.trim() || !registerData.email.trim() || !registerData.nombre.trim() || !registerData.apellido.trim()) {
      toast.error("Por favor, completa todos los campos");
      return;
    }
    if (registerData.password.length < 8) {
      toast.error("La contraseña debe tener al menos 8 caracteres");
      return;
    }
    setIsRegistering(true);
    try {
      await AuthRepository.register(registerData);
      toast.success("Usuario registrado exitosamente. Ahora puedes iniciar sesión.");
      setIsRegisterMode(false);
      setRegisterData({
        username: "",
        password: "",
        email: "",
        nombre: "",
        apellido: "",
      });
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || error.message || "Error al registrar el usuario";
      toast.error(errorMessage);
    } finally {
      setIsRegistering(false);
    }
  };

  return (
    <div className="min-h-screen flex" style={{ backgroundColor: '#F0F4F8' }}>
      {/* Panel izquierdo - Diseño moderno y minimalista */}
      <section className="hidden lg:flex w-1/2 flex-col items-center justify-center gap-10 p-16 text-white relative overflow-hidden" 
        style={{ 
          background: 'linear-gradient(135deg, #0F6A7B 0%, #114264 50%, #0A4A5A 100%)',
          boxShadow: 'inset -1px 0 0 rgba(255,255,255,0.1)'
        }}>
        {/* Efectos de fondo decorativos */}
        <div className="absolute inset-0">
          <div className="absolute top-0 right-0 w-96 h-96 bg-white/5 rounded-full blur-3xl"></div>
          <div className="absolute bottom-0 left-0 w-80 h-80 bg-white/5 rounded-full blur-3xl"></div>
          <div className="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 w-64 h-64 bg-white/3 rounded-full blur-2xl"></div>
        </div>
        
        <div className="relative z-10 w-full max-w-3xl mx-auto">
          <div className="flex flex-col items-center gap-10 mb-10">
            {/* Logo con borde delgado y check verde */}
            <div className="relative">
              <div className="absolute inset-0 rounded-full blur-2xl" style={{ background: "rgba(255,255,255,0.08)" }}></div>
              <div className="relative rounded-full p-2" style={{ border: "2px solid rgba(255,255,255,0.35)" }}>
                <div className="relative rounded-full p-2" style={{ border: "2px solid rgba(255,255,255,0.2)" }}>
                  <img
                    src="/LogoClinicaVeterinaria.png"
                    alt="Logo Clínica Veterinaria Universitaria Humboldt"
                    className="h-[140px] w-[140px] rounded-full object-cover bg-white/5"
                  />
                </div>
              </div>
            </div>
            
            <div className="text-center space-y-2 mx-auto">
              <p className="text-[10px] font-bold uppercase tracking-[0.2em] text-white/80 mb-2">
                CLÍNICA VETERINARIA
              </p>
              <h1 className="text-6xl font-black leading-none text-white" style={{ letterSpacing: '-0.03em' }}>
                Universitaria
              </h1>
              <h1 className="text-6xl font-black leading-none text-white" style={{ letterSpacing: '-0.03em' }}>
                Humboldt
              </h1>
            </div>
          </div>
          
          <div className="max-w-2xl mx-auto text-center">
            <p className="text-sm leading-relaxed text-white/90 font-light">
              Sistema completo de administración que te permite gestionar pacientes, historias clínicas,
              inventario y mucho más desde una plataforma centralizada.
            </p>
          </div>

        </div>

        {/* Se removieron las tarjetas de características para un diseño más limpio */}
      </section>

      {/* Panel derecho - Formulario moderno */}
      <section className="flex w-full lg:w-1/2 items-center justify-center bg-white px-8 py-12 lg:px-20 relative">
        {/* Botón volver a la landing page - Posición absoluta arriba a la izquierda */}
        <button
          onClick={() => navigate('/')}
          className="absolute top-6 left-6 group flex items-center gap-2 px-4 py-2 text-sm font-semibold text-gray-700 bg-gray-100 rounded-lg transition-all duration-200 hover:bg-primary hover:text-white shadow-sm hover:shadow-md"
        >
          <svg 
            className="w-5 h-5 transition-transform duration-200 group-hover:-translate-x-1" 
            fill="none" 
            stroke="currentColor" 
            viewBox="0 0 24 24"
          >
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 19l-7-7m0 0l7-7m-7 7h18" />
          </svg>
          <span>Inicio</span>
        </button>

        <div className="w-full max-w-md space-y-10">
          {/* Logo visible en móvil */}
          <div className="flex justify-center lg:hidden mb-6">
            <div className="relative">
              <div className="rounded-full p-2" style={{ border: "2px solid rgba(26, 188, 188, 0.35)" }}>
                <img
                  src="/LogoClinicaVeterinaria.png"
                  alt="Logo Clínica Veterinaria Universitaria Humboldt"
                  className="h-[90px] w-[90px] rounded-full object-cover bg-white/5"
                />
              </div>
            </div>
          </div>
          
          <div className="space-y-2 text-center">
            <h2 className="text-5xl font-black text-gray-900 tracking-tight">
              {isRegisterMode ? "Registrarse" : "Iniciar Sesión"}
            </h2>
            <p className="text-sm text-gray-500 font-normal">
              {isRegisterMode ? "Crea una cuenta para acceder al sistema" : "Acceso a agenda, pacientes y seguimientos clínicos."}
            </p>
          </div>

          {isRegisterMode ? (
            <form className="space-y-6" noValidate onSubmit={handleRegister}>
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-1">
                  <label className="text-sm font-medium text-gray-700">Nombre *</label>
                  <input
                    type="text"
                    value={registerData.nombre}
                    onChange={(e) => setRegisterData({ ...registerData, nombre: e.target.value })}
                    required
                    className="w-full rounded-2xl border border-gray-200 bg-gray-50 px-4 py-3 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/30"
                  />
                </div>
                <div className="space-y-1">
                  <label className="text-sm font-medium text-gray-700">Apellido *</label>
                  <input
                    type="text"
                    value={registerData.apellido}
                    onChange={(e) => setRegisterData({ ...registerData, apellido: e.target.value })}
                    required
                    className="w-full rounded-2xl border border-gray-200 bg-gray-50 px-4 py-3 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/30"
                  />
                </div>
              </div>
              <div className="space-y-1">
                <label className="text-sm font-medium text-gray-700">Correo Electrónico *</label>
                <input
                  type="email"
                  value={registerData.email}
                  onChange={(e) => setRegisterData({ ...registerData, email: e.target.value })}
                  required
                  className="w-full rounded-2xl border border-gray-200 bg-gray-50 px-4 py-3 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/30"
                />
              </div>
              <div className="space-y-1">
                <label className="text-sm font-medium text-gray-700">Nombre de Usuario *</label>
                <input
                  type="text"
                  value={registerData.username}
                  onChange={(e) => setRegisterData({ ...registerData, username: e.target.value })}
                  required
                  className="w-full rounded-2xl border border-gray-200 bg-gray-50 px-4 py-3 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/30"
                />
              </div>
              <div className="space-y-1">
                <label className="text-sm font-medium text-gray-700">Contraseña *</label>
                <input
                  type="password"
                  value={registerData.password}
                  onChange={(e) => setRegisterData({ ...registerData, password: e.target.value })}
                  required
                  minLength={8}
                  className="w-full rounded-2xl border border-gray-200 bg-gray-50 px-4 py-3 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/30"
                />
                <p className="text-xs text-gray-400">Mínimo 8 caracteres</p>
              </div>
              <button
                type="submit"
                className="w-full rounded-2xl bg-primary px-4 py-3 text-sm font-semibold text-white shadow-soft transition-base hover:bg-primary-dark disabled:cursor-not-allowed disabled:bg-primary/60"
                disabled={isRegistering}
              >
                {isRegistering ? "Registrando..." : "Registrarse"}
              </button>
              <button
                type="button"
                onClick={() => {
                  setIsRegisterMode(false);
                  reset();
                }}
                className="w-full text-sm font-medium text-primary transition-base hover:text-primary-dark"
              >
                ¿Ya tienes cuenta? Inicia sesión
              </button>
            </form>
          ) : (
            <form className="space-y-5" noValidate onSubmit={onSubmit}>
            <div className="space-y-1.5">
              <label className="text-xs font-bold text-gray-700 uppercase tracking-wide" htmlFor="username">
                Correo Electrónico o Usuario
              </label>
              <input
                id="username"
                type="text"
                placeholder="ejemplo@cue.edu.co"
                className="w-full rounded-lg border-2 border-gray-300 bg-white px-4 py-3.5 text-sm font-medium transition-all duration-200 focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/30 focus:shadow-sm"
                {...register("username", {
                  required: "El usuario es obligatorio",
                  minLength: { value: 4, message: "Debe tener al menos 4 caracteres" },
                })}
              />
              {errors.username && <p className="text-xs font-semibold text-red-600 mt-1">{errors.username.message}</p>}
            </div>

            <div className="space-y-1.5">
              <label className="text-xs font-bold text-gray-700 uppercase tracking-wide" htmlFor="password">
                Contraseña
              </label>
              <input
                id="password"
                type="password"
                placeholder="********"
                className="w-full rounded-lg border-2 border-gray-300 bg-white px-4 py-3.5 text-sm font-medium transition-all duration-200 focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/30 focus:shadow-sm"
                {...register("password", {
                  required: "La contraseña es obligatoria",
                  minLength: { value: 8, message: "Debe tener mínimo 8 caracteres" },
                })}
              />
              <p className="text-xs text-gray-400 font-normal">Mínimo 8 caracteres</p>
              {errors.password && <p className="text-xs font-semibold text-red-600 mt-1">{errors.password.message}</p>}
            </div>

              <button
                type="submit"
                className="w-full rounded-lg px-4 py-3.5 text-sm font-bold text-white shadow-lg transition-all duration-200 hover:shadow-xl hover:scale-[1.02] disabled:cursor-not-allowed disabled:opacity-50 disabled:hover:scale-100"
                disabled={isSubmitting}
                style={{ 
                  background: 'linear-gradient(135deg, #1ABCBC 0%, #0F6A7B 100%)',
                  boxShadow: '0 4px 14px 0 rgba(15, 106, 123, 0.3)'
                }}
              >
                {isSubmitting ? "Ingresando..." : "Iniciar sesión"}
              </button>
            </form>
          )}

          <div className="flex flex-col items-center gap-1 pt-2">
            {!isRegisterMode && (
              <button
                type="button"
                className="text-xs font-semibold text-primary transition-all duration-200 hover:text-primary-dark hover:underline"
                onClick={() => setIsForgotPasswordModalOpen(true)}
              >
                ¿Olvidaste tu contraseña?
              </button>
            )}
          </div>
        </div>
      </section>

      <ForgotPasswordModal
        isOpen={isForgotPasswordModalOpen}
        onClose={() => setIsForgotPasswordModalOpen(false)}
        onTokenReceived={(token) => {
          setResetToken(token);
          setIsForgotPasswordModalOpen(false);
          setIsResetPasswordModalOpen(true);
        }}
      />
      <ResetPasswordModal
        isOpen={isResetPasswordModalOpen}
        token={resetToken}
        onClose={() => {
          setIsResetPasswordModalOpen(false);
          setResetToken(undefined);
        }}
        onSuccess={() => {
          // Opcional: redirigir al login o mostrar mensaje
        }}
      />
    </div>
  );
};



export default LoginPage;
