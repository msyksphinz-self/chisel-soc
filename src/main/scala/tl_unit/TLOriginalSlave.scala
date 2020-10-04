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


class TLOriginalSlave(ramBeatBytes: Int, txns: Int)(implicit p: Parameters) extends LazyModule {
  val pushers = List(
    LazyModule(new TLPatternPusher("pat0", Seq(
      new WritePattern(0x100, 0x2, 0x012345678L),
      new WritePattern(0x500, 0x2, 0x0abcdef01L),
      new ReadExpectPattern(0x100, 0x2, 0x012345678L),
      new ReadExpectPattern(0x500, 0x2, 0x0abcdef01L)
    ))),
    LazyModule(new TLPatternPusher("pat1", Seq(
      new WritePattern(0x200, 0x2, 0x012345678L),
      new WritePattern(0x600, 0x2, 0x0abcdef01L),
      new ReadExpectPattern(0x200, 0x2, 0x012345678L),
      new ReadExpectPattern(0x600, 0x2, 0x0abcdef01L)
    ))),
    LazyModule(new TLPatternPusher("pat2", Seq(
      new WritePattern(0x300, 0x2, 0x012345678L),
      new WritePattern(0x700, 0x2, 0x0abcdef01L),
      new ReadExpectPattern(0x300, 0x2, 0x012345678L),
      new ReadExpectPattern(0x700, 0x2, 0x0abcdef01L)
    ))),
    LazyModule(new TLPatternPusher("pat3", Seq(
      new WritePattern(0x400, 0x2, 0x012345678L),
      new WritePattern(0x800, 0x2, 0x0abcdef01L),
      new ReadExpectPattern(0x400, 0x2, 0x012345678L),
      new ReadExpectPattern(0x800, 0x2, 0x0abcdef01L)
    )))
  )
  val model = List(LazyModule(new TLRAMModel("SRAMSimple")),
                   LazyModule(new TLRAMModel("SRAMSimple")),
                   LazyModule(new TLRAMModel("SRAMSimple")),
                   LazyModule(new TLRAMModel("SRAMSimple")))
  val xbar      = LazyModule(new TLXbar)
  val slavereg0 = LazyModule(new TLSlaveReg(AddressSet(0x000, 0x3ff), beatBytes = ramBeatBytes))
  val slavereg1 = LazyModule(new TLSlaveReg(AddressSet(0x400, 0x3ff), beatBytes = ramBeatBytes))

  pushers.zip(model).map{ case (pusher, model) =>
    xbar.node := model.node := pusher.node
  }
  slavereg0.node := xbar.node
  slavereg1.node := xbar.node

  lazy val module = new LazyModuleImp(this) with UnitTestModule {
    // TLPatternPusher
    pushers.map(p => p.module.io.run := true.B)
    io.finished := pushers.map(p => p.module.io.done).reduce((x, y) => x && y)
  }
}


class TLOriginalSlaveTest(ramBeatBytes: Int, txns: Int = 5000, timeout: Int = 500000)(implicit p: Parameters) extends UnitTest(timeout) {
  val lazy_dut = LazyModule(new TLOriginalSlave(ramBeatBytes, txns))
  val dut = Module(lazy_dut.module)
  ElaborationArtefacts.add("graphml", lazy_dut.graphML)
  io.finished := dut.io.finished
}


class WithTLOriginalSlaveTest extends Config((site, here, up) => {
  case UnitTests => (q: Parameters) => {
    implicit val p = q
    val txns = 1 * site(TestDurationMultiplier)
    val timeout = 50000 * site(TestDurationMultiplier)
    Seq(
      Module(new TLOriginalSlaveTest(4, txns=15*txns, timeout=timeout))
    )}
})

class TLOriginalSlaveTestConfig extends Config(new WithTLOriginalSlaveTest ++ new WithTestDuration(1) ++ new BaseSubsystemConfig)
