package acal.lab05.Hw1

import chisel3._
import chisel3.util._
import acal.lab05.Lab1.SevenSeg

/**
 * `isForward` (bool): `1` mean count according to
 * `step`, `0` means count according to `-step`
 */
class AdvanceCounter(from: Int = 0,
                     to: Int = 9,
                     step: Int = 1)
    extends Module {

  def getWidth(n: Int) = n.S.getWidth
  // Width of this counter
  val cntWidth =
    Math.max(getWidth(from), getWidth(to))

  // slot to display a number
  val valueSlotSize = 4
  val ssNumbers = Math
    .round(cntWidth.doubleValue / valueSlotSize)
    .toInt

  println(
    s"[AdvanceCounter] initialize cntWidth with: ${cntWidth}")

  val io = IO(new Bundle {
    // counting direction
    val isForward = Input(Bool())
    // output width determined by SevenSeg
    val value = Output(SInt(cntWidth.W))
  })

  val cntReg = RegInit(
    Mux(io.isForward,
        from.S(cntWidth.W),
        to.S(cntWidth.W)))

  cntReg := Mux(
    io.isForward,
    Mux(cntReg === to.S, from.S, cntReg + step.S),
    Mux(cntReg === from.S, to.S, cntReg - step.S)
  )

  io.value := cntReg
}

object AdvanceCounter {
  def apply(from: Int = 0,
            to:   Int = 9,
            step: Int = 1)(isForward: Bool,
                           value: SInt) = {
    // val ac = Module(new AdvanceCounter(from, to, step))
    val ac = Module(
      new AdvanceCounter(from = from,
                         to = to,
                         step = step))

    ac.io.isForward := isForward
    value           := ac.io.value

    ac
  }
}
