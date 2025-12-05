# 🔐 Credenciales de Acceso - Clínica Veterinaria Humboldt

## URLs de Producción (Render)

| Servicio | URL |
|----------|-----|
| **Frontend** | https://veterinaria-frontend-kru7.onrender.com |
| **Backend API** | https://veterinaria-backend-lr87.onrender.com/api |
| **Health Check** | https://veterinaria-backend-lr87.onrender.com/api/health |

---

## 👤 Usuarios de Prueba

Las siguientes credenciales están disponibles para probar el sistema:

### Administradores

| Usuario | Contraseña | Rol | Descripción |
|---------|------------|-----|-------------|
| `admin` | `Admin123!` | ADMIN | Administrador principal del sistema |
| `superadmin` | `password` | ADMIN | Super administrador (contraseña simple para testing) |

### Personal

| Usuario | Contraseña | Rol | Descripción |
|---------|------------|-----|-------------|
| `vet_carlos` | `Test1234!` | VETERINARIO | Dr. Carlos Méndez - Medicina General |
| `sec_laura` | `Test1234!` | SECRETARIO | Laura Gómez - Recepción |

### Clientes

| Usuario | Contraseña | Rol | Descripción |
|---------|------------|-----|-------------|
| `cliente_diego` | `Test1234!` | CLIENTE | Diego López - Cliente de prueba |

---

## ⚠️ Notas Importantes

1. **Plan Free de Render**: El backend puede tardar ~30-60 segundos en "despertar" si ha estado inactivo.

2. **Contraseñas BCrypt**: Todas las contraseñas están hasheadas con BCrypt (10 rounds).

3. **Primer Login**: Si el login falla inicialmente, espera unos segundos a que el backend termine de iniciar.

4. **Base de Datos**: Los usuarios se crean automáticamente mediante migraciones Flyway al iniciar el backend.

---

## 🛠️ Regenerar Hashes BCrypt

Si necesitas generar nuevos hashes BCrypt, puedes usar:

```bash
mvn compile exec:java "-Dexec.mainClass=com.tuorg.veterinaria.util.PasswordHashGenerator"
```

---

## 📝 Verificar Estado del Sistema

1. **Health Check**: Visita `/api/health` para verificar que el backend está activo.

2. **Logs**: En Render Dashboard, revisa los logs del servicio backend.

3. **Base de Datos**: Verifica que las migraciones Flyway se ejecutaron correctamente.
