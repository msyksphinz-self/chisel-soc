SBT ?= java -Xmx$(JVM_MEMORY) -Xss8M -XX:MaxPermSize=256M -jar ./sbt-launch.jar

PROJ_ROOT = $(shell git rev-parse --show-toplevel)

generated_dir = $(abspath ./generated-src)
generated_dir_debug = $(abspath ./generated-src-debug)

PROJECT ?= freechips.rocketchip.system
CONFIG  ?= $(PROJECT).DefaultConfig

JAVA_HEAP_SIZE ?= 8G
JAVA_ARGS ?= -Xmx$(JAVA_HEAP_SIZE) -Xss8M -XX:MaxPermSize=256M

.PHONY = fir_build

export JAVA_ARGS

include Makefrag-verilator

tilelink: TestHarness.sv
	mkdir -p $(generated_dir_debug)/$(long_name)
	$(VERILATOR) $(VERILATOR_FLAGS) -Mdir $(generated_dir_debug)/$(long_name) \
	-o $(abspath $(sim_dir))/$@ $(verilog) $(cppfiles) -LDFLAGS "$(LDFLAGS)" \
	-CFLAGS "-I$(generated_dir_debug) -include $(generated_dir_debug)"
	$(MAKE) VM_PARALLEL_BUILDS=1 -C $(generated_dir_debug)/$(long_name) -f V$(MODEL).mk
	./$@

tilelink-xml: TestHarness.sv
	mkdir -p $(generated_dir_debug)/$(long_name)
	$(VERILATOR) $(VERILATOR_FLAGS) --xml-only -Mdir $(generated_dir_debug)/$(long_name) \
	-o $(abspath $(sim_dir))/$@ $(verilog) $(cppfiles) -LDFLAGS "$(LDFLAGS)" \
	-CFLAGS "-I$(generated_dir_debug) -include $(generated_dir_debug)"

TestHarness.sv: fir_build
	./firrtl/utils/bin/firrtl -td $(generated_dir_debug) -i $(generated_dir_debug)/$(PROJECT).$(CONFIG).fir -X sverilog

fir_build:
	$(SBT) 'runMain $(PROJECT).Generator $(generated_dir_debug) core_complex TestHarness $(PROJECT) $(CONFIG)'


clean:
	$(RM) -rf test_run_dir target *.v *.fir *.json *.log generated-src-debug *.sv
	$(RM) -rf *.d
	$(RM) -rf *.graphml
	$(RM) -rf *.plusArgs
