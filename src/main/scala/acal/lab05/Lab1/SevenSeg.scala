package acal.lab05.Lab1

import chisel3._
import chisel3.util._

class SevenSeg extends Module {
  val io = IO(new Bundle {
    val num = Input(UInt(4.W)) // 0~9 need 4 bit
    val display = Output(UInt(7.W))
  })

  val display = Wire(UInt(7.W))
  display := 0.U // Default

  switch(io.num) {
    is(0.U) { display := "b1111110".U }
    is(1.U) { display := "b0110000".U }
    is(2.U) { display := "b1101101".U }
    is(3.U) { display := "b1111001".U }
    is(4.U) { display := "b0110011".U }
    is(5.U) { display := "b1011011".U }
    is(6.U) { display := "b1011111".U }
    is(7.U) { display := "b1110000".U }
    is(8.U) { display := "b1111111".U }
    is(9.U) { display := "b1111011".U }
  }

  io.display := display
}

object SevenSeg {
  def apply() = {
    Module(new SevenSeg())
  }

  def apply(num: UInt, display: UInt) = {
    val ss = Module(new SevenSeg())

    ss.io.num := num
    display   := ss.io.display
  }
}
