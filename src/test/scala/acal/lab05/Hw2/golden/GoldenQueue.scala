package acal.lab05.Hw2.golden

import scala.collection.mutable.Queue

class GoldenQueue[T] extends GoldenSingleIoList[T] {
  var queue = new Queue[T]()

  /**
   * Push an element at the front
   */
  def push(e: T) = {
    queue.enqueue(e)
  }

  /**
   * Pop the front element
   */
  def pop() = {
    queue.dequeue()
  }

  def peek() = {
    queue.front
  }

  def isEmpty = queue.isEmpty
  def size    = queue.size
}
