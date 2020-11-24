// See LICENSE.SiFive for license details.
//VCS coverage exclude_file

import "DPI-C" function int debug_tick
(
  output bit     debug_req_valid,
  input  bit     debug_req_ready,
  output int     debug_req_bits_addr,
  output int     debug_req_bits_data
);

module sim_dtm(
  input               clock,
  input               reset,

  output logic        req_valid,
  output logic [31:0] req_bits_addr,
  output logic [31:0] req_bits_data,
  input logic         req_ready
);

logic           __debug_req_valid;
logic [31: 0]   __debug_req_bits_addr;
logic [31: 0]   __debug_req_bits_data;

logic           req_valid_reg;
logic [31:0]    req_bits_addr_reg;
logic [31:0]    req_bits_data_reg;

always_ff @(posedge clock) begin
  req_valid_reg     <= __debug_req_valid;
  req_bits_addr_reg <= __debug_req_bits_addr;
  req_bits_data_reg <= __debug_req_bits_data;
end

assign req_valid     = req_valid_reg;
assign req_bits_addr = req_bits_addr_reg;
assign req_bits_data = req_bits_data_reg;

int debug_tick_val;

always_ff @(negedge clock) begin
  if (reset) begin
  end else begin
    debug_tick_val = debug_tick(
      __debug_req_valid,
      req_ready,
      __debug_req_bits_addr,
      __debug_req_bits_data
      );
  end
end

endmodule // sim_dtm
