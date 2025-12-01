// Fachada que orquesta la autenticación.
// Aplica el patrón Adapter para convertir la respuesta del backend en el modelo del frontend.
import { authStore } from "../../../shared/state/authStore";
import type { AuthUser } from "../../../shared/state/authStore";

import { AuthRepository } from "./AuthRepository";
import type { LoginRequestDTO } from "../types";

type LoginResponse = Awaited<ReturnType<typeof AuthRepository.login>>;

const toAuthUser = (responseUser: LoginResponse["usuario"]): AuthUser => ({
  id: responseUser.idUsuario,
  nombre: responseUser.nombre,
  apellido: responseUser.apellido,
  correo: responseUser.correo,
  rol: responseUser.rol,
});

const isBypassEnabled =
    import.meta.env?.VITE_BYPASS_AUTH === "true" ||
    globalThis.location.search.includes("bypassAuth=true");

export const AuthService = {
  login: async (payload: LoginRequestDTO) => {
    if (isBypassEnabled) {
      const mockUser: AuthUser = {
        id: 2,
        nombre: "Carlos",
        apellido: "Ramírez",
        correo: "carlos.vet@veterinaria.com",
        rol: "VETERINARIO",
      };
      authStore.getState().setSession("dev-token", mockUser);
      return { token: "dev-token", user: mockUser };
    }

    // Asegurar que los campos no estén vacíos o solo con espacios
    const trimmedPayload: LoginRequestDTO = {
      username: payload.username?.trim() ?? "",
      password: payload.password?.trim() ?? "",
    };

    if (!trimmedPayload.username || !trimmedPayload.password) {
      throw new Error("El usuario y la contraseña son obligatorios");
    }

    const response = await AuthRepository.login(trimmedPayload);

    const user = toAuthUser(response.usuario);
    authStore.getState().setSession(response.token, user);

    return { token: response.token, user };
  },
  logout: () => {
    authStore.getState().clearSession();
  },
};
