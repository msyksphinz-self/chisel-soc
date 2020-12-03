package core_complex

import chisel3._
import cpu.{CpuDebugMonitor, RVConfig}
import freechips.rocketchip.config._
import freechips.rocketchip.tilelink._
import freechips.rocketchip.diplomacy._

class core_complex[Conf <: RVConfig] (conf: Conf, numCores: Int, ramBeatBytes: Int, txns: Int)(implicit p: Parameters) extends LazyModule {
  val loader = LazyModule(new loader("loader"))

  // val ifu    = LazyModule(new ifu("ifu"))
  val core   = Seq.tabulate(numCores) { case i => LazyModule(new CoreTop(conf, i, "core" + i.toString)) }
  val xbar   = LazyModule(new TLXbar)
  val memory = LazyModule(new TLRAM(AddressSet(0x80000000L, 0x0ffff), beatBytes = ramBeatBytes))

  xbar.node := loader.node
  core.foreach { case (core) => {
    xbar.node := TLDelayer(0.1) := core.inst_node
    xbar.node := TLDelayer(0.1) := core.data_node
  }
  }
  memory.node := xbar.node

  lazy val module = new LazyModuleImp(this) {
    val io = IO(new Bundle {
      val req   = Input(Bool())
      val addr  = Input(UInt(32.W))
      val data  = Input(UInt(32.W))
      val ready = Output(Bool())

      val cpu_dbg = Output(Vec(numCores, new CpuDebugMonitor(conf)))
    })

    loader.module.io.req  := io.req && (io.addr(31,16) =/= 0x2000.U)
    loader.module.io.addr := io.addr
    loader.module.io.data := io.data
    io.ready := loader.module.io.ready

    // CPU Core Contorl
    val cpu_run = Seq.fill(numCores) { RegInit(false.B) }
    cpu_run.foreach { case(cpu_run) => cpu_run := Mux(io.req && io.req && (io.addr === 0x20000000.U), io.data(0), cpu_run) }

    core.zip(cpu_run).foreach { case (core, cpu_run) => core.module.io.run := cpu_run }

    io.cpu_dbg := core.map { case(core) => core.module.io.dbg_monitor }
  }
}
