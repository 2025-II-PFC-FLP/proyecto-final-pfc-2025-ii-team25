# Informe de Procesos

---

## Informe de Procesos Recursivos del Módulo Irrigation

Este informe muestra el proceso generado por los programas recursivos del objeto Irrigation.

En cada caso se incluye:
- Un ejemplo pequeño
- Las llamadas recursivas que generan la pila
- El despliegue de la pila mientras se resuelve

Los procedimientos estudiados son:
- `tIR` (Tiempo de Inicio de Riego)
- `costoRiegoTablon`
- `costoRiegoFinca`
- `costoMovilidad`
- `generarProgramacionesRiego`
- `ProgramacionRiegoOptimo`

---

## 1. Proceso de `tIR` (Tiempo de Inicio de Riego)

### Ejemplo
```scala
val f: Finca = Vector((10,3,2), (8,2,1), (12,4,3))
val pi: ProgRiego = Vector(1, 0, 2)
// Tablon 0 se riega segundo (turno 1)
// Tablon 1 se riega primero (turno 0)
// Tablon 2 se riega tercero (turno 2)
```

### Proceso de cálculo

**Paso 1: Ordenar tablones por turno**
```
turnosOrdenados = [1, 0, 2]
// Tablon 1 primero, luego tablon 0, luego tablon 2
```

**Paso 2: Acumulación iterativa de tiempos**
```
Iteración 1:
  Tablon: 1
  tiempoActual: 0
  tiempoInicio: 0
  nuevoTiempo: 0 + tr(1) = 0 + 2 = 2

Iteración 2:
  Tablon: 0
  tiempoActual: 2
  tiempoInicio: 2
  nuevoTiempo: 2 + tr(0) = 2 + 3 = 5

Iteración 3:
  Tablon: 2
  tiempoActual: 5
  tiempoInicio: 5
  nuevoTiempo: 5 + tr(2) = 5 + 4 = 9
```

**Paso 3: Asignar tiempos en posiciones originales**
```
tiemposOrdenados = [0, 2, 5]
// Tablon 1 inicia en 0, tablon 0 en 2, tablon 2 en 5

Resultado: Vector(2, 0, 5)
// Tablon 0 inicia en tiempo 2
// Tablon 1 inicia en tiempo 0
// Tablon 2 inicia en tiempo 5
```

### Resultado final
```
tIR(f, pi) = Vector(2, 0, 5)
```

---

## 2. Proceso de `costoRiegoTablon`

### Ejemplo
```scala
val f: Finca = Vector((10,3,2), (8,2,1), (12,4,3))
val pi: ProgRiego = Vector(1, 0, 2)
costoRiegoTablon(0, f, pi)
```

### Cálculo paso a paso

**Paso 1: Obtener valores del tablón 0**
```
t = tIR(f, pi) = Vector(2, 0, 5)
ts = tsup(f, 0) = 10
tr = treg(f, 0) = 3
p = prio(f, 0) = 2
```

**Paso 2: Calcular tiempos de inicio y fin**
```
inicio = t(0) = 2
fin = inicio + tr = 2 + 3 = 5
```

**Paso 3: Evaluar condición**
```
ts - tr >= inicio ?
10 - 3 >= 2 ?
7 >= 2 ? → SÍ (el tablón se riega a tiempo)
```

**Paso 4: Calcular costo**
```
Costo = ts - fin = 10 - 5 = 5
```

### Resultado final
```
costoRiegoTablon(0, f, pi) = 5
```

### Caso con penalización

Si el tablón se regara muy tarde:
```
Supongamos inicio = 10, tr = 2, ts = 8, p = 3
fin = 10 + 2 = 12

ts - tr >= inicio ?
8 - 2 >= 10 ?
6 >= 10 ? → NO (el tablón se riega tarde)

Penalización = p * (fin - ts) = 3 * (12 - 8) = 3 * 4 = 12
```

---

## 3. Proceso Recursivo de `costoRiegoFinca`

### Ejemplo
```scala
val f: Finca = Vector((10,3,2), (8,2,1), (12,4,3))
val pi: ProgRiego = Vector(1, 0, 2)
costoRiegoFinca(f, pi)
```

### Llamadas recursivas generadas
```
costoRiegoFinca(f, pi)
 ├─ costoRiegoTablon(0, f, pi)
 ├─ costoRiegoTablon(1, f, pi)
 └─ costoRiegoTablon(2, f, pi)
```

### Pila de llamadas
```
costoRiegoFinca
  costoRiegoTablon(0)
  costoRiegoTablon(1)
  costoRiegoTablon(2)
```

### Despliegue de la pila
```
costoRiegoTablon(0, f, pi):
  inicio = 2, fin = 5, ts = 10
  Costo = 10 - 5 = 5

costoRiegoTablon(1, f, pi):
  inicio = 0, fin = 2, ts = 8
  Costo = 8 - 2 = 6

costoRiegoTablon(2, f, pi):
  inicio = 5, fin = 9, ts = 12
  Costo = 12 - 9 = 3

Suma total: 5 + 6 + 3 = 14
```

### Resultado final
```
costoRiegoFinca(f, pi) = 14
```

---

## 4. Proceso de `costoMovilidad`

### Ejemplo
```scala
val f: Finca = Vector((10,3,2), (8,2,1), (12,4,3))
val pi: ProgRiego = Vector(1, 0, 2)
val d: Distancia = Vector(
  Vector(0, 5, 8),
  Vector(5, 0, 3),
  Vector(8, 3, 0)
)
costoMovilidad(f, pi, d)
```

### Proceso de cálculo

**Paso 1: Obtener orden de riego**
```
pi.zipWithIndex = [(1,0), (0,1), (2,2)]
sortBy(_._1) = [(0,1), (1,0), (2,2)]
orden = [1, 0, 2]
// Se riega: Tablon 1 → Tablon 0 → Tablon 2
```

**Paso 2: Generar pares consecutivos**
```
orden.sliding(2):
  [1, 0]
  [0, 2]
```

**Paso 3: Calcular distancias**
```
Par [1, 0]:
  d(1)(0) = 5

Par [0, 2]:
  d(0)(2) = 8

Suma: 5 + 8 = 13
```

### Resultado final
```
costoMovilidad(f, pi, d) = 13
```

---
