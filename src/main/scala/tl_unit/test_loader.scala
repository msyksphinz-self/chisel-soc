package core_complex

import chisel3._

import scala.io.Source
import java.io._

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

class loader(name: String)(implicit p: Parameters) extends LazyModule {
  val node = TLClientNode(Seq(TLClientPortParameters(Seq(TLClientParameters(name = name)))))

  lazy val module = new LazyModuleImp(this) {
    val io = IO(new Bundle {
      val req = Input(Bool())
      val addr = Input(UInt(32.W))
      val data = Input(UInt(32.W))
      val ready = Output(Bool())
    })

    val (out, edge) = node.out(0)

    val baseEnd = 0
    val (sizeEnd,   sizeOff)   = (edge.bundle.sizeBits   + baseEnd, baseEnd)
    val (sourceEnd, sourceOff) = (edge.bundle.sourceBits + sizeEnd, sizeEnd)
    val beatBytes = edge.bundle.dataBits

    val a = out.a
    val d = out.d

    val (_, put_pbits) = edge.Put(0.U, io.addr, 2.U, io.data);
    a.valid := io.req
    a.bits  := put_pbits

    d.ready := true.B

    // Tie off unused channels
    out.b.valid := false.B
    out.c.ready := true.B
    out.e.ready := true.B


    // Ready signal to Test Bench
    io.ready := out.a.ready
  }
}


object loader
{
  def apply(name: String)(implicit p: Parameters): TLInwardNode =
  {
    val module = LazyModule(new loader(name))
    module.node
  }
}
