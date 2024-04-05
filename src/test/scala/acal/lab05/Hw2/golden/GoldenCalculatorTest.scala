package acal.lab05.Hw2.golden

import org.scalatest.funsuite.AnyFunSuite
import scala.sys.process._
import scala.language.postfixOps

class GoldenCalculatorTest extends AnyFunSuite {

  test("Normal test") {
    GoldenCalculator.normalTest()
  }

  test(
    "Random test (short expression, big number)") {
    GoldenCalculator.randomTest(10, 1000)
  }

  test(
    "Random test (long expression, small number)") {
    GoldenCalculator.randomTest(1000, 100)
  }

  test("Statistic") {
    println(
      s"Statistic: symStackPeak: ${GoldenCalculator.symStackPeak}, endLvPeak: ${GoldenCalculator.endLvPeak}, postfixPeak: ${GoldenCalculator.postfixPeak}, evaluatorPeak: ${GoldenCalculator.evaluatorPeak}, testLenPeak: ${GoldenCalculator.testLenPeak}, bitLengthPeak: ${GoldenCalculator.bitLengthPeak}")
  }
}
