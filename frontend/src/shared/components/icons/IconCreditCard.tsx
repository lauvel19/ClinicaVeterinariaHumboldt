/**
 * Componente de ícono de tarjeta de crédito estético
 * Diseño moderno para representar pagos y facturas
 * Cumple con principio de Single Responsibility
 * 
 * @component
 * @param {Object} props - Propiedades del componente
 * @param {string} props.className - Clases CSS adicionales
 * @returns {JSX.Element} Ícono de tarjeta estilizado
 */
interface IconCreditCardProps {
  className?: string;
}

export const IconCreditCard = ({ className = "w-6 h-6" }: IconCreditCardProps) => {
  return (
    <svg
      className={className}
      viewBox="0 0 24 24"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
    >
      <rect
        x="2"
        y="5"
        width="20"
        height="14"
        rx="2"
        stroke="currentColor"
        strokeWidth="2"
      />
      <path
        d="M2 10H22"
        stroke="currentColor"
        strokeWidth="2"
      />
      <path
        d="M6 15H10"
        stroke="currentColor"
        strokeWidth="2"
        strokeLinecap="round"
      />
    </svg>
  );
};
