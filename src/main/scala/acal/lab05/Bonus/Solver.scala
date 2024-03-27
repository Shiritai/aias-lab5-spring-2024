package acal.lab05.Bonus

import chisel3._
import chisel3.util._

class Solver extends Module {

  val io = IO(new Bundle {
    val A       = Input(UInt(3.W))
    val B       = Input(UInt(3.W))
    val ready   = Input(Bool())
    val guess   = Output(Vec(4, UInt(4.W)))
    val g_valid = Input(Bool())
    val s_valid = Output(Bool())
    val finish  = Output(Bool())
  })

  io.guess   := Vec(4, 0.U)
  io.s_valid := false.B
  io.finish  := false.B
}
