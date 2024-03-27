#!/bin/bash
# Helper script to elaborate a Chisel module that has no parameters.

set -e

package="$1"
module="$2"
parameter="$3"

cat <<EOF >_temphelper.scala
package _temphelper

import ${package}._

object Elaborate extends App {
  (new chisel3.stage.ChiselStage).emitVerilog(
      new ${module}(${parameter}),
      Array("-td","generated/${module}")
  )
}
EOF

echo "Elaborating module ${module}..."
sbt "runMain _temphelper.Elaborate -td ./generated/${module}"
echo "Elaborated Verilog available at generated/${module}"

rm _temphelper.scala

exit 0
