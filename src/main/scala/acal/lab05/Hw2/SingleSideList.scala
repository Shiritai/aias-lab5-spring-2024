package acal.lab05.Hw2

import chisel3._
import chisel3.util.log2Ceil

trait SingleSideListIO extends Bundle {
  def push:    Bool
  def pop:     Bool
  def en:      Bool
  def dataIn:  UInt
  def size:    UInt
  def dataOut: UInt
}

trait SingleSideList {
  def io: SingleSideListIO

  def mem: Mem[UInt]
}
