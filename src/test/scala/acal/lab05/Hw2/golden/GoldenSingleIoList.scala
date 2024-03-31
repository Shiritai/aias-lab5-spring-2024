package acal.lab05.Hw2.golden

/**
 * A list-structured data structure having single
 * side as input and single side as output.
 */
trait GoldenSingleIoList[T] {

  /**
   * @brief
   *   push the element to input side
   */
  def push(e: T): Unit

  /**
   * @brief
   *   pop the element on the output side
   */
  def pop(): T

  /**
   * @brief
   *   peek the element on the output side
   */
  def peek(): T

  /**
   * @brief
   *   check whether the list is empty
   */
  def isEmpty: Boolean

  /**
   * @brief
   *   get size of the list
   */
  def size: Int
}
