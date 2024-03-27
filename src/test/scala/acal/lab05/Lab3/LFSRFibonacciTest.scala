package acal.lab05.Lab3

import chisel3.iotesters.{Driver, PeekPokeTester}

class LFSRFibonacciTest(dut: LFSRFibonacci)
    extends PeekPokeTester(dut) {

  def int2string(i: Int): String = {
    String
      .format("%" + 4 + "s", i.toBinaryString)
      .replace(' ', '0')
  }

  poke(dut.io.seed.bits, 9)
  poke(dut.io.seed.valid, true)
  step(1)

  for (i <- 0 until 16) {
    poke(dut.io.seed.valid, false)
    var out = peek(dut.io.rndNum).toInt
    println(int2string(out))
    step(1)
  }
}

object LFSRFibonacciTest extends App {
  Driver.execute(args,
                 () => new LFSRFibonacci(4)) {
    dut: LFSRFibonacci =>
      new LFSRFibonacciTest(dut)
  }
}

// class LFSRFibonacci (n:Int, seed:Int = 1)extends Module
