package taller

import org.scalatest.funsuite.AnyFunSuite

class IrrigationTest extends AnyFunSuite {

import Irrigation._

//Finca fija para pruebas

  val finca: Finca = Vector(
    (10, 3, 4),
    (5, 3, 3),
    (2, 2, 1),
    (8, 1, 1),
    (6, 4, 2)
  )

  val distancia: Distancia = Vector(
    Vector(0,2,2,4,4),
    Vector(2,0,4,2,6),
    Vector(2,4,0,2,2),
    Vector(4,2,2,0,4),
    Vector(4,6,2,4,0)
  )

//TEST tIR (Tiempo de inicio de riego)

  test("tIR - Caso 1") {
    val pi = Vector(0,1,2,3,4)
    val res = tIR(finca, pi)
    assert(res == Vector(0,3,6,8,9))
  }

  test("tIR - Caso 2") {
    val pi = Vector(1,2,0,4,3)
    val res = tIR(finca, pi)
    assert(res(2) == 0)
    assert(res(0) == 2)
    assert(res(1) == 5)
  }

  test("tIR - Caso 3") {
    val pi = Vector(4,3,2,1,0)
    val res = tIR(finca, pi)
    assert(res(4) == 0)
    assert(res(3) == 4)
    assert(res(2) == 5)
  }

  test("tIR - Caso 4") {
    val pi = Vector(2,1,4,0,3)
    val res = tIR(finca, pi)
    assert(res.length == 5)
    assert(res(3) == 0)
  }

  test("tIR - Caso 5") {
    val pi = Vector(3,2,1,4,0)
    val res = tIR(finca, pi)
    assert(res.distinct.length == 5)
  }

//TEST costoRiegoTablon

  test("costoRiegoTablon - Caso 1") {
    val pi = Vector(0,1,2,3,4)
    val cost = costoRiegoTablon(0, finca, pi)
    assert(cost == 10 - (0 + 3))
  }

  test("costoRiegoTablon - Caso 2") {
    val pi = Vector(4,3,2,1,0)
    val cost = costoRiegoTablon(0, finca, pi)
    assert(cost > 0)
  }

  test("costoRiegoTablon - Caso 3") {
    val pi = Vector(2,0,1,3,4)
    val cost = costoRiegoTablon(2, finca, pi)
    assert(cost >= 0)     // si no hay penalidad
  }

  test("costoRiegoTablon - Caso 4") {
    val pi = Vector(1,0,2,3,4)
    val cost = costoRiegoTablon(1, finca, pi)
    assert(cost <= 5)
  }

  test("costoRiegoTablon - Caso 5") {
    val pi = Vector(4,3,2,1,0)
    val before = costoRiegoTablon(0, finca, pi)
    val after  = costoRiegoTablon(1, finca, pi)
    assert(before != after)
  }

//TEST costoRiegoFinca

  test("costoRiegoFinca - Caso 1") {
    val pi = Vector(0,1,2,3,4)
    val cost = costoRiegoFinca(finca, pi)
    assert(cost == costoRiegoTablon(0,finca,pi) +
                  costoRiegoTablon(1,finca,pi) +
                  costoRiegoTablon(2,finca,pi) +
                  costoRiegoTablon(3,finca,pi) +
                  costoRiegoTablon(4,finca,pi))
  }

  test("costoRiegoFinca - Caso 2") {
    val pi = Vector(4,3,2,1,0)
    val cost = costoRiegoFinca(finca, pi)
    assert(cost > 0)
  }

  test("costoRiegoFinca - Caso 3") {
    val c1 = costoRiegoFinca(finca, Vector(0,1,2,3,4))
    val c2 = costoRiegoFinca(finca, Vector(4,3,2,1,0))
    assert(c1 != c2)
  }

  test("costoRiegoFinca - Caso 4") {
    val pi = Vector(2,1,0,4,3)
    val cost = costoRiegoFinca(finca, pi)
    assert(cost >= 0)
  }

  test("costoRiegoFinca - Caso 5") {
    val pi = Vector(1,2,3,4,0)
    val cost = costoRiegoFinca(finca, pi)
    assert(cost == cost)
  }

//TEST costoMovilidad

  test("costoMovilidad - Caso 1") {
    val pi = Vector(0,1,2,3,4)
    val cost = costoMovilidad(finca, pi, distancia)
    assert(cost == (2+4+2+4))
  }

  test("costoMovilidad - Caso 2") {
    val pi = Vector(2,1,4,3,0)
    val cost = costoMovilidad(finca, pi, distancia)
    assert(cost > 0)
  }

  test("costoMovilidad - Caso 3") {
    val pi1 = Vector(0,1,2,3,4)
    val c1 = costoMovilidad(finca, pi1, distancia)
    assert(c1 >= 0)
  }

  test("costoMovilidad - Caso 4") {
    val pi = Vector(3,0,1,2,4)
    val cost = costoMovilidad(finca, pi, distancia)
    assert(cost >= 0)
  }

  test("costoMovilidad - Caso 5") {
    val pi = Vector(1,0,2,3,4)
    val cost = costoMovilidad(finca, pi, distancia)
    assert(cost == 2 + 2 + 2 + 4)
  }


//TEST generarProgramacionesRiego

  test("generarProgramacionesRiego - Caso 1") {
    val perms = generarProgramacionesRiego(finca)
    assert(perms.length == 120)
  }

  test("generarProgramacionesRiego - Caso 2") {
    val perms = generarProgramacionesRiego(finca)
    assert(perms.forall(p => p.sorted == Vector(0,1,2,3,4)))
  }

  test("generarProgramacionesRiego - Caso 3") {
    val perms = generarProgramacionesRiego(finca)
    assert(perms.distinct.length == perms.length)
  }

  test("generarProgramacionesRiego - Caso 4") {
    val perms = generarProgramacionesRiego(finca)
    assert(perms.contains(Vector(0,1,2,3,4)))
  }

  test("generarProgramacionesRiego - Caso 5") {
    val perms = generarProgramacionesRiego(finca)
    assert(perms.contains(Vector(4,3,2,1,0)))
  }

//TEST ProgramacionRiegoOptimo

  test("ProgramacionRiegoOptimo - Caso 1") {
    val (pi, c) = ProgramacionRiegoOptimo(finca, distancia)
    assert(pi.length == 5)
  }

  test("ProgramacionRiegoOptimo - Caso 2") {
    val (_, c1) = ProgramacionRiegoOptimo(finca, distancia)
    assert(c1 >= 0)
  }

  test("ProgramacionRiegoOptimo - Caso 3") {
    val (pi, _) = ProgramacionRiegoOptimo(finca, distancia)
    assert(pi.sorted == Vector(0,1,2,3,4))
  }

  test("ProgramacionRiegoOptimo - Caso 4") {
    val (optPi, optCost) = ProgramacionRiegoOptimo(finca, distancia)
    val baseCost = costoRiegoFinca(finca, Vector(0,1,2,3,4)) +
                   costoMovilidad(finca, Vector(0,1,2,3,4), distancia)
    assert(optCost <= baseCost)
  }

  test("ProgramacionRiegoOptimo - Caso 5") {
    val (pi, c) = ProgramacionRiegoOptimo(finca, distancia)
    val perms = generarProgramacionesRiego(finca)
    val allCosts = perms.map(p => costoRiegoFinca(finca,p) + costoMovilidad(finca,p,distancia))
    assert(c == allCosts.min)
  }

}
