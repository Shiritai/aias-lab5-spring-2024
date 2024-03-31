package acal.lab05.Hw2

import chisel3._
import chisel3.util.log2Ceil

class Queue(width: Int, depth: Int)
    extends Module with SingleSideList {

val io = IO(new SingleSideListIO {
  val push   = Input(Bool())
  val pop    = Input(Bool())
  val en     = Input(Bool())
  val dataIn = Input(UInt(width.W))
  val size   = Output(UInt(log2Ceil(depth + 1).W))
  val dataOut = Output(UInt(width.W))
})

val mem = Mem(depth, UInt(width.W))


  val front = RegInit(0.U(log2Ceil(depth + 1).W))
  val rear  = RegInit(0.U(log2Ceil(depth + 1).W))
  val out   = RegInit(0.U(width.W))

  when(io.en) {
    when(io.push && (rear < depth.asUInt)) {
      mem(rear) := io.dataIn
      rear      := rear + 1.U
    }.elsewhen(io.pop && (front < rear)) {
      front := front + 1.U
    }
    when(rear > 0.U) {
      out := mem(front)
    }
  }

  io.dataOut := out
  io.size    := rear - front
}

object Queue {
  def apply(width: Int, depth: Int)() = {
    Module(new Queue(width = width, depth = depth))
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
      new Queue(width = width, depth = depth))

    st.io.push   := push
    st.io.pop    := pop
    st.io.en     := en
    st.io.dataIn := dataIn
    size         := st.io.size
    dataOut      := st.io.dataOut

    st
  }
}
