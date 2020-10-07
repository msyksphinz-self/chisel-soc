// See LICENSE.SiFive for license details.

package core_complex

import chisel3._
import freechips.rocketchip.config.Config
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.devices.debug.Debug
import freechips.rocketchip.diplomacy.LazyModule
import freechips.rocketchip.util.AsyncResetReg
import freechips.rocketchip.tilelink.sim_dtm
import chisel3.Driver
import freechips.rocketchip.subsystem.BaseSubsystemConfig

object Generator {
    final def main(args: Array[String]) {
        println("Generator.main() started.\n")
        val verilog = Driver.emitVerilog(
            new TestHarness()(Parameters.empty)
        )
    }
}
