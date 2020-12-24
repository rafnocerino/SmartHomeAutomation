import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.CoapServer;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
/** 
 * 
 * @author rnocerino
 *
 * This class represent the main class of the Cloud application.
 * Implements the CoapServer in order to implement the registration phase for the nodes (Collecting their information)
 * The main thread represent also a server which can receive multiple request from different client creating for each fo them a thread
 * In this class are also defined all the utility methods useful to implement all the offered functionalities.  
 * 
 */
public class Cloud_Application_Main extends CoapServer {
	
	private final static Logger LOGGER = Logger.getLogger(Cloud_Application_Main.class.getName());
	private final static int ClientCommunicationPort =5000;
	
	private static int tolerance;
	
	//those 2 lists are used to keep trace of all the sensors and actuators of the WSN, 
	//registered to the cloud application	
	private  static ArrayList<IoTNode> sensor_list = new ArrayList<IoTNode>();
	private  static ArrayList<IoTNode> actuator_list = new ArrayList<IoTNode>();
	
	//those 2 maps are used to keep trace for each room, the list of all COAP relation created by the cloudApplication
	//In this way the cloudApp will be able in future to interrupt the observeRequest	
	private static HashMap<String,List<CoapObserveRelation>> relationTempSensors=new HashMap<>();
	private static HashMap<String,List<CoapObserveRelation>> relationLightSensors=new HashMap<>();
	
	//this map is used for each room,in which are installed temperature sensors, to count the number of update received for each step
	//Given a room, when its related integer reaches the n° of sensors installed will allow the cloud application
	// to understand that is possible to compute a new average temperature value used to do some controls
	private static HashMap<String,Integer> countUploadTemp; 
	
	//For each room we save the information received by the sensors that will be sent to the user when requested
	private static HashMap<String,List<JSONObject>>sensedTempInformation;
	
	//For each room the resource handler will use the related double to compute the mean value of the temperature in the room
	private static HashMap<String,Double> avgTemp;

	//Same variables like the temperature ones, now used for the Light sensor
	private static HashMap<String,Integer> countUploadLight; 
	private static HashMap<String,List<JSONObject>>sensedLightInformation;
	private static HashMap<String,Double> avgLight;
	
	//those 3 variable are used to implement the functionality of TemperatureControl with threshold
	private static boolean automaticTempControl; //true if that feature is activated by the user
	private static int threshold; // contains the temperature threshold value, if the avg temp of the room goes beyond that value the air cond is powered on
	private static int requestedTemp; //this value is the temperature requested by the user on which the air cond will be setted if the threshold will be exceeded
	
	//those 3 variables are used to implement the feature of Programmed AirCond
	private static boolean programmedAirCond;//that variable will be equal to true if the user has specified a program for air cond
	private static int programmedTemp;//that var will contain the request temperature for the air cond when it will be powered on
	private static int timeStartAir;//that var will contain the specified hour on which the air cond will be powered on
	
	//those 4 variables are used to implement the feature of progressiveLight
	//when activated the light periodically (depending on the sensor period) will increase its brightness of a value specified in brightnessStep
	//until the desired brightness is reached
	private static boolean progressiveLight;//will be true if the user has activated the progressive Light mode
	private static int desiredMaxBrightness;//will contain the maximum brightness that the light will reach progressively
	private static int brightnessStep;//represents the progressive increasing step for the light brightness
	private static int programmedHour;// will contain the specified hour on which the progressiveLight will be activated
	
	//those object are used to implement synchronization between thread in order to avoid race conditions
	private static final Object lockAutomaticControl = new Object();
	private static final Object lockSendingTempData = new Object();
	private static final Object lockSendingLightData = new Object();
	private static final Object lockProgressiveLight = new Object();
	private static final Object lockAccessNodeList = new Object();
	private static final Object lockProgrammAirCond = new Object();
	
	//That static bloc is used to initialize all the variables needed to implement the functionalities
	static {
		
		countUploadTemp=new HashMap<>();
		countUploadTemp.put("living room",0);
		countUploadTemp.put("bed room",0);
		countUploadTemp.put("kitchen",0);
		
		avgTemp=new HashMap<>();
		avgTemp.put("living room",0.0);
		avgTemp.put("bed room",0.0);
		avgTemp.put("kitchen",0.0);
		
		sensedTempInformation=new HashMap<>();
		sensedTempInformation.put("living room",new ArrayList<JSONObject>());
		sensedTempInformation.put("bed room",new ArrayList<JSONObject>());
		sensedTempInformation.put("kitchen room",new ArrayList<JSONObject>());
		
		relationTempSensors.put("living room",new ArrayList<CoapObserveRelation>());
		relationTempSensors.put("bed room",new ArrayList<CoapObserveRelation>());
		relationTempSensors.put("kitchen",new ArrayList<CoapObserveRelation>());
		
		countUploadLight=new HashMap<>();
		countUploadLight.put("living room",0);
		countUploadLight.put("bed room",0);
		countUploadLight.put("kitchen",0);
		
		avgLight=new HashMap<>();
		avgLight.put("living room",0.0);
		avgLight.put("bed room",0.0);
		avgLight.put("kitchen",0.0);
		
		sensedLightInformation=new HashMap<>();
		sensedLightInformation.put("living room",new ArrayList<JSONObject>());
		sensedLightInformation.put("bed room",new ArrayList<JSONObject>());
		sensedLightInformation.put("kitchen room",new ArrayList<JSONObject>());
		
		relationLightSensors.put("living room",new ArrayList<CoapObserveRelation>());
		relationLightSensors.put("bed room",new ArrayList<CoapObserveRelation>());
		relationLightSensors.put("kitchen",new ArrayList<CoapObserveRelation>());
				
		automaticTempControl=false;
		threshold=0;
		requestedTemp=0;
		
		programmedAirCond=false;
		programmedTemp=0;
		timeStartAir=0;
		
		progressiveLight=false;
		brightnessStep=2;
		desiredMaxBrightness=0;
		programmedHour=0;
		
		tolerance = 3;
		
	}
	/**
	 * The method is used to check if inside the node/actuator list there's already that node 
	 * avoiding multiple registration of same node
	 * @param ip is the address of the node that has to be inserted into the list
	 * @param list is the list inside which the method has to check if there are duplicates
	 * @return true if no duplicates false otherwise
	 */
	private static boolean checkForDuplicates(String ip,ArrayList<IoTNode> list) {
		for(int i=0;i<list.size();i++) {
			if(list.get(i).getIP_addr().equals(ip)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * That method will be called after the registration of a temperature sensor.
	 * Will be used to send it the observeRequest specifying also the handler method that will be
	 * executed every time an update is received by that sensor
	 * 
	 * @param c is the CoapClient(node) to which we have to send the observeRequest
	 * @param room where is installed the sensor that will receive the observeRequest
	 */
	private static void addTempRelation(CoapClient c,String room) {
		
		//add the relation inside the list of ObserveRelation (temp) linked to the specified room
		List<CoapObserveRelation> rel = relationTempSensors.get(room);
		rel.add(c.observe(new CoapHandler() {

			@Override public void onLoad(CoapResponse response) {
				
				
				String content = response.getResponseText();
				String node_address = response.advanced().getSource().getHostAddress();
				String last = String.valueOf(node_address.charAt((node_address.length())-1));
				int idTempSensor = Integer.parseInt(last);
				
				LocalTime now = LocalTime.now();
				int hour = now.getHour();
				int minute = now.getMinute();
				
				JSONObject sensor_msg=null;
				
				try{
					sensor_msg = (JSONObject)JSONValue.parseWithException(content);
				}catch(ParseException pe) {
					
					LOGGER.log(Level.WARNING,"Error during the parsing of the received update!");
					return;
				}
				String room = (String)sensor_msg.get("Room");
				
				//number of temp sensors inside the specified room
				int numberOfSensors=relationTempSensors.get(room).size();
				
				//actually the idTempSensor will not contain the real id 
				//but only a number used to distinguish different temp sensors inside the same room
				idTempSensor=(idTempSensor%numberOfSensors);
				
				double value = Double.parseDouble((String)sensor_msg.get("Value"));
				if(value==0.0) {
					//this statement is used to ignore the first sensed values during the setup phase of the sensor
					return;
				}
				int count = countUploadTemp.get(room);								
				
				double oldValue = avgTemp.get(room);
				double avg;
				oldValue+=value;
				
				count =(count+1)%(numberOfSensors);
				
				countUploadTemp.put(room,count);
				
				if(count==0) {
					//this statement is used to some check every time a new average value is available(when count==0)
					
					avg=oldValue/numberOfSensors;//the new avg temp value
					avgTemp.put(room,0.0);//the value inside the map is refresh to allow the subsequent computation of the new avg
					
					String airON = (String)sensor_msg.get("AirCond");//that string will contain the actual status of the air cond
					boolean progrMode;
					int progrTemp;
					int progrHour;
					synchronized(lockProgrammAirCond) {
						
						progrMode = programmedAirCond;
						progrTemp = programmedTemp;
						progrHour = timeStartAir;
						
					}
					
					//That check is used to control if we have to set the AirCond ON due to the programmed functionality
					if(airON.equals("OFF") && progrMode && hour==progrHour && avg >progrTemp+tolerance) {
						
						airON="ON";
						//that thread will be used to send the post request to actuator in order to activate AirCond
						Thread t = new POSTonlyThread(room,"temp_act",progrTemp,"on",0);
						t.start();
						
					}
					
					int thresh;
					boolean automaticControlON;
					int temp;
					synchronized(lockAutomaticControl){
						
						thresh=threshold;
						automaticControlON=automaticTempControl;
						temp=requestedTemp;
						
					}
					
					//If the TempControl with threshold is active the handler will control if the avg temp exceeds the threshold
					//activating the AirCond
					if(airON.equals("OFF") && automaticControlON && avg>=thresh) {
						
						sensor_msg.put("Alert","Threshold excedeed-->AIR Cond ON");
						Thread t = new POSTonlyThread(room,"temp_act",temp,"on",0);
						t.start();
					}
					
				}else {
					avgTemp.put(room,oldValue);
				}
				
				sensor_msg.put("NodeId",idTempSensor);
				
				//Our System gives to the users all the sensed data within the entire day
				//Every night between 00:00 and 00:10 the system will delete the data of the previous day
				synchronized(lockSendingTempData) {
					
					if(hour==0 && minute >0 && minute<10) {
										
						sensedTempInformation.get(room).clear();
					
					}
					sensedTempInformation.get(room).add(sensor_msg);
				}
				
				
				
			}
			
			@Override public void onError() {
				System.err.println("-Failed--------");
			}
		}));
		
	}
	
	/**
	 * That method will be called after the registration of a light sensor.
	 * Will be used to send it the observeRequest specifying also the handler method that will be
	 * executed every time an update is received by that sensor
	 * 
	 * @param c is the CoapClient(node) to which we have to send the observeRequest
	 * @param room where is installed the sensor that will receive the observeRequest
	 * 
	 */
	private static void addLightRelation(CoapClient c, String room) {
		
		List<CoapObserveRelation> rel = relationLightSensors.get(room);
		rel.add(c.observe(new CoapHandler() {

			@Override public void onLoad(CoapResponse response) {
				
				String content = response.getResponseText();
				String node_address = response.advanced().getSource().getHostAddress();
				String last = String.valueOf(node_address.charAt((node_address.length())-1));
				int idLightSensor = Integer.parseInt(last);
				
				JSONObject sensor_msg=null;
				
				LocalTime now = LocalTime.now();
				int hour = now.getHour();
				int minute = now.getMinute();
				
				
				try{
					sensor_msg = (JSONObject)JSONValue.parseWithException(content);
				}catch(ParseException pe) {
					
					LOGGER.log(Level.WARNING,"Error during the parsing of the received update from light sensor!");
					return;
				}
				String room = (String)sensor_msg.get("Room");
				
				int numberOfSensors=relationLightSensors.get(room).size();
				idLightSensor=(idLightSensor%numberOfSensors);
				
				double value = Double.parseDouble((String)sensor_msg.get("Value"));
				int count = countUploadLight.get(room);								
				
				double oldValue = avgLight.get(room);
				double avg;
				oldValue+=value;
							
				count =(count+1)%(numberOfSensors);
				
				countUploadLight.put(room,count);
		
				if(count==0) {
					//every time a new average value is available the handler will do some controls
					
					avg=oldValue/numberOfSensors;
					avgLight.put(room,0.0);
					
					
					synchronized(lockProgressiveLight) {
						
						//if the handler recognize the progressiveLight mode active and the current hour
						//correspond to the requested hour by the user and also the brightness inside the room 
						//is lower than the requested the light will be powered ON (if it is not) and increased gradually
						
						if(hour==programmedHour) {
						
							if(progressiveLight && avg < desiredMaxBrightness) {
								
								int data =(int) avg + brightnessStep;
								Thread t = new POSTonlyThread(room,"light_act",0,"on",data);
								t.start();	
							}
									
						}
											
					}
															
				}else {
					avgLight.put(room,oldValue);
				}
				
				sensor_msg.put("NodeId",idLightSensor);
				
				
				//setup phase-->delete old data
				synchronized(lockSendingLightData) {
					
					if(hour==0 && minute >0 && minute<10) {
					
						sensedLightInformation.get(room).clear();
					
					}
					sensedLightInformation.get(room).add(sensor_msg);
				}
				
					
			}
			
			@Override public void onError() {
				System.err.println("-Failed--------");
			}
		}));
	}
	
	/**
	 * The method checks if the node is an actuator/sensor and after adds it inside the corresponding list.
	 * 
	 * @param n the node that has to be added inside the actuator/sensor list
	 * @return false if there are duplicates true if the node is inserted correctly
	 */
	public static boolean add_node(IoTNode n) {
		
		if(n.getType().equals("actuator")&& checkForDuplicates(n.getIP_addr(),actuator_list)){
			
			boolean b;
			synchronized(lockAccessNodeList) {
				
				 b = actuator_list.add(n);
			
			}
			
			return b;
		}
		
		if(n.getType().equals("sensor") && checkForDuplicates(n.getIP_addr(),sensor_list)) {
			
			CoapClient c=new CoapClient("coap://["+n.getIP_addr()+"]/"+n.getResource_name());
			
			if(n.getResource_name().equals("temp")) {
				
				addTempRelation(c,n.getRoom());
			
			}
			
			if(n.getResource_name().equals("light")) {
				
				addLightRelation(c,n.getRoom());
				
			}
			
			boolean b;
			synchronized(lockAccessNodeList) {
				
				 b = sensor_list.add(n);
			
			}
			
			return b;
			
		}
		
		return false;
		
	}
	
	/**
	 * Method used to power ON/OFF the AirCond
	 * 
	 * @param room the room where the AirCond has to be powered ON/OFF
	 * @param temperature the temperature value on which the AirCond has to be set
	 * @param mode specifies if the AirCond has to be powered ON or OFF
	 */
	public static void setAirCond(String room,int temperature,String mode ) {
		
		Thread t = new POSTonlyThread(room,"temp_act",temperature,mode,0);
		t.start();
		
	}
	
	/**
	 * Method used to activate/disable the automatic temp control with threshold
	 * 
	 * @param mode specifies if the automatic temperature control has to be activated or disabled
	 * @param th specifies the temperature threshold if mode == "ON"
	 * @param reqTemp specifies the requested temperature when the AirCond will be powered ON 
	 */
	public static void setAutomaticTempControl(boolean mode,int th,int reqTemp) {
		
		synchronized(lockAutomaticControl){
			
			automaticTempControl=mode;
			threshold=th;
			requestedTemp=reqTemp;
			
		}
		
	}
	
	/**
	 * Method use to program the future activation of the AirCond on a specific a hour with a specific temperature
	 * 
	 * @param mode specifies if the programmed mode has to be activated or disabled
	 * @param temp if mode==true specifies the requested temperature when the AirCond will be powered ON
	 * @param hour specifies the hour on which the AirCond should be activated
	 */
	public static void setProgramAirCond(boolean mode,int temp,int hour) {
		
		synchronized(lockProgrammAirCond) {
			
			programmedAirCond = mode;
			programmedTemp = temp;
			timeStartAir = hour;
			
		}
				
	}
	/**
	 * Method used to interrupt the receiving of temperature updates in a specified room
	 * @param room specifies the room in which we want to interrupt the temperature updates
	 */
	public static void removeTempRelations(String room) {
		
		List<CoapObserveRelation> del=relationTempSensors.get(room);
		for(int i=0;i<del.size();i++){
			del.get(i).proactiveCancel();
		}
		
	}
	/**
	 * Method use to activate/disable the progressiveLight mode
	 * 
	 * @param mode specifies if the progressiveLight mode has to be activated or disabled
	 * @param desiredB if mode==true specified the desired brightness that the user wants to reach progressively in the room
	 * @param hour specifies the starting hour of the progressiveLight mode
	 */
	public static void setProgressiveLight(boolean mode,int desiredB, int hour) {
		
		synchronized(lockProgressiveLight) {
			
			progressiveLight=mode;
			desiredMaxBrightness=desiredB;
			programmedHour=hour;
			
		}
		
	}
	
	/**
	 * Method that is used to send a POST request to the actuator in order to power ON/OFF the light
	 * 
	 * @param room specifies the room in which we want to power ON/OFF the light
	 * @param brightness if mode=="ON" specifies the brightness of the light
	 * @param mode specifies if the light has to be powered ON or OFF
	 */
	public static void setLight(String room,int brightness, String mode) {
		
		Thread t = new POSTonlyThread(room,"light_act",0,mode,brightness);
		t.run();
		
	}
	
	/**
	 * Method used to send, for a specific room and a specific resource, data to the user 
	 * 
	 * @param room specifies the room for which we want to send the data to the user
	 * @param resource specifies the resource for which we want to send the data to the user
	 * @return
	 */
	public static List<JSONObject> getSendingData(String room,String resource) {
		
		List<JSONObject> ret=null;
		if(resource.equals("temp")) {
			
			synchronized(lockSendingTempData) {
			
				 ret = new ArrayList<>(sensedTempInformation.get(room));
				 				 
			}
				
		}
		
		if(resource.equals("light")) {
			
			synchronized(lockSendingLightData) {
				
				ret = new ArrayList<>(sensedLightInformation.get(room));
				
			}
			
		}
		
		return ret;
				
	}
	
	public static List<IoTNode> getSensorNodesList(){
		
		List <IoTNode> ret;
		synchronized(lockAccessNodeList) {
			ret = new ArrayList<IoTNode>(sensor_list);
		}
		return ret;
	}
	
	public static List<IoTNode> getActuatorNodesList(){
		
		List <IoTNode> ret;
		synchronized(lockAccessNodeList) {
			ret = new ArrayList<IoTNode>(actuator_list);
		}
		return ret;
		
	}
	
	
	public static void main(String[] args) {
		
		Cloud_Application_Main server = new Cloud_Application_Main();
		server.add(new RegistrationResource("Registration"));
		server.start();
		
		try {
			ServerSocket ss = new ServerSocket(ClientCommunicationPort);
			int i=0;
			while(true){
				
				
				//wait for a new client request
				Socket clientSocket;
				clientSocket=ss.accept();
				
				String ipClient = clientSocket.getInetAddress().getHostAddress();
				LOGGER.log(Level.INFO,"A new client is connected !");
				
				DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
				DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
				ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
				ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
				
				Thread thread = new ClientThread(clientSocket,dis,dos,ois,oos);
				thread.start();
				i++;
												
			}
			
						
		}catch(IOException io) {
			io.printStackTrace();
		}
		
	}
	
	
	

}
