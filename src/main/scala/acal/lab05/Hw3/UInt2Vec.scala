package acal.lab05.Hw3

import chisel3._
import chisel3.util._

class UInt2Vec(cnt: Int, width: Int)
    extends Module {
  val io = IO(new Bundle {
    val uint = Input(UInt((cnt  * width).W))
    val vec  = Output(Vec(cnt, UInt(width.W)))
  })

  for (i <- 0 until cnt) {
    io.vec(i) := io.uint((i + 1) * width - 1,
                         i * width)
  }
}

/**
 * Convert UInt to Vector of UInt.
 */
object UInt2Vec {
  def apply(cnt: Int, width: Int)(
      uint: UInt) = {
    val convert = Module(new UInt2Vec(cnt, width))

    convert.io.uint := uint

    convert.io.vec
  }
}
