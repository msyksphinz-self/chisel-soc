// See LICENSE.SiFive for license details.

package freechips.rocketchip.tilelink

import chisel3._
import chisel3.experimental.chiselName
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.diplomaticobjectmodel.logicaltree.{BusMemoryLogicalTreeNode, LogicalModuleTree, LogicalTreeNode}
import freechips.rocketchip.diplomaticobjectmodel.model.{OMECC, TL_UL}
import freechips.rocketchip.util._
import freechips.rocketchip.util.property._

class TLSlaveReg(
  address: AddressSet,
  parentLogicalTreeNode: Option[LogicalTreeNode] = None,
  beatBytes: Int = 4,
  val devName: Option[String] = None,
  val dtsCompat: Option[Seq[String]] = None
)(implicit p: Parameters) extends LazyModule
{
  val device = devName
    .map(new SimpleDevice(_, dtsCompat.getOrElse(Seq("sifive,sram0"))))
    .getOrElse(new MemoryDevice())

  val node = TLManagerNode(Seq(TLManagerPortParameters(
    Seq(TLManagerParameters(
      address            = List(address),
      resources          = device.reg("TLSlaveReg"),
      regionType         = RegionType.IDEMPOTENT,
      executable         = false,
      supportsGet        = TransferSizes(1, beatBytes),
      supportsPutPartial = TransferSizes(1, beatBytes),
      supportsPutFull    = TransferSizes(1, beatBytes),
      supportsArithmetic = TransferSizes.none,
      supportsLogical    = TransferSizes.none,
      fifoId             = Some(0))), // requests are handled in order
    beatBytes  = beatBytes,
    minLatency = 2))) // no bypass needed for this device

  lazy val module = new LazyModuleImp(this) {
    val (in, edge) = node.in(0)

    val baseEnd = 0
    val (sizeEnd,   sizeOff)   = (edge.bundle.sizeBits   + baseEnd, baseEnd)
    val (sourceEnd, sourceOff) = (edge.bundle.sourceBits + sizeEnd, sizeEnd)

    val counter = RegInit(0.U((beatBytes * 8).W))

    val a = in.a
    val d = in.d

    val a_read  = a.bits.opcode === TLMessages.Get
    val a_write = a.bits.opcode =/= TLMessages.Get
    val a_extra = a.bits.source ## a.bits.size

    in.a.ready := true.B

    when (a.fire && a_write) { printf("A.Write Addr = %x, Data = %x", a.bits.address, a.bits.data) }
    when (a.fire && a_read ) { printf("A.Read  Addr = %x", a.bits.address) }

    when (a_write) {
      counter := counter + a.bits.data
    }

    d.valid := RegNext(a_read | a_write)
    d.bits := edge.AccessAck(
      toSource    = a_extra(sourceEnd-1, sourceOff),
      lgSize      = a_extra(sizeEnd-1, sizeOff))
    d.bits.data   := counter
    d.bits.opcode := Mux(RegNext(a_read), TLMessages.AccessAckData, TLMessages.AccessAck)

    // Tie off unused channels
    in.b.valid := false.B
    in.c.ready := true.B
    in.e.ready := true.B
  }
}

object TLSlaveReg
{
  def apply(
    address: AddressSet,
    parentLogicalTreeNode: Option[LogicalTreeNode] = None,
    beatBytes: Int = 4,
    devName: Option[String] = None,
  )(implicit p: Parameters): TLInwardNode =
  {
    val ram = LazyModule(new TLSlaveReg(address, parentLogicalTreeNode, beatBytes, devName))
    ram.node
  }
}
