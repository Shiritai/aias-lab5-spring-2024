package acal.lab05.Hw1

import chisel3._
import chisel3.util._
import chisel3.iotesters.{Driver, PeekPokeTester}
import scala.util.Random

class AdvanceCounterTest(counter: AdvanceCounter,
                         from: Int,
                         to: Int)
    extends PeekPokeTester(counter) {
  // Use software to simulate the same
  // logic as AdvanceCounter
  var value  = from
  var reset  = false
  var enable = true
  var revert = false

  val kinds = 5
  val norm :: flipRst :: flipEn :: flipRev :: doInject :: Nil =
    Enum(kinds).map(n => n.toInt)

  var genRnd = new Random()
  for (_ <- 0 until 1000) { // try 1000 random tests
    val op = genRnd.nextInt(kinds)
    val inject = 
      genRnd.nextInt(to - from + 1) + from;

    expect(
      counter.io.value,
      value,
      s"${counter.io.value} != golden: ${value}")

    op match {
      case `norm` => {}
      case `flipRst` => {
        reset = !reset
      }
      case `flipEn` => {
        enable = !enable
      }
      case `flipRev` => {
        revert = !revert
      }
      case `doInject` => {
        poke(counter.io.toInject, true.B)
        poke(counter.io.inject, inject.U)
      }
    }

    poke(counter.io.reset, reset.B)
    poke(counter.io.enable, enable.B)
    poke(counter.io.revert, revert.B)
    if (op != doInject) {
        poke(counter.io.toInject, false.B)
    }

    value = if (reset) { if (revert) to else from }
    else {
      if (enable) {
        if (op == doInject) {
          inject
        } else {
          if (revert) {
            if (value == from) to else value - 1
          } else {
            if (value == to) from else value + 1
          }
        }
      } else {
        value
      }
    }

    step(1)
  }
}

object AdvanceCounterTest extends App {
  for (f <- 0 until 9) {
    for (t <- f + 1 until 10) {
      Driver.execute(
        args,
        () => new AdvanceCounter(f, t)) {
        counter: AdvanceCounter =>
          new AdvanceCounterTest(counter, f, t)
      }
    }
  }
}
