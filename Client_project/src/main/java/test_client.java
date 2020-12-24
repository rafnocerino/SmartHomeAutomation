

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.Request;
import org.json.simple.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;

import org.eclipse.californium.core.*;
import java.util.Scanner;

public class test_client {
	
	private static Socket sock;
	private static DataInputStream dis;
	private static DataOutputStream dos;
	private static ObjectInputStream ois;
	private static ObjectOutputStream oos;
	private static HashMap<String,Boolean> rooms;
	private static HashMap<String,List<String>> possibleRoomActions;
	
	static {

			 rooms = new HashMap<String,Boolean>();
			 rooms.put("living room",true);
			 rooms.put("bed room",true);
			 possibleRoomActions = new HashMap<String,List<String>>();
			 List<String> actLivingroom = new ArrayList<String>();
			 actLivingroom.add("AutomaticTempControlON");
			 actLivingroom.add("AutomaticTempControlOFF");
			 actLivingroom.add("AirCondON");
			 actLivingroom.add("AirCondOFF");
			 actLivingroom.add("ObserveTemperature");
			 actLivingroom.add("ProgramAirCondON");
			 actLivingroom.add("ProgramAirCondOFF");
			 actLivingroom.add("REJECT");
			 possibleRoomActions.put("living room", actLivingroom);
			 
			 List<String> actBedroom = new ArrayList<String>();
			 actBedroom.add("SetLightON");
			 actBedroom.add("SetLightONwithBrightness");
			 actBedroom.add("SetLightOFF");
			 actBedroom.add("SetProgressiveLightON");
			 actBedroom.add("SetProgressiveLightOFF");
			 actBedroom.add("ObserveBrightness");
			 actBedroom.add("REJECT");
			 possibleRoomActions.put("bed room", actBedroom);			 
						
		}
		
	
	
	
	public static void main(String[] args) {
			
		try{ 
			sock = new Socket("127.0.0.1",5000);
			System.out.println("Connected"); 
			 dis = new DataInputStream(sock.getInputStream());		
			 dos = new DataOutputStream(sock.getOutputStream());			
			 oos = new ObjectOutputStream(sock.getOutputStream());
			 ois = new ObjectInputStream(sock.getInputStream());
			
			dos.writeUTF("HELLO");
			DataToClient dtc;
			
            while(true) {
            	           	
            	System.out.println("Please insert the name of the room or type EXIT to leave");
            	Scanner scan = new Scanner(System.in);
            	String room=scan.nextLine();
            	if(room.equals("EXIT")) {
            		
            		dos.writeUTF("EXIT");
            		break;
            	}else {
            		dos.writeUTF("ACTION");
            	}
            	
            	while(!rooms.containsKey(room)) {
            		System.out.println("Wrong room name inserted!");
            		System.out.println("HINT-> you can control:");
            		for (Entry<String, Boolean> entry : rooms.entrySet()) {
            			System.out.println("**"+entry.getKey());
            		}
            		System.out.println("Please reinsert the name of the room:");
                	room=scan.nextLine();
            		
            	}
            	
            	System.out.println("Which action you want to do in "+room+"? Insert HELP to see possible actions or REJECT to retur");
            	String action=scan.nextLine();
            	List<String>pos=possibleRoomActions.get(room);           	
            	
            	while(!pos.contains(action) || action.equals("HELP")) {
            		
            		if(!pos.contains(action)) {
            			System.out.println("Action inserted not allowed or wrong typed!");
            		}
            		
            		System.out.println("HINT-->you can do the specified actions in "+room+":");
            		for(int i=0;i<pos.size();i++) {
            			System.out.println("**"+pos.get(i));
            		}
            		System.out.println("Please reinsert the action:");
            		action=scan.nextLine();
            	}
            	
            	switch(action) {
            	
	            	case "AutomaticTempControlON" :{
	            		System.out.println("Please now insert the temperature threshold and after the temperature for the Air Cond :");	            		
	            		int thresh = scan.nextInt();
	            		int temp = scan.nextInt();
	            		RequestFromClient req = new RequestFromClient(room,action,temp,thresh,0,0,0,0);
	            		oos.writeObject(req);
	            		String resp = dis.readUTF();
	            		System.out.println(resp);
	            		break;
	            		
	            	}
	            	
	            	case "AutomaticTempControlOFF" :{
	            		
	            		RequestFromClient req = new RequestFromClient(room,action,0,0,0,0,0,0);
	            		oos.writeObject(req);
	            		String resp = dis.readUTF();
	            		System.out.println(resp);
	            		break;
	            		
	            	}
	            	case "AirCondON" :{
	            		
	            		System.out.println("Please insert the desired temperature for the Air Cond:");
	            		int temp = scan.nextInt();
	            		RequestFromClient req = new RequestFromClient(room,action,temp,0,0,0,0,0);
	            		oos.writeObject(req);
	            		String resp = dis.readUTF();
	            		System.out.println(resp);
	            		break;
	            		
	            	}
	            	
	            	case "AirCondOFF" :{
	            		
	            		RequestFromClient req = new RequestFromClient(room,action,0,0,0,0,0,0);
	            		oos.writeObject(req);
	            		String resp = dis.readUTF();
	            		System.out.println(resp);
	            		break;
	            		
	            	}
	            	
	            	case "ProgramAirCondON" : {
	            		
	            		System.out.println("Please insert the hour on which you want to power ON the air cond:");
	            		int hour = scan.nextInt();
	            		System.out.println("Please insert now the desired temperature:");
	            		int temp = scan.nextInt();
	            		
	            		RequestFromClient req = new RequestFromClient(room,action,temp,0,0,0,0,hour);
	            		oos.writeObject(req);
	            		String resp = dis.readUTF();
	            		System.out.println(resp);
	            		break;
	            	}
	            	
	            	case "ProgramAirCondOFF" : {
	            		
	            		RequestFromClient req = new RequestFromClient(room,action,0,0,0,0,0,0);
	            		oos.writeObject(req);
	            		String resp = dis.readUTF();
	            		System.out.println(resp);
	            		break;
	            		
	            	}
	            	
	            	case "ObserveTemperature" :{
	            		
	            		RequestFromClient req = new RequestFromClient(room,action,0,0,0,0,0,0);
	            		oos.writeObject(req);
	            		String resp = dis.readUTF();
	            		System.out.println(resp);
	            		dtc = (DataToClient)ois.readObject();
	            		List<JSONObject> list = dtc.getData();
	            		
	            		System.out.println("|--------TIME---------|-----ROOM----|---RESOURCE---|---ID---|--VALUE--|---AIR COND---|----------ALERT--------|");
	            		
	            		for(int i=0;i<list.size();i++) {
	            			String time = (String)list.get(i).get("Time");
	            			String r = (String)list.get(i).get("Room");
	            			String res = (String)list.get(i).get("Resource");
	            			int id = (Integer)list.get(i).get("NodeId");
	            			double value = Double.parseDouble((String)list.get(i).get("Value"));
	            			String airc = (String)list.get(i).get("AirCond");
	            			String alert=(String)list.get(i).get("Alert");
	            			
	            			String out = "| "+time+" | "+r+" | "+res+" | "+id+" | "+value+"°C | "+airc+" | ";
	            			
	            			if(alert!=null){
	            				out=out+alert+" | ";
	            			}
	            			System.out.println(out);
	            		}
	            		break;
	            		
	            	}
	            
	            	case "SetLightON" :{
	            		
	            		RequestFromClient r = new RequestFromClient(room,action,0,0,100,0,0,0);
	            		oos.writeObject(r);
	            		String resp = dis.readUTF();
	            		System.out.println(resp);            		
	            		break;
	            	}
	            	
	            	case "SetLightONwithBrightness" : {
	            		
	            		System.out.println("Please insert the desired light intensity (0-100)");
	            		int bright = scan.nextInt();
	            		RequestFromClient r = new RequestFromClient(room,action,0,0,bright,0,0,0);
	            		oos.writeObject(r);
	            		String resp = dis.readUTF();
	            		System.out.println(resp);       
	            		
	            		break;
	            	}
	            	
	            	case "SetLightOFF" :{
	            		
	            		RequestFromClient r = new RequestFromClient(room,action,0,0,0,0,0,0);
	            		oos.writeObject(r);
	            		String resp = dis.readUTF();
	            		System.out.println(resp);  
	            		break;
	            	}
	            	
	            	case "SetProgressiveLightON" :{
	            		
	            		System.out.println("Please insert the desired max progressive light intensity (0-100)");
	            		int des = scan.nextInt();
	            		System.out.println("Please insert the hour on whic you want to program the progressive light");
	            		int h = scan.nextInt();
	            		RequestFromClient r = new RequestFromClient(room,action,0,0,0,des,0,h);
	            		oos.writeObject(r);
	            		String resp = dis.readUTF();
	            		System.out.println(resp);  
	            		break;
	            	}
	            	
	            	case "SetProgressiveLightOFF" :{
	            		
	            		RequestFromClient r = new RequestFromClient(room,action,0,0,0,0,0,0);
	            		oos.writeObject(r);
	            		String resp = dis.readUTF();
	            		System.out.println(resp);  
	            		break;
	            	}
	            	
	            	case "ObserveBrightness" :{
	            		
	            		RequestFromClient req = new RequestFromClient(room,action,0,0,0,0,0,0);
	            		oos.writeObject(req);
	            		String resp = dis.readUTF();
	            		System.out.println(resp);
	            		dtc = (DataToClient)ois.readObject();
	            		List<JSONObject> list = dtc.getData();
	            		
	            		System.out.println("|--------TIME---------|-----ROOM----|---RESOURCE---|---ID---|--VALUE--|--LIGHT STATUS--|");
	            		
	            		for(int i=0;i<list.size();i++) {
	            			String time = (String)list.get(i).get("Time");
	            			String r = (String)list.get(i).get("Room");
	            			String res = (String)list.get(i).get("Resource");
	            			int id = (Integer)list.get(i).get("NodeId");
	            			double value = Double.parseDouble((String)list.get(i).get("Value"));
	            			String stat = (String)list.get(i).get("Light");
	            			
	            			String out = "| "+time+" | "+r+" | "+res+" | "+id+" | "+value+"% | "+stat+" |";
	            			System.out.println(out);
	            		}
	            		break;
	            	}
	            	
	            	case "REJECT" :{
	            		
	            		RequestFromClient req = new RequestFromClient(room,action,0,0,0,0,0,0);
	            		oos.writeObject(req);
	            		
	            		break;
	            	}
	            }
            	
            	          	
            }
                       
        }catch(IOException i)  { 
            System.out.println("aa"); 
        }catch(ClassNotFoundException ce) {
        	ce.printStackTrace();
        }

	}

}
