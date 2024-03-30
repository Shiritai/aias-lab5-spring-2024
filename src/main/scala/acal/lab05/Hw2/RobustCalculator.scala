package acal.lab05.Hw2

import chisel3._
import chisel3.util._

class RobustCalculator extends Module {
  val io = IO(new Bundle {
    val keyIn = Input(UInt(4.W))
    val value  = Output(Valid(UInt(32.W)))
  })

}
