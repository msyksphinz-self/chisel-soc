package freechips.rocketchip.unittest

import chisel3._

import freechips.rocketchip.config._
import freechips.rocketchip.amba.ahb._
import freechips.rocketchip.amba.apb._
import freechips.rocketchip.amba.axi4._
import freechips.rocketchip.subsystem.{BaseSubsystemConfig}
import freechips.rocketchip.devices.tilelink._
import freechips.rocketchip.tilelink._
import freechips.rocketchip.util._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.diplomaticobjectmodel.logicaltree.{BusMemoryLogicalTreeNode, LogicalModuleTree, LogicalTreeNode}
import freechips.rocketchip.diplomaticobjectmodel.model.{OMECC, TL_UL}

class core_complex(ramBeatBytes: Int, txns: Int)(implicit p: Parameters) extends LazyModule {
  val loader = LazyModule(new loader("loader"))

  val ifu    = LazyModule(new ifu("ifu"))
  val xbar   = LazyModule(new TLXbar)
  val memory = LazyModule(new TLRAM(AddressSet(0x020000000, 0x0ffff), beatBytes = ramBeatBytes))

  xbar.node := loader.node
  xbar.node := TLDelayer(0.0001) := ifu.node
  memory.node := xbar.node

  lazy val module = new LazyModuleImp(this) {
    val io = IO(new Bundle {
      val req   = Input(Bool())
      val addr  = Input(UInt(32.W))
      val data  = Input(UInt(32.W))
      val ready = Output(Bool())
    })

    loader.module.io.req  := io.req
    loader.module.io.addr := io.addr
    loader.module.io.data := io.data
    io.ready := loader.module.io.ready

    // TLPatternPusher
    ifu.module.io.run := true.B

    // io.finished := ifu.module.io.done
    when (ifu.module.io.error) {
      printf("Error is detected")
    }
  }
}


class core_complex_test(ramBeatBytes: Int, txns: Int = 5000, timeout: Int = 500000)(implicit p: Parameters) extends UnitTest(timeout) {
  val lazy_dut = LazyModule(new core_complex(ramBeatBytes, txns))
  val dut = Module(lazy_dut.module)
  ElaborationArtefacts.add("graphml", lazy_dut.graphML)
}


class withcore_complex_test extends Config((site, here, up) => {
  case UnitTests => (q: Parameters) => {
    implicit val p = q
    val txns = 1 * site(TestDurationMultiplier)
    val timeout = 50000 * site(TestDurationMultiplier)
    Seq(
      Module(new core_complex_test(4, txns=15*txns, timeout=timeout))
    )}
})

class CoreComplexTestConfig
    extends Config(new withcore_complex_test ++
      new WithTestDuration(1) ++
      new BaseSubsystemConfig)
