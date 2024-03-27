package acal.lab05.Hw3

import chisel3._
import chisel3.util._

class NumGuess(seed: Int = 1) extends Module {
  require(seed > 0, "Seed cannot be 0")

  val io = IO(new Bundle {
    val gen     = Input(Bool())
    val guess   = Input(UInt(16.W))
    val puzzle  = Output(Vec(4, UInt(4.W)))
    val ready   = Output(Bool())
    val g_valid = Output(Bool())
    val A       = Output(UInt(3.W))
    val B       = Output(UInt(3.W))

    // don't care at Hw6-3-2 but should be considered at Bonus
    val s_valid = Input(Bool())
  })

  io.puzzle  := Vec(4, 0.U)
  io.ready   := false.B
  io.g_valid := false.B
  io.A       := 0.U
  io.B       := 0.U
}
