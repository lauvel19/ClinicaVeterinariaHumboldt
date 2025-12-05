import { useState, useMemo } from "react";
import { useQuery } from "@tanstack/react-query";
import dayjs from "dayjs";
import "dayjs/locale/es";
import toast from "react-hot-toast";

import { FullscreenLoader } from "../../../app/components/feedback/FullscreenLoader";
import { FacturasRepository } from "../services/FacturasRepository";
import { CreateFacturaModal } from "../components/CreateFacturaModal";
import { FacturaDetailModal } from "../components/FacturaDetailModal";
import type { ApiFacturaResponse } from "../../shared/types/backend";
import { authStore } from "../../../shared/state/authStore";

dayjs.locale("es");

type EstadoFilter = "TODAS" | "PENDIENTE" | "PAGADA" | "ANULADA";

export const FacturasPage = () => {
  const user = authStore((state) => state.user);
  const isCliente = user?.rol === "CLIENTE";
  
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [selectedFacturaId, setSelectedFacturaId] = useState<number | null>(null);
  const [search, setSearch] = useState("");
  const [estadoFilter, setEstadoFilter] = useState<EstadoFilter>("TODAS");
  const [dateFrom, setDateFrom] = useState<string>("");
  const [dateTo, setDateTo] = useState<string>("");

  const { data: facturas, isLoading } = useQuery({
    queryKey: isCliente ? ["facturas-cliente", user?.id] : ["facturas"],
    queryFn: () => {
      if (isCliente && user?.id) {
        return FacturasRepository.getByCliente(user.id);
      }
      return FacturasRepository.getAll();
    },
    enabled: !isCliente || !!user?.id,
  });

  const facturasFiltradas = useMemo(() => {
    if (!facturas) return [];

    const term = search.trim().toLowerCase();
    const fromDate = dateFrom ? dayjs(dateFrom) : null;
    const toDate = dateTo ? dayjs(dateTo).endOf("day") : null;

    return facturas.filter((factura) => {
      const matchesSearch =
        term.length === 0 ||
        factura.numero.toLowerCase().includes(term) ||
        factura.cliente?.nombreCompleto.toLowerCase().includes(term) ||
        factura.cliente?.correo.toLowerCase().includes(term);

      const matchesEstado = estadoFilter === "TODAS" || factura.estado === estadoFilter;

      const fecha = dayjs(factura.fechaEmision);
      const matchesDate =
        (!fromDate || fecha.isAfter(fromDate.subtract(1, "day"))) &&
        (!toDate || fecha.isBefore(toDate.add(1, "day")));

      return matchesSearch && matchesEstado && matchesDate;
    });
  }, [facturas, search, estadoFilter, dateFrom, dateTo]);

  const stats = useMemo(() => {
    if (!facturas) return { total: 0, pendientes: 0, pagadas: 0, totalMonto: 0 };
    const pendientes = facturas.filter((f) => f.estado === "PENDIENTE").length;
    const pagadas = facturas.filter((f) => f.estado === "PAGADA").length;
    const totalMonto = facturas.reduce((sum, f) => sum + parseFloat(f.total), 0);
    return { total: facturas.length, pendientes, pagadas, totalMonto };
  }, [facturas]);

  if (isLoading) {
    return <FullscreenLoader />;
  }

  return (
    <div className="w-full space-y-6">
      <header className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-semibold text-secondary">
            {isCliente ? "Mis Facturas" : "Gestión de Facturas"}
          </h2>
          <p className="text-sm text-gray-500">
            {isCliente ? "Consulta tus facturas y pagos" : "Administra las facturas emitidas"}
          </p>
        </div>
        {!isCliente && (
          <button
            onClick={() => setIsCreateModalOpen(true)}
            className="rounded-2xl bg-primary px-4 py-2 text-sm font-semibold text-white shadow-soft transition-base hover:bg-primary-dark"
          >
            Nueva Factura
          </button>
        )}
      </header>

      <section className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <StatCard title="Total Facturas" value={stats.total.toString()} description="Emitidas" tone="primary" />
        <StatCard
          title="Pendientes"
          value={stats.pendientes.toString()}
          description="Por pagar"
          tone="warning"
        />
        <StatCard title="Pagadas" value={stats.pagadas.toString()} description="Completadas" tone="success" />
        <StatCard
          title="Total Recaudado"
          value={stats.totalMonto.toLocaleString("es-CO", { style: "currency", currency: "COP" })}
          description="Monto total"
          tone="info"
        />
      </section>

      <section className="rounded-3xl bg-white p-6 shadow-soft">
        <div className="mb-6 flex flex-wrap items-center gap-4">
          <input
            type="text"
            placeholder="Buscar por número, cliente o correo..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="flex-1 min-w-[200px] rounded-2xl border border-gray-200 bg-white px-4 py-2 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/30"
          />
          <select
            value={estadoFilter}
            onChange={(e) => setEstadoFilter(e.target.value as EstadoFilter)}
            className="rounded-2xl border border-gray-200 bg-white px-4 py-2 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/30"
          >
            <option value="TODAS">Todos los estados</option>
            <option value="PENDIENTE">Pendientes</option>
            <option value="PAGADA">Pagadas</option>
            <option value="ANULADA">Anuladas</option>
          </select>
          <input
            type="date"
            value={dateFrom}
            onChange={(e) => setDateFrom(e.target.value)}
            className="rounded-2xl border border-gray-200 bg-white px-4 py-2 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/30"
            placeholder="Desde"
          />
          <input
            type="date"
            value={dateTo}
            onChange={(e) => setDateTo(e.target.value)}
            className="rounded-2xl border border-gray-200 bg-white px-4 py-2 text-sm focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/30"
            placeholder="Hasta"
          />
        </div>

        <div className="overflow-hidden rounded-3xl border border-gray-100">
          <table className="min-w-full divide-y divide-gray-100 text-sm text-gray-600">
            <thead className="bg-gray-50 text-xs uppercase text-gray-500">
              <tr>
                <th className="px-4 py-3 text-left">Número</th>
                <th className="px-4 py-3 text-left">Fecha</th>
                <th className="px-4 py-3 text-left">Cliente</th>
                <th className="px-4 py-3 text-left">Total</th>
                <th className="px-4 py-3 text-left">Estado</th>
                <th className="px-4 py-3 text-right">Acciones</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100 bg-white">
              {facturasFiltradas.length === 0 ? (
                <tr>
                  <td className="px-4 py-6 text-center text-sm text-gray-500" colSpan={6}>
                    No se encontraron facturas con los criterios seleccionados.
                  </td>
                </tr>
              ) : (
                facturasFiltradas.map((factura) => (
                  <FacturaRow
                    key={factura.idFactura}
                    factura={factura}
                    onViewDetail={() => setSelectedFacturaId(factura.idFactura)}
                  />
                ))
              )}
            </tbody>
          </table>
        </div>
      </section>

      <CreateFacturaModal isOpen={isCreateModalOpen} onClose={() => setIsCreateModalOpen(false)} />
      <FacturaDetailModal
        isOpen={selectedFacturaId !== null}
        facturaId={selectedFacturaId}
        onClose={() => setSelectedFacturaId(null)}
      />
    </div>
  );
};

interface StatCardProps {
  readonly title: string;
  readonly value: string;
  readonly description: string;
  readonly tone?: "primary" | "success" | "warning" | "danger" | "info";
}

const StatCard = ({ title, value, description, tone = "primary" }: StatCardProps) => {
  const toneClasses = {
    primary: "bg-primary/10 text-primary border-primary/20",
    success: "bg-success/10 text-success border-success/20",
    warning: "bg-warning/10 text-warning border-warning/20",
    danger: "bg-danger/10 text-danger border-danger/20",
    info: "bg-blue-100 text-blue-600 border-blue-200",
  };

  return (
    <article className="rounded-2xl border bg-white p-6 shadow-soft">
      <p className="text-sm font-medium text-gray-500">{title}</p>
      <p className="mt-2 text-2xl font-semibold text-secondary">{value}</p>
      <span className={`mt-3 inline-flex rounded-full border px-3 py-1 text-xs font-semibold ${toneClasses[tone]}`}>
        {description}
      </span>
    </article>
  );
};

interface FacturaRowProps {
  readonly factura: ApiFacturaResponse;
  readonly onViewDetail: () => void;
}

const FacturaRow = ({ factura, onViewDetail }: FacturaRowProps) => {
  const fecha = dayjs(factura.fechaEmision);
  const total = parseFloat(factura.total);
  const estadoTone =
    factura.estado === "PAGADA"
      ? "bg-success/20 text-success"
      : factura.estado === "ANULADA"
        ? "bg-danger/20 text-danger"
        : "bg-warning/20 text-warning";

  return (
    <tr className="transition-base hover:bg-gray-50">
      <td className="px-4 py-3 font-semibold text-secondary">{factura.numero}</td>
      <td className="px-4 py-3">{fecha.format("DD/MM/YYYY")}</td>
      <td className="px-4 py-3">{factura.cliente?.nombreCompleto || "Sin cliente"}</td>
      <td className="px-4 py-3 font-semibold">
        {total.toLocaleString("es-CO", { style: "currency", currency: "COP" })}
      </td>
      <td className="px-4 py-3">
        <span className={`rounded-full px-3 py-1 text-xs font-semibold ${estadoTone}`}>{factura.estado}</span>
      </td>
      <td className="px-4 py-3 text-right">
        <button
          onClick={onViewDetail}
          className="rounded-2xl border border-primary px-4 py-2 text-xs font-semibold text-primary transition-base hover:bg-primary hover:text-white"
        >
          Ver detalle
        </button>
      </td>
    </tr>
  );
};

