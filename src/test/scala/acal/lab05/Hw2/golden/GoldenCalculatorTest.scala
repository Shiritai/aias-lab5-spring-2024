package acal.lab05.Hw2.golden

import org.scalatest.funsuite.AnyFunSuite
import scala.sys.process._
import scala.language.postfixOps

class GoldenCalculatorTest extends AnyFunSuite {

  test("Normal test") {
    GoldenCalculatorTester.normalTest()
  }

  test(
    "Random test (short expression, big number)") {
    GoldenCalculatorTester.randomTest(10, 1000)
  }

  test(
    "Random test (long expression, small number)") {
    GoldenCalculatorTester.randomTest(1000, 100)
  }

  test("Statistic") {
    println(
      s"Statistic: symStackPeak: ${GoldenCalculatorTester.symStackPeak}, endLvPeak: ${GoldenCalculatorTester.endLvPeak}, postfixPeak: ${GoldenCalculatorTester.postfixPeak}, evaluatorPeak: ${GoldenCalculatorTester.evaluatorPeak}, testLenPeak: ${GoldenCalculatorTester.testLenPeak}, bitLengthPeak: ${GoldenCalculatorTester.bitLengthPeak}")
  }
}
