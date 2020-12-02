package freechips.rocketchip.tilelink

import chisel3._
import chisel3.util.HasBlackBoxResource
import chisel3.util.DecoupledIO
import cpu.{CpuDebugMonitor, RVConfig}
import freechips.rocketchip.config._

class debugIO extends Bundle {
  val addr  = Output(UInt(32.W))
  val data  = Output(UInt(32.W))
}

class sim_dtm[Conf <: RVConfig](conf: Conf)(implicit p: Parameters) extends BlackBox with HasBlackBoxResource {
  val io = IO(new Bundle {
    val clock = Input(Clock())
    val reset = Input(Reset())
    val req = DecoupledIO(new debugIO())
  })

}
