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
#define ACCURACY 3

static void res_post_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void res_event_handler(void);

EVENT_RESOURCE(res_light,
        "title=\"light: ? brightness=0..\", POST/PUT mode=on|off\";rt=\"Control\"",
		 res_get_handler,
        res_post_handler,
		 NULL,
         NULL,
		 res_event_handler);


	static bool on=false; 	   // variable used to implement consistency. Is used by the sensor to know if the lightin the room is ON/OFF
	static int brightness=0;  // variable used to implement consistency. Is used by the sensor to know, if the ligh is ON, what is its brightness percentage
	static int bright_sent;	   // variable in which the trigger handler will save the next "sensed" value that will be sent by the GET handler
	
	
	static void res_event_handler(void){
		
		//If the light is ON the sensor will generate a brightness percentage around the setted value (never getting over 100% or under 0%)
	    // To get more accuracy it will generate 5 values and after it will compute the mean value
		if( on && brightness > 0){
			
			int UPPER_BRIGHT;
			int LOWER_BRIGHT;
			if(brightness > (100 - ACCURACY) && brightness <=100){
				
				UPPER_BRIGHT=100;
				
			}else{
			
				UPPER_BRIGHT=brightness + ACCURACY;
			
			}
			
			if(brightness >= 0 && brightness < ACCURACY){
				
				LOWER_BRIGHT=0;
				
			}else{
			
				LOWER_BRIGHT=brightness - ACCURACY;
			
			}
			
			bright_sent=( rand() % (UPPER_BRIGHT - LOWER_BRIGHT + 1)) + LOWER_BRIGHT;
			
		}else{
		
			// if the light is powered OFF the sensor has to sense the brightness percentage of the natural light
			// to do this the sensor will generate some random values within a range of brightness depending on the specified hour of the day
			
			time_t timer = time(NULL);
			struct tm* tm_struct = localtime(&timer);
			int hour=tm_struct->tm_hour;
			int sum=0;
			
			if(hour>=9 && hour <=15){
				
				int UPPER_BRIGHT=95;
				int LOWER_BRIGHT=70;
				for ( int i =0; i<5;i++){
					sum += ( rand() % (UPPER_BRIGHT - LOWER_BRIGHT + 1)) + LOWER_BRIGHT;
				}
		
				bright_sent=sum/5;
			}
			
			if(hour>=16 && hour<=18){
				
				int UPPER_BRIGHT=69;
				int LOWER_BRIGHT=30;
				for ( int i =0; i<5;i++){
					sum += ( rand() % (UPPER_BRIGHT - LOWER_BRIGHT + 1)) + LOWER_BRIGHT;
				}
		
				bright_sent=sum/5;
			}
			
			if(hour>=19 && hour <=20){
				
				int UPPER_BRIGHT=29;
				int LOWER_BRIGHT=10;
				for ( int i =0; i<5;i++){
					sum += ( rand() % (UPPER_BRIGHT - LOWER_BRIGHT + 1)) + LOWER_BRIGHT;
				}
		
				bright_sent=sum/5;
			}
			
			if(hour>20 ){
				
				int UPPER_BRIGHT=9;
				int LOWER_BRIGHT=0;
				for ( int i =0; i<5;i++){
					sum += ( rand() % (UPPER_BRIGHT - LOWER_BRIGHT + 1)) + LOWER_BRIGHT;
				}
		
				bright_sent=sum/5;
			}
			
		
		}
		
		coap_notify_observers(&res_light);
		
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
	
	len = snprintf( (char*)buffer,preferred_size,"%s%s\",\"Room\":\"bed room\",\"Resource\":\"light\",\"Value\":\"%d\",\"Light\":\"%s\"}",temp,buf,bright_sent,temp2);
	
	coap_set_payload(response, buffer, len );

	}
	
	//this handler is used only to implement data consistency after the actuation
	//the cloud application will send in parallel 2 POST requests. A "real" one to the actuator 
	// and a "dummy" one to the sensor only to notify it that an actuation was done
	// in this way the sensor will be able to generate consistent data
	
	static void res_post_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset){
		
		
		const char* mode=NULL;
		const char* br=NULL;
		bool success=0;
		
		coap_get_post_variable(request, "mode", &mode);
		
		//POST request notify that light is now ON--> boolean on is setted to true and brightness takes the specified percentage
		if(strcmp(mode,"on")==0){ 
			
			success=1;
			coap_get_query_variable(request,"brightness",&br);
			int b=atoi(br);
			brightness=b;
			on=true;
			printf("LIGHT ON and BRIGHT= %d \n",brightness);
		}
		
		//POST request notify that light is now OFF--> boolean on is setted to false 
		if(strcmp(mode,"off")==0){
			success=1;
			brightness=0;
			on=false;
			printf("LIGHT OFF \n");
		}
		
		if(!success){
			coap_set_status_code(response, BAD_REQUEST_4_00);
		}
		
	}
			
		
			
	
	
		
		
		
		
		
		
		
		
		