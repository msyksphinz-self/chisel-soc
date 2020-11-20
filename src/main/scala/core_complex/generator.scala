// See LICENSE.SiFive for license details.

package core_complex

import java.io.{File, FileWriter}

import freechips.rocketchip.config.Parameters
import chisel3.Driver
import freechips.rocketchip.util.ElaborationArtefacts

object Generator {
    final def main(args: Array[String]) {
        println("Generator.main() started.\n")
        val verilog = Driver.emitVerilog(
            new TestHarness()(Parameters.empty)
        )
        ElaborationArtefacts.files.foreach { case (extension, contents) =>
            val f = new File(".", "TestHarness." + extension)
            val fw = new FileWriter(f)
            fw.write(contents())
            fw.close
        }
    }
}
