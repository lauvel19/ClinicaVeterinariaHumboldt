# Solución al Problema de Carga de Clientes

## 🔍 Problema Identificado
Los clientes no se cargan en la página `/admin/usuarios` (Gestión de Clientes) aunque existan en la base de datos.

## ✅ Cambios Realizados

### 1. **Logs Mejorados en ApiClient.ts**
Se agregaron logs detallados para todas las peticiones HTTP y respuestas:
- `📤 HTTP Request`: Muestra método, URL completa, y si hay token
- `✅ HTTP Response`: Muestra el estado, tipo de datos recibidos y claves del objeto
- `❌ HTTP Error`: Muestra detalles completos de errores

### 2. **Logs en ClientesRepository.ts**
Se agregaron logs específicos para rastrear la carga de clientes:
- Inicio de la petición
- Respuesta recibida del backend
- Datos procesados después de unwrapResponse

### 3. **Archivo .env.local Creado**
Se creó el archivo de variables de entorno con la URL correcta del backend:
```
VITE_API_URL=http://localhost:8080/api
```

### 4. **Corrección en ApiClient.ts**
Se corrigió el método de logout para usar `clearSession()` en lugar de `logout()`.

## 🧪 Pasos para Depurar

### 1. Verificar que el Backend esté Corriendo
```cmd
tasklist | findstr "java"
```
Deberías ver procesos `java.exe` ejecutándose.

### 2. Iniciar el Frontend
```cmd
cd C:\Users\laura\IdeaProjects\VeterinariaHumboldt2\frontend
npm run dev
```

### 3. Abrir el Navegador con DevTools
1. Abre `http://localhost:5173` (o el puerto que muestre Vite)
2. Presiona `F12` para abrir las DevTools
3. Ve a la pestaña **Console**

### 4. Iniciar Sesión como Admin
- Usuario: `admin` (o el que tengas configurado)
- Contraseña: la contraseña del admin

### 5. Navegar a Gestión de Clientes
Ve a `/admin/usuarios` o haz clic en "Clientes" en el menú lateral.

### 6. Revisar los Logs en la Consola
Deberías ver algo como esto:

#### ✅ **Si funciona correctamente:**
```
📤 HTTP Request: {method: "GET", url: "/clientes", fullURL: "http://localhost:8080/api/clientes", hasToken: true}
✅ HTTP Response: {method: "GET", url: "/clientes", status: 200, dataType: "object", hasData: true, dataKeys: ["success", "message", "data", "timestamp"]}
🔍 ClientesRepository.getAll() - Iniciando petición...
📦 ClientesRepository.getAll() - Respuesta recibida: {success: true, message: "Clientes obtenidos exitosamente", data: [...], timestamp: "..."}
✅ ClientesRepository.getAll() - Datos procesados: [...]
```

#### ❌ **Si hay un error:**

**Error 401 (No autenticado):**
```
❌ HTTP Error: {method: "GET", url: "/clientes", status: 401, statusText: "Unauthorized", ...}
```
**Solución**: El token JWT expiró o es inválido. Cierra sesión y vuelve a iniciar sesión.

**Error 403 (No autorizado):**
```
❌ HTTP Error: {method: "GET", url: "/clientes", status: 403, statusText: "Forbidden", ...}
```
**Solución**: El usuario no tiene permisos para ver clientes. Verifica que sea un usuario ADMIN.

**Error 404 (No encontrado):**
```
❌ HTTP Error: {method: "GET", url: "/clientes", status: 404, statusText: "Not Found", ...}
```
**Solución**: La URL del backend está mal configurada o el endpoint no existe.

**Error CORS:**
```
Access to XMLHttpRequest at 'http://localhost:8080/api/clientes' from origin 'http://localhost:5173' has been blocked by CORS policy
```
**Solución**: Verifica la configuración CORS en `SecurityConfig.java` del backend.

**Error de Red:**
```
❌ HTTP Error: {message: "Network Error", ...}
```
**Solución**: El backend no está corriendo. Inicia el backend con `start-backend.bat`.

## 🔧 Verificaciones Adicionales

### Verificar Clientes en la Base de Datos
Ejecuta esta consulta SQL para verificar que existan clientes:
```sql
SELECT c.*, u.correo, u.username 
FROM cliente c
INNER JOIN persona p ON c.id_persona = p.id_persona
INNER JOIN usuario u ON p.id_persona = u.id_persona
WHERE u.activo = TRUE;
```

### Verificar Token JWT
En la consola del navegador, ejecuta:
```javascript
console.log(JSON.parse(localStorage.getItem('auth-storage')));
```
Deberías ver algo como:
```json
{
  "state": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "nombre": "Ana",
      "apellido": "Ramírez",
      "correo": "admin@veterinaria.com",
      "rol": "ADMIN"
    }
  },
  "version": 0
}
```

### Verificar URL del Backend
En la consola del navegador, ejecuta:
```javascript
console.log(import.meta.env.VITE_API_URL);
```
Debería mostrar: `http://localhost:8080/api`

## 🚀 Próximos Pasos

Una vez que identifiques el error específico en la consola:

1. **Si es un error 401**: Reinicia sesión
2. **Si es un error 403**: Verifica los roles en la base de datos
3. **Si es un error CORS**: Revisa `SecurityConfig.java`
4. **Si es un error de red**: Verifica que el backend esté corriendo
5. **Si los datos vienen vacíos**: Verifica que haya clientes en la base de datos

## 📝 Información de Contacto
Si el problema persiste después de seguir estos pasos, copia los logs completos de la consola del navegador para un análisis más detallado.

