/**
 * Componente de ícono de calendario estético
 * Implementa un diseño visual consistente sin dependencias externas
 * Cumple con principio de Single Responsibility
 * 
 * @component
 * @param {Object} props - Propiedades del componente
 * @param {string} props.className - Clases CSS adicionales
 * @returns {JSX.Element} Ícono de calendario estilizado
 */
interface IconCalendarProps {
  className?: string;
}

export const IconCalendar = ({ className = "w-6 h-6" }: IconCalendarProps) => {
  return (
    <svg
      className={className}
      viewBox="0 0 24 24"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
    >
      <rect
        x="3"
        y="6"
        width="18"
        height="15"
        rx="2"
        stroke="currentColor"
        strokeWidth="2"
        fill="none"
      />
      <path
        d="M3 10H21"
        stroke="currentColor"
        strokeWidth="2"
        strokeLinecap="round"
      />
      <path
        d="M7 3V7"
        stroke="currentColor"
        strokeWidth="2"
        strokeLinecap="round"
      />
      <path
        d="M17 3V7"
        stroke="currentColor"
        strokeWidth="2"
        strokeLinecap="round"
      />
      <circle cx="7" cy="14" r="1" fill="currentColor" />
      <circle cx="12" cy="14" r="1" fill="currentColor" />
      <circle cx="17" cy="14" r="1" fill="currentColor" />
      <circle cx="7" cy="18" r="1" fill="currentColor" />
      <circle cx="12" cy="18" r="1" fill="currentColor" />
    </svg>
  );
};
