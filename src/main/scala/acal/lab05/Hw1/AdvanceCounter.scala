package acal.lab05.Hw1

import chisel3._
import chisel3.util._
import acal.lab05.Lab1.SevenSeg

/**
 * `revert` (bool): `1` mean count according to
 * `step`, `0` means count according to `-step`
 */
class AdvanceCounter(from: Int = 0,
                     to: Int = 9,
                     step: Int = 1)
    extends Module {

  val msgPrefix = "[AdvanceCounter]"

  assert(
    step >= 0,
    s"${msgPrefix} step of counter must >= 0, but get ${step}")

  def getWidth(n: Int) = n.U.getWidth
  // Width of this counter
  val cntWidth =
    Math.max(getWidth(from), getWidth(to))

  println(
    s"${msgPrefix} initialize cntWidth with: ${cntWidth}")

  val io = IO(new Bundle {
    // counting direction
    val reset    = Input(Bool())
    val enable   = Input(Bool())
    val revert   = Input(Bool())
    val toInject = Input(Bool())
    val inject   = Input(UInt(cntWidth.W))
    // output width determined by SevenSeg
    val value = Output(UInt(cntWidth.W))
  })

  val initValue = Mux(~io.revert,
                      from.U(cntWidth.W),
                      to.U(cntWidth.W))

  val cntReg = RegInit(initValue)

  /**
   * Normal up-or-down count value
   */
  val normCntValue =
    Mux(
      ~io.revert,
      Mux(cntReg === to.U, from.U, cntReg + step.U),
      Mux(cntReg === from.U, to.U, cntReg - step.U)
    )

  /**
   * Next value considering value injection
   */
  val nextValue =
    Mux(io.toInject, io.inject, normCntValue)

  cntReg := Mux(io.reset,
                initValue,
                Mux(io.enable, nextValue, cntReg))
  io.value := cntReg
}

object AdvanceCounter {
  def apply(from: Int = 0,
            to:   Int = 9,
            step: Int = 1)(value: UInt,
                           reset:    Bool = false.B,
                           enable:   Bool = true.B,
                           revert:   Bool = false.B,
                           toInject: Bool = false.B,
                           inject:   UInt = 0.U) = {
    val ac = Module(
      new AdvanceCounter(from = from,
                         to = to,
                         step = step))

    ac.io.reset    := reset
    ac.io.enable   := enable
    ac.io.revert   := revert
    ac.io.toInject := toInject
    ac.io.inject   := inject
    value          := ac.io.value

    ac
  }
}
