// See LICENSE.SiFive for license details.

package core_complex

import chisel3._
import cpu.RV64IConfig
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.diplomacy.LazyModule
import freechips.rocketchip.util.ElaborationArtefacts
import freechips.rocketchip.tilelink.sim_dtm

class TestHarness()(implicit p: Parameters) extends Module {
  val rv_conf = new RV64IConfig

  val io = IO(new Bundle {
    val success = Output(Bool())
  })

  val ldut = LazyModule(new core_complex(rv_conf, 4, 5000))
  val dut = Module(ldut.module)

  val dtm = Module(new sim_dtm(rv_conf));
  dtm.io.clock := clock
  dtm.io.reset := reset
  dut.io.req   := dtm.io.req.valid
  dut.io.addr  := dtm.io.req.bits.addr
  dut.io.data  := dtm.io.req.bits.data
  dtm.io.req.ready := dut.io.ready

  val sim_mon0 = Module(new sim_monitor(rv_conf))
  sim_mon0.io.clock := clock
  sim_mon0.io.reset := reset
  sim_mon0.io.dbg := dut.io.cpu0_dbg
  val sim_mon1 = Module(new sim_monitor(rv_conf))
  sim_mon1.io.clock := clock
  sim_mon1.io.reset := reset
  sim_mon1.io.dbg := dut.io.cpu1_dbg

  io.success := false.B

  ElaborationArtefacts.add("graphml", ldut.graphML)
}
