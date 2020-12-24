#include "contiki.h"
#include "net/routing/routing.h"
#include "net/netstack.h"
#include <string.h>
#include <stdio.h>
#include "sys/log.h"
#include "sys/etimer.h"
#include "net/ipv6/uip.h"
#include "net/ipv6/uip-ds6.h"
#include "net/ipv6/uip-debug.h"
#include "net/ipv6/simple-udp.h"
#include "net/linkaddr.h"
#include "coap-engine.h"
#include "coap-blocking-api.h"

#define PERIOD (CLOCK_SECOND*4)
#define REGISTRATION_ADDR "coap://[fd00::1]"

char* service_url = "/Registration";

/*------- handler for the registration response from the cloud app ---------------*/
void handle_reg_request(coap_message_t* response){
	
	const uint8_t* chunk;
	
	if(response == NULL){
		puts("Request timed out! \n");
		return;
	}
	
	int len = coap_get_payload(response, &chunk);
	
	printf("|%.*s",len, (char*)chunk);

}

extern coap_resource_t res_temp_act; // observable resource --> handled periodically with a timer

/*----------------------- MAIN THREAD-----------------------*/

PROCESS(act_temp, "actuator_temp");
AUTOSTART_PROCESSES(&act_temp);

PROCESS_THREAD(act_temp,ev,data){
	
	static coap_endpoint_t server_ep;
	static coap_message_t request[1];
	
	PROCESS_BEGIN();
	
/*---------------------SENSOR REGISTRATION -----------------------*/
	coap_endpoint_parse(REGISTRATION_ADDR, strlen(REGISTRATION_ADDR),&server_ep);
	
	coap_init_message(request, COAP_TYPE_CON,COAP_POST,0);
	coap_set_header_uri_path(request,service_url);
	
	const char msg[ ]="{\"Resource\":\"temp_act\",\"Room\":\"living room\",\"Description\":\"Remote control AirCond\",\"Type\":\"actuator\"}";
	coap_set_payload(request, (uint8_t*)msg,sizeof(msg) - 1);
	COAP_BLOCKING_REQUEST(&server_ep, request,handle_reg_request);
	
/*-------------------END SENSOR REGISTRATION------------------*/	

	coap_activate_resource(&res_temp_act, "temp_act");

	while(1){

		PROCESS_WAIT_EVENT();

	}

	PROCESS_END();
	
}



