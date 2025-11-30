# ============================================
# GUÍA DE INTEGRACIÓN CON EPAYCO
# ============================================

## 📋 Descripción

Este módulo integra ePayco como pasarela de pagos online para el sistema veterinario. Permite a los clientes pagar sus facturas usando PSE, tarjetas de crédito/débito, o efectivo (Baloto, Efecty, etc.).

---

## 🔑 Configuración

### 1. Obtener credenciales de ePayco

1. Crear cuenta en [https://dashboard.epayco.co](https://dashboard.epayco.co)
2. En el dashboard, ir a **Configuración > API Keys**
3. Copiar:
   - **Public Key** (para frontend)
   - **Private Key** (para backend)
   - **P_CUST_ID_CLIENTE** (ID de cliente)

### 2. Configurar variables de entorno

Crear o actualizar archivo `.env` en la raíz del proyecto:

```bash
# ePayco - Credenciales de PRUEBA
EPAYCO_PUBLIC_KEY=test_pub_63fcc3XXXXXXXXXX
EPAYCO_PRIVATE_KEY=test_priv_XXXXXXXXXXXXXXXXX
EPAYCO_CUSTOMER_ID=1

# URLs de webhook y respuesta (usar ngrok en desarrollo)
EPAYCO_CONFIRMATION_URL=https://tu-dominio.ngrok.io/api/pagos/webhook/epayco
EPAYCO_RESPONSE_URL=http://localhost:5173/facturas/pago-resultado
```

### 3. Configuración en `application.yml`

Las credenciales ya están configuradas para leer desde variables de entorno. Ver sección `app.epayco` en `application.yml`.

---

## 🚀 Uso

### Flujo de Pago

```
1. Cliente → POST /api/facturas/{id}/iniciar-pago-online
   ↓
2. Backend crea PagoOnline y genera URL de ePayco
   ↓
3. Frontend redirige al cliente a la URL de ePayco
   ↓
4. Cliente completa el pago en ePayco
   ↓
5. ePayco → POST /api/pagos/webhook/epayco (confirmación)
   ↓
6. Backend actualiza PagoOnline y Factura
   ↓
7. ePayco redirige al cliente → Frontend muestra resultado
```

### Endpoints

#### Iniciar Pago
```http
POST /api/facturas/123/iniciar-pago-online
Authorization: Bearer {token}
Content-Type: application/json

{
  "nombrePagador": "Juan Pérez",
  "emailPagador": "juan@example.com",
  "telefonoPagador": "+573001234567"
}
```

**Respuesta:**
```json
{
  "idPagoOnline": 45,
  "facturaId": 123,
  "numeroFactura": "FACT-20251129-0042",
  "referenciaEpayco": "VET-20251129-123-5678",
  "monto": 145000.50,
  "moneda": "COP",
  "estado": "PENDIENTE",
  "urlPago": "https://secure.epayco.co/checkout.php?public-key=...",
  "fechaCreacion": "2025-11-29T14:30:00"
}
```

#### Consultar Pago
```http
GET /api/pagos-online/45
Authorization: Bearer {token}
```

#### Listar Pagos de una Factura
```http
GET /api/facturas/123/pagos-online
Authorization: Bearer {token}
```

---

## 🧪 Testing en Desarrollo

### Usando ngrok para webhooks

El webhook de ePayco debe ser accesible desde internet. En desarrollo local, usar [ngrok](https://ngrok.com/):

```bash
# Instalar ngrok
npm install -g ngrok

# Exponer puerto 8080
ngrok http 8080

# Copiar la URL HTTPS generada (ej: https://abcd1234.ngrok.io)
# Actualizar EPAYCO_CONFIRMATION_URL en .env:
EPAYCO_CONFIRMATION_URL=https://abcd1234.ngrok.io/api/pagos/webhook/epayco
```

### Datos de Prueba ePayco

**Tarjetas de Crédito:**
- Visa: `4575623182290326` CVV: `123` Fecha: `12/25`
- Mastercard: `5254133674403564` CVV: `123` Fecha: `12/25`

**PSE:**
- Banco: Banco de Bogotá
- Usuario: cualquier valor
- Contraseña: cualquier valor
- Seleccionar "Transacción exitosa"

---

## 🔐 Seguridad

### Validación de Firma

El webhook de ePayco incluye una firma SHA-256 que el backend valida automáticamente para verificar que la solicitud proviene realmente de ePayco y no ha sido manipulada.

La firma se calcula con:
```
SHA256(p_cust_id^p_key^x_ref_payco^x_transaction_id^x_amount^x_currency_code)
```

### Endpoint Público

El endpoint `/api/pagos/webhook/epayco` es público (sin autenticación JWT) porque ePayco debe poder llamarlo. La seguridad se garantiza mediante:
1. Validación de firma
2. Verificación de referencias únicas
3. Validación de estados permitidos

---

## 📊 Estados de Pago

| Estado | Descripción |
|--------|-------------|
| PENDIENTE | Transacción creada, esperando pago del cliente |
| PROCESANDO | Cliente redirigido a ePayco, pago en proceso |
| APROBADA | Pago confirmado exitosamente |
| RECHAZADA | Pago rechazado por entidad financiera |
| FALLIDA | Error en la transacción |
| CANCELADA | Cliente canceló el pago |

---

## 🗄️ Base de Datos

### Tabla pagos_online

```sql
SELECT 
    po.id_pago_online,
    po.referencia_epayco,
    po.estado,
    po.monto,
    f.numero AS factura,
    po.fecha_transaccion,
    po.fecha_aprobacion
FROM pagos_online po
JOIN facturas f ON f.id_factura = po.factura_id
WHERE po.estado = 'APROBADA';
```

---

## 🐛 Troubleshooting

### El webhook no se está recibiendo

1. Verificar que ngrok está corriendo
2. Verificar que la URL en `.env` es correcta y usa HTTPS
3. Revisar logs del backend: `logs/veterinaria-backend.log`
4. Probar manualmente el webhook con curl:
```bash
curl -X POST http://localhost:8080/api/pagos/webhook/epayco \
  -H "Content-Type: application/json" \
  -d '{"x_ref_payco": "123", "x_transaction_state": "Aceptada", ...}'
```

### La firma del webhook es inválida

1. Verificar que `EPAYCO_CUSTOMER_ID` y `EPAYCO_PRIVATE_KEY` son correctos
2. Revisar los logs para ver la firma calculada vs recibida
3. Contactar soporte de ePayco si persiste

### El pago no actualiza la factura

1. Verificar que el webhook está llegando correctamente
2. Revisar estado en tabla `pagos_online`
3. Consultar logs para ver si hay errores en `procesarWebhook()`

---

## 📚 Recursos

- [Documentación oficial ePayco](https://docs.epayco.co)
- [API Reference](https://docs.epayco.co/api)
- [Webhooks](https://docs.epayco.co/tools/webhooks)
- [Dashboard ePayco](https://dashboard.epayco.co)

---

## 🚀 Próximos Pasos

1. ✅ Integración backend completada
2. ⏳ Implementar interfaz frontend para iniciar pagos
3. ⏳ Agregar página de resultado de pago en frontend
4. ⏳ Implementar notificaciones por email cuando el pago es aprobado
5. ⏳ Agregar reportes de pagos online
6. ⏳ Configurar webhooks en producción

---

## 📝 Notas Importantes

- **Modo de prueba**: Por defecto está activado (`test-mode: true`). Para producción cambiar a `false`.
- **Comisiones**: ePayco cobra comisión por transacción. Verificar tarifas en el contrato.
- **Tiempos**: Los pagos con PSE pueden tardar hasta 24 horas en confirmarse.
- **Montos mínimos**: Verificar montos mínimos por método de pago con ePayco.
