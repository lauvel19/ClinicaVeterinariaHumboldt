# 📋 Guía de Migración del Backend - Sistema Veterinaria Humboldt

## 🎯 Objetivo
Migrar el backend completo del repositorio actual a un nuevo repositorio con manejo de ramas Git, dividiendo el trabajo entre 4 personas de forma organizada y sin conflictos.

---

## 📊 Estructura Completa del Backend

```
veterinaria-backend/
├── pom.xml                                    # ⚠️ TODOS deben revisar
├── src/
│   ├── main/
│   │   ├── java/com/tuorg/veterinaria/
│   │   │   ├── VeterinariaApplication.java   # ⚠️ TODOS deben revisar
│   │   │   ├── common/                        # 👤 PERSONA 1
│   │   │   ├── config/                        # 👤 PERSONA 1
│   │   │   ├── gestionusuarios/              # 👤 PERSONA 2
│   │   │   ├── gestionpacientes/             # 👤 PERSONA 3
│   │   │   ├── prestacioneservicios/         # 👤 PERSONA 3
│   │   │   ├── gestioninventario/            # 👤 PERSONA 4
│   │   │   ├── notificaciones/                # 👤 PERSONA 4
│   │   │   ├── reportes/                     # 👤 PERSONA 4
│   │   │   └── configuracion/                # 👤 PERSONA 4
│   │   └── resources/
│   │       ├── application.yml                # 👤 PERSONA 1
│   │       └── db/migration/                  # 👤 PERSONA 1
│   └── test/                                  # Cada persona sus tests
└── init-scripts/                              # 👤 PERSONA 1
```

---

## 👥 Asignación de Trabajo por Persona

### 👤 PERSONA 1: Base e Infraestructura
**Responsabilidad:** Configuración base, seguridad, utilidades comunes y base de datos.

#### Archivos a Migrar:

**1. Configuración del Proyecto:**
- `pom.xml` (⚠️ Coordinar con todos antes de merge)
- `.gitignore` (si existe)
- `README.md` (actualizar con nueva estructura)

**2. Clase Principal:**
- `src/main/java/com/tuorg/veterinaria/VeterinariaApplication.java`

**3. Módulo Common (Utilidades Comunes):**
- `src/main/java/com/tuorg/veterinaria/common/constants/AppConstants.java`
- `src/main/java/com/tuorg/veterinaria/common/dto/ApiResponse.java`
- `src/main/java/com/tuorg/veterinaria/common/exception/BusinessException.java`
- `src/main/java/com/tuorg/veterinaria/common/exception/GlobalExceptionHandler.java`
- `src/main/java/com/tuorg/veterinaria/common/exception/ResourceNotFoundException.java`
- `src/main/java/com/tuorg/veterinaria/common/util/ValidationUtil.java`

**4. Módulo Config (Configuración y Seguridad):**
- `src/main/java/com/tuorg/veterinaria/config/SecurityConfig.java`
- `src/main/java/com/tuorg/veterinaria/config/SwaggerConfig.java`
- `src/main/java/com/tuorg/veterinaria/config/JacksonConfig.java`
- `src/main/java/com/tuorg/veterinaria/config/UsuarioDemoInitializer.java`
- `src/main/java/com/tuorg/veterinaria/config/security/JwtAuthenticationEntryPoint.java`
- `src/main/java/com/tuorg/veterinaria/config/security/JwtAuthenticationFilter.java`
- `src/main/java/com/tuorg/veterinaria/config/security/JwtTokenProvider.java`

**5. Recursos y Configuración:**
- `src/main/resources/application.yml`
- `src/main/resources/db/migration/V1__init_schema.sql`
- `src/main/resources/db/migration/V2__add_password_reset_fields.sql`

**6. Scripts de Inicialización:**
- `init-scripts/crear-secretario-demo.sql`
- `init-scripts/crear-veterinario-demo.sql`
- `init-scripts/verificar-secretario.sql`
- `init-scripts/verificar-usuario.sql`

**7. Tests de Configuración:**
- `src/test/java/com/tuorg/veterinaria/config/AbstractIntegrationTest.java`
- `src/test/java/com/tuorg/veterinaria/config/TestConfig.java`

**Orden de Trabajo:**
1. Crear rama: `feature/infrastructure-base`
2. Subir `pom.xml` y estructura base
3. Subir módulo `common/`
4. Subir módulo `config/` y seguridad
5. Subir recursos (`application.yml`, migraciones SQL)
6. Subir scripts de inicialización
7. Crear PR para revisión

---

### 👤 PERSONA 2: Gestión de Usuarios y Autenticación
**Responsabilidad:** Módulo completo de usuarios, autenticación, roles y permisos.

#### Archivos a Migrar:

**1. Modelos (Entidades JPA):**
- `src/main/java/com/tuorg/veterinaria/gestionusuarios/model/Persona.java`
- `src/main/java/com/tuorg/veterinaria/gestionusuarios/model/Usuario.java`
- `src/main/java/com/tuorg/veterinaria/gestionusuarios/model/Cliente.java`
- `src/main/java/com/tuorg/veterinaria/gestionusuarios/model/UsuarioVeterinario.java`
- `src/main/java/com/tuorg/veterinaria/gestionusuarios/model/Secretario.java`
- `src/main/java/com/tuorg/veterinaria/gestionusuarios/model/Rol.java`
- `src/main/java/com/tuorg/veterinaria/gestionusuarios/model/Permiso.java`
- `src/main/java/com/tuorg/veterinaria/gestionusuarios/model/HistorialAccion.java`

**2. Repositorios:**
- `src/main/java/com/tuorg/veterinaria/gestionusuarios/repository/UsuarioRepository.java`
- `src/main/java/com/tuorg/veterinaria/gestionusuarios/repository/ClienteRepository.java`
- `src/main/java/com/tuorg/veterinaria/gestionusuarios/repository/UsuarioVeterinarioRepository.java`
- `src/main/java/com/tuorg/veterinaria/gestionusuarios/repository/RolRepository.java`
- `src/main/java/com/tuorg/veterinaria/gestionusuarios/repository/HistorialAccionRepository.java`

**3. Servicios:**
- `src/main/java/com/tuorg/veterinaria/gestionusuarios/service/AuthService.java`
- `src/main/java/com/tuorg/veterinaria/gestionusuarios/service/UsuarioService.java`
- `src/main/java/com/tuorg/veterinaria/gestionusuarios/service/ClienteService.java`
- `src/main/java/com/tuorg/veterinaria/gestionusuarios/service/CustomUserDetailsService.java`

**4. Controladores:**
- `src/main/java/com/tuorg/veterinaria/gestionusuarios/controller/AuthController.java`
- `src/main/java/com/tuorg/veterinaria/gestionusuarios/controller/UsuarioController.java`
- `src/main/java/com/tuorg/veterinaria/gestionusuarios/controller/ClienteController.java`

**5. DTOs (Data Transfer Objects):**
- `src/main/java/com/tuorg/veterinaria/gestionusuarios/dto/LoginRequest.java`
- `src/main/java/com/tuorg/veterinaria/gestionusuarios/dto/LoginResponse.java`
- `src/main/java/com/tuorg/veterinaria/gestionusuarios/dto/RegisterRequest.java`
- `src/main/java/com/tuorg/veterinaria/gestionusuarios/dto/ForgotPasswordRequest.java`
- `src/main/java/com/tuorg/veterinaria/gestionusuarios/dto/ResetPasswordRequest.java`
- `src/main/java/com/tuorg/veterinaria/gestionusuarios/dto/UsuarioRequest.java`
- `src/main/java/com/tuorg/veterinaria/gestionusuarios/dto/UsuarioResponse.java`
- `src/main/java/com/tuorg/veterinaria/gestionusuarios/dto/UsuarioUpdateRequest.java`
- `src/main/java/com/tuorg/veterinaria/gestionusuarios/dto/ClienteRequest.java`
- `src/main/java/com/tuorg/veterinaria/gestionusuarios/dto/ClienteResponse.java`
- `src/main/java/com/tuorg/veterinaria/gestionusuarios/dto/ClienteUpdateRequest.java`
- `src/main/java/com/tuorg/veterinaria/gestionusuarios/dto/RolResponse.java`
- `src/main/java/com/tuorg/veterinaria/gestionusuarios/dto/PermisoResponse.java`

**6. Tests:**
- `src/test/java/com/tuorg/veterinaria/gestionusuarios/service/AuthServiceTest.java`
- `src/test/java/com/tuorg/veterinaria/gestionusuarios/service/UsuarioServiceTest.java`
- `src/test/java/com/tuorg/veterinaria/gestionusuarios/service/ClienteServiceTest.java`
- `src/test/java/com/tuorg/veterinaria/gestionusuarios/controller/AuthControllerIntegrationTest.java`

**Orden de Trabajo:**
1. Crear rama: `feature/gestion-usuarios`
2. Esperar a que PERSONA 1 complete la base (common + config)
3. Subir modelos (model/)
4. Subir repositorios (repository/)
5. Subir servicios (service/)
6. Subir controladores (controller/)
7. Subir DTOs (dto/)
8. Subir tests
9. Crear PR para revisión

---

### 👤 PERSONA 3: Gestión de Pacientes y Servicios
**Responsabilidad:** Pacientes, historias clínicas, citas, servicios prestados y facturación.

#### Archivos a Migrar:

**A. Módulo gestionpacientes:**

**1. Modelos:**
- `src/main/java/com/tuorg/veterinaria/gestionpacientes/model/Paciente.java`
- `src/main/java/com/tuorg/veterinaria/gestionpacientes/model/HistoriaClinica.java`
- `src/main/java/com/tuorg/veterinaria/gestionpacientes/model/RegistroMedico.java`
- `src/main/java/com/tuorg/veterinaria/gestionpacientes/model/Vacunacion.java`
- `src/main/java/com/tuorg/veterinaria/gestionpacientes/model/Desparasitacion.java`

**2. Repositorios:**
- `src/main/java/com/tuorg/veterinaria/gestionpacientes/repository/PacienteRepository.java`
- `src/main/java/com/tuorg/veterinaria/gestionpacientes/repository/HistoriaClinicaRepository.java`
- `src/main/java/com/tuorg/veterinaria/gestionpacientes/repository/RegistroMedicoRepository.java`
- `src/main/java/com/tuorg/veterinaria/gestionpacientes/repository/VacunacionRepository.java`
- `src/main/java/com/tuorg/veterinaria/gestionpacientes/repository/DesparasitacionRepository.java`

**3. Servicios:**
- `src/main/java/com/tuorg/veterinaria/gestionpacientes/service/PacienteService.java`
- `src/main/java/com/tuorg/veterinaria/gestionpacientes/service/HistoriaClinicaService.java`
- `src/main/java/com/tuorg/veterinaria/gestionpacientes/service/VacunacionService.java`
- `src/main/java/com/tuorg/veterinaria/gestionpacientes/service/DesparasitacionService.java`

**4. Controladores:**
- `src/main/java/com/tuorg/veterinaria/gestionpacientes/controller/PacienteController.java`
- `src/main/java/com/tuorg/veterinaria/gestionpacientes/controller/HistoriaClinicaController.java`
- `src/main/java/com/tuorg/veterinaria/gestionpacientes/controller/VacunacionController.java`
- `src/main/java/com/tuorg/veterinaria/gestionpacientes/controller/DesparasitacionController.java`

**5. DTOs:**
- `src/main/java/com/tuorg/veterinaria/gestionpacientes/dto/PacienteRequest.java`
- `src/main/java/com/tuorg/veterinaria/gestionpacientes/dto/PacienteResponse.java`
- `src/main/java/com/tuorg/veterinaria/gestionpacientes/dto/PacienteUpdateRequest.java`
- `src/main/java/com/tuorg/veterinaria/gestionpacientes/dto/PacienteOwnerResponse.java`
- `src/main/java/com/tuorg/veterinaria/gestionpacientes/dto/HistoriaClinicaResponse.java`
- `src/main/java/com/tuorg/veterinaria/gestionpacientes/dto/RegistroMedicoRequest.java`
- `src/main/java/com/tuorg/veterinaria/gestionpacientes/dto/RegistroMedicoResponse.java`
- `src/main/java/com/tuorg/veterinaria/gestionpacientes/dto/VacunacionRequest.java`
- `src/main/java/com/tuorg/veterinaria/gestionpacientes/dto/VacunacionResponse.java`
- `src/main/java/com/tuorg/veterinaria/gestionpacientes/dto/ProgramarProximaDosisRequest.java`
- `src/main/java/com/tuorg/veterinaria/gestionpacientes/dto/DesparasitacionRequest.java`
- `src/main/java/com/tuorg/veterinaria/gestionpacientes/dto/DesparasitacionResponse.java`

**6. Tests:**
- `src/test/java/com/tuorg/veterinaria/gestionpacientes/service/PacienteServiceTest.java`
- `src/test/java/com/tuorg/veterinaria/gestionpacientes/service/VacunacionServiceTest.java`

**B. Módulo prestacioneservicios:**

**1. Modelos:**
- `src/main/java/com/tuorg/veterinaria/prestacioneservicios/model/Cita.java`
- `src/main/java/com/tuorg/veterinaria/prestacioneservicios/model/Servicio.java`
- `src/main/java/com/tuorg/veterinaria/prestacioneservicios/model/ServicioPrestado.java`
- `src/main/java/com/tuorg/veterinaria/prestacioneservicios/model/Factura.java`

**2. Repositorios:**
- `src/main/java/com/tuorg/veterinaria/prestacioneservicios/repository/CitaRepository.java`
- `src/main/java/com/tuorg/veterinaria/prestacioneservicios/repository/ServicioRepository.java`
- `src/main/java/com/tuorg/veterinaria/prestacioneservicios/repository/ServicioPrestadoRepository.java`
- `src/main/java/com/tuorg/veterinaria/prestacioneservicios/repository/FacturaRepository.java`

**3. Servicios:**
- `src/main/java/com/tuorg/veterinaria/prestacioneservicios/service/CitaService.java`
- `src/main/java/com/tuorg/veterinaria/prestacioneservicios/service/ServicioPrestadoService.java`
- `src/main/java/com/tuorg/veterinaria/prestacioneservicios/service/FacturaService.java`

**4. Controladores:**
- `src/main/java/com/tuorg/veterinaria/prestacioneservicios/controller/CitaController.java`
- `src/main/java/com/tuorg/veterinaria/prestacioneservicios/controller/ServicioPrestadoController.java`
- `src/main/java/com/tuorg/veterinaria/prestacioneservicios/controller/FacturaController.java`

**5. DTOs:**
- `src/main/java/com/tuorg/veterinaria/prestacioneservicios/dto/CitaRequest.java`
- `src/main/java/com/tuorg/veterinaria/prestacioneservicios/dto/CitaResponse.java`
- `src/main/java/com/tuorg/veterinaria/prestacioneservicios/dto/CitaReprogramarRequest.java`
- `src/main/java/com/tuorg/veterinaria/prestacioneservicios/dto/CitaCancelarRequest.java`
- `src/main/java/com/tuorg/veterinaria/prestacioneservicios/dto/ServicioPrestadoRequest.java`
- `src/main/java/com/tuorg/veterinaria/prestacioneservicios/dto/ServicioPrestadoResponse.java`
- `src/main/java/com/tuorg/veterinaria/prestacioneservicios/dto/ServicioPrestadoInsumoRequest.java`
- `src/main/java/com/tuorg/veterinaria/prestacioneservicios/dto/FacturaRequest.java`
- `src/main/java/com/tuorg/veterinaria/prestacioneservicios/dto/FacturaResponse.java`
- `src/main/java/com/tuorg/veterinaria/prestacioneservicios/dto/FacturaPagoRequest.java`

**6. Tests:**
- `src/test/java/com/tuorg/veterinaria/prestacioneservicios/service/CitaServiceTest.java`
- `src/test/java/com/tuorg/veterinaria/prestacioneservicios/service/FacturaServiceTest.java`

**Orden de Trabajo:**
1. Crear rama: `feature/gestion-pacientes-servicios`
2. Esperar a que PERSONA 1 y PERSONA 2 completen (necesitas usuarios y base)
3. Subir módulo `gestionpacientes/` completo (model → repository → service → controller → dto)
4. Subir módulo `prestacioneservicios/` completo
5. Subir tests de ambos módulos
6. Crear PR para revisión

---

### 👤 PERSONA 4: Inventario, Notificaciones y Reportes
**Responsabilidad:** Gestión de inventario, sistema de notificaciones, reportes y configuración del sistema.

#### Archivos a Migrar:

**A. Módulo gestioninventario:**

**1. Modelos:**
- `src/main/java/com/tuorg/veterinaria/gestioninventario/model/Producto.java`
- `src/main/java/com/tuorg/veterinaria/gestioninventario/model/Proveedor.java`
- `src/main/java/com/tuorg/veterinaria/gestioninventario/model/MovimientoInventario.java`
- `src/main/java/com/tuorg/veterinaria/gestioninventario/model/Lote.java`
- `src/main/java/com/tuorg/veterinaria/gestioninventario/model/AlertaInventario.java`

**2. Repositorios:**
- `src/main/java/com/tuorg/veterinaria/gestioninventario/repository/ProductoRepository.java`
- `src/main/java/com/tuorg/veterinaria/gestioninventario/repository/ProveedorRepository.java`
- `src/main/java/com/tuorg/veterinaria/gestioninventario/repository/MovimientoInventarioRepository.java`
- `src/main/java/com/tuorg/veterinaria/gestioninventario/repository/AlertaInventarioRepository.java`

**3. Servicios:**
- `src/main/java/com/tuorg/veterinaria/gestioninventario/service/ProductoService.java`
- `src/main/java/com/tuorg/veterinaria/gestioninventario/service/ProveedorService.java`
- `src/main/java/com/tuorg/veterinaria/gestioninventario/service/MovimientoInventarioService.java`
- `src/main/java/com/tuorg/veterinaria/gestioninventario/service/AlertaInventarioService.java`

**4. Controladores:**
- `src/main/java/com/tuorg/veterinaria/gestioninventario/controller/ProductoController.java`
- `src/main/java/com/tuorg/veterinaria/gestioninventario/controller/ProveedorController.java`
- `src/main/java/com/tuorg/veterinaria/gestioninventario/controller/MovimientoInventarioController.java`

**5. DTOs:**
- `src/main/java/com/tuorg/veterinaria/gestioninventario/dto/ProductoRequest.java`
- `src/main/java/com/tuorg/veterinaria/gestioninventario/dto/ProductoResponse.java`
- `src/main/java/com/tuorg/veterinaria/gestioninventario/dto/ProductoUpdateRequest.java`
- `src/main/java/com/tuorg/veterinaria/gestioninventario/dto/ProveedorRequest.java`
- `src/main/java/com/tuorg/veterinaria/gestioninventario/dto/ProveedorResponse.java`
- `src/main/java/com/tuorg/veterinaria/gestioninventario/dto/MovimientoEntradaRequest.java`
- `src/main/java/com/tuorg/veterinaria/gestioninventario/dto/MovimientoSalidaRequest.java`
- `src/main/java/com/tuorg/veterinaria/gestioninventario/dto/MovimientoInventarioResponse.java`

**6. Tests:**
- `src/test/java/com/tuorg/veterinaria/gestioninventario/service/ProductoServiceTest.java`
- `src/test/java/com/tuorg/veterinaria/gestioninventario/service/MovimientoInventarioServiceTest.java`

**B. Módulo notificaciones:**

**1. Modelos:**
- `src/main/java/com/tuorg/veterinaria/notificaciones/model/Notificacion.java`
- `src/main/java/com/tuorg/veterinaria/notificaciones/model/CanalEnvio.java`
- `src/main/java/com/tuorg/veterinaria/notificaciones/model/CanalEmail.java`
- `src/main/java/com/tuorg/veterinaria/notificaciones/model/CanalSMS.java`
- `src/main/java/com/tuorg/veterinaria/notificaciones/model/CanalApp.java`
- `src/main/java/com/tuorg/veterinaria/notificaciones/model/PlantillaMensaje.java`

**2. Repositorios:**
- `src/main/java/com/tuorg/veterinaria/notificaciones/repository/NotificacionRepository.java`
- `src/main/java/com/tuorg/veterinaria/notificaciones/repository/CanalEnvioRepository.java`

**3. Servicios:**
- `src/main/java/com/tuorg/veterinaria/notificaciones/service/NotificacionService.java`

**4. Controladores:**
- `src/main/java/com/tuorg/veterinaria/notificaciones/controller/NotificacionController.java`

**5. DTOs:**
- `src/main/java/com/tuorg/veterinaria/notificaciones/dto/NotificacionResponse.java`
- `src/main/java/com/tuorg/veterinaria/notificaciones/dto/NotificacionEnviarRequest.java`
- `src/main/java/com/tuorg/veterinaria/notificaciones/dto/NotificacionProgramarRequest.java`

**C. Módulo reportes:**

**1. Modelos:**
- `src/main/java/com/tuorg/veterinaria/reportes/model/Reporte.java`
- `src/main/java/com/tuorg/veterinaria/reportes/model/Indicador.java`
- `src/main/java/com/tuorg/veterinaria/reportes/model/Estadistica.java`

**2. Repositorios:**
- `src/main/java/com/tuorg/veterinaria/reportes/repository/ReporteRepository.java`
- `src/main/java/com/tuorg/veterinaria/reportes/repository/IndicadorRepository.java`
- `src/main/java/com/tuorg/veterinaria/reportes/repository/EstadisticaRepository.java`

**3. Servicios:**
- `src/main/java/com/tuorg/veterinaria/reportes/service/ReporteService.java`
- `src/main/java/com/tuorg/veterinaria/reportes/service/IndicadorService.java`
- `src/main/java/com/tuorg/veterinaria/reportes/service/EstadisticaService.java`

**4. Controladores:**
- `src/main/java/com/tuorg/veterinaria/reportes/controller/ReporteController.java`

**5. DTOs:**
- `src/main/java/com/tuorg/veterinaria/reportes/dto/ReporteRequest.java`
- `src/main/java/com/tuorg/veterinaria/reportes/dto/ReporteResponse.java`
- `src/main/java/com/tuorg/veterinaria/reportes/dto/EstadisticaResponse.java`

**D. Módulo configuracion:**

**1. Modelos:**
- `src/main/java/com/tuorg/veterinaria/configuracion/model/ParametroSistema.java`
- `src/main/java/com/tuorg/veterinaria/configuracion/model/LogSistema.java`
- `src/main/java/com/tuorg/veterinaria/configuracion/model/BackupSistema.java`

**2. Repositorios:**
- `src/main/java/com/tuorg/veterinaria/configuracion/repository/ParametroSistemaRepository.java`
- `src/main/java/com/tuorg/veterinaria/configuracion/repository/LogSistemaRepository.java`
- `src/main/java/com/tuorg/veterinaria/configuracion/repository/BackupSistemaRepository.java`

**3. Servicios:**
- `src/main/java/com/tuorg/veterinaria/configuracion/service/ConfigService.java`
- `src/main/java/com/tuorg/veterinaria/configuracion/service/LogSistemaService.java`

**4. Controladores:**
- `src/main/java/com/tuorg/veterinaria/configuracion/controller/ConfiguracionController.java`

**Orden de Trabajo:**
1. Crear rama: `feature/inventario-notificaciones-reportes`
2. Esperar a que PERSONA 1 complete la base
3. Subir módulo `gestioninventario/` completo
4. Subir módulo `notificaciones/` completo
5. Subir módulo `reportes/` completo
6. Subir módulo `configuracion/` completo
7. Subir tests
8. Crear PR para revisión

---

## 🔄 Flujo de Trabajo Recomendado

### Fase 1: Preparación (Todos)
1. Crear nuevo repositorio en GitHub/GitLab
2. Clonar repositorio nuevo
3. Crear rama `main` o `master`
4. PERSONA 1 crea estructura base de carpetas

### Fase 2: Base e Infraestructura (PERSONA 1)
1. Crear rama `feature/infrastructure-base`
2. Subir `pom.xml` (coordinado con todos)
3. Subir módulo `common/`
4. Subir módulo `config/` y seguridad
5. Subir recursos y migraciones SQL
6. Hacer commit y push
7. Crear Pull Request → Merge a `main`

### Fase 3: Gestión de Usuarios (PERSONA 2)
1. Hacer `git pull` de `main` (actualizado por PERSONA 1)
2. Crear rama `feature/gestion-usuarios`
3. Subir módulo `gestionusuarios/` completo
4. Hacer commit y push
5. Crear Pull Request → Merge a `main`

### Fase 4: Pacientes y Servicios (PERSONA 3)
1. Hacer `git pull` de `main` (actualizado por PERSONA 1 y 2)
2. Crear rama `feature/gestion-pacientes-servicios`
3. Subir módulos `gestionpacientes/` y `prestacioneservicios/`
4. Hacer commit y push
5. Crear Pull Request → Merge a `main`

### Fase 5: Inventario, Notificaciones y Reportes (PERSONA 4)
1. Hacer `git pull` de `main` (actualizado por todos)
2. Crear rama `feature/inventario-notificaciones-reportes`
3. Subir módulos `gestioninventario/`, `notificaciones/`, `reportes/`, `configuracion/`
4. Hacer commit y push
5. Crear Pull Request → Merge a `main`

### Fase 6: Validación Final (Todos)
1. Todos hacen `git pull` de `main`
2. Compilar proyecto: `mvn clean install`
3. Ejecutar tests: `mvn test`
4. Verificar que la aplicación inicia: `mvn spring-boot:run`
5. Si hay errores, crear issues y asignar a la persona correspondiente

---

## ⚠️ Consideraciones Importantes

### Dependencias entre Módulos:
- **PERSONA 2** necesita que PERSONA 1 termine (usa `common/` y `config/`)
- **PERSONA 3** necesita que PERSONA 1 y 2 terminen (usa usuarios y base)
- **PERSONA 4** necesita que PERSONA 1 termine (usa base)

### Archivos Compartidos:
- `pom.xml`: PERSONA 1 lo sube primero, pero todos deben revisar antes del merge
- `VeterinariaApplication.java`: PERSONA 1 lo sube, pero puede necesitar ajustes si otros módulos requieren configuración adicional

### Conflictos Potenciales:
- Si dos personas modifican el mismo archivo, Git mostrará conflictos
- **Solución:** Coordinar antes de trabajar en archivos compartidos
- Usar Pull Requests para revisar cambios antes de merge

### Buenas Prácticas:
1. **Siempre hacer `git pull` antes de empezar** a trabajar en tu rama
2. **Commit frecuente** con mensajes descriptivos
3. **No hacer merge directo a `main`**, siempre usar Pull Requests
4. **Revisar PRs de compañeros** antes de aprobar
5. **Comunicar cambios importantes** en archivos compartidos

---

## 📝 Checklist de Migración por Persona

### PERSONA 1 ✅
- [ ] `pom.xml`
- [ ] `VeterinariaApplication.java`
- [ ] Módulo `common/` completo
- [ ] Módulo `config/` completo
- [ ] `application.yml`
- [ ] Migraciones SQL (V1 y V2)
- [ ] Scripts de inicialización
- [ ] Tests de configuración
- [ ] PR creado y aprobado

### PERSONA 2 ✅
- [ ] Modelos de usuarios (8 archivos)
- [ ] Repositorios de usuarios (5 archivos)
- [ ] Servicios de usuarios (4 archivos)
- [ ] Controladores de usuarios (3 archivos)
- [ ] DTOs de usuarios (13 archivos)
- [ ] Tests de usuarios (4 archivos)
- [ ] PR creado y aprobado

### PERSONA 3 ✅
- [ ] Módulo `gestionpacientes/` completo
- [ ] Módulo `prestacioneservicios/` completo
- [ ] Tests de ambos módulos
- [ ] PR creado y aprobado

### PERSONA 4 ✅
- [ ] Módulo `gestioninventario/` completo
- [ ] Módulo `notificaciones/` completo
- [ ] Módulo `reportes/` completo
- [ ] Módulo `configuracion/` completo
- [ ] Tests correspondientes
- [ ] PR creado y aprobado

---

## 🚀 Comandos Git Útiles

```bash
# Clonar nuevo repositorio
git clone <url-nuevo-repo>
cd <nombre-repo>

# Crear y cambiar a nueva rama
git checkout -b feature/infrastructure-base

# Agregar archivos
git add src/main/java/com/tuorg/veterinaria/common/
git add pom.xml

# Commit
git commit -m "feat: agregar módulo common y configuración base"

# Push a remoto
git push origin feature/infrastructure-base

# Actualizar rama local con cambios de main
git checkout main
git pull origin main
git checkout feature/mi-rama
git merge main  # o git rebase main
```

---

## 📞 Contacto y Coordinación

**Recomendación:** Crear un grupo de WhatsApp/Telegram o canal de Discord para:
- Avisar cuando termines tu parte
- Reportar problemas o conflictos
- Coordinar cambios en archivos compartidos
- Pedir ayuda si te quedas bloqueado

---

**¡Éxito en la migración! 🎉**

