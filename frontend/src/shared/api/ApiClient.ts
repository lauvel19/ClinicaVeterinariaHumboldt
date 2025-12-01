// Singleton responsable de configurar Axios para toda la aplicación.
// Aplica interceptores para adjuntar el token JWT y centraliza el manejo de errores.
import axios from "axios";
import type { AxiosInstance, AxiosResponse } from "axios";

import { authStore } from "../state/authStore";

// URL base del backend. Se puede parametrizar mediante variables de entorno.
const BASE_URL = import.meta.env.VITE_API_URL ?? "http://localhost:8080/api";

let apiClient: AxiosInstance | null = null;

const buildClient = (): AxiosInstance => {
  const instance = axios.create({
    baseURL: BASE_URL,
    timeout: 15000,
    headers: {
      "Content-Type": "application/json",
    },
  });

  // Interceptor para incluir el token JWT si existe.
  instance.interceptors.request.use((config) => {
    const token = authStore.getState().token;
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    // Log detallado para debug
    console.log("📤 HTTP Request:", {
      method: config.method?.toUpperCase(),
      url: config.url,
      fullURL: `${config.baseURL}${config.url}`,
      hasToken: !!token,
    });
    return config;
  });

  // Interceptor de respuesta para logging y unificación de errores.
  instance.interceptors.response.use(
    (response: AxiosResponse) => {
      console.log("✅ HTTP Response:", {
        method: response.config.method?.toUpperCase(),
        url: response.config.url,
        status: response.status,
        dataType: typeof response.data,
        hasData: !!response.data,
        dataKeys: response.data && typeof response.data === 'object' ? Object.keys(response.data) : [],
      });
      return response;
    },
    (error) => {
      // Si es error 401, significa que el token expiró o no hay sesión
      if (error.response?.status === 401) {
        const isLoginRequest = error.config?.url?.includes("/auth/login");
        
        if (!isLoginRequest) {
          console.warn("⚠️ Error 401 - Token inválido o expirado. Cerrando sesión...");
          // Limpiar el estado de autenticación
          authStore.getState().clearSession();
          // Redirigir al login
          globalThis.location.href = "/login";
          return Promise.reject(error);
        }
      }

      // Log detallado del error
      console.error("❌ HTTP Error:", {
        method: error.config?.method?.toUpperCase(),
        url: error.config?.url,
        status: error.response?.status,
        statusText: error.response?.statusText,
        data: error.response?.data,
        message: error.message,
      });
      return Promise.reject(error);
    },
  );

  return instance;
};

export const getApiClient = (): AxiosInstance => {
  if (!apiClient) {
    apiClient = buildClient();
  }
  return apiClient;
};


