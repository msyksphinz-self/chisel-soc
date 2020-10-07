package freechips.rocketchip.tilelink

import chisel3._
import chisel3.util.HasBlackBoxResource
import chisel3.util.DecoupledIO

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

class debugIO extends Bundle {
  val addr  = Output(UInt(32.W))
  val data  = Output(UInt(32.W))
}

class sim_dtm(implicit p: Parameters) extends BlackBox with HasBlackBoxResource {
  val io = IO(new Bundle {
    val clock = Input(Clock())
    val reset = Input(Reset())
    val req = DecoupledIO(new debugIO())
  })

  // def connect(tbclk: Clock, tbreset: Bool, dutio: ClockedDMIIO, tbsuccess: Bool) = {
  //   io.clk := tbclk
  //   io.reset := tbreset
  //   dutio.dmi <> io.debug
  //   dutio.dmiClock := tbclk
  //   dutio.dmiReset := tbreset
  //
  //   tbsuccess := io.exit === 1.U
  //   when (io.exit >= 2.U) {
  //     printf("*** FAILED *** (exit code = %d)\n", io.exit >> 1.U)
  //     stop(1)
  //   }
  // }

  // addResource("sim_dtm.v")
  // addResource("sim_dtm.cc")
}
