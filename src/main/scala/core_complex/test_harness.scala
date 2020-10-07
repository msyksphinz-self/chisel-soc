// See LICENSE.SiFive for license details.

package core_complex

import chisel3._
import freechips.rocketchip.config.Config
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.devices.debug.Debug
import freechips.rocketchip.diplomacy.LazyModule
import freechips.rocketchip.util.AsyncResetReg
import freechips.rocketchip.tilelink.sim_dtm
import freechips.rocketchip.subsystem.BaseSubsystemConfig

class TestHarness()(implicit p: Parameters) extends Module {
  val io = IO(new Bundle {
    val success = Output(Bool())
  })

  val ldut = LazyModule(new core_complex(4, 5000))
  val dut = Module(ldut.module)

  val dtm = Module(new sim_dtm);
  dtm.io.clock := clock
  dtm.io.reset := reset
  dut.io.req   := dtm.io.req.valid
  dut.io.addr  := dtm.io.req.bits.addr
  dut.io.data  := dtm.io.req.bits.data
  dtm.io.req.ready := dut.io.ready

  io.success := false.B
}

// class DefaultConfig extends Config(new BaseSubsystemConfig)
