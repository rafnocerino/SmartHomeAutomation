#include "contiki.h"
#include "coap-engine.h"
#include "stdio.h"
#include "stdlib.h"
#include <time.h>
#include <string.h>
#include "os/dev/leds.h"

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "App"
#define LOG_LEVEL LOG_LEVEL_APP

static void res_post_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);

RESOURCE(res_temp_act,
         "title=\"temp_act: ? temp=0..\", POST/PUT mode=on|off\";rt=\"Control\"",
		 NULL,
         res_post_handler,
		 NULL,
         NULL);
		 

//this POST handler represent the actuation caused by a specific command received by the cloud application
//in order to see if all the actions were done correctly the actuator uses a led which is setted to the GREEN color when the air cond is powered ON
//when the air cond is powered OFF the light will be shutted off

static void res_post_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset){
	
	size_t len = 0;
	const char *mode = NULL;
	bool success=0;
	
	
	if((len = coap_get_post_variable(request, "mode", &mode))) {
		printf("%s \n",mode);
	}
	
	if(strcmp(mode,"on")==0){
		 leds_single_on(LEDS_GREEN);
		 success=1;
		 printf("AIR ON \n");
	}
	if(strcmp(mode,"off")==0){
		
		 leds_single_off(LEDS_GREEN);
		 success=1;
		 printf("AIR OFF \n");
	}
	if(!success){
		coap_set_status_code(response, BAD_REQUEST_4_00);
	}
	
	
}



	