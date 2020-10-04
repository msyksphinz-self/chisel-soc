// See LICENSE.SiFive for license details.

package freechips.rocketchip.system

import Chisel._
// import chisel3._
import freechips.rocketchip.config.Config
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.devices.debug.Debug
import freechips.rocketchip.diplomacy.LazyModule
import freechips.rocketchip.util.AsyncResetReg
import freechips.rocketchip.unittest.core_complex
import freechips.rocketchip.tilelink.sim_dtm
import freechips.rocketchip.subsystem.BaseSubsystemConfig

class TestHarness()(implicit p: Parameters) extends Module {
  val io = IO(new Bundle {
    val success = Output(Bool())
  })

  val ldut = LazyModule(new core_complex(4, 5000))
  val dut = Module(ldut.module)

  val dtm = Module(new sim_dtm);
  dut.io.req  := dtm.io.req
  dut.io.addr := dtm.io.addr
  dut.io.data := dtm.io.data
  dtm.io.ready := dut.io.ready
}

class DefaultConfig extends Config(new BaseSubsystemConfig)
