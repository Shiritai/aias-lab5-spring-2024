package acal.lab05.Hw2.golden

import scala.util.Random
import scala.sys.process._

/**
 * A robust calculator, software modeling of
 * hardware `Calculator`
 */
class GoldenCalculator {
  val enc =
    Seq('0', '1', '2', '3', '4', '5', '6', '7', '8',
        '9', '+', '-', '*', '(', ')', '=')

  val opOrder =
    Map('+' -> 0, '-' -> 0, '*' -> 1, '(' -> -1)

  var symStack =
    new GoldenStack[Char]() // symbol stack
  var endLv =
    new GoldenStack[Int]() // end level stack
  var postfix   = new GoldenStack[Char]()
  var evaluator = new GoldenStack[BigInt]
  var opCnt     = 0 // numbers of operators
  var numCnt    = 0 // numbers of number
  var wasNumIn = false // were we keying in a number
  var level    = 0     // level of ()
  val numEndSignal =
    '(' // end signal of a number in postfix

  var logger        = List[String]()
  var bitLengthPeak = 0

  def startLevelPair(withLevelMark: Boolean) = {
    symStack.push('(') // push in all cases
    if (withLevelMark) {
      endLv.push(level)
    } else {
      level += 1
    }
  }

  /**
   * End of a level section
   *
   * If this is the end of some marked level, do
   * flush once
   */
  def checkAndEndLevelPair = {
    while (!endLv.isEmpty && level == endLv.peek) {
      endLv.pop
      flushPairedParenthesis(true)
    }
  }

  /**
   * Update counter of number if needed, this should
   * be called anytime we key in an operator
   */
  def checkAndEndNumber = {
    if (wasNumIn) {
      postfix.push(numEndSignal)
      numCnt += 1
      wasNumIn = false
      checkAndEndLevelPair
    }
  }

  /**
   * General behavior to deal with pushing an
   * operator to operator stack
   */
  def pushOperator(c: Char) = {

    var pushList = ""

    /**
     * Before pushing an operator, pop up all that
     * has higher or equivalent priority
     */
    while (
      !symStack.isEmpty && opOrder(
        symStack.peek) >= opOrder(c)
    ) {
      val toPush = symStack.pop
      if (toPush == '(') {
        throw new Exception(
          s"[pushOperator] Before pushing ${c}, pushed ${pushList}, pop and try to push a (")
      }
      postfix.push(toPush)
      pushList += toPush
    }
    symStack.push(c)
    opCnt += 1
  }

  /**
   * Collect all operators until we find '('
   */
  def flushPairedParenthesis(
      onlyClearLevelMark: Boolean) = {
    while (symStack.peek != '(') {
      val toPush = symStack.pop
      if (toPush == '(') {
        throw new Exception(
          "[flushPairedParenthesis] Try to push a (")
      }
      postfix.push(toPush)
    }
    symStack.pop // pop '('

    if (!onlyClearLevelMark) { level -= 1 }
    assert(level >= 0) // should always satisfy!
  }

  def keyIn(i: Int) = {
    val c = enc(i)
    c match {
      case '(' => {
        startLevelPair(withLevelMark = false)
      }
      case '*' => {
        checkAndEndNumber
        pushOperator(c)
      }
      case '+' | '-' => {
        checkAndEndNumber

        /**
         * If opCnt > operands at some moment after
         * adding + or - (i.e. when opCnt ==
         * operands) It means that this is a
         * dangling (unary) + or -
         */
        if (opCnt == numCnt) {
          startLevelPair(withLevelMark = true)
          // push a dummy zero as implicit zero
          postfix.push('0')
          postfix.push(numEndSignal)
          numCnt += 1
          // finally, append dangling operator (+ or -)
          symStack.push(c)
          opCnt += 1
        } else {
          pushOperator(c)
        }
      }
      case ')' => {
        checkAndEndNumber
        flushPairedParenthesis(false)
        checkAndEndLevelPair
      }
      case '=' => {
        checkAndEndNumber
        while (!symStack.isEmpty) {
          postfix.push(symStack.pop)
        }
        opCnt += 1
      }
      case _ => {
        postfix.push(c)
        wasNumIn = true
      }
    }
    logger =
      s"($c)\t op: ${opCnt}\tnum: ${numCnt}\tlevel: ${level}\tpostfix: ${peek}\t\tsymStack: ${peek(
          symStack)}\t\t endLv: ${peek(endLv)}" :: logger
  }

  def peek[T](st: GoldenStack[T]) =
    st.stack.reverse.mkString(" ")

  def peek = postfix.stack.reverse.mkString(" ")

  def evaluate = {
    var n = ""

    for (c <- postfix.stack.reverse) {
      if (c.isDigit) {
        n = s"${n}${c.asDigit}"
      } else if (c == numEndSignal) {
        val bn = BigInt(n)
        bitLengthPeak =
          Math.max(bitLengthPeak, bn.bitLength)
        evaluator.push(bn)
        n = ""
      } else {
        val b = evaluator.pop
        val a = evaluator.pop

        val res = c match {
          case '+' => a + b
          case '-' => a - b
          case '*' => a * b
          case _ =>
            throw new Exception(
              s"[evaluate] Bad operator: ${c}")
        }

        bitLengthPeak =
          Math.max(bitLengthPeak, res.bitLength)
        evaluator.push(res)
      }
    }
    evaluator.peek
  }

  def dumpLog = {
    logger.reverse.mkString("\n")
  }
}