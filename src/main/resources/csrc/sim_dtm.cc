#include <vpi_user.h>
#include <svdpi.h>

extern "C" int debug_tick(
    unsigned char *debug_req_valid,
    unsigned char debug_req_ready,
    int *debug_req_bits_addr,
    int *debug_req_bits_data)
{

  static int count = 0;
  static int addr = 0;
  static int data = 0xdead000;
  if (count < 100) {
    *debug_req_valid     = 1;
    *debug_req_bits_addr = addr ++;
    *debug_req_bits_data = data ++;

    count++;
  }

  return 0;
}
