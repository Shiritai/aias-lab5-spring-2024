package acal.lab05.Hw3

import chisel3._
import chisel3.util._
import chisel3.iotesters.{Driver, PeekPokeTester}
import scala.util.Random
import scala.io.StdIn.readInt
import scala.language.implicitConversions

class NumGuessTest(dut: NumGuess)
    extends PeekPokeTester(dut) {

  implicit def bigint2boolean(b: BigInt): Boolean =
    if (b > 0) true else false

  poke(dut.io.s_valid, true)

  // Randomly Initial the puzzle
  step(Random.nextInt(50))
  poke(dut.io.gen, true)
  step(1)
  poke(dut.io.gen, false)
  while (!peek(dut.io.ready)) { step(1) }
  println(s"Puzzle:${peek(dut.io.puzzle(3))} ${peek(
      dut.io.puzzle(2))} ${peek(
      dut.io.puzzle(1))} ${peek(dut.io.puzzle(0))}")

  while (peek(dut.io.A) != 4) {
    // Input the guess when puzzle is ready
    var guess = 0
    for (i <- (0 until 4).reverse) {
      println(s"plz enter the ${i}'s digit :")
      var digit = readInt()
      guess = (guess << 4) + digit
    }
    poke(dut.io.guess, guess)
    while (!peek(dut.io.g_valid)) { step(1) }

    // print the outcome of each guess
    println(
      s"Guess:${guess >> 12 & 0xf} ${guess >> 8 & 0xf} ${guess >> 4 & 0xf} ${guess & 0xf}")
    println("A = " + peek(dut.io.A))
    println("B = " + peek(dut.io.B))

    step(1)
  }
}

object NumGuessTest extends App {
  Driver.execute(args, () => new NumGuess) {
    dut: NumGuess => new NumGuessTest(dut)
  }
}
