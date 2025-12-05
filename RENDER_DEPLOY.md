# 🚀 Guía de Despliegue en Render - Clínica Veterinaria Humboldt

Esta guía te llevará paso a paso para desplegar la aplicación completa en Render.

## 📋 Requisitos Previos

1. Cuenta en [Render](https://render.com) (puedes usar GitHub para registrarte)
2. Repositorio en GitHub con el código actualizado
3. Los archivos de configuración ya están listos:
   - `render.yaml` - Blueprint de infraestructura
   - `Dockerfile.render` - Dockerfile optimizado para Render
   - `application-render.yml` - Configuración de Spring Boot para Render

---

## 🔧 Método 1: Despliegue Automático con Blueprint (Recomendado)

### Paso 1: Subir cambios a GitHub

```bash
git add .
git commit -m "feat: configuración para despliegue en Render"
git push origin main
```

### Paso 2: Crear servicios en Render

1. Ve a [Render Dashboard](https://dashboard.render.com)
2. Haz clic en **"New"** → **"Blueprint"**
3. Conecta tu repositorio de GitHub
4. Render detectará automáticamente el archivo `render.yaml`
5. Revisa la configuración y haz clic en **"Apply"**

### Paso 3: Esperar el despliegue

- La base de datos PostgreSQL se creará primero (~2-3 minutos)
- El backend se construirá y desplegará (~5-10 minutos)
- El frontend se construirá y desplegará (~3-5 minutos)

---

## 🔧 Método 2: Despliegue Manual (Paso a Paso)

### Paso 1: Crear la Base de Datos PostgreSQL

1. En Render Dashboard, clic en **"New"** → **"PostgreSQL"**
2. Configurar:
   - **Name**: `veterinaria-db`
   - **Database**: `veterinaria_db`
   - **User**: `vet_admin`
   - **Region**: Oregon (US West)
   - **PostgreSQL Version**: 15
   - **Plan**: Free (o Starter para producción)
3. Clic en **"Create Database"**
4. **¡IMPORTANTE!** Copia la **Internal Database URL** (la necesitarás para el backend)

### Paso 2: Desplegar el Backend

1. En Render Dashboard, clic en **"New"** → **"Web Service"**
2. Conecta tu repositorio de GitHub
3. Configurar:
   - **Name**: `veterinaria-backend`
   - **Region**: Oregon (mismo que la DB)
   - **Branch**: `main` (o `develop`)
   - **Runtime**: Docker
   - **Dockerfile Path**: `./Dockerfile.render`
   - **Plan**: Free (o Starter)

4. Agregar **Variables de Entorno**:

   | Key | Value |
   |-----|-------|
   | `SPRING_PROFILES_ACTIVE` | `render` |
   | `SPRING_DATASOURCE_URL` | `jdbc:postgresql://[HOST]:5432/veterinaria_db` * |
   | `SPRING_DATASOURCE_USERNAME` | `vet_admin` |
   | `SPRING_DATASOURCE_PASSWORD` | [Password de la DB] |
   | `APP_JWT_SECRET` | [Generar string aleatorio de 64+ caracteres] |
   | `JWT_SECRET` | [Mismo valor que APP_JWT_SECRET] |
   | `SPRING_FLYWAY_ENABLED` | `true` |
   | `SPRING_FLYWAY_BASELINE_ON_MIGRATE` | `true` |
   | `SPRING_JPA_HIBERNATE_DDL_AUTO` | `update` |
   | `APP_CORS_ALLOWED_ORIGINS` | `https://veterinaria-frontend.onrender.com` |

   > * Usa la **Internal Database URL** de Render, pero cambia el formato:
   > - Render da: `postgres://user:pass@host:5432/db`
   > - Spring necesita: `jdbc:postgresql://host:5432/db`

5. Clic en **"Create Web Service"**

### Paso 3: Desplegar el Frontend

1. En Render Dashboard, clic en **"New"** → **"Static Site"**
2. Conecta tu repositorio de GitHub
3. Configurar:
   - **Name**: `veterinaria-frontend`
   - **Branch**: `main`
   - **Build Command**: `cd frontend && npm ci --legacy-peer-deps && npm run build`
   - **Publish Directory**: `frontend/dist`

4. Agregar **Variables de Entorno**:

   | Key | Value |
   |-----|-------|
   | `VITE_API_URL` | `https://veterinaria-backend.onrender.com/api` |

5. En la sección **"Redirects/Rewrites"**, agregar:
   - **Source**: `/*`
   - **Destination**: `/index.html`
   - **Action**: `Rewrite`

6. Clic en **"Create Static Site"**

---

## 🔄 Actualizar URLs Después del Deploy

### Actualizar el Backend con la URL del Frontend

1. Ve al servicio `veterinaria-backend` en Render
2. En **Environment** → **Environment Variables**
3. Actualiza `APP_CORS_ALLOWED_ORIGINS` con la URL real del frontend:
   ```
   https://veterinaria-frontend.onrender.com,http://localhost:3000
   ```
4. Clic en **"Save Changes"** (esto reiniciará el servicio)

### Actualizar el Frontend con la URL del Backend

1. Ve al sitio `veterinaria-frontend` en Render
2. En **Environment** → **Environment Variables**
3. Verifica que `VITE_API_URL` tenga la URL correcta:
   ```
   https://veterinaria-backend.onrender.com/api
   ```
4. Si cambió, haz **"Manual Deploy"** → **"Clear build cache & deploy"**

---

## 🔐 Credenciales de Acceso

Después del despliegue, puedes acceder con los usuarios creados por Flyway:

| Usuario | Contraseña | Rol |
|---------|------------|-----|
| `admin` | (ver hash en V1__init_schema.sql) | ADMIN |
| `vet_carlos` | (hash BCrypt) | VETERINARIO |
| `sec_laura` | (hash BCrypt) | SECRETARIO |
| `cliente_diego` | (hash BCrypt) | CLIENTE |

> ⚠️ **IMPORTANTE**: Los hashes en V1 no son BCrypt estándar. Puede que necesites actualizar las contraseñas después del deploy.

### Actualizar Contraseña del Admin

Conéctate a la base de datos desde Render y ejecuta:

```sql
UPDATE usuarios 
SET password_hash = '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqxGl0ynZy4aQo8xJv5.kEBn1VYKi'
WHERE username = 'admin';
```

Esto establece la contraseña como: `Admin123!`

---

## 📊 Monitoreo y Logs

### Ver Logs del Backend
1. Ve al servicio `veterinaria-backend`
2. Clic en **"Logs"** en el menú lateral

### Ver Métricas
1. En el dashboard del servicio
2. Sección **"Metrics"** muestra CPU, memoria y requests

### Base de Datos
1. Ve a `veterinaria-db`
2. Usa **"Connect"** para acceder via PSQL o herramientas externas

---

## ⚠️ Limitaciones del Plan Free

| Recurso | Limitación |
|---------|------------|
| **Web Services** | Se "duermen" después de 15 min de inactividad |
| **PostgreSQL** | 1GB almacenamiento, conexiones limitadas |
| **Build** | 500 minutos/mes gratis |
| **Bandwidth** | 100 GB/mes |

### Recomendaciones para Producción

1. **Actualizar a Starter** ($7/mes) para evitar spin-down
2. **PostgreSQL Starter** ($7/mes) para más almacenamiento y conexiones
3. Configurar **Health Checks** apropiados
4. Usar **Custom Domains** con SSL gratuito

---

## 🐛 Solución de Problemas

### El backend no inicia
1. Verifica los logs en Render
2. Confirma que `SPRING_DATASOURCE_URL` tiene el formato correcto
3. Verifica que la DB esté activa y accesible

### Error de CORS
1. Verifica que `APP_CORS_ALLOWED_ORIGINS` incluya la URL del frontend
2. Asegúrate de usar HTTPS en producción

### El frontend muestra "Network Error"
1. Verifica que `VITE_API_URL` apunte al backend correcto
2. Confirma que el backend esté activo (puede estar "dormido")
3. Revisa la consola del navegador para más detalles

### La base de datos no conecta
1. Usa la **Internal Database URL** (no la externa)
2. Verifica que el backend y la DB estén en la misma región
3. Revisa que el usuario y contraseña sean correctos

---

## 📝 URLs Finales

Después del despliegue exitoso, tendrás:

| Servicio | URL |
|----------|-----|
| Frontend | `https://veterinaria-frontend.onrender.com` |
| Backend API | `https://veterinaria-backend.onrender.com/api` |
| Swagger UI | `https://veterinaria-backend.onrender.com/api/swagger-ui.html` |
| Health Check | `https://veterinaria-backend.onrender.com/api/actuator/health` |

---

## ✅ Checklist de Despliegue

- [ ] Código subido a GitHub
- [ ] Base de datos PostgreSQL creada
- [ ] Backend desplegado y funcionando
- [ ] Frontend desplegado y funcionando
- [ ] CORS configurado correctamente
- [ ] Variables de entorno verificadas
- [ ] Contraseñas de usuarios actualizadas
- [ ] Prueba de login exitosa

---

¡Listo! Tu aplicación debería estar funcionando en Render. 🎉
