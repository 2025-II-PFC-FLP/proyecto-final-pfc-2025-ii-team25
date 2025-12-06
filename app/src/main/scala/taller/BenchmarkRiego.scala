package taller

import org.scalameter._
import Irrigation._

object BenchmarkRiego {

  def main(args: Array[String]): Unit = {

    val tamanos = List(3, 4, 7, 9, 10)

    println(f"${"Tamaño"}%10s | ${"Secuencial (ms)"}%10s | ${"Paralelo (ms)"}%10s | ${"Aceleración (%)"}%16s")
    println("-" * 60)

    for (n <- tamanos) {

      val finca = fincaAlAzar(n)
      val distancia = distanciaAlAzar(n)

      val timeSeq = measure {
        ProgramacionRiegoOptimo(finca, distancia)
      }.value

      val timePar = measure {
        ProgramacionRiegoOptimoPar(finca, distancia)
      }.value

      val aceleracion =
        ((timeSeq - timePar) / timeSeq) * 100

      println(f"$n%10d | $timeSeq%10.2f | $timePar%10.2f | $aceleracion%16.2f")
    }
  }
}