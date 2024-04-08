package acal.lab05.Hw2.golden

import scala.util.Random
import scala.sys.process._

object GoldenCalculatorTester {
  var symStackPeak  = 0
  var endLvPeak     = 0
  var postfixPeak   = 0
  var evaluatorPeak = 0
  var testLenPeak   = 0
  var bitLengthPeak = 0

  def generateRandomExpression(
      parts: Int): (String, String) = {
    var res   = ""
    var last  = '_'
    var level = 0
    var rnd = new Random(System.currentTimeMillis())
    var cnt = parts

    def pushEle(s: String) = {
      res += s.toString
      last = s.last
      cnt -= 1
    }

    /**
     * @brief
     *   Generate next bit integer in string format
     *   with maximal value: 10^24
     */
    def genNextInt() = {
      (0 until 4)
        .map(_ => rnd.nextInt(10).toString)
        .reduce((a, b) => {
          if (a == "0") {
            b
          } else {
            a + b
          }
        })
    }

    while (cnt > 0) {
      val op = rnd.nextInt(8)
      op match {
        case 0 => { // parenthesis
          if (rnd.nextInt(5) < 2) {
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
          if (!last.isDigit && last != ')') {
            pushEle(genNextInt())
          }
        }
      }
    }

    if (last == '+' || last == '-' || last == '*') {
      pushEle(genNextInt())
    }

    if (level != 0) {
      if (!last.isDigit && last != ')') {
        pushEle(genNextInt())
      }
      pushEle(")" * level)
    }

    val eval = evaluate(res)

    // println(s"eval: ${eval}")

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
      log += res.toString + (res == test._2 match {
        case true => "[[ Correct ]]"
        case false =>
          s"[[ !!!INCORRECT!!! ]] golden: ${test._2}\tlog:\n${gc.dumpLog}"
      })

      if (res != test._2) {
        println(log)
      }
    } catch {
      case e: Exception =>
        println(
          s"\nexception: ${e.getMessage}\n${gc.dumpLog}")
    }

    symStackPeak =
      Math.max(symStackPeak, gc.symStack.peak)
    endLvPeak = Math.max(endLvPeak, gc.endLv.peak)
    postfixPeak =
      Math.max(postfixPeak, gc.postfix.peak)
    evaluatorPeak =
      Math.max(evaluatorPeak, gc.evaluator.peak)
    bitLengthPeak =
      Math.max(bitLengthPeak, gc.bitLengthPeak)
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
      "python3 -c print(" + s + ")" // cmd to run
    // println(s"cmd is: $cmd")
    cmd.!!.strip // captures the output
  }

  def randomTest(times: Int = 100,
                 parts: Int = 100) = {
    for (_ <- 0 until times) {
      val (rndExp, res) =
        generateRandomExpression(parts)
      try {
        var golden = BigInt(res)
        singleTest(rndExp, golden)
        testLenPeak =
          Math.max(testLenPeak, rndExp.size)
      } catch {
        case ex: NumberFormatException => {
          println(
            (("=" * 10) + "\n") + rndExp + " --> [ " + res + " ]" + ("\n" + ("=" * 10) + "\n"))
        }
      }
      // print(s"${rndExp} -> ${golden}: ")
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
}
