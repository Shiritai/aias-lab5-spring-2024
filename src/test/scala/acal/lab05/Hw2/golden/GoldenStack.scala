package acal.lab05.Hw2.golden

class GoldenStack[T] extends GoldenSingleIoList[T] {
  var stack = List[T]()
  var peak  = 0

  /**
   * Push an element at the front
   */
  def push(e: T) = {
    stack = e :: stack
    peak  = Math.max(peak, stack.size)
  }

  /**
   * Pop the front element
   */
  def pop() = {
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