package nvdla

import chisel3._
import chisel3.experimental._
import chisel3.util._

class NV_NVDLA_SDP_CORE_y(implicit val conf: sdpConfiguration) extends Module {
   val io = IO(new Bundle {

        val nvdla_core_clk = Input(Bool())
        val nvdla_core_rstn = Input(Bool())
        val pwrbus_ram_pd = Input(Bool())
        //alu_in
        val ew_alu_in_vld = Input(Bool())
        val ew_alu_in_rdy = Output(Bool())
        val ew_alu_in_data = Input(UInt(conf.EW_OP_DW.W))
        // data_in
        val ew_data_in_pvld = Input(Bool())
        val ew_data_in_prdy = Output(Bool())
        val ew_data_in_pd = Input(UInt(conf.EW_IN_DW.W))
        // mul_in
        val ew_mul_in_vld = Input(Bool())
        val ew_mul_in_rdy = Output(Bool())
        val ew_mul_in_data = Input(UInt(conf.EW_OP_DW.W))
        // data_out
        val ew_data_out_pvld = Output(Bool())
        val ew_data_out_prdy = Input(Bool())
        val ew_data_out_pd = Output(UInt(conf.EW_OUT_DW.W))
        // reg2dp
        val reg2dp_ew_alu_algo = Input(UInt(2.W))
        val reg2dp_ew_alu_bypass = Input(Bool())
        val reg2dp_ew_alu_cvt_bypass = Input(Bool())
        val reg2dp_ew_alu_cvt_offset = Input(UInt(32.W))
        val reg2dp_ew_alu_cvt_scale = Input(UInt(16.W))
        val reg2dp_ew_alu_cvt_truncate = Input(UInt(6.W))
        val reg2dp_ew_alu_operand = Input(UInt(32.W))
        val reg2dp_ew_alu_src = Input(Bool())
        val reg2dp_ew_lut_bypass = Input(Bool())
        val reg2dp_ew_mul_bypass = Input(Bool())
        val reg2dp_ew_mul_cvt_bypass = Input(Bool())
        val reg2dp_ew_mul_cvt_offset = Input(UInt(32.W))
        val reg2dp_ew_mul_cvt_scale = Input(UInt(16.W))
        val reg2dp_ew_mul_cvt_truncate = Input(UInt(6.W))
        val reg2dp_ew_mul_operand = Input(UInt(32.W))
        val reg2dp_ew_mul_prelu = Input(Bool())
        val reg2dp_ew_mul_src = Input(Bool())
        val reg2dp_ew_truncate = Input(UInt(10.W))
        val reg2dp_lut_hybrid_priority = if(conf.NVDLA_SDP_LUT_ENABLE) Some(Input(Bool())) else None
        val reg2dp_lut_int_access_type = if(conf.NVDLA_SDP_LUT_ENABLE) Some(Input(Bool())) else None
        val reg2dp_lut_int_addr = if(conf.NVDLA_SDP_LUT_ENABLE) Some(Input(UInt(10.W))) else None
        val reg2dp_lut_int_data = if(conf.NVDLA_SDP_LUT_ENABLE) Some(Input(UInt(16.W))) else None
        val reg2dp_lut_int_data_wr = if(conf.NVDLA_SDP_LUT_ENABLE) Some(Input(Bool())) else None
        val reg2dp_lut_int_table_id = if(conf.NVDLA_SDP_LUT_ENABLE) Some(Input(Bool())) else None
        val reg2dp_lut_le_end = if(conf.NVDLA_SDP_LUT_ENABLE) Some(Input(UInt(32.W))) else None
        val reg2dp_lut_le_function = if(conf.NVDLA_SDP_LUT_ENABLE) Some(Input(Bool())) else None
        val reg2dp_lut_le_index_offset = if(conf.NVDLA_SDP_LUT_ENABLE) Some(Input(UInt(8.W))) else None
        val reg2dp_lut_le_index_select = if(conf.NVDLA_SDP_LUT_ENABLE) Some(Input(UInt(8.W))) else None
        val reg2dp_lut_le_slope_oflow_scale = if(conf.NVDLA_SDP_LUT_ENABLE) Some(Input(UInt(16.W))) else None
        val reg2dp_lut_le_slope_oflow_shift = if(conf.NVDLA_SDP_LUT_ENABLE) Some(Input(UInt(5.W))) else None
        val reg2dp_lut_le_slope_uflow_scale = if(conf.NVDLA_SDP_LUT_ENABLE) Some(Input(UInt(16.W))) else None
        val reg2dp_lut_le_slope_uflow_shift = if(conf.NVDLA_SDP_LUT_ENABLE) Some(Input(UInt(5.W))) else None
        val reg2dp_lut_le_start = if(conf.NVDLA_SDP_LUT_ENABLE) Some(Input(UInt(32.W))) else None
        val reg2dp_lut_lo_end = if(conf.NVDLA_SDP_LUT_ENABLE) Some(Input(UInt(32.W))) else None
        val reg2dp_lut_lo_index_select = if(conf.NVDLA_SDP_LUT_ENABLE) Some(Input(UInt(8.W))) else None
        val reg2dp_lut_lo_slope_oflow_scale = if(conf.NVDLA_SDP_LUT_ENABLE) Some(Input(UInt(16.W))) else None
        val reg2dp_lut_lo_slope_oflow_shift = if(conf.NVDLA_SDP_LUT_ENABLE) Some(Input(UInt(5.W))) else None
        val reg2dp_lut_lo_slope_uflow_scale = if(conf.NVDLA_SDP_LUT_ENABLE) Some(Input(UInt(16.W))) else None
        val reg2dp_lut_lo_slope_uflow_shift = if(conf.NVDLA_SDP_LUT_ENABLE) Some(Input(UInt(5.W))) else None
        val reg2dp_lut_lo_start = if(conf.NVDLA_SDP_LUT_ENABLE) Some(Input(UInt(32.W))) else None
        val reg2dp_lut_oflow_priority = if(conf.NVDLA_SDP_LUT_ENABLE) Some(Input(Bool())) else None
        val reg2dp_lut_uflow_priority = if(conf.NVDLA_SDP_LUT_ENABLE) Some(Input(Bool())) else None
        val dp2reg_lut_hybrid = if(conf.NVDLA_SDP_LUT_ENABLE) Some(Output(UInt(32.W))) else None
        val dp2reg_lut_int_data = if(conf.NVDLA_SDP_LUT_ENABLE) Some(Output(UInt(16.W))) else None
        val dp2reg_lut_le_hit = if(conf.NVDLA_SDP_LUT_ENABLE) Some(Output(UInt(32.W))) else None
        val dp2reg_lut_lo_hit = if(conf.NVDLA_SDP_LUT_ENABLE) Some(Output(UInt(32.W))) else None
        val dp2reg_lut_oflow = if(conf.NVDLA_SDP_LUT_ENABLE) Some(Output(UInt(32.W))) else None
        val dp2reg_lut_uflow = if(conf.NVDLA_SDP_LUT_ENABLE) Some(Output(UInt(32.W))) else None

        val reg2dp_nan_to_zero = Input(Bool())
        val reg2dp_perf_lut_en = Input(Bool())
        val reg2dp_proc_precision = Input(UInt(2.W))

        val op_en_load = Input(Bool())
    })
    //     
    //          ┌─┐       ┌─┐
    //       ┌──┘ ┴───────┘ ┴──┐
    //       │                 │
    //       │       ───       │          
    //       │  ─┬┘       └┬─  │
    //       │                 │
    //       │       ─┴─       │
    //       │                 │
    //       └───┐         ┌───┘
    //           │         │
    //           │         │
    //           │         │
    //           │         └──────────────┐
    //           │                        │
    //           │                        ├─┐
    //           │                        ┌─┘    
    //           │                        │
    //           └─┐  ┐  ┌───────┬──┐  ┌──┘         
    //             │ ─┤ ─┤       │ ─┤ ─┤         
    //             └──┴──┘       └──┴──┘ 
withClock(io.nvdla_core_clk){   

    val cfg_proc_precision = RegInit("b0".asUInt(2.W))
    val cfg_nan_to_zero = RegInit(false.B)
    val cfg_ew_alu_operand = RegInit("b0".asUInt(32.W))
    val cfg_ew_alu_bypass = RegInit(false.B)
    val cfg_ew_alu_algo = RegInit("b0".asUInt(2.W))
    val cfg_ew_alu_src = RegInit(false.B)
    val cfg_ew_alu_cvt_bypass = RegInit(false.B)
    val cfg_ew_alu_cvt_offset = RegInit("b0".asUInt(32.W))
    val cfg_ew_alu_cvt_scale = RegInit("b0".asUInt(16.W))
    val cfg_ew_alu_cvt_truncate = RegInit("b0".asUInt(6.W))
    val cfg_ew_mul_operand = RegInit("b0".asUInt(32.W))
    val cfg_ew_mul_bypass = RegInit(false.B)
    val cfg_ew_mul_src = RegInit(false.B)
    val cfg_ew_mul_cvt_bypass = RegInit(false.B)
    val cfg_ew_mul_cvt_offset = RegInit("b0".asUInt(32.W))
    val cfg_ew_mul_cvt_scale = RegInit("b0".asUInt(16.W))
    val cfg_ew_mul_cvt_truncate = RegInit("b0".asUInt(6.W))
    val cfg_ew_truncate = RegInit("b0".asUInt(10.W))
    val cfg_ew_mul_prelu = RegInit(false.B)
    val cfg_ew_lut_bypass = if(conf.NVDLA_SDP_LUT_ENABLE) Some(RegInit(false.B)) else None
    val cfg_lut_le_start = if(conf.NVDLA_SDP_LUT_ENABLE) Some(RegInit("b0".asUInt(32.W))) else None
    val cfg_lut_le_end = if(conf.NVDLA_SDP_LUT_ENABLE) Some(RegInit("b0".asUInt(32.W))) else None
    val cfg_lut_lo_start = if(conf.NVDLA_SDP_LUT_ENABLE) Some(RegInit("b0".asUInt(32.W))) else None
    val cfg_lut_lo_end = if(conf.NVDLA_SDP_LUT_ENABLE) Some(RegInit("b0".asUInt(32.W))) else None
    val cfg_lut_le_index_offset = if(conf.NVDLA_SDP_LUT_ENABLE) Some(RegInit("b0".asUInt(8.W))) else None
    val cfg_lut_le_index_select = if(conf.NVDLA_SDP_LUT_ENABLE) Some(RegInit("b0".asUInt(8.W))) else None
    val cfg_lut_lo_index_select = if(conf.NVDLA_SDP_LUT_ENABLE) Some(RegInit("b0".asUInt(8.W))) else None
    val cfg_lut_le_function = if(conf.NVDLA_SDP_LUT_ENABLE) Some(RegInit(false.B)) else None
    val cfg_lut_uflow_priority = if(conf.NVDLA_SDP_LUT_ENABLE) Some(RegInit(false.B)) else None
    val cfg_lut_oflow_priority = if(conf.NVDLA_SDP_LUT_ENABLE) Some(RegInit(false.B)) else None
    val cfg_lut_hybrid_priority = if(conf.NVDLA_SDP_LUT_ENABLE) Some(RegInit(false.B)) else None
    val cfg_lut_le_slope_oflow_scale = if(conf.NVDLA_SDP_LUT_ENABLE) Some(RegInit("b0".asUInt(16.W))) else None
    val cfg_lut_le_slope_oflow_shift = if(conf.NVDLA_SDP_LUT_ENABLE) Some(RegInit("b0".asUInt(5.W))) else None
    val cfg_lut_le_slope_uflow_scale = if(conf.NVDLA_SDP_LUT_ENABLE) Some(RegInit("b0".asUInt(16.W))) else None
    val cfg_lut_le_slope_uflow_shift = if(conf.NVDLA_SDP_LUT_ENABLE) Some(RegInit("b0".asUInt(5.W))) else None
    val cfg_lut_lo_slope_oflow_scale = if(conf.NVDLA_SDP_LUT_ENABLE) Some(RegInit("b0".asUInt(16.W))) else None
    val cfg_lut_lo_slope_oflow_shift = if(conf.NVDLA_SDP_LUT_ENABLE) Some(RegInit("b0".asUInt(5.W))) else None
    val cfg_lut_lo_slope_uflow_scale = if(conf.NVDLA_SDP_LUT_ENABLE) Some(RegInit("b0".asUInt(16.W))) else None
    val cfg_lut_lo_slope_uflow_shift = if(conf.NVDLA_SDP_LUT_ENABLE) Some(RegInit("b0".asUInt(5.W))) else None
    val cfg_ew_lut_bypass = if(conf.NVDLA_SDP_LUT_ENABLE) None else Some(RegInit(true.B))

    when(io.op_en_load){
        cfg_proc_precision := io.reg2dp_proc_precision
        cfg_nan_to_zero := io.reg2dp_nan_to_zero
        cfg_ew_alu_operand := io.reg2dp_ew_alu_operand
        cfg_ew_alu_bypass := io.reg2dp_ew_alu_bypass
        cfg_ew_alu_algo := io.reg2dp_ew_alu_algo
        cfg_ew_alu_src := io.reg2dp_ew_alu_src
        cfg_ew_alu_cvt_bypass := io.reg2dp_ew_alu_cvt_bypass
        cfg_ew_alu_cvt_offset := io.reg2dp_ew_alu_cvt_offset
        cfg_ew_alu_cvt_scale := io.reg2dp_ew_alu_cvt_scale
        cfg_ew_alu_cvt_truncate := io.reg2dp_ew_alu_cvt_truncate
        cfg_ew_mul_operand := io.reg2dp_ew_mul_operand  
        cfg_ew_mul_bypass := io.reg2dp_ew_mul_bypass      
        cfg_ew_mul_src := io.reg2dp_ew_mul_src         
        cfg_ew_mul_cvt_bypass := io.reg2dp_ew_mul_cvt_bypass  
        cfg_ew_mul_cvt_offset := io.reg2dp_ew_mul_cvt_offset  
        cfg_ew_mul_cvt_scale  := io.reg2dp_ew_mul_cvt_scale   
        cfg_ew_mul_cvt_truncate := io.reg2dp_ew_mul_cvt_truncate
        cfg_ew_truncate := io.reg2dp_ew_truncate        
        cfg_ew_mul_prelu := io.reg2dp_ew_mul_prelu  

        if(conf.NVDLA_SDP_LUT_ENABLE){   
            cfg_ew_lut_bypass.get := io.reg2dp_ew_lut_bypass     
            cfg_lut_le_start.get := io.reg2dp_lut_le_start.get          
            cfg_lut_le_end.get := io.reg2dp_lut_le_end.get 
            cfg_lut_lo_start.get := io.reg2dp_lut_lo_start.get
            cfg_lut_lo_end.get := io.reg2dp_lut_lo_end.get         
            cfg_lut_le_index_offset.get := io.reg2dp_lut_le_index_offset.get
            cfg_lut_le_index_select.get := io.reg2dp_lut_le_index_select.get
            cfg_lut_lo_index_select.get := io.reg2dp_lut_lo_index_select.get
            cfg_lut_le_function.get := io.reg2dp_lut_le_function.get
            cfg_lut_uflow_priority.get := io.reg2dp_lut_uflow_priority.get
            cfg_lut_oflow_priority.get := io.reg2dp_lut_oflow_priority.get
            cfg_lut_hybrid_priority.get := io.reg2dp_lut_hybrid_priority.get

            cfg_lut_le_slope_oflow_scale.get := io.reg2dp_lut_le_slope_oflow_scale.get
            cfg_lut_le_slope_oflow_shift.get := io.reg2dp_lut_le_slope_oflow_shift.get
            cfg_lut_le_slope_uflow_scale.get := io.reg2dp_lut_le_slope_uflow_scale.get
            cfg_lut_le_slope_uflow_shift.get := io.reg2dp_lut_le_slope_uflow_shift.get

            cfg_lut_lo_slope_oflow_scale.get := io.reg2dp_lut_lo_slope_oflow_scale.get
            cfg_lut_lo_slope_oflow_shift.get := io.reg2dp_lut_lo_slope_oflow_shift.get
            cfg_lut_lo_slope_uflow_scale.get := io.reg2dp_lut_lo_slope_uflow_scale.get
            cfg_lut_lo_slope_uflow_shift.get := io.reg2dp_lut_lo_slope_uflow_shift.get
        }  
        else{
            cfg_ew_lut_bypass.get := true.B
        }  
    }
    //===========================================
    // y input pipe
    //===========================================

    //=================================================
    val alu_cvt_out_prdy = Wire(Bool())
    val u_alu_cvt = Module(new NV_NVDLA_SDP_HLS_Y_cvt_top)
    u_alu_cvt.io.nvdla_core_clk := io.nvdla_core_clk
    u_alu_cvt.io.cfg_cvt_bypass := cfg_ew_alu_cvt_bypass
    u_alu_cvt.io.cfg_cvt_offset := cfg_ew_alu_cvt_offset
    u_alu_cvt.io.cfg_cvt_scale := cfg_ew_alu_cvt_scale
    u_alu_cvt.io.cfg_cvt_truncate := cfg_ew_alu_cvt_truncate
    u_alu_cvt.io.cvt_data_in := io.ew_alu_in_data
    u_alu_cvt.io.cvt_in_pvld := io.ew_alu_in_vld
    io.ew_alu_in_rdy := u_alu_cvt.io.cvt_out_prdy
    u_alu_cvt.io.cvt_out_prdy := alu_cvt_out_prdy
    val alu_cvt_out_pd = u_alu_cvt.io.cvt_data_out
    val alu_cvt_out_pvld = u_alu_cvt.io.cvt_out_pvld

    val mul_cvt_out_prdy = Wire(Bool())
    val u_mul_cvt = Module(new NV_NVDLA_SDP_HLS_Y_cvt_top)
    u_mul_cvt.io.nvdla_core_clk := io.nvdla_core_clk
    u_mul_cvt.io.cfg_cvt_bypass := cfg_ew_mul_cvt_bypass
    u_mul_cvt.io.cfg_cvt_offset := cfg_ew_mul_cvt_offset
    u_mul_cvt.io.cfg_cvt_scale := cfg_ew_mul_cvt_scale
    u_mul_cvt.io.cfg_cvt_truncate := cfg_ew_mul_cvt_truncate
    u_mul_cvt.io.cvt_data_in := io.ew_mul_in_data
    u_mul_cvt.io.cvt_in_pvld := io.ew_mul_in_vld
    io.ew_mul_in_rdy := u_mul_cvt.io.cvt_out_prdy
    u_mul_cvt.io.cvt_out_pvld := mul_cvt_out_prdy
    val mul_cvt_out_pd = u_mul_cvt.io.cvt_data_out
    val mul_cvt_out_pvld = u_mul_cvt.io.cvt_out_pvld

    val u_core = Module(new NV_NVDLA_SDP_HLS_Y_int_core)





}}

