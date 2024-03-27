package acal.lab05.Bonus

import chisel3._
import chisel3.util._
import chisel3.iotesters.{Driver, PeekPokeTester}
import scala.util.Random
import scala.io.StdIn.readInt
import scala.language.implicitConversions

class TopTest(dut: Top)
    extends PeekPokeTester(dut) {

  implicit def bigint2boolean(b: BigInt): Boolean =
    if (b > 0) true else false

  // Randomly Initial the puzzle
  step(Random.nextInt(10))
  poke(dut.io.gen, true)
  step(1)
  poke(dut.io.gen, false)

  while (!peek(dut.io.finish)) {
    step(1)
  }
}

object TopTest extends App {
  Driver.execute(args, () => new Top) { dut: Top =>
    new TopTest(dut)
  }
}
