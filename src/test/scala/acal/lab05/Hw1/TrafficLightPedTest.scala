package acal.lab05.Hw1

import chisel3.iotesters.{Driver, PeekPokeTester}

class TrafficLightPedTest(tl: TrafficLightPed)
    extends PeekPokeTester(tl) {

  // for first round complete period without the effect of p_button
  step(25)

  // for 2nd round test : consider the effect of p_button
  step(14)
  poke(tl.io.pButton, true)
  step(1)
  poke(tl.io.pButton, false)
  step(17)
  poke(tl.io.pButton, true)
  step(1)
  poke(tl.io.pButton, false)

  step(50)

  println(
    "Simulation completed!! Go to check your vcd file!!!")
}

object TrafficLightPedTest extends App {
  val yTime = 3
  val gTime = 7
  val pTime = 5
  Driver.execute(
    args,
    () =>
      new TrafficLightPed(yTime, gTime, pTime)) {
    c => new TrafficLightPedTest(c)
  }
}
