package acal.lab05.Lab2

import chisel3._
import chisel3.util._

class IntGen extends Module {
  val io = IO(new Bundle {
    val keyIn = Input(UInt(4.W))
    val value  = Output(Valid(UInt(32.W)))
  })

  val equal = WireDefault(false.B)
  equal := io.keyIn === 15.U

  val sIdle :: sAccept :: sEqual :: Nil = Enum(3)
  val state = RegInit(sIdle)
  // Next State Decoder
  switch(state) {
    is(sIdle) {
      state := sAccept
    }
    is(sAccept) {
      when(equal) { state := sEqual }
    }
    is(sEqual) {
      state := sAccept
    }
  }

  val in_buffer = RegNext(io.keyIn)

  val number = RegInit(0.U(32.W))
  when(state === sAccept) {
    number := (number << 3.U) + (number << 1.U) + in_buffer
  }.elsewhen(state === sEqual) {
    in_buffer := 0.U
    number    := 0.U
  }

  io.value.valid := Mux(state === sEqual,
                        true.B,
                        false.B)
  io.value.bits := number
}
