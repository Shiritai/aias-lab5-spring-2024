package acal.lab05.Hw2

import chisel3._
import chisel3.util._

class LongCal extends Module {
  val io = IO(new Bundle {
    val keyIn = Input(UInt(4.W))
    val value  = Output(Valid(UInt(32.W)))
  })

  // please implement your code below
  io.value.valid := false.B
  io.value.bits  := 0.U
}
