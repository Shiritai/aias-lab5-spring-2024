package acal.lab05.Lab3

import chisel3._, chisel3.util._

object LfsrTaps {
  def apply(size: Int): Seq[Int] = {
    size match {
      // Seq[Int] means the taps in LFSR
      case 4 => Seq(3) // p(x) = x^4+x^3+1
      case 8 =>
        Seq(6, 5, 4) // p(x) = x^8+x^6+x^5+x^4+1
      case 16 =>
        Seq(14,
            13,
            11
           ) // p(x) = x^16+x^14+x^13+x^11+1
      case _ =>
        throw new Exception(
          "No LFSR taps stored for requested size")
    }
  }
}

class LFSRFibonacci(n: Int) extends Module {

  val io = IO(new Bundle {
    val seed   = Input(Valid(UInt(n.W)))
    val rndNum = Output(UInt(n.W))
  })

  val shiftReg = RegInit(
    VecInit(Seq.fill(n)(false.B)))

  when(io.seed.valid) {
    shiftReg zip io.seed.bits.asBools map {
      case (l, r) => l := r
    }
  }.otherwise {

    // Barrel Shift Register
    (shiftReg.zipWithIndex).map { case (sr, i) =>
      sr := shiftReg((i + 1) % n)
    }
    // Fibonacci LFSR
    shiftReg(n - 1) := (LfsrTaps(n)
      .map(x => shiftReg(n - x))
      .reduce(_ ^ _)) ^ shiftReg(0)
  }

  io.rndNum := shiftReg.asUInt
}
