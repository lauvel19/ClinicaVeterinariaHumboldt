/**
 * Componente de ícono de huella de mascota estético
 * Diseño personalizado sin dependencias externas
 * Cumple con principio de Single Responsibility
 * 
 * @component
 * @param {Object} props - Propiedades del componente
 * @param {string} props.className - Clases CSS adicionales
 * @returns {JSX.Element} Ícono de huella estilizado
 */
interface IconPawProps {
  className?: string;
}

export const IconPaw = ({ className = "w-6 h-6" }: IconPawProps) => {
  return (
    <svg
      className={className}
      viewBox="0 0 24 24"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
    >
      {/* Almohadilla principal */}
      <ellipse
        cx="12"
        cy="15"
        rx="3.5"
        ry="3"
        fill="currentColor"
      />
      {/* Dedos */}
      <ellipse
        cx="7"
        cy="10"
        rx="2"
        ry="2.5"
        fill="currentColor"
      />
      <ellipse
        cx="10.5"
        cy="7"
        rx="2"
        ry="2.5"
        fill="currentColor"
      />
      <ellipse
        cx="13.5"
        cy="7"
        rx="2"
        ry="2.5"
        fill="currentColor"
      />
      <ellipse
        cx="17"
        cy="10"
        rx="2"
        ry="2.5"
        fill="currentColor"
      />
    </svg>
  );
};
