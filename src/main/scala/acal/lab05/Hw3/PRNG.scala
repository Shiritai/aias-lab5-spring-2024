package acal.lab05.Hw3

import chisel3._
import chisel3.util._

class PRNG(seed: Int) extends Module {
  val digitWidth = 4
  val digitCnt   = 4

  val io = IO(new Bundle {
    val gen = Input(Bool())
    val puzzle =
      Output(Vec(digitCnt, UInt(digitWidth.W)))
    val ready = Output(Bool())
  })

  // registers
  val rgs = RegInit(
    VecInit(Seq(0x5, 0x3, 0x7, 0x8).map(n =>
      n.U(digitWidth.W))))

  io.puzzle := rgs

  val nxtRgs = (rgs.asUInt() << 1 | (rgs(2)(
    2) ^ rgs(3)(0) ^ rgs(3)(1) ^ rgs(3)(3)))(
    digitWidth * digitCnt - 1,
    0)
  val nxtRgVec =
    UInt2Vec(digitCnt, digitWidth)(nxtRgs)

  val isInRange = nxtRgVec
    .map { case n => n <= 9.U }
    .reduce((a, b) => a & b)

  val noReputation = nxtRgVec
    .combinations(2)
    .map(e => e(0) =/= e(1))
    .reduce((a, b) => a & b)

  val isValid = isInRange && noReputation

  val sIdle :: sGen :: Nil = Enum(2)
  val state                = RegInit(sIdle)

  io.ready  := true.B
  io.puzzle := rgs

  switch(state) {
    is(sIdle) {
      when(io.gen) {
        state    := sGen
        io.ready := false.B
      }
    }
    is(sGen) {
      io.ready := false.B
      rgs      := nxtRgVec
      when(isValid) {
        state := sIdle
      }
    }
  }
}

object PRNG {
  def apply(seed: Int)(gen: Bool,
                       ready:  Bool,
                       puzzle: Vec[UInt]) = {
    val p = Module(new PRNG(seed))

    p.io.gen := gen
    ready    := p.io.ready
    puzzle   := p.io.puzzle

    p
  }
}
