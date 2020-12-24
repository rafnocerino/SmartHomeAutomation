CONTIKI_PROJECT = actuator_temp
all: $(CONTIKI_PROJECT)

CFLAGS += -DPROJECT_CONF_H=\"project-conf.h\"

CONTIKI = ../../../

# Do not try to build on Sky because of code size limitation
PLATFORMS_EXCLUDE = sky z1

# Include CoAP resources
MODULES_REL += $(CONTIKI)/examples/project/TempActuator/resources


# Include the CoAP implementation
include $(CONTIKI)/Makefile.dir-variables
MODULES += $(CONTIKI_NG_APP_LAYER_DIR)/coap

include $(CONTIKI)/Makefile.include

