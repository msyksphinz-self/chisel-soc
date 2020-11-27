package core_complex

import chisel3._

import freechips.rocketchip.config._
import freechips.rocketchip.tilelink._
import freechips.rocketchip.diplomacy._

class core_complex(ramBeatBytes: Int, txns: Int)(implicit p: Parameters) extends LazyModule {
  val loader = LazyModule(new loader("loader"))

  // val ifu    = LazyModule(new ifu("ifu"))
  val core   = LazyModule(new CoreTop("core"))
  val xbar   = LazyModule(new TLXbar)
  val memory = LazyModule(new TLRAM(AddressSet(0x80000000L, 0x0fff), beatBytes = ramBeatBytes))

  xbar.node := loader.node
  xbar.node := /* TLDelayer(0.0001) := */ core.inst_node
  xbar.node := /* TLDelayer(0.0001) := */ core.data_node
  memory.node := xbar.node

  lazy val module = new LazyModuleImp(this) {
    val io = IO(new Bundle {
      val req   = Input(Bool())
      val addr  = Input(UInt(32.W))
      val data  = Input(UInt(32.W))
      val ready = Output(Bool())
    })

    loader.module.io.req  := io.req && (io.addr =/= 0x20000000.U)
    loader.module.io.addr := io.addr
    loader.module.io.data := io.data
    io.ready := loader.module.io.ready

    // CPU Core Contorl
    val cpu_run = RegInit(false.B)
    cpu_run := Mux(io.req && (io.addr === 0x20000000.U), io.data(0), cpu_run)
    core.module.io.run := cpu_run

    // io.finished := ifu.module.io.done
    // when (ifu.module.io.error) {
    //   printf("Error is detected")
    // }
  }
}
