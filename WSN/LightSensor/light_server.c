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

#define PERIOD (CLOCK_SECOND*30)
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
/*------- end handler for registration process---------------------*/


extern coap_resource_t res_light;

/*----------------------- MAIN THREAD-----------------------*/

PROCESS(server_light, "server light");
AUTOSTART_PROCESSES(&server_light);

PROCESS_THREAD(server_light,ev,data){

	static struct etimer tim;
	static coap_endpoint_t server_ep;
	static coap_message_t request[1];
	
	PROCESS_BEGIN();
	
	coap_activate_resource(&res_light, "light");
	
/*---------------------SENSOR REGISTRATION -----------------------*/
	coap_endpoint_parse(REGISTRATION_ADDR, strlen(REGISTRATION_ADDR),&server_ep);
	
	coap_init_message(request, COAP_TYPE_CON,COAP_POST,0);
	coap_set_header_uri_path(request,service_url);
	
	const char msg[ ]="{\"Resource\":\"light\",\"Room\":\"bed room\",\"Description\":\"Sensor for brightness % of the room\",\"Type\":\"sensor\"}";
	coap_set_payload(request, (uint8_t*)msg,sizeof(msg) - 1);
	COAP_BLOCKING_REQUEST(&server_ep, request,handle_reg_request);
	
/*-------------------END SENSOR REGISTRATION------------------*/	
	etimer_set(&tim,PERIOD);

	

	while(1){

		PROCESS_WAIT_EVENT_UNTIL(etimer_expired(&tim));

		res_light.trigger();
		etimer_reset(&tim);

	}

	PROCESS_END();
	
}









