package acal.lab05.Lab2

import chisel3.iotesters.{Driver, PeekPokeTester}

class IntGenTest(dut: IntGen)
    extends PeekPokeTester(dut) {

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

  "1234=".foreach { x =>
    poke(dut.io.key_in, dict(x))
    step(1)
  }
  expect(dut.io.value.bits, 1234)

  println("Input: 1234= ")
  println(
    "Output : " + peek(dut.io.value.bits).toString)

  step(1)

  Seq('5', '4', '3', '2', '=').foreach { x =>
    poke(dut.io.key_in, dict(x))
    step(1)
  }
  expect(dut.io.value.bits,
         Seq('5', '4', '3', '2')
           .map { dict(_) }
           .reduceLeft(10 * _ + _))
  println("Input: 5432= ")
  println(
    "Output : " + peek(dut.io.value.bits).toString)

  step(1)
}

object IntGenTest extends App {
  Driver.execute(args, () => new IntGen) {
    c: IntGen => new IntGenTest(c)
  }
}
