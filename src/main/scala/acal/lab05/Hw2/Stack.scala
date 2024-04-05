package acal.lab05.Hw2

import chisel3._
import chisel3.util.log2Ceil

class Stack(width: Int, depth: Int)
    extends Module
    with SingleSideList {
  
  val lim = log2Ceil(depth + 1).W

  val io = IO(new SingleSideListIO {
    val push   = Input(Bool())
    val pop    = Input(Bool())
    val en     = Input(Bool())
    val clear  = Input(Bool())
    val dataIn = Input(UInt(width.W))
    val size   = Output(UInt(lim))
    val dataOut = Output(UInt(width.W))
    val subscriptIdx =
      Input(UInt(lim))
    val subscriptDataOut = Output(UInt(width.W))
  })

  val mem = Mem(depth, UInt(width.W))

  val ptrInit = 0.U(lim)
  val sp  = RegInit(ptrInit)
  val out = RegInit(0.U(width.W))

  println(s"Stack depth: ${depth}")

  io.subscriptDataOut := 0.U

  when(io.clear) {
    mem.write(0.U, 0.asUInt((depth * width).W))
    sp := ptrInit
  }

  when(io.en) {
    when(io.push && (sp < depth.asUInt)) {
      mem(sp) := io.dataIn
      sp      := sp + 1.U
    }.elsewhen(io.pop && (sp > 0.U)) {
      sp := sp - 1.U
    }
    when(sp > 1.U && io.pop) {
      out := mem(sp - 2.U)
    }.elsewhen(sp === 0.U && io.push) {
      out := io.dataIn
    }.elsewhen(sp > 0.U) {
      out := mem(sp - 1.U)
    }.otherwise {
      out := 0.U
    }
    when(io.subscriptIdx < sp) {
      io.subscriptDataOut := mem(io.subscriptIdx)
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
            depth:        Int,
            push:         Bool,
            pop:          Bool,
            en:           Bool,
            dataIn:       UInt,
            size:         UInt,
            dataOut:      UInt,
            clear:        Bool = WireInit(false.B),
            subscriptIdx: UInt = WireInit(0.U),
            subscriptDataOut: UInt =
              WireInit(0.U)) = {
    val st = Module(
      new Stack(width = width, depth = depth))

    st.io.push         := push
    st.io.pop          := pop
    st.io.en           := en
    st.io.clear        := clear
    st.io.dataIn       := dataIn
    size               := st.io.size
    dataOut            := st.io.dataOut
    st.io.subscriptIdx := subscriptIdx
    subscriptDataOut   := st.io.subscriptDataOut

    st
  }
}
