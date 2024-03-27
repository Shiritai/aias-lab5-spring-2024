package acal.lab05.Hw3

import chisel3._
import chisel3.util._
import chisel3.iotesters.{Driver, PeekPokeTester}
import scala.util.Random
import scala.language.implicitConversions

class PRNGTest(dut: PRNG)
    extends PeekPokeTester(dut) {

  implicit def bigint2boolean(b: BigInt): Boolean =
    if (b > 0) true else false

  var error = 0

  for (i <- 1 to 100) {
    println(s"The ${i} testing :")
    step(Random.nextInt(10))
    poke(dut.io.gen,    true)
    step(1)
    poke(dut.io.gen,    false)
    while (!peek(dut.io.ready)) { step(1) }

    var out = Seq.range(0, 4).map { x =>
      peek(dut.io.puzzle(x))
    }

    var check = out
      .map { x => (1 << (x.toInt)) }
      .reduce(_ + _)
    var count = 0

    for (i <- 0 until 10) {
      count = count + (check & 0x1)
      check = check >> 1
    }

    println(
      "Output : " + out(3) + " " + out(
        2) + " " + out(1) + " " + out(0))

    if (count != 4) {
      println(
        "Oh no!! There must be something wrong in your PRNG...Correct these situations...")
      error += 1
    }
    step(1)
  }

  if (error == 0) {
    println("You pass the Hw6-2-1, Well done!!")
  } else {
    println(
      "Oh no!! There must be something wrong in your PRNG...Correct these situations...")
    println("1. Repeated numbers")
    println("2. numbers that not in 0~9")
  }
}

object PRNGTest extends App {
  Driver.execute(args, () => new PRNG(9764)) {
    dut: PRNG => new PRNGTest(dut)
  }
}
