package acal.lab05.Lab1

import scala.util.Random
import chisel3.iotesters.{Driver, PeekPokeTester}

class TrafficLightTest(tl: TrafficLight)
    extends PeekPokeTester(tl) {
  def int2string(i: Int): String = {
    String
      .format("%" + 7 + "s", i.toBinaryString)
      .replace(' ', '0')
  }

  val decode = Map(
    "1111110" -> "0",
    "0110000" -> "1",
    "1101101" -> "2",
    "1111001" -> "3",
    "0110011" -> "4",
    "1011011" -> "5",
    "1011111" -> "6",
    "1110000" -> "7",
    "1111111" -> "8",
    "1111011" -> "9"
  )

  for (i <- 0 until 10) {
    step(1)
    var xString = int2string(
      peek(dut.io.display).toInt)
    println("timer:" + decode(xString))
  }

}

object TrafficLightTest extends App {
  val yTime = 3
  val gTime = 7
  Driver.execute(
    args,
    () => new TrafficLight(yTime, gTime)) { c =>
    new TrafficLightTest(c)
  }
}
