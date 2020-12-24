//import org.json.*;
import org.json.simple.JSONObject;

public class IoTNode {
	
	private String IP_addr;
	private String resource_name;
	private String room;
	private String description;
	private String type;
//  private int node_id;
	
	public IoTNode(String addr, JSONObject info) {
		
		IP_addr=addr;
		resource_name = (String)info.get("Resource");
		room = (String)info.get("Room");
		description = (String)info.get("Description");
		type = (String)info.get("Type");
		
		
	}
	
	public String getIP_addr() {
		return IP_addr;
	}
	public void setIP_addr(String iP_addr) {
		IP_addr = iP_addr;
	}
	public String getResource_name() {
		return resource_name;
	}
	public void setResource_name(String resource_name) {
		this.resource_name = resource_name;
	}
	public String getRoom() {
		return room;
	}
	public void setRoom(String room) {
		this.room = room;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	
	
}
