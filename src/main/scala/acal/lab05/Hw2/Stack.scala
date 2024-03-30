package acal.lab05.Hw2

import chisel3._
import chisel3.util.log2Ceil

class Stack(width: Int, depth: Int) extends Module {
  val io = IO(new Bundle {
    val push   = Input(Bool())
    val pop    = Input(Bool())
    val en     = Input(Bool())
    val dataIn = Input(UInt(width.W))
    val size   = Output(UInt(log2Ceil(depth + 1).W))
    val dataOut = Output(UInt(width.W))
  })

  val stackMem = Mem(depth, UInt(width.W))
  val sp       = RegInit(0.U(log2Ceil(depth + 1).W))
  val out      = RegInit(0.U(width.W))

  println(s"depth: ${depth}")

  when(io.en) {
    when(io.push && (sp < depth.asUInt)) {
      stackMem(sp) := io.dataIn
      sp           := sp + 1.U
    }.elsewhen(io.pop && (sp > 0.U)) {
      sp := sp - 1.U
    }
    when(sp > 0.U) {
      out := stackMem(sp - 1.U)
    }
  }

  io.dataOut := out
  io.size    := sp
}

object Stack {
  def apply(width: Int, depth: Int)() = {
    Module(new Stack(width = width, depth = depth))
  }

  def apply(width: Int,
            depth:   Int,
            push:    Bool,
            pop:     Bool,
            en:      Bool,
            dataIn:  UInt,
            size:    UInt,
            dataOut: UInt) = {
    val st = Module(
      new Stack(width = width, depth = depth))

    st.io.push   := push
    st.io.pop    := pop
    st.io.en     := en
    st.io.dataIn := dataIn
    size         := st.io.size
    dataOut      := st.io.dataOut

    st
  }
}
