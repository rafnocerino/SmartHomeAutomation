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

RESOURCE(res_light_act,
         "title=\"light_act: ? brightness=0..\", POST/PUT mode=on|off\";rt=\"Control\"",
		 NULL,
         res_post_handler,
		 NULL,
         NULL);
		 
//this POST handler represent the actuation caused by a specific command received by the cloud application
//in order to see if all the actions were done correctly the actuator uses 3 leds that depending on the command will be powered ON/OFF
// In particular if the remote control receive by the cloud application a command to set the light ON, if :
//- the brightness percentage requested stays between 70-100 --> GREEN light ON (High Intensity)
//- the brightness percentage requested stays between 40-70 --> YELLOW light ON (Medium Intensity)
//- the brightness percentage requested stays between 1-40 --> RED light ON (Low Intensity)
//If the received command is Light OFF all the leds are powered OFF

static void res_post_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset){
	
		const char* mode=NULL;
		const char* br=NULL;
		bool success=0;
		
		coap_get_post_variable(request, "mode", &mode);
		
		if(strcmp(mode,"on")==0){
			
			success=1;
			coap_get_query_variable(request,"brightness",&br);
			int b=atoi(br);
			
			if(b>=70 && b<=100){
				
				leds_single_off(LEDS_RED);
				leds_single_off(LEDS_GREEN);
				leds_single_off(LEDS_YELLOW);
				
				leds_single_on(LEDS_GREEN);
				printf("high intensity on \n");
				
			}
			
			if(b>=40 && b<70){
				
				leds_single_off(LEDS_RED);
				leds_single_off(LEDS_GREEN);
				leds_single_off(LEDS_YELLOW);
				
				leds_single_on(LEDS_YELLOW);
				printf("medium intensity on \n");
				
			}
			
			if(b>=1 && b<40){
				
				leds_single_off(LEDS_RED);
				leds_single_off(LEDS_GREEN);
				leds_single_off(LEDS_YELLOW);
				
				leds_single_on(LEDS_RED);
				printf("low intensity on \n");
				
			}
		}
		
		if(strcmp(mode,"off")==0){
			
			success=1;
			leds_single_off(LEDS_RED);
			leds_single_off(LEDS_GREEN);
			leds_single_off(LEDS_YELLOW);
			printf("light shutted down \n");
			
			
		}
		
		if(!success){
			coap_set_status_code(response, BAD_REQUEST_4_00);
		}
		
}