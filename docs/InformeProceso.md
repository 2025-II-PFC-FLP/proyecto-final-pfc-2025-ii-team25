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

## 5. Proceso Recursivo de `generarProgramacionesRiego`

Este es el proceso más complejo por su naturaleza factorial.

### Ejemplo pequeño
```scala
val f: Finca = Vector((10,3,2), (8,2,1), (12,4,3))
// n = 3 tablones
generarProgramacionesRiego(f)
```

### Llamadas recursivas (árbol de permutaciones)
```
permutaciones([0,1,2])
├─ elem=0, resto=[1,2]
│   └─ permutaciones([1,2])
│       ├─ elem=1, resto=[2]
│       │   └─ permutaciones([2])
│       │       └─ permutaciones([]) → [[]]
│       │       └─ return [[2]]
│       │   └─ return [[1,2]]
│       └─ elem=2, resto=[1]
│           └─ permutaciones([1])
│               └─ permutaciones([]) → [[]]
│               └─ return [[1]]
│           └─ return [[2,1]]
│   └─ return [[0,1,2], [0,2,1]]
│
├─ elem=1, resto=[0,2]
│   └─ permutaciones([0,2])
│       ├─ elem=0, resto=[2]
│       │   └─ return [[0,2]]
│       └─ elem=2, resto=[0]
│           └─ return [[2,0]]
│   └─ return [[1,0,2], [1,2,0]]
│
└─ elem=2, resto=[0,1]
    └─ permutaciones([0,1])
        ├─ elem=0, resto=[1]
        │   └─ return [[0,1]]
        └─ elem=1, resto=[0]
            └─ return [[1,0]]
    └─ return [[2,0,1], [2,1,0]]
```

### Pila de llamadas (profundidad máxima)
```
permutaciones([0,1,2])
  permutaciones([1,2])
    permutaciones([2])
      permutaciones([])
```

### Despliegue de la pila
```
permutaciones([]) → Vector(Vector())
permutaciones([2]) → Vector(Vector(2))
permutaciones([1,2]) → Vector(Vector(1,2), Vector(2,1))
permutaciones([0,1,2]) → Vector(
  Vector(0,1,2),
  Vector(0,2,1),
  Vector(1,0,2),
  Vector(1,2,0),
  Vector(2,0,1),
  Vector(2,1,0)
)
```

### Resultado final
```
generarProgramacionesRiego(f) = [
  [0,1,2], [0,2,1], [1,0,2],
  [1,2,0], [2,0,1], [2,1,0]
]
// Total: 3! = 6 programaciones
```

---

## 6. Proceso Recursivo de `ProgramacionRiegoOptimo`

Este proceso integra todos los anteriores.

### Ejemplo
```scala
val f: Finca = Vector((10,3,2), (8,2,1), (12,4,3))
val d: Distancia = Vector(
  Vector(0, 5, 8),
  Vector(5, 0, 3),
  Vector(8, 3, 0)
)
ProgramacionRiegoOptimo(f, d)
```

### Paso 1: Generar todas las programaciones
```
programaciones = generarProgramacionesRiego(f)
= [[0,1,2], [0,2,1], [1,0,2], [1,2,0], [2,0,1], [2,1,0]]
```

### Paso 2: Evaluar cada programación
```
Para pi = [0,1,2]:
  costoRiegoFinca(f, [0,1,2]):
    → tIR = [0,3,5]
    → costos = [7,3,3]
    → suma = 13
  costoMovilidad(f, [0,1,2], d):
    → orden = [0,1,2]
    → distancias = [5,3]
    → suma = 8
  costoTotal = 13 + 8 = 21

Para pi = [0,2,1]:
  costoRiegoFinca(f, [0,2,1]):
    → tIR = [0,7,3]
    → costos = [7,-1,5]  // Tablón 1 tiene penalización
    → suma = 11
  costoMovilidad(f, [0,2,1], d):
    → orden = [0,2,1]
    → distancias = [8,3]
    → suma = 11
  costoTotal = 11 + 11 = 22

Para pi = [1,0,2]:
  costoRiegoFinca(f, [1,0,2]):
    → tIR = [2,0,5]
    → costos = [5,6,3]
    → suma = 14
  costoMovilidad(f, [1,0,2], d):
    → orden = [1,0,2]
    → distancias = [5,8]
    → suma = 13
  costoTotal = 14 + 13 = 27

Para pi = [1,2,0]:
  costoRiegoFinca(f, [1,2,0]):
    → tIR = [6,0,2]
    → costos = [1,6,6]
    → suma = 13
  costoMovilidad(f, [1,2,0], d):
    → orden = [1,2,0]
    → distancias = [3,8]
    → suma = 11
  costoTotal = 13 + 11 = 24

Para pi = [2,0,1]:
  costoRiegoFinca(f, [2,0,1]):
    → tIR = [4,7,0]
    → costos = [3,-3,8]  // Tablón 1 con penalización
    → suma = 8
  costoMovilidad(f, [2,0,1], d):
    → orden = [2,0,1]
    → distancias = [8,5]
    → suma = 13
  costoTotal = 8 + 13 = 21

Para pi = [2,1,0]:
  costoRiegoFinca(f, [2,1,0]):
    → tIR = [6,4,0]
    → costos = [1,2,8]
    → suma = 11
  costoMovilidad(f, [2,1,0], d):
    → orden = [2,1,0]
    → distancias = [3,5]
    → suma = 8
  costoTotal = 11 + 8 = 19
```

### Paso 3: Seleccionar el mínimo
```
Programación: [0,1,2] → Costo: 21
Programación: [0,2,1] → Costo: 22
Programación: [1,0,2] → Costo: 27
Programación: [1,2,0] → Costo: 24
Programación: [2,0,1] → Costo: 21
Programación: [2,1,0] → Costo: 19  ← MÍNIMO
```

### Resultado final
```
ProgramacionRiegoOptimo(f, d) = ([2,1,0], 19)
// La programación óptima es: regar tablón 2, luego 1, luego 0
// Con un costo total de 19
```

---

## 7. Complejidad de los Procesos

### Análisis de complejidad temporal

| Función | Complejidad | Justificación |
|---------|-------------|---------------|
| `tIR` | O(n log n) | Ordenamiento + acumulación lineal |
| `costoRiegoTablon` | O(n) | Llamada a tIR |
| `costoRiegoFinca` | O(n²) | n llamadas a costoRiegoTablon |
| `costoMovilidad` | O(n log n) | Ordenamiento + sliding |
| `generarProgramacionesRiego` | O(n! × n) | n! permutaciones, cada una de tamaño n |
| `ProgramacionRiegoOptimo` | O(n! × n²) | n! evaluaciones × costo O(n²) cada una |

### Crecimiento factorial
```
n = 3  → 3! = 6 programaciones
n = 4  → 4! = 24 programaciones
n = 5  → 5! = 120 programaciones
n = 7  → 7! = 5,040 programaciones
n = 10 → 10! = 3,628,800 programaciones
```

**Conclusión:** El algoritmo es intratable para n ≥ 11 sin paralelización.

---

## 8. Diagrama de Flujo de Ejecución Completa
```
ProgramacionRiegoOptimo(f, d)
│
├─→ generarProgramacionesRiego(f)
│   │
│   └─→ permutaciones([0,1,2,...,n-1])
│       ├─→ Caso base: [] → [[]]
│       └─→ Caso recursivo: construir árbol
│           └─→ return: n! programaciones
│
└─→ Para cada programación pi:
    │
    ├─→ costoRiegoFinca(f, pi)
    │   │
    │   ├─→ tIR(f, pi)
    │   │   ├─→ Ordenar tablones
    │   │   ├─→ Acumular tiempos
    │   │   └─→ Asignar posiciones
    │   │
    │   └─→ ∑ costoRiegoTablon(i, f, pi)
    │       └─→ Para cada tablón i:
    │           ├─→ Calcular inicio y fin
    │           ├─→ Verificar condición
    │           └─→ Aplicar fórmula
    │
    ├─→ costoMovilidad(f, pi, d)
    │   ├─→ Obtener orden
    │   ├─→ Generar pares
    │   └─→ Sumar distancias
    │
    └─→ costoTotal = CR + CM
│
└─→ minBy(costoTotal)
    └─→ return (mejorPi, menorCosto)
```

---

## Conclusión del Informe de Procesos

El sistema de programación de riego óptimo utiliza:

1. **Recursión exhaustiva** en `generarProgramacionesRiego` para explorar todas las posibilidades (n!)
2. **Acumulación iterativa** en `tIR` para calcular tiempos de inicio
3. **Evaluación condicional** en `costoRiegoTablon` para determinar penalizaciones
4. **Agregación** en `costoRiegoFinca` y `costoMovilidad` para sumar costos parciales
5. **Optimización** en `ProgramacionRiegoOptimo` para seleccionar la mejor programación

La naturaleza factorial del problema hace esencial la **paralelización** para instancias con n ≥ 9 tablones
