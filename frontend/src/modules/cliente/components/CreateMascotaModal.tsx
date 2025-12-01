import { useState } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import toast from "react-hot-toast";
import { X } from "lucide-react";
import { getApiClient } from "../../../shared/api/ApiClient";
import { unwrapResponse } from "../../../shared/api/ApiResponseAdapter";
import type { ApiResponse } from "../../../shared/api/types";

/**
 * Interfaz para el formulario de creación de mascota
 * Cumple con principio de Interface Segregation
 */
interface MascotaFormData {
  nombre: string;
  especie: string;
  raza: string;
  edad: number;
  peso: number;
  sexo: string;
  colorPelaje: string;
}

/**
 * Props del componente CreateMascotaModal
 */
interface CreateMascotaModalProps {
  isOpen: boolean;
  onClose: () => void;
  clienteId: number;
}

/**
 * Modal para crear una nueva mascota
 * 
 * @component
 * @param {CreateMascotaModalProps} props - Propiedades del componente
 * @returns {JSX.Element | null} Modal de creación de mascota
 * 
 * @remarks
 * - Utiliza React Hook Form para validación
 * - Implementa patrón de mutación optimista con React Query
 * - Valida datos antes de enviar al backend
 * - Cumple con principio de Single Responsibility
 * - Maneja errores de forma robusta
 */
export const CreateMascotaModal = ({ isOpen, onClose, clienteId }: CreateMascotaModalProps) => {
  const queryClient = useQueryClient();
  const [isSubmitting, setIsSubmitting] = useState(false);

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<MascotaFormData>();

  /**
   * Mutación para crear una nueva mascota
   * Invalida el cache automáticamente al finalizar
   */
  const createMascotaMutation = useMutation({
    mutationFn: async (data: MascotaFormData) => {
      const client = getApiClient();
      const payload = {
        ...data,
        cliente: { idCliente: clienteId },
      };
      const response = await client.post<ApiResponse<unknown>>("/pacientes", payload);
      return unwrapResponse(response.data);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["pacientes-cliente", clienteId] });
      toast.success("Mascota registrada exitosamente");
      reset();
      onClose();
    },
    onError: (error: Error) => {
      toast.error(`Error al registrar mascota: ${error.message}`);
    },
    onSettled: () => {
      setIsSubmitting(false);
    },
  });

  /**
   * Maneja el envío del formulario
   * @param {MascotaFormData} data - Datos del formulario validados
   */
  const onSubmit = (data: MascotaFormData) => {
    setIsSubmitting(true);
    createMascotaMutation.mutate(data);
  };

  /**
   * Cierra el modal y resetea el formulario
   */
  const handleClose = () => {
    reset();
    onClose();
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-lg shadow-xl max-w-2xl w-full max-h-[90vh] overflow-y-auto">
        {/* Header */}
        <div className="flex justify-between items-center p-6 border-b">
          <h2 className="text-2xl font-semibold text-gray-900">Registrar Nueva Mascota</h2>
          <button
            onClick={handleClose}
            className="text-gray-400 hover:text-gray-600 transition-colors"
            disabled={isSubmitting}
          >
            <X className="w-6 h-6" />
          </button>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit(onSubmit)} className="p-6 space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {/* Nombre */}
            <div>
              <label htmlFor="nombre" className="block text-sm font-medium text-gray-700 mb-1">
                Nombre <span className="text-red-500">*</span>
              </label>
              <input
                id="nombre"
                type="text"
                {...register("nombre", {
                  required: "El nombre es requerido",
                  minLength: { value: 2, message: "Mínimo 2 caracteres" },
                  maxLength: { value: 50, message: "Máximo 50 caracteres" },
                })}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                placeholder="Ej: Max"
              />
              {errors.nombre && (
                <p className="text-red-500 text-xs mt-1">{errors.nombre.message}</p>
              )}
            </div>

            {/* Especie */}
            <div>
              <label htmlFor="especie" className="block text-sm font-medium text-gray-700 mb-1">
                Especie <span className="text-red-500">*</span>
              </label>
              <select
                id="especie"
                {...register("especie", { required: "La especie es requerida" })}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              >
                <option value="">Seleccionar...</option>
                <option value="Perro">Perro</option>
                <option value="Gato">Gato</option>
                <option value="Ave">Ave</option>
                <option value="Conejo">Conejo</option>
                <option value="Hamster">Hamster</option>
                <option value="Otro">Otro</option>
              </select>
              {errors.especie && (
                <p className="text-red-500 text-xs mt-1">{errors.especie.message}</p>
              )}
            </div>

            {/* Raza */}
            <div>
              <label htmlFor="raza" className="block text-sm font-medium text-gray-700 mb-1">
                Raza <span className="text-red-500">*</span>
              </label>
              <input
                id="raza"
                type="text"
                {...register("raza", {
                  required: "La raza es requerida",
                  maxLength: { value: 50, message: "Máximo 50 caracteres" },
                })}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                placeholder="Ej: Labrador"
              />
              {errors.raza && (
                <p className="text-red-500 text-xs mt-1">{errors.raza.message}</p>
              )}
            </div>

            {/* Sexo */}
            <div>
              <label htmlFor="sexo" className="block text-sm font-medium text-gray-700 mb-1">
                Sexo <span className="text-red-500">*</span>
              </label>
              <select
                id="sexo"
                {...register("sexo", { required: "El sexo es requerido" })}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              >
                <option value="">Seleccionar...</option>
                <option value="Macho">Macho</option>
                <option value="Hembra">Hembra</option>
              </select>
              {errors.sexo && (
                <p className="text-red-500 text-xs mt-1">{errors.sexo.message}</p>
              )}
            </div>

            {/* Edad */}
            <div>
              <label htmlFor="edad" className="block text-sm font-medium text-gray-700 mb-1">
                Edad (años) <span className="text-red-500">*</span>
              </label>
              <input
                id="edad"
                type="number"
                step="1"
                min="0"
                max="50"
                {...register("edad", {
                  required: "La edad es requerida",
                  min: { value: 0, message: "La edad mínima es 0" },
                  max: { value: 50, message: "La edad máxima es 50" },
                  valueAsNumber: true,
                })}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                placeholder="Ej: 3"
              />
              {errors.edad && (
                <p className="text-red-500 text-xs mt-1">{errors.edad.message}</p>
              )}
            </div>

            {/* Peso */}
            <div>
              <label htmlFor="peso" className="block text-sm font-medium text-gray-700 mb-1">
                Peso (kg) <span className="text-red-500">*</span>
              </label>
              <input
                id="peso"
                type="number"
                step="0.1"
                min="0.1"
                max="200"
                {...register("peso", {
                  required: "El peso es requerido",
                  min: { value: 0.1, message: "El peso mínimo es 0.1 kg" },
                  max: { value: 200, message: "El peso máximo es 200 kg" },
                  valueAsNumber: true,
                })}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                placeholder="Ej: 15.5"
              />
              {errors.peso && (
                <p className="text-red-500 text-xs mt-1">{errors.peso.message}</p>
              )}
            </div>

            {/* Color de Pelaje */}
            <div className="md:col-span-2">
              <label htmlFor="colorPelaje" className="block text-sm font-medium text-gray-700 mb-1">
                Color de Pelaje
              </label>
              <input
                id="colorPelaje"
                type="text"
                {...register("colorPelaje", {
                  maxLength: { value: 50, message: "Máximo 50 caracteres" },
                })}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                placeholder="Ej: Dorado con manchas blancas"
              />
              {errors.colorPelaje && (
                <p className="text-red-500 text-xs mt-1">{errors.colorPelaje.message}</p>
              )}
            </div>
          </div>

          {/* Footer */}
          <div className="flex justify-end gap-3 pt-4 border-t">
            <button
              type="button"
              onClick={handleClose}
              disabled={isSubmitting}
              className="px-4 py-2 text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200 transition-colors disabled:opacity-50"
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={isSubmitting}
              className="px-4 py-2 text-white bg-blue-600 rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
            >
              {isSubmitting ? (
                <>
                  <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white" />
                  Registrando...
                </>
              ) : (
                "Registrar Mascota"
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
