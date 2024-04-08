package acal.lab05.Hw2

import chisel3._
import chisel3.util._
import chisel3.iotesters.{Driver, PeekPokeTester}
import scala.util.Random
import acal.lab05.Hw2.golden._

class SingleSideListTest[
    T <: Module with SingleSideList](
    ssl:   T,
    gSsl:  GoldenSingleIoList[Int],
    width: Int,
    depth: Int)
    extends PeekPokeTester(ssl) {
  var genRnd = new Random()
  val opKind = 4
  val push :: peek :: pop :: idle :: Nil =
    Enum(opKind).map(n => n.toInt)

  for (_ <- 0 until 100) {
    val op = genRnd.nextInt(opKind)

    op match {
      case `push` => { // push
        if (gSsl.size < depth) {
          val n = genRnd.nextInt(1 << width)
          gSsl.push(n)
          poke(ssl.io.en,      true)
          poke(ssl.io.push,      true)
          poke(ssl.io.pop,      false)
          poke(ssl.io.dataIn,      n)
        }
      }
      case `peek` => { // peek
        poke(ssl.io.en, true)
        poke(ssl.io.push, false)
        poke(ssl.io.pop, false)
      }
      case `pop` => { // pop
        if (!gSsl.isEmpty) {
          gSsl.pop
          poke(ssl.io.en, true)
          poke(ssl.io.push, false)
          poke(ssl.io.pop, true)
        }
      }
      case idle => {
        poke(ssl.io.en, false)
      }
    }

    step(1)

    if (op == peek) {
      expect(
        ssl.io.size,
        gSsl.size,
        s"Golden size: ${gSsl.size} != signal size: ${ssl.io.size}")

      if (!gSsl.isEmpty) {
        expect(
          ssl.io.dataOut,
          gSsl.peek,
          s"Golden top: ${gSsl.peek} != signal top: ${ssl.io.dataOut}")
      }
    }
  }
}

object SingleSideListTest extends App {
  val width = 4
  val depth = 1000

  Driver.execute(args,
                 () => new Stack(width, depth)) {
    ssl: Stack =>
      new SingleSideListTest[Stack](
        ssl,
        new GoldenStack[Int](),
        width,
        depth)
  }

  Driver.execute(args,
                 () => new Queue(width, depth)) {
    ssl: Queue =>
      new SingleSideListTest[Queue](
        ssl,
        new GoldenQueue[Int](),
        width,
        depth)
  }
}
