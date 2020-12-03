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
  val numCores = 4

  val io = IO(new Bundle {
    val success = Output(Bool())
  })

  val ldut = LazyModule(new core_complex(rv_conf, numCores = numCores, 4, 5000))
  val dut = Module(ldut.module)

  val dtm = Module(new sim_dtm(rv_conf));
  dtm.io.clock := clock
  dtm.io.reset := reset
  dut.io.req   := dtm.io.req.valid
  dut.io.addr  := dtm.io.req.bits.addr
  dut.io.data  := dtm.io.req.bits.data
  dtm.io.req.ready := dut.io.ready

  val sim_mon = Seq.fill(numCores) { Module(new sim_monitor(rv_conf)) }
  sim_mon.zip(dut.io.cpu_dbg).foreach { case (sim_mon, cpu_dbg) => {
    sim_mon.io.clock := clock
    sim_mon.io.reset := reset
    sim_mon.io.dbg := cpu_dbg
  }}

  io.success := false.B

  ElaborationArtefacts.add("graphml", ldut.graphML)
}
