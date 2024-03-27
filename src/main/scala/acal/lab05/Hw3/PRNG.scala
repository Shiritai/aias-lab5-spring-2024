package acal.lab05.Hw3

import chisel3._
import chisel3.util._

class PRNG(seed: Int) extends Module {
  val io = IO(new Bundle {
    val gen    = Input(Bool())
    val puzzle = Output(Vec(4, UInt(4.W)))
    val ready  = Output(Bool())
  })

  io.puzzle := Vec(4, 0.U)
  io.ready  := false.B
}
