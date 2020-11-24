package core_complex

import chisel3._
import chisel3.util._

import scala.io.Source
import java.io._

import freechips.rocketchip.config._
import freechips.rocketchip.tilelink._
import freechips.rocketchip.diplomacy._

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

    d.ready := true.B

    // Tie off unused channels
    out.b.valid := false.B
    out.c.ready := true.B
    out.e.ready := true.B

    val s_init :: s_trans :: Nil = Enum(2)
    val trans_state = RegInit(s_init)

    val reg_a_valid = RegInit(false.B)
    val reg_a_address = RegNext(io.addr)
    val reg_a_data = RegNext(io.data)

    switch (trans_state) {
      is (s_init) {
        when(io.req) {
          reg_a_valid := true.B
          reg_a_address := io.addr
          reg_a_data := io.data
          io.ready := false.B
          trans_state := s_trans
        }
      }
      is (s_trans) {
        when(out.a.fire) {
          reg_a_valid := false.B
          io.ready := true.B
        }
      }
    }
    out.a.valid := reg_a_valid
    out.a.bits.address := reg_a_address
    out.a.bits.data := reg_a_data
    out.a.bits.opcode := TLMessages.PutFullData
    out.a.bits.mask := 0xf.U
    out.a.bits.param := 0.U

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
