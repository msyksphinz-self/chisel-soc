package core_complex

import chisel3._
import cpu.{CpuDebugMonitor, RVConfig}
import freechips.rocketchip.config._
import freechips.rocketchip.tilelink._
import freechips.rocketchip.diplomacy._

class core_complex[Conf <: RVConfig] (conf: Conf, ramBeatBytes: Int, txns: Int)(implicit p: Parameters) extends LazyModule {

  val loader = LazyModule(new loader("loader"))

  // val ifu    = LazyModule(new ifu("ifu"))
  val core0   = LazyModule(new CoreTop(conf, 0, "core0"))
  val core1   = LazyModule(new CoreTop(conf, 1, "core1"))
  val xbar   = LazyModule(new TLXbar)
  val memory = LazyModule(new TLRAM(AddressSet(0x80000000L, 0x0ffff), beatBytes = ramBeatBytes))

  xbar.node := loader.node
  xbar.node := /* TLDelayer(0.0001) := */ core0.inst_node
  xbar.node := /* TLDelayer(0.0001) := */ core0.data_node
  xbar.node := core1.inst_node
  xbar.node := core1.data_node
  memory.node := xbar.node

  lazy val module = new LazyModuleImp(this) {
    val io = IO(new Bundle {
      val req   = Input(Bool())
      val addr  = Input(UInt(32.W))
      val data  = Input(UInt(32.W))
      val ready = Output(Bool())

      val cpu0_dbg = Output(new CpuDebugMonitor(conf))
      val cpu1_dbg = Output(new CpuDebugMonitor(conf))
    })

    loader.module.io.req  := io.req && (io.addr(31,16) =/= 0x2000.U)
    loader.module.io.addr := io.addr
    loader.module.io.data := io.data
    io.ready := loader.module.io.ready

    // CPU Core Contorl
    val cpu0_run = RegInit(false.B)
    val cpu1_run = RegInit(false.B)
    cpu0_run := Mux(io.req && (io.addr === 0x20000000.U), io.data(0), cpu0_run)
    cpu1_run := Mux(io.req && (io.addr === 0x20000004.U), io.data(0), cpu1_run)

    core0.module.io.run := cpu0_run
    core1.module.io.run := cpu1_run

    io.cpu0_dbg := core0.module.io.dbg_monitor
    io.cpu1_dbg := core1.module.io.dbg_monitor

  }
}
