package acal.lab05.Hw2

import chisel3._
import chisel3.util.log2Ceil

class Queue(width: Int, depth: Int)
    extends Module
    with SingleSideList {

  val io = IO(new SingleSideListIO {
    val push   = Input(Bool())
    val pop    = Input(Bool())
    val en     = Input(Bool())
    val clear  = Input(Bool())
    val dataIn = Input(UInt(width.W))
    val size   = Output(UInt(log2Ceil(depth + 1).W))
    val dataOut = Output(UInt(width.W))
    val subscriptIdx =
      Input(UInt(log2Ceil(depth + 1).W))
    val subscriptDataOut = Output(UInt(width.W))
  })

  val mem = Mem(depth, UInt(width.W))

  val ptrInit = 0.U(log2Ceil(depth + 1).W)
  val front = RegInit(ptrInit)
  val rear  = RegInit(ptrInit)
  val out   = RegInit(0.U(width.W))

  io.subscriptDataOut := 0.U

  when(io.clear) {
    mem.write(0.U, 0.asUInt((depth * width).W))
    front := ptrInit
    rear := ptrInit
  }

  when(io.en) {
    when(io.push && (rear < depth.asUInt)) {
      mem(rear) := io.dataIn
      rear      := rear + 1.U
    }.elsewhen(io.pop && (front < rear)) {
      front := front + 1.U
    }
    when(rear > front) {
      out := mem(front)
    }.elsewhen(rear === front && io.push) {
      out := io.dataIn
    }.otherwise {
      out := 0.U
    }
    when(
      io.subscriptIdx >= front && io.subscriptIdx < rear) {
      io.subscriptDataOut := mem(io.subscriptIdx)
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
    val q = Module(
      new Queue(width = width, depth = depth))

    q.io.push         := push
    q.io.pop          := pop
    q.io.en           := en
    q.io.clear        := clear
    q.io.dataIn       := dataIn
    size              := q.io.size
    dataOut           := q.io.dataOut
    q.io.subscriptIdx := subscriptIdx
    subscriptDataOut  := q.io.subscriptDataOut

    q
  }
}
