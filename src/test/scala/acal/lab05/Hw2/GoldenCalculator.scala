package acal.lab05.Hw2

import scala.util.Try
import scala.util.Random
import scala.sys.process._

class Stack[T] {
  var stack = List[T]()

  /**
   * Push an element at the front
   */
  def push(e: T) = {
    stack = e :: stack
  }

  /**
   * Pop the front element
   */
  def pop = {
    val ret = stack(0)
    stack = stack.tail
    ret
  }

  /**
   * Peek the front element
   */
  def peek = stack(0)

  def isEmpty = stack.isEmpty
  def size    = stack.size
}

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

  var symStack = new Stack[Char]() // symbol stack
  var endLv = new Stack[Int]() // stack of end level
  var postfix  = new Stack[String]()
  var opCnt    = 0     // numbers of operators
  var numCnt   = 0     // numbers of number
  var wasNumIn = false // were we keying in a number
  var level    = 0     // level of ()

  var logger = List[String]()

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
      flushPairedParenthesis(true)
      endLv.pop
    }
  }

  /**
   * Update counter of number if needed, this should
   * be called anytime we key in an operator
   */
  def checkAndEndSomeNumber = {
    if (wasNumIn) {
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
      val toPush = symStack.pop.toString
      if (toPush == "(") {
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
      val toPush = symStack.pop.toString
      if (toPush == "(") {
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
        checkAndEndSomeNumber
        startLevelPair(withLevelMark = false)
      }
      case '*' => {
        checkAndEndSomeNumber
        pushOperator(c)
      }
      case '+' | '-' => {
        checkAndEndSomeNumber

        /**
         * If opCnt > operands at some moment after
         * adding + or - (i.e. when opCnt ==
         * operands) It means that this is a
         * dangling (unary) + or -
         */
        if (opCnt == numCnt) {
          startLevelPair(withLevelMark = true)
          // push a dummy zero to kill dangling zero
          postfix.push("0")
          numCnt += 1
          // wasNumIn = false // this line should be useless
          // finally, append dangling operator (+ or -)
          symStack.push(c)
          opCnt += 1
        } else {
          pushOperator(c)
        }
      }
      case ')' => {
        checkAndEndSomeNumber
        flushPairedParenthesis(false)
        checkAndEndLevelPair
      }
      case '=' => {
        checkAndEndSomeNumber
        while (!symStack.isEmpty) {
          postfix.push(symStack.pop.toString)
        }
        opCnt += 1
      }
      case _ => {
        // for numbers, just push it
        postfix.push(wasNumIn match {
          case true  => s"${postfix.pop}${c}"
          case false => s"${c}"
        })
        wasNumIn = true
      }
    }
    logger =
      s"($c)\t op: ${opCnt}\tnum: ${numCnt}\tlevel: ${level}\tpostfix: ${peek}\t\tsymStack: ${peek(
          symStack)}\t\t endLv: ${peek(endLv)}" :: logger
  }

  def peek[T](st: Stack[T]) =
    st.stack.reverse.mkString(" ")

  def peek = postfix.stack.reverse.mkString(" ")

  def evaluate = {
    var st = new Stack[BigInt]
    for (s <- postfix.stack.reverse) {
      Try(BigInt(s)).toOption match {
        case Some(value) => st.push(value)
        case None => {
          val b = st.pop
          val a = st.pop

          s match {
            case "+" => st.push(a + b)
            case "-" => st.push(a - b)
            case "*" => st.push(a * b)
            case _ =>
              throw new Exception(
                s"[evaluate] Bad operator: ${s}")
          }
        }
      }
    }

    st.stack.reduce((last, e) => last + e)
  }

  def dumpLog = {
    logger.reverse.mkString("\n")
  }
}

object GoldenCalculator {
  def generateRandomExpression()
      : (String, String) = {
    var res   = ""
    var last  = '_'
    var level = 0
    var rnd = new Random(System.currentTimeMillis())
    var cnt = 100

    def pushEle(s: String) = {
      res += s.toString
      last = s.last
      cnt -= 1
    }

    while (cnt > 0) {
      val op = rnd.nextInt(8)
      op match {
        case 0 => { // parenthesis
          if (rnd.nextInt(3) == 0) {
            if (
              res.isEmpty || (!last.isDigit && last != ')')
            ) {
              pushEle("(")
              level += 1
            }
          } else {
            if (
              level > 1 && last != '(' && last != '+' && last != '-' && last != '*'
            ) {
              pushEle(")")
              level -= 1
            }
          }
        }
        case 1 => {
          if (
            !res.isEmpty && last != '*' && last != '('
          ) {
            pushEle("+")
          }
        }
        case 2 => {
          if (
            !res.isEmpty && last != '*' && last != '('
          ) {
            pushEle("-")
          }
        }
        case 3 => {
          if (
            !res.isEmpty && (last.isDigit || last == ')')
          ) {
            pushEle("*")
          }
        }
        case _ => {
          if (last != '0' && last != ')') {
            pushEle(rnd.nextInt(10).toString)
          }
        }
      }
    }

    if (last == '+' || last == '-' || last == '*') {
      pushEle(rnd.nextInt(100).toString)
    }

    if (level != 0) {
      if (!last.isDigit && last != ')') {
        pushEle(rnd.nextInt(100).toString)
      }
      pushEle(")" * level)
    }

    val eval = evaluate(res)

    res += "="
    (res, eval)
  }

  def singleTest(test: (String, BigInt)) = {
    val gc  = new GoldenCalculator
    var log = ""

    log += s"${test._1} ==> "

    try {

      for (c <- test._1) {
        gc.keyIn(gc.enc.indexOf(c))
      }
      log += s"${gc.peek} ==> "

      val res = gc.evaluate
      log += s"${res} ${res == test._2 match {
          case true => "[[ Correct ]]"
          case false =>
            s"[[ !!!INCORRECT!!! ]] golden: ${test._2}\tlog:\n${gc.dumpLog}"
        }}"

      if (res != test._2) {
        println(log)
      }
    } catch {
      case e: Exception =>
        println(
          s"\nexception: ${e.getMessage}\n${gc.dumpLog}")
    }
  }

  def normalTest() = {
    val num = BigInt(
      "5360161621086697477532205257084500572")
    val tests = Seq(
      "50="                       -> BigInt(50),
      "-50="                      -> BigInt(-50),
      "(-50)="                    -> BigInt(-50),
      "+50="                      -> BigInt(50),
      "(--50)="                   -> BigInt(50),
      "-((-50)+0)="               -> BigInt(50),
      "++++++++++50="             -> BigInt(50),
      "+-50="                     -> BigInt(-50),
      "++++++++++-50="            -> BigInt(-50),
      "-+50="                     -> BigInt(-50),
      "--50="                     -> BigInt(50),
      "--(-(-(-(-50)+0)+0-0)-0)=" -> BigInt(50),
      "0+-50="                    -> BigInt(-50),
      "0-+50="                    -> BigInt(-50),
      "0--50="                    -> BigInt(50),
      "30+40="                    -> BigInt(70),
      "30-40="                    -> BigInt(-10),
      "20*20="                    -> BigInt(400),
      "-123="                     -> BigInt(-123),
      "(-123)="                   -> BigInt(-123),
      "(-10)+11+12-(-13)+(-14)="  -> BigInt(12),
      "-10+11+12--13+-14="        -> BigInt(12),
      "((-15)+(-10))*12-(34+66)*(-4)=" -> BigInt(
        100),
      "(-15+-10)*12-(34+66)*(-4)="  -> BigInt(100),
      "1+2*3+4*5="                  -> BigInt(27),
      "(-15)-15-(-15)+(-15)="       -> BigInt(-30),
      "17-16+(-15)-14+13-12+(-11)=" -> BigInt(-38),
      "(15-8)*(2+9)="               -> BigInt(77),
      "-(15-(-15)*3+8)="            -> BigInt(-68),
      "(15-8)*(2+9)-(15-(-15)*3+8)*(-10)=" -> BigInt(
        757),
      "(((((-12)+8)*((5-1)*((-3)*9)-3)+1)-(-3))*4*(5-3)-3)=" -> BigInt(
        3581),
      "((-123)*((-32)+3)*4+(15-(-16)))*(((-4)-2)*((-2)+1))=" -> BigInt(
        85794),
      "(((((((((8-3)*2-4)*3-2)*4-1)*3+5)*2-1)*3+2)*2+4)*3+8)*4-1234567890*98271811098-244817292034*(674373294052-3472781923742)*7823924729230=" -> num
    )

    for (test <- tests) {
      singleTest(test)
    }

    println(s"Passed all normal tests")
  }

  /**
   * Ref:
   * https://stackoverflow.com/questions/16162483/execute-external-command
   */
  def evaluate(s: String) = {
    val cmd =
      "python3 -c \"print(" + s + ")\"" // cmd to run
    cmd.!! // captures the output
  }

  def randomTest(times: Int = 1000) = {
    for (_ <- 0 until times) {
      val (rndExp, res) =
        generateRandomExpression()
      val golden = BigInt(res.strip())
      // print(s"${rndExp} -> ${golden}: ")
      singleTest(rndExp, golden)
      // var success = false
      // while (!success) {
      //   try {
      //     success = true
      //   } catch {
      //     case ex: java.lang.RuntimeException => {}
      //   }
      // }
    }
    println(s"Passed ${times} random tests")
  }

  def main(args: Array[String]) = {
    normalTest()
    randomTest(1000)
  }
}
