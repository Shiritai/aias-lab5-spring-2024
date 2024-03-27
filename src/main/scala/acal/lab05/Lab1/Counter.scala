package acal.lab05.Lab1

import chisel3._
import chisel3.util._

class Counter(upTo: Int) extends Module {
  val io = IO(new Bundle {
    val display = Output(UInt(7.W))
  })

  val cntReg = RegInit(0.U(4.W))
  cntReg := Mux(cntReg === upTo.U,
                0.U,
                cntReg + (1.U))

  val ss = Module(new SevenSeg())
  ss.io.num  := cntReg
  io.display := ss.io.display
}
