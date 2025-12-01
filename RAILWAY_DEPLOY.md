# Despliegue en Railway - Clínica Veterinaria Humboldt

## 📋 Pre-requisitos

- Cuenta en [Railway.app](https://railway.app)
- Repositorio en GitHub
- PostgreSQL plugin en Railway

## 🚀 Pasos de Despliegue

### 1. Crear proyecto en Railway

1. Ve a [railway.app](https://railway.app) y crea una cuenta
2. Click en "New Project"
3. Selecciona "Deploy from GitHub repo"
4. Conecta tu repositorio

### 2. Agregar base de datos PostgreSQL

1. En tu proyecto de Railway, click en "New"
2. Selecciona "Database" → "Add PostgreSQL"
3. Railway creará automáticamente la base de datos

### 3. Configurar variables de entorno para el Backend

En Railway, ve al servicio del backend → Variables → Add Variable:

```env
# Perfil de Spring Boot
SPRING_PROFILES_ACTIVE=prod

# Base de datos (Railway las proporciona automáticamente)
DATABASE_URL=${{Postgres.DATABASE_URL}}
PGHOST=${{Postgres.PGHOST}}
PGPORT=${{Postgres.PGPORT}}
PGDATABASE=${{Postgres.PGDATABASE}}
PGUSER=${{Postgres.PGUSER}}
PGPASSWORD=${{Postgres.PGPASSWORD}}

# JWT Secret (CAMBIAR en producción)
JWT_SECRET=tu-secreto-jwt-muy-largo-y-seguro-al-menos-256-bits

# CORS (agregar tu dominio de frontend)
CORS_ALLOWED_ORIGINS=https://tu-frontend.railway.app,http://localhost:3000

# Puerto (Railway lo asigna automáticamente)
PORT=8080
```

### 4. Configurar variables de entorno para el Frontend

En Railway, ve al servicio del frontend → Variables → Add Variable:

```env
# URL del backend (se generará después del deploy del backend)
VITE_API_URL=https://tu-backend.railway.app/api
```

### 5. Configurar servicios

#### Backend:
- **Root Directory**: `/` (raíz del proyecto)
- **Dockerfile Path**: `Dockerfile`
- **Build Command**: (automático, usa Dockerfile)
- **Start Command**: (automático, usa Dockerfile ENTRYPOINT)

#### Frontend:
- **Root Directory**: `/frontend`
- **Dockerfile Path**: `frontend/Dockerfile`
- **Build Command**: (automático, usa Dockerfile)
- **Start Command**: (automático, usa Dockerfile CMD)

### 6. Orden de despliegue

1. **Primero**: PostgreSQL (se crea automáticamente)
2. **Segundo**: Backend (esperará a que PostgreSQL esté listo)
3. **Tercero**: Frontend (necesita la URL del backend)

### 7. Verificar despliegue

1. Backend: `https://tu-backend.railway.app/api/actuator/health`
2. Frontend: `https://tu-frontend.railway.app`

## 🔧 Solución de problemas comunes

### Error de conexión a base de datos
- Verifica que las variables `${{Postgres.*}}` estén correctamente referenciadas
- Revisa logs: Railway → Service → Logs

### Flyway migration errors
- Asegúrate de que todas las migraciones están en `src/main/resources/db/migration`
- Verifica que no hay migraciones duplicadas
- Si es necesario, habilita `spring.flyway.baseline-on-migrate=true`

### CORS errors en frontend
- Actualiza `CORS_ALLOWED_ORIGINS` con la URL del frontend
- Asegúrate de incluir el protocolo (`https://`)

### Frontend no puede conectar al backend
- Verifica que `VITE_API_URL` apunte a la URL correcta del backend
- Incluye `/api` al final de la URL

## 📝 Notas importantes

- Railway asigna URLs aleatorias. Guarda las URLs generadas.
- El plan gratuito tiene límites de recursos y horas de ejecución.
- Para producción seria, considera el plan de pago.
- Las variables `${{Postgres.*}}` se actualizan automáticamente si cambias la base de datos.

## 🔐 Seguridad

- **NUNCA** subas el `JWT_SECRET` al repositorio
- Usa variables de entorno para datos sensibles
- Cambia las credenciales por defecto
- Habilita HTTPS (Railway lo hace automáticamente)

## 📧 Notificaciones por email

Para envío de emails, considera usar servicios externos:
- SendGrid (free tier: 100 emails/día)
- Mailgun
- Amazon SES

Configura las credenciales en las variables de entorno del backend.
