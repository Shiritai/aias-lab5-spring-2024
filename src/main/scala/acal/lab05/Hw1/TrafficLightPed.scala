package acal.lab05.Hw1

import chisel3._
import chisel3.util._

class TrafficLightPed(yTime: Int,
                      gTime: Int,
                      pTime: Int)
    extends Module {
  val io = IO(new Bundle {
    val pButton  = Input(Bool())
    val hTraffic = Output(UInt(2.W))
    val vTraffic = Output(UInt(2.W))
    val pTraffic = Output(UInt(2.W))
    val timer    = Output(UInt(5.W))
  })

  val off :: red :: yellow :: green :: Nil = Enum(4)

  /**
   * `s`: state, `H`: horizontal, `V`: vertical,
   * `r`: red, `y`: yellow, `g`: green.
   */
  val sIdle :: sHgVr :: sHyVr :: sHrVg :: sHrVy :: sPG :: Nil =
    Enum(6)

  // State register
  val state = RegInit(sIdle)

  // please implement your code below...
  io.hTraffic := 0.U
  io.vTraffic := 0.U
  io.pTraffic := 0.U
  io.timer    := 0.U
}
