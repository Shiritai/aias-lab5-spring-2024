package acal.lab05.Hw2

import chisel3.iotesters.{Driver, PeekPokeTester}

class RobustCalculatorTest(cal: RobustCalculator)
    extends PeekPokeTester(cal) {
    
}

object RobustCalculatorTest extends App {
  Driver.execute(args, () => new RobustCalculator) {
    c: RobustCalculator => new RobustCalculatorTest(c)
  }
}
