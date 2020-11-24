package core_complex

import chisel3._
import cpu._
import chipsalliance.rocketchip.config.Parameters
import chisel3.{Bool, Bundle, Input, Output, RegInit}
import freechips.rocketchip.diplomacy.{LazyModule, LazyModuleImp}
import freechips.rocketchip.tilelink._
import freechips.rocketchip.tilelink.{TLClientNode, TLClientParameters, TLClientPortParameters}

class CoreTop(name: String)(implicit p: Parameters) extends LazyModule {
  val inst_node = TLClientNode(Seq(TLClientPortParameters(Seq(TLClientParameters(name = name + "_inst")))))
  val data_node = TLClientNode(Seq(TLClientPortParameters(Seq(TLClientParameters(name = name + "_name")))))

  lazy val module = new LazyModuleImp(this) {
    val io = IO(new Bundle {
      val run = Input(Bool())
      val done = Output(Bool())
      val error = Output(Bool())
    })

    val (inst_out, inst_edge) = inst_node.out(0)
    val (data_out, data_edge) = data_node.out(0)
    val baseEnd = 0
    val (sizeEnd, sizeOff) = (inst_edge.bundle.sizeBits + baseEnd, baseEnd)
    val (sourceEnd, sourceOff) = (inst_edge.bundle.sourceBits + sizeEnd, sizeEnd)
    val beatBytes = inst_edge.bundle.dataBits

    val cpu = Module(new Cpu(new RV64IConfig))

    cpu.io.run := true.B

    inst_out.a.valid := cpu.io.inst_bus.req
    inst_out.a.bits.address := cpu.io.inst_bus.addr
    inst_out.a.bits.opcode := TLMessages.Get
    inst_out.a.bits.mask := 0xf.U
    inst_out.a.bits.param := 0.U
    cpu.io.inst_bus.ack := inst_out.d.valid
    cpu.io.inst_bus.rddata := inst_out.d.bits.data.asSInt

    data_out.a.valid := cpu.io.data_bus.req
    data_out.a.bits.address := cpu.io.data_bus.addr
    data_out.a.bits.opcode := TLMessages.Get
    data_out.a.bits.mask := 0xf.U
    data_out.a.bits.param := 0.U
    cpu.io.data_bus.ack := data_out.d.valid
    cpu.io.data_bus.rddata := data_out.d.bits.data.asSInt
  }
}
