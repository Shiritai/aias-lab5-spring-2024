package acal.lab05.Lab3

import chisel3._, chisel3.util._

class LFSRGalois(n: Int) extends Module {

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

    // Right Barrel Shift Register
    (shiftReg.zipWithIndex).map { case (sr, i) =>
      sr := shiftReg((i + 1) % n)
    }

    // Galois LFSR
    LfsrTaps(n).map { x =>
      {
        shiftReg(x - 1) := shiftReg(x) ^ shiftReg(0)
      }
    }
  }

  io.rndNum := shiftReg.asUInt
}
