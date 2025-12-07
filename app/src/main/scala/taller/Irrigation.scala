package taller
import common._

import scala.collection.parallel.CollectionConverters.ImmutableIterableIsParallelizable
import scala.util.Random

object Irrigation {
  type Tablon = (Int, Int, Int) //ts,tr,p
  /*
  ts: tiempo de supervivencia
  tr: tiempo de regado
  p: prioridad 
  Una finca es un vector de tablones
  */
  type Finca = Vector[Tablon]
  type Distancia = Vector[Vector[Int]]

  /* Una programacion de riego es un vector que asocia
  cada tablon i con su turno de riego (0 es el primer turno,
  n-1 es el ultimo turno) */
  type ProgRiego = Vector[Int]

  /* El tiempo de inicio de riego es un vector que asocia
  cada tablon i con el momento del tiempo en que se riega */
  type TiempoInicioRiego = Vector[Int]

  //---Generadores de finca aleatoria---
  val random = new Random()

  def fincaAlAzar(long: Int): Finca = {
    /* Crea una finca de long tablones, con valores aleatorios entre 1 y long * 2
       para el tiempo de supervivencia, entre 1 y long para el tiempo
       de regado y entre 1 y 4 para la prioridad */
    val v = Vector.fill(long)(
      (random.nextInt(long * 2) + 1,
       random.nextInt(long) + 1,
       random.nextInt(4) + 1)
    )
    v
  }

  def imprimirFinca(f: Finca): Unit = {
  val lineas: Vector[String] =
    f.zipWithIndex.map { case ((ts, tr, p), i) =>
      s"Tablon $i -> ts=$ts tr=$tr p=$p"
    }

  // Mostrar finca generada
  lineas.foreach(println)
}

  def distanciaAlAzar(long: Int): Distancia = {
    /* Crea una matriz de distancias para una finca
       de long tablones, con valores aleatorios entre
       1 y long * 3 */
    val v = Vector.fill(long, long)(random.nextInt(long * 3) + 1)
    Vector.tabulate(long, long)((i, j) =>
      if (i < j) v(i)(j)
      else if (i == j) 0
      else v(j)(i)
    )
  }

  // Explorar entradas generadas

  def tsup(f: Finca, i: Int): Int = {
    f(i)._1
  }

  def treg(f: Finca, i: Int): Int = {
    f(i)._2
  }

  def prio(f: Finca, i: Int): Int = {
    f(i)._3
  }

  def tIR(f: Finca, pi: ProgRiego): TiempoInicioRiego = {
    val n = f.length

    val orden = pi.indices.sortBy(pi)

    val tiemposOrdenados =
      orden.scanLeft(0)((acum, tabPrev) => acum + treg(f, tabPrev)).dropRight(1)

    val res = Array.fill(n)(0)

    orden.zip(tiemposOrdenados).foreach { case (tablon, t) =>
      res(tablon) = t
    }

    res.toVector
  }

  def costoRiegoTablon(i: Int, f: Finca, pi: ProgRiego): Int = {
  val t = tIR(f, pi)         
  val ts = tsup(f, i)        
  val tr = treg(f, i)        
  val p  = prio(f, i)        

  val inicio = t(i)
  val fin = inicio + tr

  if (ts - tr >= inicio)
    ts - fin

  // Penalizacion CRΠ[i] = p_i * ((tΠ_i + tr_i) - ts_i)
  else
    p * (fin - ts)
}

  def costoRiegoFinca(f: Finca, pi: ProgRiego): Int =
  (0 until f.length).map(i => costoRiegoTablon(i, f, pi)).sum //Sumatoria costos Tablon

  def costoMovilidad(f: Finca, pi: ProgRiego, d: Distancia): Int = {
  val orden = pi.zipWithIndex.sortBy(_._1).map(_._2)   

  orden
    .sliding(2)
    .collect { case Vector(a, b) => d(a)(b) } 
    .sum
}

  def generarProgramacionesRiego(f: Finca): Vector[ProgRiego] = {
  val n = f.length
  val base: Vector[Int] = (0 until n).toVector

  def permutaciones(xs: Vector[Int]): LazyList[Vector[Int]] = {
    if (xs.isEmpty) LazyList(Vector())
    else {
      LazyList.from(xs.indices).flatMap { i => //Evaluacion Lazy para contrarestar O(n!)
        val elem = xs(i)
        val resto = xs.patch(i, Nil, 1)  // Permutar sin (i) ya generado
        permutaciones(resto).map(elem +: _) 
      }
    }
  }
  permutaciones(base).toVector
}

  def ProgramacionRiegoOptimo(f: Finca, d: Distancia): (ProgRiego, Int) = {
  val programaciones: Vector[ProgRiego] = generarProgramacionesRiego(f)

  def costoTotal(pi: ProgRiego): Int =
    costoRiegoFinca(f, pi) + costoMovilidad(f, pi, d)

  programaciones
    .map(pi => (pi, costoTotal(pi)))  // asignar costo total a cada ProgRiego
    .minBy(_._2)              
}

//Inicio defs paralelos

  def costoRiegoFincaPar(f: Finca, pi: ProgRiego): Int = {
    // 1. Generamos los índices de todos los tablones (0 hasta n-1).
    //    (0 until f.length)

    // 2. Convertimos el rango de índices a una Colección Paralela (.par).
    //    Esto distribuye el cálculo de los costos individuales.
    (0 until f.length).par

      // 3. Mapeamos sobre los índices para calcular el costo de cada tablón individual.
      //    costoRiegoTablon(i, f, pi) es la función secuencial ya implementada.
      .map(i => costoRiegoTablon(i, f, pi))

      // 4. Sumamos todos los costos parciales. La suma se realiza de forma paralela
      //    y eficiente sobre la colección paralela.
      .sum
  }

  def costoMovilidadPar(f: Finca, pi: ProgRiego, d: Distancia): Int = {
    // 1. Obtenemos el orden de los tablones a regar (índices de tablón).
    //    Esta es la permutación π.
    val orden = pi.zipWithIndex.sortBy(_._1).map(_._2)

    // 2. Utilizamos `sliding(2)` para obtener pares de tablones consecutivos (π_j, π_{j+1}).
    //    Luego convertimos esto a una colección paralela (.par).
    orden
      .sliding(2)
      .toVector.par // Convertimos el iterador a Vector y luego a paralelo.

      // 3. Mapeamos sobre cada par (a, b) para obtener el costo de movilidad D_F[a, b].
      .map { case Vector(a, b) => d(a)(b) }

      // 4. Sumamos los costos de movilidad parciales.
      .sum
  }

  def generarProgramacionesRiegoPar(f: Finca): Vector[ProgRiego] = {
    val n = f.length
    val base: Vector[Int] = (0 until n).toVector

    // Función interna para generar permutaciones con paralelismo
    def permutacionesPar(xs: Vector[Int]): LazyList[Vector[Int]] = {
      if (xs.isEmpty) LazyList(Vector())
      else {
        // Usamos LazyList para una evaluación diferida, y luego .par para distribuir el trabajo
        LazyList.from(xs.indices).par.flatMap { i => // Paralelismo de Tareas/Datos en el primer nivel
          val elem = xs(i)
          val resto = xs.patch(i, Nil, 1)

          // La llamada recursiva sigue siendo secuencial (o con menor grado de paralelismo)
          permutacionesPar(resto).map(elem +: _)
        }
      }.to(LazyList) // Aseguramos que el resultado es un LazyList
    }

    // Si la generación es demasiado lenta/grande, esta es una alternativa más sencilla
    // que aprovecha la estructura de la función original para delegar:
    def permutacionesParSimple(xs: Vector[Int]): Vector[ProgRiego] = {
      if (xs.isEmpty) Vector(Vector())
      else {
        // 1. Distribuimos las iteraciones del 'i' inicial en paralelo.
        (xs.indices).toVector.par.flatMap { i =>
          val elem = xs(i)
          val resto = xs.patch(i, Nil, 1)

          // 2. Llamada recursiva (esta parte sigue siendo secuencial para simplificar y evitar sobrecarga)
          val subPermutaciones = generarProgramacionesRiego(f.patch(i, Nil, 1)) // Usamos la secuencial interna

          // 3. Combinamos el elemento principal con cada sub-permutación
          subPermutaciones.map(elem +: _)
        }.toVector
      }
    }

    // Usamos la versión `permutacionesParSimple` por ser más funcionalmente clara para el objetivo
    // de generar un Vector[ProgRiego] con paralelismo en el primer nivel de la recursión.
    permutacionesParSimple(base)
  }

  def ProgramacionRiegoOptimoPar(f: Finca, d: Distancia): (ProgRiego, Int) = {
    // 1. Generamos todas las programaciones de riego.
    //    Para acelerar, podemos usar la versión paralela si el tamaño de n lo justifica.
    val programaciones: Vector[ProgRiego] = generarProgramacionesRiegoPar(f)

    // 2. Definición del cálculo del Costo Total (CR + CM).
    //    Usamos las funciones paralelas de costo para acelerar el cálculo individual.
    def costoTotalPar(pi: ProgRiego): Int =
      costoRiegoFincaPar(f, pi) + costoMovilidadPar(f, pi, d)

    // 3. Paralelizamos la Colección de Programaciones y el cálculo del costo.
    //    Esto distribuye la evaluación de la función de costo sobre todas las posibles Pi.
    programaciones.par

      // Mapeamos para asociar cada programación con su costo total.
      .map(pi => (pi, costoTotalPar(pi)))

      // Buscamos el elemento (Programacion, Costo) con el costo mínimo.
      // Esta operación (minBy) es eficiente en colecciones paralelas.
      .minBy(_._2)
  }

}
