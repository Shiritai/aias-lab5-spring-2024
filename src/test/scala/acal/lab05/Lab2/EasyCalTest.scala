package acal.lab05.Lab2

import chisel3.iotesters.{Driver, PeekPokeTester}

class EasyCalTest(dut: EasyCal)
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

  "1234-234=".foreach { x =>
    poke(dut.io.key_in, dict(x))
    step(1)
  }
  expect(dut.io.value.bits, 1000)
  step(1)

  "12*5=".foreach { x =>
    poke(dut.io.key_in, dict(x))
    step(1)
  }
  expect(dut.io.value.bits, 60)
  step(1)

  "3-18=".foreach { x =>
    poke(dut.io.key_in, dict(x))
    step(1)
  }
  // expect(dut.io.value.bits,-15)
  assert(peek(dut.io.value.bits).toInt == (-15))
  step(1)

}

object EasyCalTest extends App {
  Driver.execute(args, () => new EasyCal) {
    c: EasyCal => new EasyCalTest(c)
  }
}
