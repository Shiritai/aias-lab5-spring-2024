package acal.lab05.Hw1

import chisel3._
import chisel3.util._
import acal.lab05.Lab1.SevenSeg

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

  // State of traffic lights
  val off :: red :: yellow :: green :: Nil = Enum(4)

  /**
   * `s`: state, `H`: horizontal, `V`: vertical,
   * `r`: red, `y`: yellow, `g`: green, `P`:
   * pedestrian
   */
  val sIdle :: sHgVr :: sHyVr :: sHrVg :: sHrVy :: sPg :: Nil =
    Enum(6)

  val timeRange =
    Seq(yTime, gTime, pTime).reduce(Math.max)

  println(s"timeRange is ${timeRange}")

  val stateCache = RegInit(sIdle)
  val inject     = Wire(UInt(5.W))
  val isCntToEnd = io.timer === 0.U | io.pButton

  AdvanceCounter(0, timeRange)(
    value    = io.timer,
    revert   = true.B, // always down count
    toInject = isCntToEnd,
    inject   = inject
  )

  // State register
  val state = RegInit(sIdle)

  /**
   * @brief
   *   Determine:
   *   - current traffic light
   *   - if count to end of current state:
   *     - Next state
   *     - Next injection value of timer
   */
  state       := state
  io.hTraffic := off
  io.vTraffic := off
  io.pTraffic := off
  inject      := 0.U
  stateCache  := stateCache
  switch(state) {
    is(sIdle) {
      state := sHgVr
    }
    is(sHgVr) {
      io.hTraffic := green
      io.vTraffic := red
      io.pTraffic := red
      when(isCntToEnd) {
        inject := (yTime - 1).U
        state  := sHyVr
      }
    }
    is(sHyVr) {
      io.hTraffic := yellow
      io.vTraffic := red
      io.pTraffic := red
      when(isCntToEnd) {
        inject := (gTime - 1).U
        state  := sHrVg
      }
    }
    is(sHrVg) {
      io.hTraffic := red
      io.vTraffic := green
      io.pTraffic := red
      when(isCntToEnd) {
        inject := (yTime - 1).U
        state  := sHrVy
      }
    }
    is(sHrVy) {
      io.hTraffic := red
      io.vTraffic := yellow
      io.pTraffic := red
      when(isCntToEnd) {
        inject := (gTime - 1).U
        state  := sHgVr
      }
    }
    is(sPg) {
      io.hTraffic := red
      io.vTraffic := red
      io.pTraffic := green
      when(isCntToEnd) {
        state := stateCache
        when(
          stateCache === sHgVr || stateCache === sHrVg) {
          inject := (gTime - 1).U
        }.otherwise {
          inject := (yTime - 1).U
        }
      }
    }
  }

  when(io.pButton) {

    /**
     * Cache last state and update it if needed.
     * Notice that if state is sPg, we should NOT
     * override cached old state, otherwise we can't
     * get back to origin state
     */
    when(state =/= sPg) {
      stateCache := state
    }
    state  := sPg
    inject := (pTime - 1).U
    // set what we shall do in sPg state
    io.hTraffic := red
    io.vTraffic := red
    io.pTraffic := green
  }
}
