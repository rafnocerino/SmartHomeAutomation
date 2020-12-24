
import java.util.logging.*;

import org.eclipse.californium.core.*;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.json.simple.JSONObject;
//import org.json.*;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;



public class RegistrationResource extends CoapResource {
	
	private final static Logger LOGGER = Logger.getLogger(RegistrationResource.class.getName());
	
	public RegistrationResource(String name) {
		super(name);
		//setObservable(true);
	}
	
	public void handlePOST(CoapExchange exc) {
		
		String addr = exc.getSourceAddress().getHostAddress();
		LOGGER.log(Level.INFO, "Received a POST request from: " + addr );
		LOGGER.log(Level.INFO,exc.getRequestText());
		try {	
			JSONObject post_msg = (JSONObject)JSONValue.parseWithException(exc.getRequestText());
			IoTNode n = new IoTNode(addr,post_msg);
			
			if(n.getType().equals("sensor") || n.getType().equals("actuator")) {
				
				exc.respond("Sensor correctly registered! \n");
				
			}else {
				
				exc.respond(ResponseCode.BAD_REQUEST);
				
			}
			if(!Cloud_Application_Main.add_node(n)) {
				LOGGER.log(Level.WARNING,"Error during the storing of the sensor's information! \n");
			}
		}catch(ParseException pe) {
			
			LOGGER.log(Level.WARNING,"Error during the parsing of the request received!");
			exc.respond(ResponseCode.BAD_REQUEST);
			return;
		}
		
		
	}

}
