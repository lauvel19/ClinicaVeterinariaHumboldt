# 🐳 Docker - Clínica Veterinaria Humboldt

## 📋 Requisitos Previos

- **Docker Desktop** instalado y corriendo
- **Docker Compose** v2.0 o superior
- Al menos **4GB de RAM** disponible para Docker

## 🚀 Inicio Rápido

### 1. Construir e iniciar todos los servicios

```bash
# Construir y levantar todos los contenedores
docker-compose up -d --build

# Ver logs en tiempo real
docker-compose logs -f
```

### 2. Acceder a la aplicación

| Servicio | URL | Descripción |
|----------|-----|-------------|
| **Frontend** | http://localhost:3000 | Aplicación React |
| **Backend API** | http://localhost:8080/api | API REST Spring Boot |
| **Swagger UI** | http://localhost:8080/api/swagger-ui.html | Documentación API |
| **PostgreSQL** | localhost:5432 | Base de datos |

### 3. Credenciales por defecto

**Usuario Administrador:**
- **Username:** `admin`
- **Password:** `Admin123!`

**Base de datos:**
- **Host:** localhost (o `postgres` desde otros contenedores)
- **Puerto:** 5432
- **Database:** veterinaria_db
- **Usuario:** vet_admin
- **Password:** Petrico123

## 📦 Servicios

### Base de Datos (PostgreSQL)
```yaml
Container: veterinaria-db
Puerto: 5432
Volumen: veterinaria-postgres-data
```

### Backend (Spring Boot)
```yaml
Container: veterinaria-backend
Puerto: 8080
Dependencias: PostgreSQL
```

### Frontend (React + Nginx)
```yaml
Container: veterinaria-frontend
Puerto: 3000
Dependencias: Backend
```

## 🛠️ Comandos Útiles

### Control de Servicios

```bash
# Iniciar todos los servicios
docker-compose up -d

# Detener todos los servicios
docker-compose down

# Reiniciar un servicio específico
docker-compose restart backend

# Ver estado de los servicios
docker-compose ps

# Ver logs de un servicio
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f postgres
```

### Gestión de Datos

```bash
# Backup de la base de datos
docker exec veterinaria-db pg_dump -U vet_admin veterinaria_db > backup.sql

# Restaurar base de datos
docker exec -i veterinaria-db psql -U vet_admin veterinaria_db < backup.sql

# Acceder a PostgreSQL
docker exec -it veterinaria-db psql -U vet_admin -d veterinaria_db
```

### Reconstrucción

```bash
# Reconstruir sin cache
docker-compose build --no-cache

# Reconstruir un servicio específico
docker-compose build --no-cache backend

# Limpiar todo y empezar de nuevo
docker-compose down -v --rmi all
docker-compose up -d --build
```

## 🔧 Configuración

### Variables de Entorno

Copia el archivo de ejemplo y modifica según necesites:

```bash
cp .env.docker .env
```

Edita `.env` con tus configuraciones:

```env
# Base de datos
POSTGRES_PASSWORD=tu_password_seguro

# JWT
APP_JWT_SECRET=tu_secreto_jwt_muy_largo

# Email (opcional)
GMAIL_USERNAME=tu_correo@gmail.com
GMAIL_APP_PASSWORD=tu_app_password
```

### Personalizar Puertos

Edita `docker-compose.yml`:

```yaml
services:
  frontend:
    ports:
      - "80:80"  # Cambiar 3000 por 80
  
  backend:
    ports:
      - "8081:8080"  # Cambiar puerto externo
```

## 🐛 Solución de Problemas

### El backend no conecta con la base de datos

```bash
# Verificar que PostgreSQL esté corriendo
docker-compose ps postgres

# Ver logs de PostgreSQL
docker-compose logs postgres

# Verificar conectividad
docker exec veterinaria-backend ping postgres
```

### El frontend no carga

```bash
# Verificar que el build fue exitoso
docker-compose logs frontend

# Reconstruir el frontend
docker-compose build --no-cache frontend
docker-compose up -d frontend
```

### Limpiar y empezar de nuevo

```bash
# Eliminar contenedores, redes y volúmenes
docker-compose down -v

# Eliminar imágenes del proyecto
docker rmi $(docker images -q 'clinicaveterinariahumboldt*')

# Reconstruir todo
docker-compose up -d --build
```

### Verificar uso de recursos

```bash
docker stats
```

## 🏗️ Desarrollo

### Construir imágenes individualmente

```bash
# Backend
docker build -f Dockerfile.backend -t veterinaria-backend .

# Frontend
docker build -f frontend/Dockerfile.prod -t veterinaria-frontend .
```

### Ejecutar solo la base de datos (para desarrollo local)

```bash
docker-compose up -d postgres
```

Luego ejecuta el backend y frontend localmente conectando a `localhost:5432`.

## 📊 Monitoreo

### Health Checks

Todos los servicios tienen health checks configurados:

```bash
# Ver estado de salud
docker inspect --format='{{.State.Health.Status}}' veterinaria-backend
docker inspect --format='{{.State.Health.Status}}' veterinaria-frontend
docker inspect --format='{{.State.Health.Status}}' veterinaria-db
```

### Endpoints de salud

- Backend: http://localhost:8080/api/actuator/health
- Frontend: http://localhost:3000/health

## 🔒 Seguridad

⚠️ **Para producción, asegúrate de:**

1. Cambiar todas las contraseñas por defecto
2. Usar secretos seguros para JWT
3. Configurar HTTPS con certificados SSL
4. Limitar el acceso a puertos de la base de datos
5. Usar Docker secrets para credenciales sensibles

## 📝 Arquitectura

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│                 │     │                 │     │                 │
│    Frontend     │────▶│    Backend      │────▶│   PostgreSQL    │
│   (Nginx:80)    │     │  (Spring:8080)  │     │    (:5432)      │
│                 │     │                 │     │                 │
└─────────────────┘     └─────────────────┘     └─────────────────┘
        │                       │                       │
        └───────────────────────┴───────────────────────┘
                    veterinaria-network
```

---

💡 **Tip:** Para desarrollo, considera usar `docker-compose.override.yml` para configuraciones locales sin modificar el archivo principal.
