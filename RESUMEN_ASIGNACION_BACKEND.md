# 📦 Resumen de Asignación - Backend Veterinaria

## Estructura del Backend

```
src/main/java/com/tuorg/veterinaria/
├── VeterinariaApplication.java          [PERSONA 1]
├── common/                              [PERSONA 1]
│   ├── constants/AppConstants.java
│   ├── dto/ApiResponse.java
│   ├── exception/
│   └── util/
├── config/                              [PERSONA 1]
│   ├── SecurityConfig.java
│   ├── SwaggerConfig.java
│   └── security/
├── gestionusuarios/                     [PERSONA 2]
│   ├── model/ (8 archivos)
│   ├── repository/ (5 archivos)
│   ├── service/ (4 archivos)
│   ├── controller/ (3 archivos)
│   └── dto/ (13 archivos)
├── gestionpacientes/                    [PERSONA 3]
│   ├── model/ (5 archivos)
│   ├── repository/ (5 archivos)
│   ├── service/ (4 archivos)
│   ├── controller/ (4 archivos)
│   └── dto/ (11 archivos)
├── prestacioneservicios/                [PERSONA 3]
│   ├── model/ (4 archivos)
│   ├── repository/ (4 archivos)
│   ├── service/ (3 archivos)
│   ├── controller/ (3 archivos)
│   └── dto/ (10 archivos)
├── gestioninventario/                   [PERSONA 4]
│   ├── model/ (5 archivos)
│   ├── repository/ (4 archivos)
│   ├── service/ (4 archivos)
│   ├── controller/ (3 archivos)
│   └── dto/ (8 archivos)
├── notificaciones/                      [PERSONA 4]
│   ├── model/ (6 archivos)
│   ├── repository/ (2 archivos)
│   ├── service/ (1 archivo)
│   ├── controller/ (1 archivo)
│   └── dto/ (3 archivos)
├── reportes/                            [PERSONA 4]
│   ├── model/ (3 archivos)
│   ├── repository/ (3 archivos)
│   ├── service/ (3 archivos)
│   ├── controller/ (1 archivo)
│   └── dto/ (3 archivos)
└── configuracion/                       [PERSONA 4]
    ├── model/ (3 archivos)
    ├── repository/ (3 archivos)
    ├── service/ (2 archivos)
    └── controller/ (1 archivo)
```

---

## 👥 Asignación por Persona

### 👤 PERSONA 1: Base e Infraestructura
**Total: ~25 archivos**

**Archivos clave:**
- `pom.xml`
- `VeterinariaApplication.java`
- `src/main/resources/application.yml`
- `src/main/resources/db/migration/V1__init_schema.sql`
- `src/main/resources/db/migration/V2__add_password_reset_fields.sql`
- `init-scripts/*.sql` (4 archivos)
- `common/*` (6 archivos)
- `config/*` (7 archivos)
- `config/security/*` (3 archivos)

**Rama:** `feature/infrastructure-base`

---

### 👤 PERSONA 2: Gestión de Usuarios
**Total: ~33 archivos**

**Módulo completo:**
- `gestionusuarios/model/*` (8 archivos)
- `gestionusuarios/repository/*` (5 archivos)
- `gestionusuarios/service/*` (4 archivos)
- `gestionusuarios/controller/*` (3 archivos)
- `gestionusuarios/dto/*` (13 archivos)
- `gestionusuarios/**/*Test.java` (4 archivos)

**Rama:** `feature/gestion-usuarios`

**Depende de:** PERSONA 1 (necesita common y config)

---

### 👤 PERSONA 3: Pacientes y Servicios
**Total: ~50 archivos**

**Módulos:**
- `gestionpacientes/*` completo (~29 archivos)
- `prestacioneservicios/*` completo (~21 archivos)

**Rama:** `feature/gestion-pacientes-servicios`

**Depende de:** PERSONA 1 y PERSONA 2 (necesita usuarios y base)

---

### 👤 PERSONA 4: Inventario, Notificaciones y Reportes
**Total: ~50 archivos**

**Módulos:**
- `gestioninventario/*` completo (~24 archivos)
- `notificaciones/*` completo (~13 archivos)
- `reportes/*` completo (~13 archivos)
- `configuracion/*` completo (~9 archivos)

**Rama:** `feature/inventario-notificaciones-reportes`

**Depende de:** PERSONA 1 (necesita base)

---

## 📋 Orden de Ejecución

```
1. PERSONA 1 → Sube base e infraestructura
   ↓
2. PERSONA 2 → Sube gestión de usuarios
   ↓
3. PERSONA 3 → Sube pacientes y servicios
   ↓
4. PERSONA 4 → Sube inventario, notificaciones y reportes
```

---

## ⚡ Comandos Rápidos

```bash
# 1. Clonar nuevo repo
git clone <url-nuevo-repo>
cd <nombre-repo>

# 2. Crear tu rama
git checkout -b feature/tu-modulo

# 3. Copiar tus archivos (desde repo antiguo)
# Ejemplo para PERSONA 1:
cp -r ../VeterinariaHumboldt2/src/main/java/com/tuorg/veterinaria/common ./
cp -r ../VeterinariaHumboldt2/src/main/java/com/tuorg/veterinaria/config ./
cp ../VeterinariaHumboldt2/pom.xml ./

# 4. Agregar y commitear
git add .
git commit -m "feat: agregar módulo [nombre-modulo]"

# 5. Push
git push origin feature/tu-modulo

# 6. Crear Pull Request en GitHub/GitLab
```

---

## ✅ Checklist Rápido

### PERSONA 1
- [ ] pom.xml
- [ ] VeterinariaApplication.java
- [ ] common/
- [ ] config/
- [ ] application.yml
- [ ] Migraciones SQL
- [ ] Scripts init

### PERSONA 2
- [ ] gestionusuarios/ completo
- [ ] Tests de usuarios

### PERSONA 3
- [ ] gestionpacientes/ completo
- [ ] prestacioneservicios/ completo
- [ ] Tests correspondientes

### PERSONA 4
- [ ] gestioninventario/ completo
- [ ] notificaciones/ completo
- [ ] reportes/ completo
- [ ] configuracion/ completo
- [ ] Tests correspondientes

---

**Ver guía completa en:** `GUIA_MIGRACION_BACKEND.md`

