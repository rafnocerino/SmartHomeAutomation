import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;
import org.json.simple.JSONObject;
/**
 * 
 * @author rnocerino
 * 
 * Implements the thread which has to communicate with client receiving its requests and sending to it the related responses
 *
 */
public class ClientThread extends Thread {

	
	private final DataInputStream dis;
	private final DataOutputStream dos;
	private final ObjectInputStream ois;
	private final ObjectOutputStream oos;
	private final Socket sock;

	private volatile boolean running = true;
	private final static Logger LOGGER = Logger.getLogger(ClientThread.class.getName());

	public ClientThread(Socket s, DataInputStream dis, DataOutputStream dos, ObjectInputStream ois,
			ObjectOutputStream oos) {

		
		this.dis = dis;
		this.dos = dos;
		this.sock = s;
		this.oos = oos;
		this.ois = ois;
	}

	private void sendObservedTemperature(String room) {

		List<JSONObject> data = Cloud_Application_Main.getSendingData(room, "temp");

		DataToClient dtc = new DataToClient(data);

		try {
			
			oos.writeObject(dtc);

		}catch(IOException io) {
			io.printStackTrace();
		}
		
	}
	
	private void sendObservedLight(String room) {
		
		List<JSONObject> data = Cloud_Application_Main.getSendingData(room, "light");
		
		DataToClient dtc = new DataToClient(data);

		try {
			
			oos.writeObject(dtc);

		}catch(IOException io) {
			io.printStackTrace();
		}
		
		
	}
	
	
	
	@Override
	public void run() {
		
		 RequestFromClient req;
		 System.out.println("Started waiting commands");
		 while (running) {
			
			try {
				
				
				// now the server waits for a command from the client

				String cmd = dis.readUTF();
				if (cmd.equals("EXIT")) {

					System.out.println("Client disconnected! \n");
					sock.close();
					break;

				}

				if (cmd.equals("ACTION")) {
					
					System.out.println("ACTION received");
					
					req = (RequestFromClient)ois.readObject();
					String room = req.getRoom();
					String action = req.getAction();
					switch (room) {

						case "living room": {
	
							if (action.equals("AutomaticTempControlON")) {
	
								int temperature = req.getTemperature();
								int thresh = req.getThreshTemp();
								LOGGER.log(Level.INFO, "Received request for handling temp of dining room! \n");
								Cloud_Application_Main.setAutomaticTempControl(true, thresh, temperature);
								dos.writeUTF("Automatic temperature control successfully activated");
								
							}
	
							if (action.equals("AutomaticTempControlOFF")) {
	
								// int temperature = Integer.parseInt(options[2]);
								// int thresh = Integer.parseInt(options[3]);
								LOGGER.log(Level.INFO, "Received request for disabling automatic temp control of dining room! \n");
								Cloud_Application_Main.setAutomaticTempControl(false, 0, 0);
								dos.writeUTF("Automatic temperature control successfully disabled");
	
							}
	
							if (action.equals("AirCondON")) {
								int temperature = req.getTemperature();
								LOGGER.log(Level.INFO, "Received req to Activate AirCond \n");
								Cloud_Application_Main.setAirCond(room, temperature, "on");
								dos.writeUTF("Request to power on the AirCond successfully received!");
							}
	
							if (action.equals("AirCondOFF")) {
								LOGGER.log(Level.INFO, "Received request to shutdown aircond! \n");
								Cloud_Application_Main.setAirCond(room, 0, "off");
								dos.writeUTF("Request to power off the AirCond successfully received!");
							}
							
							if (action.equals("ProgramAirCondON")) {
								LOGGER.log(Level.INFO, "Received request to programm the aircond! \n");
								int temp = req.getTemperature();
								int hour = req.getHour();
								Cloud_Application_Main.setProgramAirCond(true, temp, hour);
								dos.writeUTF("Request to program the AirCond successfully received!");
							}
							
							if (action.equals("ProgramAirCondOFF")) {
								LOGGER.log(Level.INFO, "Received request to remove program for aircond! \n");
								Cloud_Application_Main.setProgramAirCond(false, 0, 0);
								dos.writeUTF("Request to remove program for the AirCond successfully received!");
							}

							if (action.equals("ObserveTemperature")) {
								LOGGER.log(Level.INFO, "Received request to observe sensed temperatures in "+room);
								dos.writeUTF("Request to observe the daily temperatures correctly received!");
								sendObservedTemperature(room);
							}
							
							if (action.equals("REJECT")) {
								
								LOGGER.log(Level.INFO, "The user has cancelled the operation-->Wait for a new request!");
							}
						}
						
						case "bed room":{
							
							if(action.equals("SetLightON")) {
								
								LOGGER.log(Level.INFO, "Received request to set ligh ON in "+room);
								int br = req.getBrightness();
								Cloud_Application_Main.setLight(room, br, "on");
								dos.writeUTF("Request to set light ON correctly received");
								
							}
							
							if(action.equals("SetLightONwithBrightness")) {
								
								LOGGER.log(Level.INFO, "Received request to set ligh ON with intensity in "+room);
								int br = req.getBrightness();
								Cloud_Application_Main.setLight(room, br, "on");
								dos.writeUTF("Request to set light ON with intensity correctly received");
								
							}
							
							if(action.equals("SetLightOFF")) {
								
								LOGGER.log(Level.INFO, "Received request to set ligh OFF in "+room);
								int br = req.getBrightness();
								Cloud_Application_Main.setLight(room, br, "off");
								dos.writeUTF("Request to set light OFF correctly received");
								
							}
							
							if(action.equals("SetProgressiveLightON")) {
								
								LOGGER.log(Level.INFO, "Received request to set the progressive light ON in "+room);
								int maxbr = req.getBrightMax();
								int hour = req.getHour();
								Cloud_Application_Main.setProgressiveLight(true,maxbr,hour);
								dos.writeUTF("Request to set progressive light ON correctly received");
							}
							
							if(action.equals("SetProgressiveLightOFF")) {
								
								LOGGER.log(Level.INFO, "Received request to set the progressive light OFF in "+room);
								Cloud_Application_Main.setProgressiveLight(false,0,0);
								dos.writeUTF("Request to set progressive light OFF correctly received");
								
							}
							
							if(action.equals("ObserveBrightness")) {
								
								LOGGER.log(Level.INFO, "Received request to observe sensed brightness values in "+room);
								dos.writeUTF("Request to observe the daily brightness values correctly received!");
								sendObservedLight(room);
								
							}
							
							if (action.equals("REJECT")) {
								
								LOGGER.log(Level.INFO, "The user has cancelled the operation-->Wait for a new request!");
							}
							
							
							
							
						}
					}

				}

		

			} catch (IOException io) {
				 //io.printStackTrace();
				System.err.println("Client no more reachable !");
				return;
				
			}catch(ClassNotFoundException ce) {
				ce.printStackTrace();
			}

		}
	}

	

}
