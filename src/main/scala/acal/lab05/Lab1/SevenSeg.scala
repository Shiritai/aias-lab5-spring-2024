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
    is("h0".U(4.W)) { display := "b1111110".U }
    is("h1".U(4.W)) { display := "b0110000".U }
    is("h2".U(4.W)) { display := "b1101101".U }
    is("h3".U(4.W)) { display := "b1111001".U }
    is("h4".U(4.W)) { display := "b0110011".U }
    is("h5".U(4.W)) { display := "b1011011".U }
    is("h6".U(4.W)) { display := "b1011111".U }
    is("h7".U(4.W)) { display := "b1110000".U }
    is("h8".U(4.W)) { display := "b1111111".U }
    is("h9".U(4.W)) { display := "b1111011".U }
    is("ha".U(4.W)) { display := "b1110111".U }
    is("hb".U(4.W)) { display := "b0011111".U }
    is("hc".U(4.W)) { display := "b1001110".U }
    is("hd".U(4.W)) { display := "b0111101".U }
    is("he".U(4.W)) { display := "b1001111".U }
    is("hf".U(4.W)) { display := "b1000111".U }
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
