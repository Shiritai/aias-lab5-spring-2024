package acal.lab05.Hw2

import chisel3.iotesters.{Driver, PeekPokeTester}
import scala.language.implicitConversions

class LongCalTest(dut: LongCal)
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
  "(-15)-15-(-15)+(-15)=".foreach { x =>
    poke(dut.io.key_in, dict(x))
    step(1)
  }
  while (peek(dut.io.value.valid) == 0) {
    step(1)
  }
  if (peek(dut.io.value.bits).toInt != (-30)) {
    println(
      "Test 1 : (-15)-15-(-15)+(-15)= failed , your output is " + peek(
        dut.io.value.bits).toInt.toString)
  } else {
    println("Test 1 :  pass")
  }
  step(1)

  // Test 2
  "17-16+(-15)-14+13-12+(-11)=".foreach { x =>
    poke(dut.io.key_in, dict(x))
    step(1)
  }
  while (peek(dut.io.value.valid) == 0) {
    step(1)
  }
  if (peek(dut.io.value.bits).toInt != (-38)) {
    println(
      "Test 2 : 17-16+(-15)-14+13-12+(-11)= failed , your output is " + peek(
        dut.io.value.bits).toInt.toString)
  } else {
    println("Test 2 :  pass")
  }
  step(1)

  // Test 3
  "(-15)=".foreach { x =>
    poke(dut.io.key_in, dict(x))
    step(1)
  }
  while (peek(dut.io.value.valid) == 0) {
    step(1)
  }
  if (peek(dut.io.value.bits).toInt != (-15)) {
    println(
      "Test 3 : (-15)= failed , your output is " + peek(
        dut.io.value.bits).toInt.toString)
  } else {
    println("Test 3 :  pass")
  }
  step(1)

  // Test 4
  "15=".foreach { x =>
    poke(dut.io.key_in, dict(x))
    step(1)
  }
  while (peek(dut.io.value.valid) == 0) {
    step(1)
  }
  if (peek(dut.io.value.bits).toInt != 15) {
    println(
      "Test 4 : 15= failed , your output is " + peek(
        dut.io.value.bits).toInt.toString)
  } else {
    println("Test 4 :  pass")
  }
  step(1)
}

object LongCalTest extends App {
  Driver.execute(args, () => new LongCal) {
    c: LongCal => new LongCalTest(c)
  }
}
