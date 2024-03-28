package acal.lab05.Lab1

import chisel3._
import chisel3.util._
import acal.lab05.Hw1.AdvanceCounter

class Counter(upTo: Int) extends Module {
  val io = IO(new Bundle {
    val display = Output(UInt(7.W))
  })

  val res = Wire(UInt((4 + 1).W))

  AdvanceCounter(from = 0, to = upTo, step = 1)(
    true.B,
    res)

  SevenSeg(res, io.display)
}
