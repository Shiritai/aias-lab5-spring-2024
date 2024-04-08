package acal.lab05.Hw2

import chisel3._
import chisel3.util._

/**
 * A calculator robust enough to handle `+` `-` `*`
 * `(` `)` and unary version of `+` `-`, fully
 * parameterized.
 *
 * @param maxEleWidth
 *   Max element width as well as the output width.
 *   Please make sure that all intermediate
 *   calculation result is affordable in this width.
 * @param maxKeyInCnt
 *   Max numbers of key in characters.
 * @param maxSymbolCnt
 *   Max numbers of operators and parenthesis.
 * @param maxParenthesisLv
 *   Max numbers of nested parenthesis depth.
 * @param maxUnaryOp
 *   Max numbers of unary operators.
 * @param maxEvalDepth
 *   Max depth of evaluator stack.
 */
class RobustCalculator(maxEleWidth:      Int = 128,
                       maxKeyInCnt:      Int = 16383,
                       maxSymbolCnt:     Int = 63,
                       maxParenthesisLv: Int = 63,
                       maxUnaryOp:       Int = 31,
                       maxEvalDepth:     Int = 63)
    extends Module {
  val maxKeyInWidth  = log2Ceil(maxKeyInCnt + 1)
  val evaluatorWidth = log2Ceil(maxEvalDepth + 1)
  val symbolWidth    = 4
  val maxParenthesisLvWidth = log2Ceil(
    maxParenthesisLv + 1)
  val maxUnaryOpWidth = log2Ceil(maxUnaryOp + 1)

  println(s"maxKeyInWidth: ${maxKeyInWidth}, evaluatorWidth: ${evaluatorWidth}, maxParenthesisLvWidth: ${maxParenthesisLvWidth}, maxUnaryOpWidth: ${maxUnaryOpWidth}")

  val io = IO(new Bundle {
    val keyIn = Input(UInt(symbolWidth.W))
    val value = Output(Valid(UInt(maxEleWidth.W)))
  })

  val queuePush    = Wire(Bool())
  val queuePop     = Wire(Bool())
  val queueEn      = Wire(Bool())
  val queueClear   = Wire(Bool())
  val queueDataIn  = Wire(UInt(symbolWidth.W))
  val queueSize    = Wire(UInt(maxKeyInWidth.W))
  val queueDataOut = Wire(UInt(symbolWidth.W))
  val queueIsEmpty = queueSize === 0.U
  Queue(width = symbolWidth,
        depth   = maxKeyInCnt,
        push    = queuePush,
        pop     = queuePop,
        en      = queueEn,
        clear   = queueClear,
        dataIn  = queueDataIn,
        size    = queueSize,
        dataOut = queueDataOut)

  val symStackPush   = Wire(Bool())
  val symStackPop    = Wire(Bool())
  val symStackEn     = Wire(Bool())
  val symStackDataIn = Wire(UInt(symbolWidth.W))
  val symStackSize = Wire(
    UInt(log2Ceil(maxSymbolCnt + 1).W))
  val symStackDataOut = Wire(UInt(symbolWidth.W))
  val symStackIsEmpty = symStackSize === 0.U
  Stack(width = symbolWidth,
        depth   = maxSymbolCnt,
        push    = symStackPush,
        pop     = symStackPop,
        en      = symStackEn,
        dataIn  = symStackDataIn,
        size    = symStackSize,
        dataOut = symStackDataOut)

  val endLvPush = Wire(Bool())
  val endLvPop  = Wire(Bool())
  val endLvEn   = Wire(Bool())
  val endLvDataIn = Wire(
    UInt(maxParenthesisLvWidth.W))
  val endLvSize = Wire(UInt(maxUnaryOpWidth.W))
  val endLvDataOut = Wire(
    UInt(maxParenthesisLvWidth.W))
  val endLvIsEmpty = endLvSize === 0.U
  Stack(width = maxParenthesisLvWidth,
        depth   = maxUnaryOp,
        push    = endLvPush,
        pop     = endLvPop,
        en      = endLvEn,
        dataIn  = endLvDataIn,
        size    = endLvSize,
        dataOut = endLvDataOut)

  val postfixPush    = Wire(Bool())
  val postfixPop     = Wire(Bool())
  val postfixEn      = Wire(Bool())
  val postfixClear   = Wire(Bool())
  val postfixDataIn  = Wire(UInt(symbolWidth.W))
  val postfixSize    = Wire(UInt(maxKeyInWidth.W))
  val postfixDataOut = Wire(UInt(symbolWidth.W))
  val postfixIsEmpty = postfixSize === 0.U
  // data subscription to  postfix memory
  val postfixIter = RegInit(0.U(maxKeyInWidth.W))
  val postfixSubscriptDataOut = Wire(
    UInt(symbolWidth.W))
  Stack(
    width            = symbolWidth,
    depth            = maxKeyInCnt,
    push             = postfixPush,
    pop              = postfixPop,
    en               = postfixEn,
    clear            = postfixClear,
    dataIn           = postfixDataIn,
    size             = postfixSize,
    dataOut          = postfixDataOut,
    subscriptIdx     = postfixIter,
    subscriptDataOut = postfixSubscriptDataOut
  )

  val evaluatorPush   = Wire(Bool())
  val evaluatorPop    = Wire(Bool())
  val evaluatorEn     = Wire(Bool())
  val evaluatorDataIn = Wire(UInt(maxEleWidth.W))
  val evaluatorSize   = Wire(UInt(evaluatorWidth.W))
  val evaluatorDataOut = Wire(UInt(maxEleWidth.W))
  val evaluatorIsEmpty = evaluatorSize === 0.U
  Stack(
    width   = maxEleWidth,
    depth   = maxEvalDepth,
    push    = evaluatorPush,
    pop     = evaluatorPop,
    en      = evaluatorEn,
    dataIn  = evaluatorDataIn,
    size    = evaluatorSize,
    dataOut = evaluatorDataOut
  )

  val opCnt    = RegInit(0.U(maxKeyInWidth.W))
  val numCnt   = RegInit(0.U(maxKeyInWidth.W))
  val wasNumIn = RegInit(false.B)
  val level    = RegInit(0.U(10.W))
  val zero :: one :: two :: three :: four :: five :: six :: seven :: eight :: nine :: add :: sub :: mul :: lP :: rP :: eq :: Nil =
    Enum(16)
  // end signal of a number in postfix
  val numEndSignal     = WireInit(lP)
  val postfixEndSignal = WireInit(eq)

  // top-level state
  val sIdle :: sInput :: sParse :: sEval :: Nil =
    Enum(4)
  val state = RegInit(sIdle)

  // In parse state
  val sPsIdle :: sPsReadyToParse :: sPsLeftParenthesis :: sPsAddSub :: sPsMul :: sPsRightParenthesis :: sPsEq :: sPsNumber :: Nil =
    Enum(8)
  val sPs = RegInit(sPsIdle)

  // State diagram in `(`
  val _ :: doCheckAndEndNum :: doCheckAndEndLevelPair :: doFlushPairedParenthesisT :: doStartLevelPair :: Nil =
    Enum(5)
  // State diagram in `+-*`
  val _ :: _ :: _ :: _ :: _ :: doPushOperator :: doPushZero :: doPushEndSig :: Nil =
    Enum(8)
  // State diagram in `)`
  val doHalt :: _ :: _ :: _ :: _ :: doFlushPairedParenthesisF :: doPopAndPushF :: doCheckAndEndLevelPair2 :: doFlushPairedParenthesisT2 :: doPopAndPushT2 :: Nil =
    Enum(10)
  // State diagram in `=`
  val _ :: _ :: _ :: _ :: _ :: doPopAndPush :: Nil =
    Enum(6)

  // In evaluate state
  val sEvIdle :: sEvFetchB :: sEvFetchA :: sEvCal :: Nil =
    Enum(4)
  val sEv = RegInit(sEvIdle)

  val inst = RegInit(doHalt)

  /**
   * Default: disable push and pop function of queue
   * and all stacks make lists always ready to peek
   */
  queueEn       := true.B
  queuePush     := false.B
  queuePop      := false.B
  queueClear    := false.B
  symStackEn    := true.B
  symStackPush  := false.B
  symStackPop   := false.B
  endLvEn       := true.B
  endLvPush     := false.B
  endLvPop      := false.B
  postfixEn     := true.B
  postfixPush   := false.B
  postfixPop    := false.B
  postfixClear  := false.B
  evaluatorEn   := true.B
  evaluatorPush := false.B
  evaluatorPop  := false.B

  // initialize for debug
  queueDataIn     := 0.U
  symStackDataIn  := 0.U
  endLvDataIn     := 0.U
  postfixDataIn   := 0.U
  evaluatorDataIn := 0.U
  io.value.bits   := 0.U
  io.value.valid  := false.B

  val toParse = RegInit(0.U(4.W))
  val bigNum  = RegInit(0.U(maxEleWidth.W))
  val bigNumA = RegInit(0.U(maxEleWidth.W))

  // val debugLimiter = RegInit(0.U(20.W))
  // val debugLimit   = WireInit(10000.U(20.W))

  switch(state) {
    is(sIdle) {
      when(io.keyIn =/= zero) {
        when(io.keyIn === eq) {
          // "=" is pressed
          io.value.valid := true.B
          io.value.bits  := zero
        }.otherwise {
          queuePush   := true.B
          queueDataIn := io.keyIn
          state       := sInput
        }
      }
    }
    is(sInput) {
      /**
       * In input state, all the works are required
       * to be finished in SINGLE CLOCK CYCLE
       */
      queuePush   := true.B
      queueDataIn := io.keyIn

      when(io.keyIn === eq) {
        // "=" is pressed
        state := sParse
      }
    }
    is(sParse) {
      switch(sPs) {
        is(sPsIdle) {
          when(!queueIsEmpty) {
            // pop element and fetch
            sPs      := sPsReadyToParse
            queuePop := true.B
            toParse  := queueDataOut
          }
        }
        is(sPsReadyToParse) {
          // use queue top to determine next state
          sPs := sPsNumber // default case
          switch(toParse) {
            is(lP) { sPs := sPsLeftParenthesis }
            is(mul) { sPs := sPsMul }
            is(add) { sPs := sPsAddSub }
            is(sub) { sPs := sPsAddSub }
            is(rP) { sPs := sPsRightParenthesis }
            is(eq) { sPs := sPsEq }
          }
        }
        is(sPsLeftParenthesis) {
          switch(inst) {
            is(doHalt) {
              inst := doStartLevelPair
            }
            is(doStartLevelPair) {
              symStackPush   := true.B
              symStackDataIn := lP
              level          := level + 1.U
              inst           := doHalt
              sPs            := sPsIdle
            }
          }
        }
        is(sPsMul) {
          switch(inst) {
            is(doHalt) {
              inst := doCheckAndEndNum
            }
            is(doCheckAndEndNum) {
              when(wasNumIn) {
                // postfix.push(numEndSignal)
                postfixPush   := true.B
                postfixDataIn := numEndSignal
                numCnt        := numCnt + 1.U
                wasNumIn      := false.B
                inst := doCheckAndEndLevelPair
              }.otherwise {
                inst := doPushOperator
              }
            }
            is(doCheckAndEndLevelPair) {
              when(
                !endLvIsEmpty && level === endLvDataOut) {
                endLvPop := true.B
                inst := doFlushPairedParenthesisT
              }.otherwise {
                inst := doPushOperator
              }
            }
            is(doFlushPairedParenthesisT) {
              when(symStackDataOut =/= lP) {
                // postfix.push(symStack.pop)
                postfixPush   := true.B
                postfixDataIn := symStackDataOut
                symStackPop   := true.B
              }.otherwise {
                symStackPop := true.B // pop '('
                inst := doCheckAndEndLevelPair
              }
            }
            is(doPushOperator) {
              when(
                !symStackIsEmpty && symStackDataOut === mul) {
                // postfix.push(symStack.pop)
                postfixPush   := true.B
                postfixDataIn := symStackDataOut
                symStackPop   := true.B
              }.otherwise {
                symStackPush   := true.B
                symStackDataIn := mul
                opCnt          := opCnt + 1.U
                inst           := doHalt
                sPs            := sPsIdle
              }
            }
          }
        }
        is(sPsAddSub) {
          switch(inst) {
            is(doHalt) {
              when(wasNumIn) {
                inst := doCheckAndEndNum
              }.elsewhen(opCnt === numCnt) {
                inst := doStartLevelPair
              }.otherwise {
                inst := doPushOperator
              }
            }
            is(doCheckAndEndNum) {
              // postfix.push(numEndSignal)
              postfixPush   := true.B
              postfixDataIn := numEndSignal
              numCnt        := numCnt + 1.U
              wasNumIn      := false.B
              inst := doCheckAndEndLevelPair
            }
            is(doCheckAndEndLevelPair) {
              when(
                !endLvIsEmpty && level === endLvDataOut) {
                endLvPop := true.B
                inst := doFlushPairedParenthesisT
              }.otherwise {
                when(opCnt === numCnt) {
                  inst := doStartLevelPair
                }.otherwise {
                  inst := doPushOperator
                }
              }
            }
            is(doFlushPairedParenthesisT) {
              when(symStackDataOut =/= lP) {
                // postfix.push(symStack.pop)
                postfixPush   := true.B
                postfixDataIn := symStackDataOut
                symStackPop   := true.B
              }.otherwise {
                symStackPop := true.B // pop '('
                inst := doCheckAndEndLevelPair
              }
            }
            is(doPushOperator) {
              when(
                !symStackIsEmpty && symStackDataOut =/= lP) {
                // postfix.push(symStack.pop)
                postfixPush   := true.B
                postfixDataIn := symStackDataOut
                symStackPop   := true.B
              }.otherwise {
                symStackPush   := true.B
                symStackDataIn := toParse
                opCnt          := opCnt + 1.U
                inst           := doHalt
                sPs            := sPsIdle
              }
            }
            is(doStartLevelPair) {
              symStackPush   := true.B
              symStackDataIn := lP
              endLvPush      := true.B
              endLvDataIn    := level
              inst           := doPushZero
            }
            is(doPushZero) {
              postfixPush   := true.B
              postfixDataIn := zero
              inst          := doPushEndSig
            }
            is(doPushEndSig) {
              postfixPush    := true.B
              postfixDataIn  := numEndSignal
              numCnt         := numCnt + 1.U
              symStackPush   := true.B
              symStackDataIn := toParse
              opCnt          := opCnt + 1.U
              inst           := doHalt
              sPs            := sPsIdle
            }
          }
        }
        is(sPsRightParenthesis) {
          switch(inst) {
            is(doHalt) {
              inst := doCheckAndEndNum
            }
            is(doCheckAndEndNum) {
              when(wasNumIn) {
                // postfix.push(numEndSignal)
                postfixPush   := true.B
                postfixDataIn := numEndSignal
                numCnt        := numCnt + 1.U
                wasNumIn      := false.B
                inst := doCheckAndEndLevelPair
              }.otherwise {
                inst := doFlushPairedParenthesisF
              }
            }
            is(doCheckAndEndLevelPair) {
              when(
                !endLvIsEmpty && level === endLvDataOut) {
                endLvPop := true.B
                inst := doFlushPairedParenthesisT
              }.otherwise {
                inst := doFlushPairedParenthesisF
              }
            }
            is(doFlushPairedParenthesisT) {
              when(symStackDataOut =/= lP) {
                // postfix.push(symStack.pop)
                postfixPush   := true.B
                postfixDataIn := symStackDataOut
                symStackPop   := true.B
              }.otherwise {
                symStackPop := true.B // pop '('
                inst := doCheckAndEndLevelPair
              }
            }
            is(doFlushPairedParenthesisF) {
              when(symStackDataOut =/= lP) {
                // postfix.push(symStack.pop)
                postfixPush   := true.B
                postfixDataIn := symStackDataOut
                symStackPop   := true.B
              }.otherwise {
                symStackPop := true.B // pop '('
                level       := level - 1.U
                inst := doCheckAndEndLevelPair2
              }
            }
            is(doCheckAndEndLevelPair2) {
              when(
                !endLvIsEmpty && level === endLvDataOut) {
                endLvPop := true.B
                inst := doFlushPairedParenthesisT2
              }.otherwise {
                inst := doHalt
                sPs  := sPsIdle
              }
            }
            is(doFlushPairedParenthesisT2) {
              when(symStackDataOut =/= lP) {
                // postfix.push(symStack.pop)
                postfixPush   := true.B
                postfixDataIn := symStackDataOut
                symStackPop   := true.B
              }.otherwise {
                symStackPop := true.B // pop '('
                inst := doCheckAndEndLevelPair2
              }
            }
          }
        }
        is(sPsEq) {
          switch(inst) {
            is(doHalt) {
              inst := doCheckAndEndNum
            }
            is(doCheckAndEndNum) {
              when(wasNumIn) {
                // postfix.push(numEndSignal)
                postfixPush   := true.B
                postfixDataIn := numEndSignal
                numCnt        := numCnt + 1.U
                wasNumIn      := false.B
                inst := doCheckAndEndLevelPair
              }.otherwise {
                inst := doPopAndPush
              }
            }
            is(doCheckAndEndLevelPair) {
              when(
                !endLvIsEmpty && level === endLvDataOut) {
                endLvPop := true.B
                inst := doFlushPairedParenthesisT
              }.otherwise {
                inst := doPopAndPush
              }
            }
            is(doFlushPairedParenthesisT) {
              when(symStackDataOut =/= lP) {
                // postfix.push(symStack.pop)
                postfixPush   := true.B
                postfixDataIn := symStackDataOut
                symStackPop   := true.B
              }.otherwise {
                symStackPop := true.B // pop '('
                inst := doCheckAndEndLevelPair
              }
            }
            is(doPopAndPush) {
              when(!symStackIsEmpty) {
                postfixPush   := true.B
                symStackPop   := true.B
                postfixDataIn := symStackDataOut
              }.otherwise {
                // reset counters
                opCnt  := 0.U
                numCnt := 0.U
                // flush inner states of parse to the initial one
                inst := doHalt
                sPs  := sPsIdle
                // add end signal to postfix list
                postfixPush   := true.B
                postfixDataIn := postfixEndSignal
                // clear queue for next input
                queueClear := true.B
                // go to evaluation
                state := sEval
              }
            }
          }
        }
        is(sPsNumber) {
          postfixPush   := true.B
          postfixDataIn := toParse
          wasNumIn      := true.B
          inst          := doHalt
          sPs           := sPsIdle
        }
      }
    }
    is(sEval) {
      switch(sEv) {
        // this should be able to run in parallel while parsing
        is(sEvIdle) {
          when(
            postfixSubscriptDataOut === numEndSignal) {
            evaluatorPush   := true.B
            evaluatorDataIn := bigNum
            bigNum          := 0.U
            postfixIter     := postfixIter + 1.U
          }.elsewhen(
            postfixSubscriptDataOut >= zero && postfixSubscriptDataOut <= nine) {
            bigNum := (bigNum << 3.U) + (bigNum << 1.U) + postfixSubscriptDataOut
            postfixIter := postfixIter + 1.U
          }.elsewhen(
            postfixSubscriptDataOut === postfixEndSignal) {
            // End of all
            state          := sIdle
            io.value.valid := true.B
            io.value.bits  := evaluatorDataOut
            evaluatorPop   := true.B
            postfixIter    := 0.U
            postfixClear   := true.B
          }.otherwise {
            sEv := sEvFetchB
          }
        }
        is(sEvFetchB) {
          bigNum       := evaluatorDataOut
          evaluatorPop := true.B
          sEv          := sEvFetchA
        }
        is(sEvFetchA) {
          bigNumA      := evaluatorDataOut
          evaluatorPop := true.B
          sEv := sEvCal
        }
        is(sEvCal) {
          evaluatorPush   := true.B
          bigNum          := 0.U
          bigNumA         := 0.U
          postfixIter     := postfixIter + 1.U
          sEv             := sEvIdle
          switch(postfixSubscriptDataOut) {
            is(add) { evaluatorDataIn := bigNumA + bigNum }
            is(sub) { evaluatorDataIn := bigNumA - bigNum }
            is(mul) { evaluatorDataIn := bigNumA * bigNum }
          }
        }
      }
    }
  }

  // when(debugLimiter === debugLimit) {
  //   // Quit and reinitialize
  //   state          := sIdle
  //   io.value.valid := true.B
  //   io.value.bits  := evaluatorDataOut
  //   evaluatorPop   := true.B
  //   postfixIter    := 0.U
  //   postfixClear   := true.B
  // }.otherwise {
  //   debugLimiter := debugLimiter + 1.U
  // }
}
