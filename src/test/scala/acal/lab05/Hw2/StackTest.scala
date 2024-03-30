package acal.lab05.Hw2

import chisel3._
import chisel3.util._
import chisel3.iotesters.{Driver, PeekPokeTester}
import scala.util.Random
import golden.GoldenStack

class StackTest(st: Stack, width: Int, depth: Int)
    extends PeekPokeTester(st) {
  var gst    = new GoldenStack[Int]()
  var genRnd = new Random()
  val push :: peek :: pop :: idle :: Nil =
    Enum(4).map(n => n.toInt)

  for (_ <- 0 until 100) {
    val op = genRnd.nextInt(4)

    op match {
      case `push` => { // push
        if (gst.size < depth) {
          val n = genRnd.nextInt(5)
          gst.push(n)
          poke(st.io.en,     true)
          poke(st.io.push,     true)
          poke(st.io.pop,     false)
          poke(st.io.dataIn,     n)
        }
      }
      case `peek` => { // peek
        poke(st.io.en, true)
        poke(st.io.push, false)
        poke(st.io.pop, false)
      }
      case `pop` => { // pop
        if (!gst.isEmpty) {
          gst.pop
          poke(st.io.en, true)
          poke(st.io.push, false)
          poke(st.io.pop, true)
        }
      }
      case idle => {
        poke(st.io.en, false)
      }
    }

    step(1)

    if (op == peek) {
      expect(
        st.io.size,
        gst.size,
        s"Golden size: ${gst.size} != signal size: ${st.io.size}")

      if (!gst.isEmpty) {
        expect(
          st.io.dataOut,
          gst.peek,
          s"Golden top: ${gst.peek} != signal top: ${st.io.dataOut}")
      }
    }
  }
}

object StackTest extends App {
  val width = 6
  val depth = 1000

  Driver.execute(args,
                 () => new Stack(width, depth)) {
    c: Stack => new StackTest(c, width, depth)
  }
}
