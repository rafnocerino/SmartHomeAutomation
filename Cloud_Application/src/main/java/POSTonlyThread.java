import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.CoAP.Code;
/**
 * 
 * @author rnocerino
 * Class that will implements a thread that will be used only to sent POST request to the actuators. Actually the thread sends also
 * POST request to the sensor only to implement consistency
 *
 */
public class POSTonlyThread extends Thread{
	
	private String room;
	private String resource;
	private int temperature;
	private String mode;
	private int brightness;

	
	public POSTonlyThread(String room, String res, int temp,String mode,int bright) {
		
		this.room=room;
		this.resource=res;
		this.temperature=temp;
		this.mode=mode;
		this.brightness=bright;
		
	}
	/**
	 * This method represents the most important operation of the POSTonlyThread. This method is used to find all the actuator to
	 * which the thread has to send the POST request. The thread in order to implement consistency will also send a POST request also to sensors
	 * in order to notify them that an actuation was done.
	 * 
	 * @param act_res specifies the name of the resource on the actuators that have to be contacted
	 * @param sens_resspecifies the name of the resource on the sensors that have to be contacted (only for consistency)
	 */
	private void sendPOST(String act_res, String sens_res) {
		
		List<IoTNode> act = Cloud_Application_Main.getActuatorNodesList();
		List<IoTNode> sens = Cloud_Application_Main.getSensorNodesList();
		
		
		ArrayList<Integer> index = new ArrayList<Integer>();
		List<CoapClient>dst_list=new ArrayList<>();
		for(int i=0;i<act.size();i++) {
			if(act.get(i).getRoom().equals(room) && act.get(i).getResource_name().contentEquals(act_res)) {
				index.add(i);
			}
		}

		Collections.sort(index);
		//we generally expect only one actuator, but it's not a general rule => for this reason we use the for cycle
		for(int i=0;i<index.size();i++) {
			dst_list.add(new CoapClient("coap://["+act.get(index.get(i)).getIP_addr()+"]/"+act.get(index.get(i)).getResource_name()));
		}

		
		for(int i=0;i<dst_list.size();i++) {
			
			Request req = new Request(Code.POST);
			if(sens_res.equals("temp")) {
				req.getOptions().addUriQuery("temp="+temperature);
			}else {
				req.getOptions().addUriQuery("brightness="+brightness);
			}
			
			req.setPayload("mode="+mode);
			CoapResponse resp = dst_list.get(i).advanced(req);

		}
		//using this code we will send POST request also to all the temperature sensor installed inside
		//the specified room in order to implement data consistency. In this way the sensors will be able
		//to generate in a coherent way the temperatures

		
		List<CoapClient> coherence_sens = new ArrayList<>();
		index.clear();
		for(int i=0;i<sens.size();i++) {
			
			if(sens.get(i).getRoom().equals(room) && sens.get(i).getResource_name().equals(sens_res)) {
				index.add(i);
			}
			
		}
		Collections.sort(index);
		
		for(int i=0;i<index.size();i++) {
			coherence_sens.add(new CoapClient("coap://["+sens.get(index.get(i)).getIP_addr()+"]/"+sens.get(index.get(i)).getResource_name()));
		}	
		
		for(int i=0;i<coherence_sens.size();i++) {

			Request req1 = new Request(Code.POST);
			
			if(sens_res.equals("temp")) {
				req1.getOptions().addUriQuery("temp="+temperature);
			}else {
				req1.getOptions().addUriQuery("brightness="+brightness);
			}
			
			req1.setPayload("mode="+mode);
			CoapResponse resp = coherence_sens.get(i).advanced(req1);
		}
		
	}
	
	
	@Override
	public void run() {
		
			
		if(resource.equals("temp_act")) {
			
			sendPOST("temp_act","temp");
		
		}
		
		if(resource.equals("light_act")) {
			
			sendPOST("light_act","light");
			
		}
		
		
		return;
	}	
	
	
}


