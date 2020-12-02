module sim_monitor
  (
   input logic        clock,
   input logic        reset,

   input logic        dbg_hart_id,
   input logic        dbg_valid,

   input logic        dbg_inst_fetch_req,
   input logic [31:0] dbg_inst_fetch_addr,
   input logic        dbg_inst_fetch_ack,
   input logic [31:0] dbg_inst_fetch_rddata,
   input logic [2:0]  dbg_pc_update_cause,
   input logic        dbg_inst_valid,
   input logic [31:0] dbg_inst_addr,
   input logic [31:0] dbg_inst_hex,
   input logic        dbg_reg_wren,
   input logic [4:0]  dbg_reg_wraddr,
   input logic [63:0] dbg_reg_wrdata,
   input logic [63:0] dbg_alu_rdata0,
   input logic [4:0]  dbg_alu_reg_rs0,
   input logic [63:0] dbg_alu_rdata1,
   input logic [4:0]  dbg_alu_reg_rs1,
   input logic [4:0]  dbg_alu_func,
   input logic        dbg_mem_inst_valid,
   input logic [4:0]  dbg_mem_inst_rd,
   input logic [63:0] dbg_mem_alu_res,
   input logic [2:0]  dbg_csr_cmd,
   input logic [11:0] dbg_csr_addr,
   input logic [63:0] dbg_csr_wdata,
   input logic        dbg_data_bus_req,
   input logic [1:0]  dbg_data_bus_cmd,
   input logic [31:0] dbg_data_bus_addr,
   input logic [63:0] dbg_data_bus_wrdata,
   input logic        dbg_data_bus_ack,
   input logic [63:0] dbg_data_bus_rddata
);

always_ff @ (posedge clock, posedge reset) begin
  if (reset) begin
  end else begin
    if (dbg_valid) begin
      $write("%t HART%01d : ", $time, dbg_hart_id);
      if (dbg_inst_fetch_req & dbg_inst_fetch_ack) begin
        $write("%08x", dbg_inst_fetch_addr);
      end else begin
        $write("        ");
      end

      if (dbg_reg_wren) begin
        $write("R%02d<=%016x", dbg_reg_wraddr, dbg_reg_wrdata);
      end else begin
        $write("                     ");
      end
      $write("\n");
    end // if (dbg_valid)
  end
end

endmodule // sim_monitor
