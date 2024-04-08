package acal.lab05.Hw2

import chisel3.iotesters.{Driver, PeekPokeTester}
import acal.lab05.Hw2.golden.GoldenCalculatorTester

class RobustCalculatorTest(dut: RobustCalculator)
    extends PeekPokeTester(dut) {

  val dict = Map(
    '0' -> 0,
    '1' -> 1,
    '2' -> 2,
    '3' -> 3,
    '4' -> 4,
    '5' -> 5,
    '6' -> 6,
    '7' -> 7,
    '8' -> 8,
    '9' -> 9,
    '+' -> 10,
    '-' -> 11,
    '*' -> 12,
    '(' -> 13,
    ')' -> 14,
    '=' -> 15
  )

  val num = BigInt(
    "5360161621086697477532205257084500572")

  val numNeg = BigInt(
    "-5360161621086697477532205257084500572")

  val tests = Seq(
    "="                         -> BigInt(0),
    "0="                        -> BigInt(0),
    "1="                        -> BigInt(1),
    "23="                       -> BigInt(23),
    "456="                      -> BigInt(456),
    "(789)="                    -> BigInt(789),
    "((10))="                   -> BigInt(10),
    "1*1="                      -> BigInt(1),
    "(1*1)="                    -> BigInt(1),
    "20*30="                    -> BigInt(600),
    "20+30="                    -> BigInt(50),
    "(20+30)="                  -> BigInt(50),
    "((20+30))="                -> BigInt(50),
    "(((20)+30))="              -> BigInt(50),
    "50="                       -> BigInt(50),
    "-50="                      -> BigInt(-50),
    "(-50)="                    -> BigInt(-50),
    "50*-50="                   -> BigInt(-2500),
    "+50="                      -> BigInt(50),
    "----50="                   -> BigInt(50),
    "(--50)="                   -> BigInt(50),
    "-((-50)+0)="               -> BigInt(50),
    "++++++++++50="             -> BigInt(50),
    "+-50="                     -> BigInt(-50),
    "++++++++++-50="            -> BigInt(-50),
    "+++-+++++++50="            -> BigInt(-50),
    "-+50="                     -> BigInt(-50),
    "--50="                     -> BigInt(50),
    "--(-(-(-(-50)+0)+0-0)-0)=" -> BigInt(50),
    "11*(12-3)*14+(15-(-16))="  -> BigInt(1417),
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
    "((-15)+(-10))*12-(34+66)*(-4)=" -> BigInt(100),
    "(-15+-10)*12-(34+66)*(-4)="     -> BigInt(100),
    "1+2*3+4*5="                     -> BigInt(27),
    "(-15)-15-(-15)+(-15)="          -> BigInt(-30),
    "17-16+(-15)-14+13-12+(-11)="    -> BigInt(-38),
    "(15-8)*(2+9)="                  -> BigInt(77),
    "-(15-(-15)*3+8)="               -> BigInt(-68),
    "(15-8)*(2+9)-(15-(-15)*3+8)*(-10)=" -> BigInt(
      757),
    "(((((-12)+8)*((5-1)*((-3)*9)-3)+1)-(-3))*4*(5-3)-3)=" -> BigInt(
      3581),
    "((-123)*((-32)+3)*4+(15-(-16)))*(((-4)-2)*((-2)+1))=" -> BigInt(
      85794),
    "(((((((((8-3)*2-4)*3-2)*4-1)*3+5)*2-1)*3+2)*2+4)*3+8)*4-1234567890*98271811098-244817292034*(674373294052-3472781923742)*7823924729230=" -> num,
    "-((((((((((8-3)*2-4)*3-2)*4-1)*3+5)*2-1)*3+2)*2+4)*3+8)*4-1234567890*98271811098-244817292034*(674373294052-3472781923742)*7823924729230)=" -> numNeg
  )

  def singleTest =
    (singleCase: ((String, BigInt), Int)) => {
      val input  = singleCase._1._1
      val output = singleCase._1._2
      val index  = singleCase._2

      input.foreach { ch =>
        poke(dut.io.keyIn, dict(ch))
        step(1)
      }
      while (peek(dut.io.value.valid) == 0) {
        step(1)
      }
      val res = peek(dut.io.value.bits)
      val resIsNeg =
        (res & (1 << (output.bitLength))) != BigInt(
          0)
      val outputIsNeg = output < BigInt(0)

      /**
       * When golden < 0, module result will always
       * be `11111.....`, we must manually convert
       * it into twos complement value before
       * comparison.
       *
       * Meanwhile, `BigInt` saves sign and value
       * differently, so we should also take the
       * absolute value of golden for comparison
       * when golden is negative
       */
      val absRes =
        ~(res - 1) & ~(~BigInt(
          0) << output.bitLength)
      val absOutput = output.abs

      val isPositive = output >= BigInt(0)
      val resToCmp = if (isPositive) { res }
      else { absRes }
      val outToCmp = if (isPositive) { output }
      else { absOutput }

      expect(
        resToCmp == outToCmp,
        s"Question ${(index + 1).toString}: ${input} has...\nModule result: ${resToCmp}\nGolden: ${outToCmp}, resIsNeg: ${resIsNeg}, goldenIsNeg: ${outputIsNeg}, res len: ${res.bitLength}, golden len: ${output.bitLength}"
      )

      step(1)
    }

  println("Running normal tests...")
  tests.zipWithIndex.foreach(singleTest)

  val randTests = (0 until 1000).map(_ => {
    val (rndExp, res) =
      GoldenCalculatorTester.generateRandomExpression(30)
    (rndExp, BigInt(res))
  })

  println("Running random tests...")
  randTests.zipWithIndex.foreach(singleTest)

}

object RobustCalculatorTest extends App {
  Driver.execute(args, () => new RobustCalculator) {
    c: RobustCalculator =>
      new RobustCalculatorTest(c)
  }
}
