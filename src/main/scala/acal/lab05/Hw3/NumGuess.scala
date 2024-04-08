package acal.lab05.Hw3

import chisel3._
import chisel3.util._

class NumGuess(seed: Int = 1) extends Module {
  require(seed > 0, "Seed cannot be 0")

  val digitWidth = 4
  val digitCnt   = 4

  val io = IO(new Bundle {
    val gen   = Input(Bool())
    val guess = Input(UInt((digitCnt * digitWidth).W))
    val puzzle =
      Output(Vec(digitCnt, UInt(digitWidth.W)))
    val ready   = Output(Bool())
    val g_valid = Output(Bool())
    val A       = Output(UInt(3.W))
    val B       = Output(UInt(3.W))

    // don't care at Hw6-3-2 but should be considered at Bonus
    val s_valid = Input(Bool())
  })

  PRNG(seed)(io.gen, io.ready, io.puzzle)
  val guessVec =
    UInt2Vec(digitCnt, digitWidth)(io.guess)

  io.g_valid := false.B

  def countSame(ls: Seq[(UInt, UInt)]) = {
    ls.map { case (a, b) => (a === b).asUInt() }
      .reduce((a, b) => (0.U(3.W) | a) + b)
  }

  io.A := countSame(io.puzzle.zip(guessVec))

  // ref 1: https://stackoverflow.com/questions/27101500/scala-permutations-using-two-lists#comment107534190_56796943
  // ref 2: https://stackoverflow.com/questions/7949785/scalas-for-comprehension-if-statements
  io.B := countSame(for {
    (p, pi) <- io.puzzle.zipWithIndex;
    (g, gi) <- guessVec.zipWithIndex; if pi != gi
  } yield (p, g))

  val sIdle :: sGuess :: Nil = Enum(2)
  val state                  = RegInit(sIdle)

  switch(state) {
    is(sIdle) {
      when(io.ready) {
        state := sGuess
      }
    }
    is(sGuess) {
      io.g_valid := true.B
    }
  }
}
