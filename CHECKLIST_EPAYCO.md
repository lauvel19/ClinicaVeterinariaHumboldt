# ✅ CHECKLIST COMPLETO - INTEGRACIÓN EPAYCO

## 📋 **Qué necesitas obtener de ePayco**

### 1️⃣ **Crear cuenta en ePayco** ⏱️ 10 min
```
1. Ve a: https://dashboard.epayco.co/register
2. Completa el registro con tus datos
3. Verifica tu email
4. Ingresa al dashboard
```

### 2️⃣ **Obtener credenciales de PRUEBA** ⏱️ 5 min
```
📍 Ubicación: Dashboard → Configuración → API Keys

Necesitas copiar 3 valores:

✅ Public Key (test)
   Ejemplo: test_pub_63fcc3e8d5XXXXXXXXXXXXXXXX
   Uso: Para generar URLs de pago

✅ Private Key (test)  
   Ejemplo: test_priv_XXXXXXXXXXXXXXXXXXXXXXXXX
   Uso: Para validar firmas del webhook

✅ P_CUST_ID_CLIENTE
   Ejemplo: 1 (o tu ID de cliente)
   Uso: Para cálculo de firma de seguridad
```

### 3️⃣ **Configurar Webhooks en ePayco** ⏱️ 15 min
```
📍 Ubicación: Dashboard → Configuración → Webhooks

⚠️ IMPORTANTE para desarrollo local:

El webhook de ePayco DEBE ser accesible desde internet.
Como tu backend está en localhost:8080, necesitas NGROK:

1. Instalar ngrok:
   npm install -g ngrok
   
   O descargar de: https://ngrok.com/download

2. Exponer tu puerto:
   ngrok http 8080

3. Copiar la URL HTTPS generada:
   Ejemplo: https://abc123def456.ngrok.io

4. En el dashboard de ePayco, configurar:
   URL de Confirmación: https://abc123def456.ngrok.io/api/pagos/webhook/epayco
   ✅ Marcar: "Activar webhook"
```

---

## 🔧 **Configuración en tu Proyecto**

### Paso 1: Variables de entorno

Crea/actualiza tu archivo `.env` en la raíz:

```bash
# ePayco - Credenciales de PRUEBA
EPAYCO_PUBLIC_KEY=test_pub_TU_PUBLIC_KEY_AQUI
EPAYCO_PRIVATE_KEY=test_priv_TU_PRIVATE_KEY_AQUI
EPAYCO_CUSTOMER_ID=1

# URLs (actualizar con tu ngrok)
EPAYCO_CONFIRMATION_URL=https://TU-NGROK-URL.ngrok.io/api/pagos/webhook/epayco
EPAYCO_RESPONSE_URL=http://localhost:5173/facturas/pago-resultado
```

### Paso 2: Verificar que el backend lea el .env

El proyecto ya tiene configurado `DotEnvConfig.java` que carga el .env automáticamente.

---

## 🧪 **Cómo Probar la Integración**

### Test 1: Iniciar un pago

```bash
# 1. Asegúrate que el backend esté corriendo
mvn spring-boot:run

# 2. Asegúrate que ngrok esté corriendo
ngrok http 8080

# 3. Obtén un token JWT (login)
POST http://localhost:8080/api/auth/login
{
  "username": "admin",
  "password": "admin123"
}

# 4. Inicia un pago para una factura PENDIENTE
POST http://localhost:8080/api/facturas/123/iniciar-pago-online
Authorization: Bearer {tu-token}
{
  "nombrePagador": "Juan Pérez",
  "emailPagador": "juan@example.com",
  "telefonoPagador": "+573001234567"
}

# 5. El backend te devolverá una URL como:
{
  "urlPago": "https://secure.epayco.co/checkout.php?public-key=..."
}

# 6. Abre esa URL en el navegador
```

### Test 2: Simular un pago exitoso

```
1. Al abrir la URL de ePayco, verás la pasarela de pago

2. Usa estos datos de prueba:

   📌 TARJETA DE CRÉDITO:
   - Número: 4575623182290326
   - CVV: 123
   - Fecha: 12/25
   - Nombre: APPROVED
   - Cuotas: 1

   📌 PSE (Débito bancario):
   - Banco: Banco de Bogotá
   - Usuario: cualquier valor
   - Contraseña: cualquier valor
   - Seleccionar: "Transacción exitosa"

3. Completa el pago

4. ePayco enviará el webhook a tu backend

5. Tu backend:
   - Validará la firma
   - Actualizará PagoOnline a APROBADA
   - Marcará la Factura como PAGADA
   - Establecerá fechaPago

6. ePayco te redirigirá al frontend
```

### Test 3: Verificar el resultado

```sql
-- En tu base de datos, verifica:
SELECT 
    po.id_pago_online,
    po.referencia_epayco,
    po.estado,
    po.monto,
    po.fecha_aprobacion,
    f.numero,
    f.estado AS factura_estado,
    f.fecha_pago
FROM pagos_online po
JOIN facturas f ON f.id_factura = po.factura_id
ORDER BY po.created_at DESC
LIMIT 5;
```

---

## 📝 **Resumen de Archivos Creados**

```
✅ Backend - 12 archivos nuevos:
├── EpaycoConfig.java                    (Configuración)
├── PagoOnline.java                      (Entidad JPA)
├── PagoOnlineRepository.java            (Repositorio)
├── EpaycoService.java                   (Lógica de negocio)
├── PagoOnlineController.java            (REST API)
├── PagoOnlineRequest.java               (DTO Request)
├── PagoOnlineResponse.java              (DTO Response)
├── EpaycoWebhookRequest.java            (DTO Webhook)
├── V10__create_pagos_online.sql         (Migración DB)
├── SecurityConfig.java                  (Webhook público)
├── application.yml                      (Config ePayco)
└── .env.example                         (Ejemplo vars)

✅ Documentación - 2 archivos:
├── INTEGRACION_EPAYCO.md               (Guía completa)
└── CHECKLIST_EPAYCO.md                 (Este archivo)

✅ Dependencias agregadas:
└── OkHttp 4.12.0                       (Cliente HTTP)
```

---

## 🚨 **Errores Comunes y Soluciones**

### ❌ Error: "Webhook no se recibe"
```
Causa: ngrok no está corriendo o la URL cambió
Solución:
1. Verificar que ngrok esté activo: ngrok http 8080
2. Copiar la nueva URL de ngrok
3. Actualizar EPAYCO_CONFIRMATION_URL en .env
4. Reiniciar el backend
5. Actualizar la URL en el dashboard de ePayco
```

### ❌ Error: "Firma inválida"
```
Causa: EPAYCO_CUSTOMER_ID o EPAYCO_PRIVATE_KEY incorrectos
Solución:
1. Verificar credenciales en dashboard.epayco.co
2. Copiar exactamente las keys (sin espacios)
3. Actualizar .env
4. Reiniciar backend
```

### ❌ Error: "Factura ya tiene un pago aprobado"
```
Causa: Ya existe un pago APROBADO para esa factura
Solución:
1. Usar otra factura PENDIENTE
2. O crear una nueva factura para probar
```

### ❌ Error: "Cannot connect to ePayco"
```
Causa: Problemas de red o credenciales incorrectas
Solución:
1. Verificar conexión a internet
2. Verificar que las credenciales sean de PRUEBA (test_*)
3. Revisar logs del backend
```

---

## 📱 **Frontend (Pendiente de Implementar)**

Para completar la integración, el frontend necesita:

```typescript
// 1. Botón "Pagar Online" en detalle de factura
// 2. Llamada al endpoint de inicio de pago
// 3. Redirección a la URL de ePayco
// 4. Página de resultado de pago

// Ejemplo básico:
const iniciarPago = async (facturaId) => {
  const response = await api.post(
    `/facturas/${facturaId}/iniciar-pago-online`,
    {
      nombrePagador: "Juan Pérez",
      emailPagador: "juan@example.com",
      telefonoPagador: "+573001234567"
    }
  );
  
  // Redirigir a ePayco
  window.location.href = response.data.urlPago;
};
```

---

## ✅ **Checklist Final**

Antes de hacer el commit, verifica:

- [ ] Credenciales de ePayco copiadas a `.env`
- [ ] ngrok corriendo y URL actualizada
- [ ] Backend compilando sin errores
- [ ] Migración V10 aplicada correctamente
- [ ] Webhook configurado en dashboard ePayco
- [ ] Test de pago exitoso completado
- [ ] Factura marcada como PAGADA en DB
- [ ] Registro en tabla `pagos_online` creado

---

## 📞 **Soporte ePayco**

- **Documentación**: https://docs.epayco.co
- **Dashboard**: https://dashboard.epayco.co
- **Soporte técnico**: soporte@epayco.co
- **Chat en vivo**: Disponible en el dashboard

---

## 🎯 **Próximos Pasos**

1. ✅ Obtener credenciales de ePayco
2. ✅ Configurar ngrok
3. ✅ Actualizar .env
4. ✅ Probar flujo completo
5. ⏳ Implementar frontend
6. ⏳ Testing exhaustivo
7. ⏳ Documentar para cliente
8. ⏳ Solicitar credenciales de PRODUCCIÓN
9. ⏳ Deploy a servidor real
10. ⏳ Configurar webhook en dominio real

---

## 💰 **Costos y Comisiones**

ePayco cobra una comisión por transacción:

- **PSE**: ~$900 COP + 1.4% + IVA
- **Tarjeta Crédito**: 3.49% + IVA
- **Tarjeta Débito**: 2.99% + IVA
- **Efectivo**: Varía según red

⚠️ Verificar tarifas actuales con ePayco antes de producción.
