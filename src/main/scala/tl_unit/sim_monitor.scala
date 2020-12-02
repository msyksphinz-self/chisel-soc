package core_complex

import chisel3._
import chisel3.util.HasBlackBoxResource
import chisel3.util.DecoupledIO
import cpu.{CpuDebugMonitor, RVConfig}
import freechips.rocketchip.config._
import freechips.rocketchip.tilelink.debugIO

class sim_monitor[Conf <: RVConfig](conf: Conf)(implicit p: Parameters) extends BlackBox with HasBlackBoxResource {
  val io = IO(new Bundle {
    val clock = Input(Clock())
    val reset = Input(Reset())

    val dbg = Input(new CpuDebugMonitor(conf))
  })
}
