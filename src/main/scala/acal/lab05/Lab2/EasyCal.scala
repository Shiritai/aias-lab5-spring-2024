package acal.lab05.Lab2

import chisel3._
import chisel3.util._

class EasyCal extends Module {
  val io = IO(new Bundle {
    val keyIn = Input(UInt(4.W))
    val value  = Output(Valid(UInt(32.W)))
  })

  // Wire Declaration===================================
  val operator = WireDefault(false.B)
  operator := io.keyIn >= 10.U && io.keyIn <= 12.U

  val num = WireDefault(false.B)
  num := io.keyIn < 10.U

  val equal = WireDefault(false.B)
  equal := io.keyIn === 15.U

  // Reg Declaration====================================
  val in_buffer = RegNext(io.keyIn)
  val src1      = RegInit(0.U(32.W))
  val op        = RegInit(0.U(2.W))
  val src2      = RegInit(0.U(32.W))

  // State and Constant Declaration=====================
  val sIdle :: sSrc1 :: sOp :: sSrc2 :: sEqual :: Nil =
    Enum(5)
  val add = 0.U
  val sub = 1.U
  val mul = 2.U

  val state = RegInit(sIdle)

  // Next State Decoder
  switch(state) {
    is(sIdle) {
      state := sSrc1
    }
    is(sSrc1) {
      when(operator) { state := sOp }
    }
    is(sOp) {
      when(num) { state := sSrc2 }
    }
    is(sSrc2) {
      when(equal) { state := sEqual }
    }
    is(sEqual) {
      state := sSrc1
    }
  }
  // ==================================================

  when(state === sSrc1) {
    src1 := (src1 << 3.U) + (src1 << 1.U) + in_buffer
  }
  when(state === sSrc2) {
    src2 := (src2 << 3.U) + (src2 << 1.U) + in_buffer
  }
  when(state === sOp) { op := in_buffer - 10.U }

  when(state === sEqual) {
    src1      := 0.U
    src2      := 0.U
    op        := 0.U
    in_buffer := 0.U
  }

  io.value.valid := Mux(state === sEqual,
                        true.B,
                        false.B)
  io.value.bits := MuxLookup(op,
                             0.U,
                             Seq(
                               add -> (src1 + src2),
                               sub -> (src1 - src2),
                               mul -> (src1 * src2)
                             ))
}
