# Informe de Correccion - Riego Optimo
## 1. Cálculo de tiempos de inicio de riego

### 1.1 Definición matemática del tiempo de inicio de riego

La fórmula para calcular el tiempo de inicio de riego de un tablón es:

$$
t^{\Pi}*{\pi*{j+1}} = t^{\Pi}*{\pi_j} + tr(F*{\pi_j})
$$

Donde:

* ( t^{\Pi}_{\pi_j} ) es el tiempo de inicio de riego del tablón ( \pi_j ),
* ( tr(F_{\pi_j}) ) es el tiempo de riego del tablón ( \pi_j ),
* ( t^{\Pi}*{\pi*{j+1}} ) es el tiempo de inicio de riego del siguiente tablón ( \pi_{j+1} ).

### 1.2 Implementación de cálculo del tiempo de inicio de riego

La función `tIR` toma como entrada la finca `f` (que es un vector de tablones) y la programación de riego `pi` (que asigna un turno de riego a cada tablón). La función calcula los tiempos de inicio de riego de acuerdo con la fórmula mencionada anteriormente:

```scala
def tIR(f: Finca, pi: ProgRiego): TiempoInicioRiego = {
  val n = f.length

  // Ordenamos los turnos de riego
  val turnosOrdenados: Vector[Int] = pi.zipWithIndex.sortBy(_._1).map(_._2)

  // Calculamos los tiempos de inicio de riego
  val tiemposOrdenados: Vector[Int] = turnosOrdenados.foldLeft((Vector.empty[Int], 0)) {
    case ((acc, tiempoActual), tablon) =>
      val tiempoInicio = tiempoActual
      val nuevoTiempo = tiempoActual + treg(f, tablon)
      (acc :+ tiempoInicio, nuevoTiempo)
  }._1

  // Asignamos los tiempos calculados en el vector final
  val resultado: Vector[Int] = turnosOrdenados.zip(tiemposOrdenados).foldLeft(Vector.fill(n)(0)) {
    case (acc, (tablon, tiempoInicio)) =>
      acc.updated(tablon, tiempoInicio)
  }

  resultado
}
```

#### Análisis práctico:

1. **Ordenación de los turnos de riego**: Primero, ordenamos los tablones según su turno de riego ( \pi_j ), lo cual tiene una complejidad (O(n \log n)) debido a la función `sortBy`.

2. **Cálculo de los tiempos**: Luego, calculamos los tiempos de inicio en una sola pasada sobre los tablones ordenados, lo que se hace en (O(n)), ya que solo sumamos los tiempos de riego de los tablones anteriores.

El tiempo total de ejecución de la función `tIR` es (O(n \log n)), principalmente debido a la ordenación de los turnos.


## 2. Cálculo de costos de riego

### 2.1 Cálculo del costo de riego por tablón

El costo de riego de un tablón (i) depende de su tiempo de inicio de riego, su tiempo de riego, y su tiempo de supervivencia. La penalización por retraso es la siguiente definicion:

$$
CR_{\Pi F}[i] =
\begin{cases}
ts_{F_i} - (t^{\Pi}*{i} + tr*{F_i}), & \text{si } ts_{F_i} - tr_{F_i} \geq t^{\Pi}*{i}, \
p*{F_i} \cdot \left( (t^{\Pi}*{i} + tr*{F_i}) - ts_{F_i} \right), & \text{de lo contrario}.
\end{cases}
$$

El cálculo del costo de riego por tablón es el siguiente:

```scala
def costoRiegoTablon(i: Int, f: Finca, pi: ProgRiego): Int = {
  val t = tIR(f, pi)           // Obtener tiempos de inicio de riego
  val ts = tsup(f, i)          // Tiempo de supervivencia del tablón i
  val tr = treg(f, i)          // Tiempo de riego del tablón i
  val p = prio(f, i)           // Prioridad del tablón i

  val inicio = t(i)
  val fin = inicio + tr

  if (ts - tr >= inicio)
    ts - fin
  else
    p * (fin - ts)
}
```

#### Análisis práctico:

1. **Cálculo de los tiempos**: El costo de riego por tablón se calcula de manera eficiente en (O(1)), dado que solo se realizan algunas operaciones matemáticas simples para calcular los tiempos de inicio y las penalizaciones.

2. **Penalización por retraso**: Si el tiempo de inicio de riego excede el tiempo de supervivencia del tablón, se aplica una penalización proporcional a la prioridad (p). Esta penalización es esencial para optimizar la programación de riego, minimizando los costos por incumplimiento de los tiempos de riego.


## 3. Generación de permutaciones y evaluación perezosa

### 3.1 Generación de todas las permutaciones posibles

Para encontrar la programación de riego óptima, necesitamos evaluar todas las posibles permutaciones de los turnos de riego. Para evitar calcular todas las permutaciones a la vez y optimizar el rendimiento, usamos evaluación perezosa (Lazy Evaluation) en la función `generarProgramacionesRiego`.

La función `generarProgramacionesRiego` genera permutaciones de manera eficiente utilizando `LazyList`:

```scala
def generarProgramacionesRiego(f: Finca): Vector[ProgRiego] = {
  val n = f.length
  val base: Vector[Int] = (0 until n).toVector

  def permutaciones(xs: Vector[Int]): LazyList[Vector[Int]] = {
    if (xs.isEmpty) LazyList(Vector())
    else {
      LazyList.from(xs.indices).flatMap { i =>
        val elem = xs(i)
        val resto = xs.patch(i, Nil, 1)
        permutaciones(resto).map(elem +: _)
      }
    }
  }

  permutaciones(base).toVector
}
```

#### Análisis práctico:

1. **Evaluación perezosa**: Al usar `LazyList`, no generamos todas las permutaciones de inmediato. En cambio, las generamos bajo demanda, lo que reduce el uso de memoria y mejora la eficiencia de ejecución.

2. **Complejidad**: El proceso de generar las permutaciones tiene una complejidad de (O(n!)) en el peor de los casos, pero la evaluación perezosa garantiza que solo se generen las permutaciones que realmente se necesitan, optimizando la carga de trabajo.

## 4. Cálculo del costo total de riego

### 4.1 Cálculo del costo total de riego para la finca

El costo total de riego para una finca es la sumatoria de los costos de riego de cada tablón [i], más el costo de movilidad (que depende de las distancias entre los tablones en la programación de riego):

$$
Costo_{\text{total}} = \sum_{i=0}^{n-1} CR_{\Pi F}[i]
$$

### 4.2 Representación de la evaluación total

El cálculo del costo total se realiza de forma eficiente, con una complejidad (O(n)) para cada programación de riego, ya que la evaluación del costo de riego y el costo de movilidad se realiza en (O(n)) para cada permutación.

---

## 5. Conclusiones

Este enfoque optimiza significativamente la ejecución del algoritmo para generar y evaluar las programaciones de riego. Las mejoras clave son:

1. **Evaluación perezosa**: Generación de permutaciones bajo demanda, evitando cálculos innecesarios y optimizando el uso de memoria.

2. **Cálculos eficientes de costos**: El cálculo de los tiempos de inicio de riego y los costos se realiza en tiempo (O(n \log n)) o (O(n)), sin necesidad de recalcular resultados intermedios.
