package acal.lab05.Bonus

import chisel3.iotesters.{Driver, PeekPokeTester}
import scala.language.implicitConversions

class SolverTest(dut: Solver)
    extends PeekPokeTester(dut) {

  implicit def bigint2boolean(b: BigInt): Boolean =
    if (b > 0) true else false

  // always set ready to High

  val ans_seq = Seq(1, 3, 6, 9)

  while (!peek(dut.io.finish)) {
    poke(dut.io.ready, true)
    step(1)
    poke(dut.io.ready, false)

    while (!peek(dut.io.s_valid)) { step(1) }

    println(
      peek(dut.io.guess(3)).toString     + " "
        + peek(dut.io.guess(2)).toString + " "
        + peek(dut.io.guess(1)).toString + " "
        + peek(dut.io.guess(0)).toString)

    var A = 0
    var B = 0

    for (i <- 0 until 4) {
      (ans_seq.zipWithIndex).foreach {
        case (element, index) =>
          if (i == 0) {
            if (
              element == peek(dut.io.guess(
                ((4 - 1) - index + i) % 4))
            ) {
              A += 1
            }
          } else {
            if (
              element == peek(dut.io.guess(
                ((4 - 1) - index + i) % 4))
            ) {
              B += 1
            }
          }
      }
    }

    println("A = " + A.toString)
    println("B = " + B.toString)

    // computation time in NumGuess
    step(16)

    poke(dut.io.A, A)
    poke(dut.io.B, B)
    poke(dut.io.g_valid, true)
    step(1)
    poke(dut.io.g_valid, false)
  }

}

object SolverTest extends App {
  Driver.execute(args, () => new Solver) {
    dut: Solver => new SolverTest(dut)
  }
}
