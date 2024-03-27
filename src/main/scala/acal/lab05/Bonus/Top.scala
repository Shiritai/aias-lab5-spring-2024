package acal.lab05.Bonus

import chisel3._
import chisel3.util._
import acal.lab05.Hw3.NumGuess

class Top extends Module {
  val io = IO(new Bundle {
    val gen    = Input(Bool())
    val finish = Output(Bool())
  })

  io.finish := false.B
}
