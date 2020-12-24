#include "contiki.h"
#include "coap-engine.h"
#include "stdio.h"
#include "stdlib.h"
#include <time.h>
#include <string.h>
#include <math.h>


/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "App"
#define LOG_LEVEL LOG_LEVEL_APP
#define ACCURACY 0.5

static void res_post_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void res_event_handler(void);

EVENT_RESOURCE(res_temp,
        "title=\"temp: ? temp=0..\", POST/PUT mode=on|off\";rt=\"Control\"",
		 res_get_handler,
        res_post_handler,
		 NULL,
         NULL,
		 res_event_handler);
		 

static int temp=0; // this variable is used to implement consistency and represent the temperature setted by the air cond.
							 // Its value will be setted by a POST request parallel to the "real actuate" POST (see documenation to have a better understanding) 
							 
static bool on=false; //this value is used by the resource to know if the air cond is powered on/off to generate accordingly the values.
								 //Like the temp variable is sette by a parallel POST sent by the cloud application.
								 
static double sending_temp; //variable used by the handler event to save the next value that will  be sent after a GET request.

static void res_event_handler(void){
	
	double gen_temp;
	
	double sum=0;

	
	if(!on){ // this is the logic taht the sensor will execute in order to generate the temperature when the air cond is off
			    //It generates 5 times random values within a specified range and after will compute the mean value.
				
		int UPPER_TEMP=27;
		int LOWER_TEMP=15;
		for ( int i =0; i<5;i++){
			sum += ( rand() % (UPPER_TEMP - LOWER_TEMP + 1)) + LOWER_TEMP;
		}
		
		gen_temp=sum/5;
		sending_temp=gen_temp;
	}else{
		
		//if the air cond is on the sensor will execute that code. 
		// It will generates random values near to the semperature setted (using a radius of 0.5 degrees)
		
		double UPPER_TEMP=temp+ACCURACY;
		double LOWER_TEMP=temp-ACCURACY;
	
		for ( int i =0; i<5;i++){
			sum += fmod(rand(),(UPPER_TEMP - LOWER_TEMP + 1.0))+LOWER_TEMP;
		}
		
		gen_temp=sum/5;
		sending_temp=gen_temp;
		
	}
	
	coap_notify_observers(&res_temp);
}

static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset){
	
	coap_set_header_content_format(response, TEXT_PLAIN);
	
	time_t timer;
	char buf[26];
	char temp[15];
	char temp2[4];
	struct tm* tm_info;
	int len;
	
	timer=time(NULL);
	tm_info = localtime(&timer);
	
	len=snprintf((char*)temp,15,"{\"Time\":\"");
	
	if(on){
		
		snprintf((char*)temp2,3,"ON");
		
	}else{
		snprintf((char*)temp2,4,"OFF");
	}
	
	strftime(buf,26,"%Y-%m-%d %H:%M:%S",tm_info);
	puts(buf);
	
	//after creating some "pieces" of the json msg that will be sent by the sensor as a payload, They are attached all together using snprintf(..)
	
	len = snprintf( (char*)buffer,preferred_size,"%s%s\",\"Room\":\"living room\",\"Resource\":\"temp\",\"Value\":\"%0.1f\",\"AirCond\":\"%s\"}",temp,buf,sending_temp,temp2);
	
	coap_set_payload(response, buffer, len );
	
}


//this handler is used only to implement data consistency after the actuation
//the cloud application will send in parallel 2 POST requests. A "real" one to the actuator 
// and a "dummy" one to the sensor only to notify it that an actuation was done
// in this way the sensor will be able to generate consistent data

static void res_post_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset){
	
	
	const char *mode = NULL;
	const char* rcv_t=NULL;
	int rcv_temp;
	bool success=0;
	
	
	coap_get_post_variable(request, "mode", &mode);

	//POST request notify that air cond is now ON--> boolean on is setted to true and temp takes the specified temperature
	if(strcmp(mode,"on")==0){
		on=true;
		coap_get_query_variable(request,"temp",&rcv_t);
		rcv_temp=atoi(rcv_t);
		success=1;
	}
	
	//POST request notify that air cond is now OFF--> boolean on is setted to false
	if(strcmp(mode,"off")==0){
		on=false;
		rcv_temp=0;
		success=1;
	}
	
	temp=rcv_temp;
	
	if(!success){
		coap_set_status_code(response, BAD_REQUEST_4_00);
	}
	
}

	