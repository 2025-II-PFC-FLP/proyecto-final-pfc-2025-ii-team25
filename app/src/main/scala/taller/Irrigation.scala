package taller
import common._
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

def tIR(f: Finca, pi: ProgRiego): TiempoInicioRiego = { // Calculo del tiempo de inicio de riego
  val n = f.length

  val turnosOrdenados: Vector[Int] =
    pi.zipWithIndex
      .sortBy(_._1)     // π_j definidos por el orden de turnos
      .map(_._2)        

  val tiemposOrdenados: Vector[Int] =
    turnosOrdenados.foldLeft((Vector.empty[Int], 0)) {
      case ((acc, tiempoActual), tablon) =>
        val tiempoInicio = tiempoActual

        // t^Π_{π_{j+1}} = t^Π_{π_j} + tr(F_{π_j})
        val nuevoTiempo = tiempoActual + treg(f, tablon)

        (acc :+ tiempoInicio, nuevoTiempo)
    }._1

  // Asignar tiempos calculados t^Π_{π_j} en la posición del tablón π_j
  val resultado: Vector[Int] =
    turnosOrdenados.zip(tiemposOrdenados).foldLeft(
      Vector.fill(n)(0)
    ){ case (acc, (tablon, tiempoInicio)) =>
      acc.updated(tablon, tiempoInicio)
    }

  resultado
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
}
