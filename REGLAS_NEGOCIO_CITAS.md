# 📅 Reglas de Negocio - Sistema de Citas

## 🎯 Objetivo
Garantizar que el sistema de agendamiento de citas funcione de manera lógica, eficiente y realista, siguiendo las mejores prácticas de una clínica veterinaria profesional.

---

## ✅ Validaciones Implementadas

### 1. **Validación de Fecha y Hora Pasada**
**Regla:** No se pueden agendar citas en fechas pasadas.

```
❌ NO PERMITIDO:
- Agendar cita para ayer
- Agendar cita para hace 2 horas

✅ PERMITIDO:
- Agendar cita para mañana
- Agendar cita para la próxima semana
```

**Mensaje de Error:**
> "No se puede programar una cita en una fecha pasada. Por favor, seleccione una fecha futura."

---

### 2. **Validación de Anticipación Mínima**
**Regla:** Las citas deben agendarse con al menos 2 horas de anticipación.

**Constante:** `ANTICIPACION_MINIMA_HORAS = 2`

```
❌ NO PERMITIDO:
- Agendar cita para dentro de 1 hora
- Agendar cita para dentro de 30 minutos

✅ PERMITIDO:
- Agendar cita para dentro de 3 horas
- Agendar cita para mañana
```

**Mensaje de Error:**
> "Debe agendar la cita con al menos 2 horas de anticipación. Por favor, seleccione una fecha y hora posterior."

**Justificación:** Permite a la clínica prepararse adecuadamente y al cliente organizarse.

---

### 3. **Validación de Anticipación Máxima**
**Regla:** Las citas no se pueden agendar con más de 90 días (3 meses) de anticipación.

**Constante:** `ANTICIPACION_MAXIMA_DIAS = 90`

```
❌ NO PERMITIDO:
- Agendar cita para dentro de 4 meses
- Agendar cita para dentro de 1 año

✅ PERMITIDO:
- Agendar cita para dentro de 1 mes
- Agendar cita para dentro de 2 meses
```

**Mensaje de Error:**
> "No se pueden agendar citas con más de 90 días de anticipación. Por favor, contacte directamente a la clínica para citas más lejanas."

**Justificación:** Evita que los usuarios bloqueen espacios muy adelante, mantiene flexibilidad en la agenda.

---

### 4. **Validación de Intervalos de Tiempo**
**Regla:** Las citas solo se pueden agendar en intervalos de 30 minutos.

**Constante:** `INTERVALO_CITAS_MINUTOS = 30`

```
❌ NO PERMITIDO:
- 08:15
- 09:45
- 10:23

✅ PERMITIDO:
- 08:00
- 08:30
- 09:00
- 09:30
- 10:00
```

**Mensaje de Error:**
> "Las citas solo se pueden agendar en intervalos de 30 minutos. Por favor, seleccione una hora como 8:00, 8:30, 9:00, 9:30, etc."

**Justificación:** Facilita la organización de la agenda y evita confusiones.

---

### 5. **Validación de Horario Laboral**
**Regla:** Las citas deben estar dentro del horario de atención de la clínica.

**Horario:**
- **Lunes a Viernes:** 8:00 AM - 12:00 PM y 2:00 PM - 6:00 PM
- **Sábados:** 8:00 AM - 12:00 PM
- **Domingos:** Cerrado

**Constantes:**
```
HORARIO_MANANA_INICIO = 8   (8:00 AM)
HORARIO_MANANA_FIN = 12      (12:00 PM)
HORARIO_TARDE_INICIO = 14    (2:00 PM)
HORARIO_TARDE_FIN = 18       (6:00 PM)
```

```
❌ NO PERMITIDO:
- Lunes 7:30 AM (antes de apertura)
- Lunes 1:00 PM (hora de almuerzo)
- Lunes 7:00 PM (después de cierre)
- Sábado 3:00 PM (sábados solo mañana)
- Domingo cualquier hora (cerrado)

✅ PERMITIDO:
- Lunes 9:00 AM
- Martes 3:30 PM
- Sábado 10:00 AM
```

**Mensajes de Error:**
> "No se pueden agendar citas los domingos. Horario de atención: Lunes a Viernes 8:00-12:00 y 14:00-18:00, Sábados 8:00-12:00"

> "Los sábados el horario de atención es de 8:00 AM a 12:00 PM"

> "La cita debe estar dentro del horario de atención: Lunes a Viernes de 8:00 AM a 12:00 PM y de 2:00 PM a 6:00 PM, Sábados de 8:00 AM a 12:00 PM"

---

### 6. **Validación de Citas Duplicadas por Paciente**
**Regla:** Un paciente no puede tener más de una cita programada en el mismo día.

```
❌ NO PERMITIDO:
- Paciente "Max" ya tiene cita el Lunes 10:00 AM
- Intentar agendar otra cita para "Max" el Lunes 3:00 PM

✅ PERMITIDO:
- Paciente "Max" tiene cita el Lunes
- Agendar otra cita para "Max" el Martes
```

**Mensaje de Error:**
> "El paciente ya tiene una cita programada para este día. Por favor, seleccione otra fecha o cancele la cita existente."

**Justificación:** Evita saturación del sistema y garantiza mejor atención por visita.

---

### 7. **Validación de Límite de Citas por Cliente por Día**
**Regla:** Un cliente no puede agendar más de 3 citas en total (para todos sus pacientes) en el mismo día.

**Constante:** `MAX_CITAS_POR_DIA_CLIENTE = 3`

```
EJEMPLO: Cliente Juan tiene 3 mascotas (Max, Luna, Rocky)

❌ NO PERMITIDO:
- Lunes: Cita para Max (10:00 AM) ✓
- Lunes: Cita para Luna (11:00 AM) ✓
- Lunes: Cita para Rocky (3:00 PM) ✓
- Lunes: Intentar agendar 4ta cita ✗

✅ PERMITIDO:
- Lunes: 3 citas
- Martes: Agendar la 4ta cita
```

**Mensaje de Error:**
> "Ha alcanzado el límite máximo de 3 citas por día. Por favor, contacte a la clínica si necesita más citas."

**Justificación:** Evita acaparamiento de espacios, permite equidad en el acceso.

---

### 8. **Validación de Disponibilidad del Veterinario**
**Regla:** Un veterinario no puede tener dos citas que se traslapen en tiempo.

**Duración de Cita:** `DURACION_CITA_MINUTOS = 30`

**Lógica de Traslape:**
Una cita nueva se traslapa con una existente si:
- La nueva cita inicia antes de que termine la existente
- **Y** la nueva cita termina después de que inicie la existente

```
EJEMPLO: Veterinario tiene cita de 10:00 a 10:30

❌ NO PERMITIDO (Traslapes):
- 09:45 (terminaría a 10:15, traslape de 10:00 a 10:15)
- 10:00 (exactamente el mismo horario)
- 10:15 (iniciaría a 10:15, traslape de 10:15 a 10:30)

✅ PERMITIDO (Sin Traslape):
- 09:30 (terminaría a 10:00, justo antes)
- 10:30 (iniciaría justo cuando termina la otra)
- 11:00 (después de la cita existente)
```

**Mensaje de Error:**
> "El veterinario ya tiene una cita programada en ese horario. Por favor, seleccione otra fecha y hora disponible."

**Justificación:** Evita doble-booking, garantiza atención de calidad sin apuros.

---

## 🔄 Validaciones Especiales para Reprogramación

Cuando se reprograma una cita, se aplican las mismas validaciones que para crear una cita nueva, **PERO** excluyendo la cita que se está reprogramando de las verificaciones de disponibilidad y duplicados.

**Ejemplo:**
```
- Cita actual: Lunes 10:00 AM
- Reprogramar a: Lunes 3:00 PM

✅ Válido: Mismo día pero diferente hora (no se considera duplicado)
```

---

## 📊 Flujo de Validación

```
1. ¿Fecha en el pasado? → ❌ Rechazar
2. ¿Menos de 2 horas de anticipación? → ❌ Rechazar
3. ¿Más de 90 días de anticipación? → ❌ Rechazar
4. ¿Minutos no son 00 o 30? → ❌ Rechazar
5. ¿Fuera de horario laboral? → ❌ Rechazar
6. ¿Paciente ya tiene cita ese día? → ❌ Rechazar
7. ¿Cliente excede 3 citas ese día? → ❌ Rechazar
8. ¿Veterinario no disponible (traslape)? → ❌ Rechazar
9. Todas las validaciones pasan → ✅ Aceptar y crear cita
```

---

## 🔍 Endpoints Afectados

### **POST /api/citas**
Crea una nueva cita aplicando todas las validaciones.

### **PUT /api/citas/{id}/reprogramar**
Reprograma una cita existente aplicando todas las validaciones (excluyendo la cita actual).

### **GET /api/citas/veterinario/{id}/disponibilidad**
Verifica si un veterinario está disponible en una fecha/hora específica.

---

## 📝 Casos de Uso Validados

### ✅ **Caso 1: Agendar Cita Normal**
```
Cliente: María
Paciente: Max (perro)
Veterinario: Dr. Carlos
Fecha: Mañana a las 10:00 AM

Validaciones:
✓ Fecha futura
✓ Más de 2 horas de anticipación
✓ Dentro de 90 días
✓ Hora en intervalo válido (10:00)
✓ Lunes dentro de horario laboral
✓ Max no tiene otra cita ese día
✓ María no excede 3 citas ese día
✓ Dr. Carlos disponible a las 10:00

Resultado: ✅ Cita creada exitosamente
```

### ❌ **Caso 2: Intentar Agendar Múltiples Citas Mismo Día**
```
Cliente: Pedro
Paciente: Luna (gato)
Ya tiene: Cita el Martes 9:00 AM
Intenta: Otra cita el Martes 4:00 PM

Validaciones:
✓ Fecha futura
✓ Más de 2 horas de anticipación
✓ Dentro de 90 días
✓ Hora en intervalo válido
✓ Dentro de horario laboral
✗ Luna ya tiene cita ese día

Resultado: ❌ Error: "El paciente ya tiene una cita programada para este día"
```

### ❌ **Caso 3: Intentar Agendar en Horario Ocupado**
```
Cliente: Ana
Paciente: Rocky (perro)
Veterinario: Dr. Carlos
Fecha: Miércoles 10:00 AM
Dr. Carlos ya tiene cita: Miércoles 10:00 AM (con otro paciente)

Validaciones:
✓ Fecha futura
✓ Anticipación adecuada
✓ Hora válida
✓ Horario laboral
✓ Rocky sin otras citas ese día
✓ Ana no excede límite
✗ Dr. Carlos no disponible (traslape)

Resultado: ❌ Error: "El veterinario ya tiene una cita programada en ese horario"
```

---

## 🚀 Beneficios de las Validaciones

1. **Prevención de Errores:** Evita doble-booking y conflictos de agenda
2. **Mejor Experiencia:** Usuarios reciben feedback claro sobre por qué no pueden agendar
3. **Eficiencia Operacional:** Facilita la gestión de la clínica
4. **Equidad:** Límites por día previenen acaparamiento
5. **Realismo:** Refleja cómo funciona una clínica veterinaria real
6. **Escalabilidad:** Fácil ajustar constantes según necesidades

---

## 🔧 Configuración

Todas las constantes se encuentran en:
```
src/main/java/com/tuorg/veterinaria/common/constants/AppConstants.java
```

Para modificar comportamiento, ajustar las constantes:
- `DURACION_CITA_MINUTOS` - Duración de cada cita
- `ANTICIPACION_MINIMA_HORAS` - Anticipación mínima requerida
- `ANTICIPACION_MAXIMA_DIAS` - Anticipación máxima permitida
- `INTERVALO_CITAS_MINUTOS` - Intervalos de tiempo permitidos
- `HORARIO_MANANA_INICIO/FIN` - Horario matutino
- `HORARIO_TARDE_INICIO/FIN` - Horario vespertino
- `MAX_CITAS_POR_DIA_CLIENTE` - Límite de citas por cliente por día

---

**Implementado con ❤️ para Clínica Veterinaria Humboldt**
