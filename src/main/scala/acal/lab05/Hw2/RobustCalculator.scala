package acal.lab05.Hw2

import chisel3._
import chisel3.util._

class RobustCalculator extends Module {
  val io = IO(new Bundle {
    val keyIn = Input(UInt(4.W))
    val value = Output(Valid(UInt(32.W)))
  })

  val queuePush    = Wire(Bool())
  val queuePop     = Wire(Bool())
  val queueEn      = Wire(Bool())
  val queueDataIn  = Wire(UInt(4.W))
  val queueSize    = Wire(UInt(14.W))
  val queueDataOut = Wire(UInt(4.W))
  val queueIsEmpty = queueSize === 0.U
  Queue(width = 4,
        depth   = 16383,
        push    = queuePush,
        pop     = queuePop,
        en      = queueEn,
        dataIn  = queueDataIn,
        size    = queueSize,
        dataOut = queueDataOut)

  val symStackPush    = Wire(Bool())
  val symStackPop     = Wire(Bool())
  val symStackEn      = Wire(Bool())
  val symStackDataIn  = Wire(UInt(3.W))
  val symStackSize    = Wire(UInt(6.W))
  val symStackDataOut = Wire(UInt(3.W))
  val symStackIsEmpty = symStackSize === 0.U
  Stack(width = 3,
        depth   = 40,
        push    = symStackPush,
        pop     = symStackPop,
        en      = symStackEn,
        dataIn  = symStackDataIn,
        size    = symStackSize,
        dataOut = symStackDataOut)

  val endLvPush    = Wire(Bool())
  val endLvPop     = Wire(Bool())
  val endLvEn      = Wire(Bool())
  val endLvDataIn  = Wire(UInt(6.W))
  val endLvSize    = Wire(UInt(4.W))
  val endLvDataOut = Wire(UInt(6.W))
  val endLvIsEmpty = endLvSize === 0.U
  Stack(width = 6,
        depth   = 15,
        push    = endLvPush,
        pop     = endLvPop,
        en      = endLvEn,
        dataIn  = endLvDataIn,
        size    = endLvSize,
        dataOut = endLvDataOut)

  val postfixPush    = Wire(Bool())
  val postfixPop     = Wire(Bool())
  val postfixEn      = Wire(Bool())
  val postfixDataIn  = Wire(UInt(4.W))
  val postfixSize    = Wire(UInt(14.W))
  val postfixDataOut = Wire(UInt(4.W))
  val postfixIsEmpty = postfixSize === 0.U
  Stack(width = 4,
        depth   = 16383,
        push    = postfixPush,
        pop     = postfixPop,
        en      = postfixEn,
        dataIn  = postfixDataIn,
        size    = postfixSize,
        dataOut = postfixDataOut)

  val evaluatorPush    = Wire(Bool())
  val evaluatorPop     = Wire(Bool())
  val evaluatorEn      = Wire(Bool())
  val evaluatorDataIn  = Wire(UInt(32.W))
  val evaluatorSize    = Wire(UInt(5.W))
  val evaluatorDataOut = Wire(UInt(32.W))
  Stack(width = 32,
        depth   = 30,
        push    = evaluatorPush,
        pop     = evaluatorPop,
        en      = evaluatorEn,
        dataIn  = evaluatorDataIn,
        size    = evaluatorSize,
        dataOut = evaluatorDataOut)

  val opCnt    = RegInit(0.U(16.W))
  val numCnt   = RegInit(0.U(16.W))
  val wasNumIn = RegInit(false.B)
  val level    = RegInit(0.U(10.W))
  val zero :: one :: two :: three :: four :: five :: six :: seven :: eight :: night :: add :: sub :: mul :: lP :: rP :: eq :: Nil =
    Enum(16)
  val numEndSignal = WireInit(
    lP
  ) // end signal of a number in postfix

  // top-level state
  val sInput :: sParse :: sEval :: Nil =
    Enum(3)
  val state = RegInit(sInput)

  // In parse state
  val sPsIdle :: sPsReadyToParse :: sPsLeftParenthesis :: sPsAddSub :: sPsMul :: sPsRIghtParenthesis :: sPsEq :: Nil =
    Enum(7)
  val sPs = RegInit(sPsIdle)

  // State diagram of `(`
  val _ :: doCheckAndEndNum :: doCheckAndEndLevelPair :: doFlushPairedParenthesisT :: doPopAndPushT :: doStartLevelPair :: Nil =
    Enum(6)
  // State diagram of `+-*`
  val _ :: _ :: _ :: _ :: _ :: _ :: doPushOperator :: doPopAndPushPrior :: doPushZero :: doPushEndSig :: Nil =
    Enum(10)
  // State diagram of `)`
  val doHalt :: _ :: _ :: _ :: _ :: _ :: doFlushPairedParenthesisF :: doPopAndPushF :: doCheckAndEndLevelPair2 :: doFlushPairedParenthesisT2 :: doPopAndPushT2 :: Nil =
    Enum(11)
  // State diagram of `=`
  val _ :: _ :: _ :: _ :: _ :: _ :: doPopAndPush :: Nil =
    Enum(7)
  // State diagram of `0123456789`
  val _ :: _ :: _ :: _ :: _ :: _ :: doPush :: Nil =
    Enum(7)

  // In evaluate state
  val sEvIdle :: sEvConcat :: sEvEndConcat :: sEvAdd :: sEvSub :: sEvMul :: Nil =
    Enum(6)
  val sEv = RegInit(sEvIdle)

  val inst = RegInit(doHalt)

  println(s"width of inst: ${inst.getWidth}")

  // default: disable queue and all stacks
  queueEn         := false.B
  queuePush       := false.B
  queuePop        := false.B
  symStackEn      := false.B
  symStackPush    := false.B
  symStackPop     := false.B
  endLvEn         := false.B
  endLvPush       := false.B
  endLvPop        := false.B
  postfixEn       := false.B
  postfixPush     := false.B
  postfixPop      := false.B
  evaluatorEn     := false.B
  evaluatorPush   := false.B
  evaluatorPop    := false.B
  
  // initialize for debug
  queueDataIn     := 0.U
  symStackDataIn  := 0.U
  endLvDataIn     := 0.U
  postfixDataIn   := 0.U
  evaluatorDataIn := 0.U
  io.value.bits   := 0.U
  io.value.valid  := false.B

  val toParse = RegInit(0.U(4.W))

  switch(state) {
    is(sInput) {

      /**
       * In input state, all the works are required
       * to be finished in SINGLE CLOCK CYCLE
       */
      when(io.keyIn === eq) {
        // "=" is pressed
        state := sParse
      }.otherwise {
        queueEn     := true.B
        queuePush   := true.B
        queueDataIn := io.keyIn
      }
    }
    is(sParse) {
      when(queueIsEmpty) {
        // no more cached input should be parsed
        state := sEval
      }.otherwise {
        switch(sPs) {
          is(sPsIdle) { // i.e. start keyIn
            queueEn := true.B // peek
            toParse := queueDataOut
            sPs     := sPsReadyToParse
          }
          is(sPsReadyToParse) {
            queueEn  := true.B
            queuePop := true.B

          }
        }
      }
    }
    is(sEval) {
      when(postfixIsEmpty) {
        // no more postfix string should be evaluate
        state := sInput
      }
    }
  }
}
