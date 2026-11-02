# Reglas de atención

Fuente de las reglas que implementa `domain/`. Completa la sección 9 de la preentrega con lo que pidió el feedback: la fórmula del puntaje y el efecto del trabajo restante.

> **Estado: propuesta.** La fórmula y sus pesos están pendientes de aprobación de Gabriela. Ni los pesos ni los umbrales están validados con usuarios.

## Estado

Deadline e inactividad son relojes independientes. El estado final es el mayor de los dos.

| Reloj | Dormido | Despierto | Reclamando |
|---|---|---|---|
| Deadline | Sin deadline o faltan más días que la ventana | Faltan entre la ventana y `diaReclamo + 1` días | Faltan `diaReclamo` días o menos, o está vencida |
| Inactividad | Menos de `diaDespierto` días sin avance | Desde `diaDespierto` hasta antes de la ventana | La ventana o más días sin avance |

| Parámetro | Cálculo | Por defecto |
|---|---|---|
| `diaReclamo` | 30 % de la ventana de deadline, redondeado hacia abajo | 7 días → 2 |
| `diaDespierto` | 40 % de la ventana de inactividad, redondeado hacia arriba | 30 días → 12 |

Redondear `diaDespierto` hacia arriba evita que una ventana de 1 día deje todas las tareas siempre despiertas.

Los días se cuentan por fecha de calendario en la zona horaria del dispositivo. Si la fecha del sistema retrocede, los días sin avance nunca son negativos.

## Puntaje

Ordena las tareas dentro de un mismo estado.

```text
Pd = presión de deadline = 0 si no hay deadline; si hay, (ventana − días restantes) / ventana, limitado a 0..1
Pi = presión de inactividad = días sin avance / ventana de inactividad, limitado a 0..1
Tr = trabajo restante = (100 − progreso) / 100

puntaje = 100 × (0,6 × Pd × (0,5 + 0,5 × Tr) + 0,4 × Pi)
```

**Trabajo restante:** solo cambia el orden dentro de un estado, nunca el estado. Así el pájaro se sigue explicando con el deadline y la inactividad.

Ejemplo: una tarea que vence en 2 días y lleva 5 sin avance da 45 puntos con 20 % de progreso y 30 con 90 %.

## Orden y límite

1. Estado: Reclamando antes que Despierto.
2. Puntaje, de mayor a menor.
3. Deadline más cercano; las tareas sin deadline van al final.
4. Id, para que el orden sea estable.

La Rama muestra las primeras N tareas y las demás que requieren atención se cuentan en "+N".

**Regla de seguridad:** toda tarea en Reclamando está en las primeras N o en "+N". Nunca queda oculta en el Nido.

## Posposición

- Pospuesta no es un cuarto estado: es una condición de visibilidad. Hasta la fecha elegida la tarea va al Nido con el pájaro dormido.
- Al llegar la fecha se evalúa con sus días reales sin avance.
- Si Reclamando por deadline aplica durante la posposición, la tarea vuelve a la Rama. Esto incluye posponer una tarea que ya está en ese estado: no se oculta, y la hoja Posponer debe avisarlo.

## Pendiente de definir

- Si llegar a 100 % de progreso completa la tarea.
- Si posponer una tarea vencida debería poder ocultarla (hoy no puede, por la regla de seguridad).
