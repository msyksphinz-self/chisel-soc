.PHONY = regression gen_test_class invididual_tests cpu_verilog gen_test_class

cpu_run:
	sbt 'testOnly cpu.Tester -- -z Basic'

sha256_verilog_test:
	sbt 'testOnly sha_256.VerilogTester'

sha256_scala_test:
	sbt 'testOnly sha_256.ScalaTester'

regression: gen_test_class
	sbt 'testOnly cpu.Tester_AllPattern'

invididual_tests: gen_test_class
	$(MAKE) run_all

cpu_verilog:
	sbt 'runMain cpu.CpuTop'

mod_verilog:
	sbt 'runMain mod_test.ParamModTop'

gen_test_class:
	$(MAKE) -C tests/riscv-tests/isa
	ruby ./gen_test_class.rb

PROJECT ?= freechips.rocketchip.unittest
CONFIG  ?= TLOriginalUnitTestConfig
CONFIG_FIR ?= $(PROJECT).$(CONFIG).fir

include Makefrag-verilator

tilelink: TestHarness.sv verilator_bin
	mkdir -p $(generated_dir_debug)/$(long_name)
	$(VERILATOR) $(VERILATOR_FLAGS) -Mdir $(generated_dir_debug)/$(long_name) \
	-o $(abspath $(sim_dir))/$@ $(verilog) $(cppfiles) -LDFLAGS "$(LDFLAGS)" \
	-CFLAGS "-I$(generated_dir_debug) -include $(model_header_debug)"
	$(MAKE) VM_PARALLEL_BUILDS=1 -C $(generated_dir_debug)/$(long_name) -f V$(MODEL).mk
	./$@

tilelink-xml: TestHarness.sv verilator_bin
	mkdir -p $(generated_dir_debug)/$(long_name)
	$(VERILATOR) $(VERILATOR_FLAGS) --xml-only -Mdir $(generated_dir_debug)/$(long_name) \
	-o $(abspath $(sim_dir))/$@ $(verilog) $(cppfiles) -LDFLAGS "$(LDFLAGS)" \
	-CFLAGS "-I$(generated_dir_debug) -include $(model_header_debug)"

TestHarness.sv: CONFIG_FIR
	./firrtl/utils/bin/firrtl -td . -i $(PROJECT).$(CONFIG).fir -X sverilog

CONFIG_FIR:
	sbt -mem 2048 'runMain $(PROJECT).Generator . $(PROJECT) TestHarness $(PROJECT) $(CONFIG)'

# include rv64ui_test.mk

clean:
	$(RM) -rf test_run_dir target *.v *.fir *.json *.log generated-src-debug *.sv
	$(RM) -rf *.d
	$(RM) -rf *.graphml
	$(RM) -rf *.plusArgs
