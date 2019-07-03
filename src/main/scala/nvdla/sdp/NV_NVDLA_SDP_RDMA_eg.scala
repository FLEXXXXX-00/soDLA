// package nvdla

// import chisel3._
// import chisel3.experimental._
// import chisel3.util._

// class NV_NVDLA_SDP_RDMA_eg(implicit val conf: sdpConfiguration) extends Module {
//     val io = IO(new Bundle {
//         // clk
//         val nvdla_core_clk = Input(Clock())

//         val pwrbus_ram_pd = Input(UInt(32.W))
//         val op_load = Input(Bool())
//         val eg_done = Output(Bool())

//         val cq2eg_pd = Input(UInt(16.W))
//         val cq2eg_pvld = Input(Bool())
//         val cq2eg_prdy = Output(Bool())     // read

//         val lat_fifo_rd_pd = Input(UInt(conf.NVDLA_DMA_RD_RSP.W))
//         val lat_fifo_rd_pvld = Input(Bool())
//         val lat_fifo_rd_prdy = Output(Bool())

//         val dma_rd_cdt_lat_fifo_pop = Output(Bool())

//         val sdp_rdma2dp_alu_pd = Output(UInt((conf.AM_DW2 + 1).W))
//         val sdp_rdma2dp_alu_valid = Output(Bool())
//         val sdp_rdma2dp_alu_ready = Input(Bool())

//         val sdp_rdma2dp_mul_pd = Output(UInt((conf.AM_DW2 + 1).W))
//         val sdp_rdma2dp_mul_valid = Output(Bool())
//         val sdp_rdma2dp_mul_ready = Input(Bool())

//         val reg2dp_batch_number = Input(UInt(5.W))
//         val reg2dp_channel = Input(UInt(13.W))
//         val reg2dp_height = Input(UInt(13.W))
//         val reg2dp_width = Input(UInt(13.W))
//         val reg2dp_proc_precision = Input(UInt(2.W))
//         val reg2dp_out_precision = Input(UInt(2.W))
//         val reg2dp_rdma_data_mode = Input(Bool())
//         val reg2dp_rdma_data_size = Input(Bool())
//         val reg2dp_rdma_data_use = Input(UInt(2.W))
//     })
// //     
// //          ┌─┐       ┌─┐
// //       ┌──┘ ┴───────┘ ┴──┐
// //       │                 │
// //       │       ───       │          
// //       │  ─┬┘       └┬─  │
// //       │                 │
// //       │       ─┴─       │
// //       │                 │
// //       └───┐         ┌───┘
// //           │         │
// //           │         │
// //           │         │
// //           │         └──────────────┐
// //           │                        │
// //           │                        ├─┐
// //           │                        ┌─┘    
// //           │                        │
// //           └─┐  ┐  ┌───────┬──┐  ┌──┘         
// //             │ ─┤ ─┤       │ ─┤ ─┤         
// //             └──┴──┘       └──┴──┘ 
//     withClock(io.nvdla_core_clk){

// //==============
// // CFG REG
// //==============

//         val cfg_data_size_1byte = (io.reg2dp_rdma_data_size === false.B)
//         val cfg_data_size_2byte = (io.reg2dp_rdma_data_size === true.B)

//         val cfg_mode_mul_only = (io.reg2dp_rdma_data_use === 0.U)
//         val cfg_mode_alu_only = (io.reg2dp_rdma_data_use === 1.U)
//         val cfg_mode_both = (io.reg2dp_rdma_data_use === 2.U)
//         val cfg_mode_per_element = (io.reg2dp_rdma_data_mode === true.B)

//         val cfg_mode_single = cfg_mode_mul_only || cfg_mode_alu_only   

//         val cfg_mode_1bytex1 = cfg_data_size_1byte & cfg_mode_single
//         val cfg_mode_2bytex1 = cfg_data_size_2byte & cfg_mode_single
//         val cfg_mode_1bytex2 = cfg_data_size_1byte & cfg_mode_both
//         val cfg_mode_2bytex2 = cfg_data_size_2byte & cfg_mode_both

//         // #ifdef NVDLA_BATCH_ENABLE
//         // #endif

//         val cfg_dp_8 = (io.reg2dp_proc_precision === 0.U)
//         val cfg_do_8 = (io.reg2dp_out_precision === 0.U)

//         val cfg_alu_en = cfg_mode_alu_only || cfg_mode_both
//         val cfg_mul_en = cfg_mode_mul_only || cfg_mode_both

// //==============
// // DMA Interface
// //==============

//         io.dma_rd_cdt_lat_fifo_pop := io.lat_fifo_rd_pvld & io.lat_fifo_rd_prdy

// //==============
// // Latency FIFO to buffer return DATA
// //==============

//         val lat_fifo_rd_mask = Cat(
//                     Fill((4-conf.NVDLA_DMA_MASK_BIT), false.B), 
//                     io.lat_fifo_rd_pd((conf.NVDLA_DMA_RD_RSP-1), conf.NVDLA_MEMIF_WIDTH)
//                     )
//         val lat_fifo_rd_size = lat_fifo_rd_mask(3) + lat_fifo_rd_mask(2) + lat_fifo_rd_mask(1) + lat_fifo_rd_mask(0)

// //==================================================================
// // Context Queue: read
// //==================================================================

//         val ig2eg_size = io.cq2eg_pd(14,0)
//         val ig2eg_cube_end = io.cq2eg_pd(15)

//         val beat_size = ig2eg_size + 1.U
//         val beat_count = RegInit(0.U(15.W))
//         val beat_count_nxt = beat_count + lat_fifo_rd_size
//         val is_last_beat = (beat_count_nxt === beat_size)
//         val is_beat_end = is_last_beat & io.lat_fifo_rd_pvld & io.lat_fifo_rd_prdy
//         io.cq2eg_prdy := is_beat_end

//         when(io.lat_fifo_rd_pvld & io.lat_fifo_rd_prdy){
//             when(is_last_beat){
//                 beat_count := 0.U
//             }.otherwise{
//                 beat_count := beat_count_nxt
//             }
//         }

// // `ifdef SPYGLASS_ASSERT_ON
// //...

//         val lat_fifo_rd_beat_end = is_last_beat
//         val unpack_out_prdy = Wire(Bool())

//         val u_rdma_unpack = Module(new NV_NVDLA_SDP_RDMA_unpack)
//         u_rdma_unpack.io.nvdla_core_clk := io.nvdla_core_clk
//         u_rdma_unpack.io.inp_end := lat_fifo_rd_beat_end
//         u_rdma_unpack.io.inp_pvld := io.lat_fifo_rd_pvld
//         io.lat_fifo_rd_prdy := u_rdma_unpack.io.inp_prdy
//         u_rdma_unpack.io.inp_data := io.lat_fifo_rd_pd

//         val unpack_out_pvld = u_rdma_unpack.io.out_pvld
//         u_rdma_unpack.io.out_prdy := unpack_out_prdy
//         val unpack_out_pd = u_rdma_unpack.io.out_data


//         val unpack_out_mask = unpack_out_pd(4*conf.AM_DW+3, 4*conf.AM_DW) 
//         val alu_rod_rdy = Wire(Bool())
//         val mul_rod_rdy = Wire(Bool())

//         unpack_out_prdy := Mux(
//                             cfg_mode_both,
//                             (alu_rod_rdy & mul_rod_rdy),
//                             Mux(cfg_mode_alu_only, alu_rod_rdy, mul_rod_rdy)
//                             ) 
        
// //============================================================
// // Re-Order FIFO to send data to SDP-core               
// //============================================================
// //      |----------------------------------------------------|
// //      |    16B     |    16B     |    16B     |     16B     |
// // MODE |----------------------------------------------------|
// //      |     0            1            2            3       |
// // 1Bx1 | ALU or MUL | ALU or MUL | ALU or MUL or ALU or MUL |
// //      |----------------------------------------------------|
// //      |            0            |            1             |
// // 2Bx1 |        ALU or MUL       |        ALU or MUL        |
// //      |====================================================|
// //      |            0            |            1             |
// // 1Bx2 |    ALU     |    MUL     |    ALU     |     MUL     |
// //      |----------------------------------------------------|
// //      |            0            |            1             |
// // 2Bx2 |           ALU           |           MUL            |
// //      |----------------------------------------------------|
// //============================================================


//         val mode_2bytex2_alu_rod0_pd = Wire(UInt(conf.AM_DW.W))

//         for(i <- 0 to conf.NVDLA_MEMORY_ATOMIC_SIZE/2 - 1){
//             mode_2bytex2_alu_rod0_pd(16*i+15, 16*i) := unpack_out_pd(16*2*i+15, 16*2*i)
//         }

//         val mode_2bytex2_alu_rod1_pd = Wire(UInt(conf.AM_DW.W))

//         for(i <- 0 to conf.NVDLA_MEMORY_ATOMIC_SIZE/2 - 1){
//             mode_2bytex2_alu_rod1_pd(16*i+15, 16*i) := unpack_out_pd(16*(2*i+conf.NVDLA_MEMORY_ATOMIC_SIZE)+15, 16*(2*i+conf.NVDLA_MEMORY_ATOMIC_SIZE))
//         }

//         val mode_1bytex2_alu_rod0_pd = Wire(UInt(conf.AM_DW.W))

//         for(i <- 0 to conf.NVDLA_MEMORY_ATOMIC_SIZE - 1){
//             mode_1bytex2_alu_rod0_pd(8*i+7, 8*i) := unpack_out_pd(8*2*i+7, 8*2*i)
//         }

//         val mode_1bytex2_alu_rod1_pd = Wire(UInt(conf.AM_DW.W))

//         for(i <- 0 to conf.NVDLA_MEMORY_ATOMIC_SIZE - 1){
//             mode_1bytex2_alu_rod1_pd(8*i+7, 8*i) := unpack_out_pd(8*(i*2+conf.NVDLA_MEMORY_ATOMIC_SIZE*2)+7, 8*(i*2+conf.NVDLA_MEMORY_ATOMIC_SIZE*2))
//         }

//         val mode_2bytex2_mul_rod0_pd = Wire(UInt(conf.AM_DW.W))

//         for(i <- 0 to conf.NVDLA_MEMORY_ATOMIC_SIZE/2 - 1){
//             mode_2bytex2_mul_rod0_pd(16*i+15, 16*i) := unpack_out_pd(16*(i*2+1)+15, 16*(i*2+1))
//         }

//         val mode_2bytex2_mul_rod1_pd = Wire(UInt(conf.AM_DW.W))

//         for(i <- 0 to conf.NVDLA_MEMORY_ATOMIC_SIZE/2 - 1){
//             mode_2bytex2_mul_rod1_pd(16*i+15, 16*i) := unpack_out_pd(16*(i*2+1+conf.NVDLA_MEMORY_ATOMIC_SIZE*2)+15, 16*(i*2+1+conf.NVDLA_MEMORY_ATOMIC_SIZE*2))
//         }

//         val mode_1bytex2_mul_rod0_pd = Wire(UInt(conf.AM_DW.W))

//         for(i <- 0 to conf.NVDLA_MEMORY_ATOMIC_SIZE - 1){
//             mode_1bytex2_mul_rod0_pd(8*i+7, 8*i) := unpack_out_pd(8*(i*2+1)+7, 8*(i*2+1))
//         }

//         val mode_1bytex2_mul_rod1_pd = Wire(UInt(conf.AM_DW.W))

//         for(i <- 0 to conf.NVDLA_MEMORY_ATOMIC_SIZE - 1){
//             mode_1bytex2_mul_rod1_pd(8*i+7, 8*i) := unpack_out_pd(8*(i*2+1+conf.NVDLA_MEMORY_ATOMIC_SIZE*2)+7, 8*(i*2+1+conf.NVDLA_MEMORY_ATOMIC_SIZE*2))
//         }



//         val alu_rod0_pd = Mux(
//                             cfg_mode_2bytex2,
//                             mode_2bytex2_alu_rod0_pd,
//                             Mux(
//                                 cfg_mode_1bytex2, 
//                                 mode_1bytex2_alu_rod0_pd, 
//                                 unpack_out_pd((conf.AM_DW*0+conf.AM_DW-1), conf.AM_DW*0)
//                                 )
//                             )
//         val alu_rod1_pd = Mux(
//                             cfg_mode_2bytex2,
//                             mode_2bytex2_alu_rod1_pd,
//                             Mux(
//                                 cfg_mode_1bytex2, 
//                                 mode_1bytex2_alu_rod1_pd, 
//                                 unpack_out_pd((conf.AM_DW*1+conf.AM_DW-1), conf.AM_DW*1)
//                                 )
//                             )
//         val alu_rod2_pd = unpack_out_pd((conf.AM_DW*2+conf.AM_DW-1), conf.AM_DW*2)                     
//         val alu_rod3_pd = unpack_out_pd((conf.AM_DW*3+conf.AM_DW-1), conf.AM_DW*3)                                                            

//         val mul_rod0_pd = Mux(
//                             cfg_mode_2bytex2,
//                             mode_2bytex2_mul_rod0_pd,
//                             Mux(
//                                 cfg_mode_1bytex2, 
//                                 mode_1bytex2_mul_rod0_pd, 
//                                 unpack_out_pd((conf.AM_DW*0+conf.AM_DW-1), conf.AM_DW*0)
//                                 )
//                             )
//         val mul_rod1_pd = Mux(
//                             cfg_mode_2bytex2,
//                             mode_2bytex2_mul_rod1_pd,
//                             Mux(
//                                 cfg_mode_1bytex2, 
//                                 mode_1bytex2_mul_rod1_pd, 
//                                 unpack_out_pd((conf.AM_DW*1+conf.AM_DW-1), conf.AM_DW*1)
//                                 )
//                             )
//         val mul_rod2_pd = unpack_out_pd((conf.AM_DW*2+conf.AM_DW-1), conf.AM_DW*2)                     
//         val mul_rod3_pd = unpack_out_pd((conf.AM_DW*3+conf.AM_DW-1), conf.AM_DW*3)                                                            

//         val alu_rod_mask = Mux(
//                         cfg_mode_both, 
//                         Cat(0.U(2.W), unpack_out_mask(2), unpack_out_mask(0)), 
//                         unpack_out_mask
//                         )
//         val mul_rod_mask = Mux(
//                         cfg_mode_both, 
//                         Cat(0.U(2.W), unpack_out_mask(2), unpack_out_mask(0)), 
//                         unpack_out_mask
//                         )
//         val alu_roc_size = alu_rod_mask(3) + alu_rod_mask(2) + alu_rod_mask(1) + alu_rod_mask(0)
//         val mul_roc_size = mul_rod_mask(3) + mul_rod_mask(2) + mul_rod_mask(1) + mul_rod_mask(0)

//         val alu_rod_vld = cfg_alu_en & unpack_out_pvld & Mux(cfg_mode_both, mul_rod_rdy, true.B)
//         val mul_rod_vld = cfg_mul_en & unpack_out_pvld & Mux(cfg_mode_both, alu_rod_rdy, true.B)

//         val mul_roc_rdy = Wire(Bool())
//         val alu_roc_rdy = Wire(Bool())
//         val alu_roc_vld = cfg_alu_en & unpack_out_pvld & Mux(cfg_mode_both, (mul_roc_rdy & mul_rod_rdy & alu_rod_rdy), alu_rod_rdy)
//         val mul_roc_vld = cfg_mul_en & unpack_out_pvld & Mux(cfg_mode_both, (alu_roc_rdy & mul_rod_rdy & alu_rod_rdy), mul_rod_rdy)

//         val alu_roc_pd = alu_roc_size - 1.U
//         val mul_roc_pd = mul_roc_size - 1.U

// ////////////////split unpack pd to 4 atomic_m alu or mul data /////////////////////
     
//         val u_alu = Module(new NV_NVDLA_SDP_RDMA_EG_ro)
//         u_alu.io.nvdla_core_clk     := io.nvdla_core_clk
//         u_alu.io.pwrbus_ram_pd      := io.pwrbus_ram_pd
//         io.sdp_rdma2dp_alu_valid    := u_alu.io.sdp_rdma2dp_valid
//         u_alu.io.sdp_rdma2dp_ready  := io.sdp_rdma2dp_alu_ready
//         io.sdp_rdma2dp_alu_pd       := u_alu.io.sdp_rdma2dp_pd
//         u_alu.io.rod0_wr_pd         := alu_rod0_pd
//         u_alu.io.rod1_wr_pd         := alu_rod1_pd
//         u_alu.io.rod2_wr_pd         := alu_rod2_pd
//         u_alu.io.rod3_wr_pd         := alu_rod3_pd
//         u_alu.io.rod_wr_mask        := alu_rod_mask
//         u_alu.io.rod_wr_vld         := alu_rod_vld
//         alu_rod_rdy                 := u_alu.io.rod_wr_rdy
//         u_alu.io.roc_wr_pd          := alu_roc_pd
//         u_alu.io.roc_wr_vld         := alu_roc_vld
//         alu_roc_rdy                 := u_alu.io.roc_wr_rdy
//         u_alu.io.cfg_dp_8           := cfg_dp_8
//         u_alu.io.cfg_dp_size_1byte  := cfg_data_size_1byte
//         u_alu.io.cfg_mode_per_element   := cfg_mode_per_element
//         // #ifdef NVDLA_BATCH_ENABLE
//         //...
//         u_alu.io.reg2dp_channel     := io.reg2dp_channel
//         u_alu.io.reg2dp_height      := io.reg2dp_height
//         u_alu.io.reg2dp_width       := io.reg2dp_width
//         val alu_layer_end           = u_alu.io.layer_end


//         val u_mul = Module(new NV_NVDLA_SDP_RDMA_EG_ro)
//         u_mul.io.nvdla_core_clk     := io.nvdla_core_clk
//         u_mul.io.pwrbus_ram_pd      := io.pwrbus_ram_pd
//         io.sdp_rdma2dp_mul_valid    := u_mul.io.sdp_rdma2dp_valid
//         u_mul.io.sdp_rdma2dp_ready  := io.sdp_rdma2dp_mul_ready
//         io.sdp_rdma2dp_mul_pd       := u_mul.io.sdp_rdma2dp_pd
//         u_mul.io.rod0_wr_pd         := mul_rod0_pd
//         u_mul.io.rod1_wr_pd         := mul_rod1_pd
//         u_mul.io.rod2_wr_pd         := mul_rod2_pd
//         u_mul.io.rod3_wr_pd         := mul_rod3_pd
//         u_mul.io.rod_wr_mask        := mul_rod_mask
//         u_mul.io.rod_wr_vld         := mul_rod_vld
//         mul_rod_rdy                 := u_mul.io.rod_wr_rdy
//         u_mul.io.roc_wr_pd          := mul_roc_pd
//         u_mul.io.roc_wr_vld         := mul_roc_vld
//         mul_roc_rdy                 := u_mul.io.roc_wr_rdy
//         u_mul.io.cfg_dp_8           := cfg_dp_8
//         u_mul.io.cfg_dp_size_1byte  := cfg_data_size_1byte
//         u_mul.io.cfg_mode_per_element   := cfg_mode_per_element
//         // #ifdef NVDLA_BATCH_ENABLE
//         //...
//         u_mul.io.reg2dp_channel     := io.reg2dp_channel
//         u_mul.io.reg2dp_height      := io.reg2dp_height
//         u_mul.io.reg2dp_width       := io.reg2dp_width
//         val mul_layer_end           = u_mul.io.layer_end

// //==========================================================
// // Layer Done
// //==========================================================

//         val alu_layer_done = RegInit(false.B)
//         val layer_done = Wire(Bool())
//         when(io.op_load){
//             when(cfg_alu_en){
//                 alu_layer_done := false.B
//             }.otherwise{
//                 alu_layer_done := true.B
//             }
//         }.elsewhen(alu_layer_end){
//             alu_layer_done := true.B
//         }.elsewhen(layer_done){
//             alu_layer_done := false.B
//         }

//         val mul_layer_done = RegInit(false.B)
//         when(io.op_load){
//             when(cfg_mul_en){
//                 mul_layer_done := false.B
//             }.otherwise{
//                 mul_layer_done := false.B
//             }
//         }.elsewhen(mul_layer_end){
//             mul_layer_done := true.B
//         }.elsewhen(layer_done){
//             mul_layer_done := false.B
//         }

//         layer_done := alu_layer_done & mul_layer_done
//         io.eg_done := RegInit(false.B)
//         io.eg_done := layer_done

// }}

// object NV_NVDLA_SDP_RDMA_egDriver extends App {
//     implicit val conf: sdpConfiguration = new sdpConfiguration
//     chisel3.Driver.execute(args, () => new NV_NVDLA_SDP_RDMA_eg())
// }

