package acal.lab05.Lab1

import chisel3._
import chisel3.util._
import chisel3.iotesters.{Driver, PeekPokeTester}

class CounterTest(dut: Counter)
    extends PeekPokeTester(dut) {

  def int2string(i: Int): String = {
    String
      .format("%" + 7 + "s", i.toBinaryString)
      .replace(' ', '0')
  }

  Seq("1111110", // 0
      "0110000", // 1
      "1101101", // 2, etc...
      "1111001",
      "0110011",
      "1011011",
      "1011111",
      "1110000",
      "1111111",
      "1111011").foreach(golden => {
    var xString =
      int2string(peek(dut.io.display).toInt)
    expect(golden == xString,
           s"Invalid display: ${xString}, should be ${golden}")
    step(1)
  })
}

object CounterTest extends App {
  Driver.execute(args, () => new Counter(9)) {
    dut: Counter => new CounterTest(dut)
  }
}
