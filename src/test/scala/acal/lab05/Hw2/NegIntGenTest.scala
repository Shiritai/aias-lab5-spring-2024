package acal.lab05.Hw2

import chisel3.iotesters.{Driver, PeekPokeTester}
import scala.language.implicitConversions

class NegIntGenTest(dut: NegIntGen)
    extends PeekPokeTester(dut) {
  implicit def bigint2boolean(b: BigInt): Boolean =
    if (b > 0) true else false

  val dict = Map(
    '0' -> 0,
    '1' -> 1,
    '2' -> 2,
    '3' -> 3,
    '4' -> 4,
    '5' -> 5,
    '6' -> 6,
    '7' -> 7,
    '8' -> 8,
    '9' -> 9,
    '+' -> 10,
    '-' -> 11,
    '*' -> 12,
    '(' -> 13,
    ')' -> 14,
    '=' -> 15
  )

  // Test 1
  "(-1234)=".foreach { x =>
    poke(dut.io.key_in, dict(x))
    step(1)
  }
  while (!peek(dut.io.value.valid)) { step(1) }

  if (peek(dut.io.value.bits).toInt != (-1234)) {
    println(
      "Test 1 : (-1234)= failed , your output is " + peek(
        dut.io.value.bits).toInt.toString)
  } else {
    println("Test 1 : pass!")
  }
  step(1)

  // Test 2
  Seq('(', '-', '5', '4', '2', '1', ')', '=')
    .foreach { x =>
      poke(dut.io.key_in, dict(x))
      step(1)
    }
  while (!peek(dut.io.value.valid)) { step(1) }
  if (peek(dut.io.value.bits).toInt != (-5421)) {
    println(
      "Test 2 : (-5421)= failed , your output is " + peek(
        dut.io.value.bits).toInt.toString)
  } else {
    println("Test 2 : pass!")
  }
  step(1)

  // Test 3
  "5487=".foreach { x =>
    poke(dut.io.key_in, dict(x))
    step(1)
  }
  while (!peek(dut.io.value.valid)) { step(1) }
  if (peek(dut.io.value.bits).toInt != (5487)) {
    println(
      "Test 3 : 5487= failed , your output is " + peek(
        dut.io.value.bits).toInt.toString)
  } else {
    println("Test 3 : pass!")
  }
  step(1)
}

object NegIntGenTest extends App {
  Driver.execute(args, () => new NegIntGen) {
    c: NegIntGen => new NegIntGenTest(c)
  }
}
