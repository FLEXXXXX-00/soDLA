package nvdla

import chisel3._


class PGAOPV_INVD2PO4 extends Module {
  val io = IO(new Bundle {
    val I = Input(Bool())
    val ZN = Output(Bool())
  })
  io.ZN := ~io.I 
}