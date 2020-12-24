import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;

public class DataToClient implements Serializable{

	private List<JSONObject> data;
	
	public DataToClient(List<JSONObject> data) {
		this.data=new ArrayList<>(data);
	}

	public List<JSONObject> getData() {
		return data;
	}

	public void setData(List<JSONObject> data) {
		this.data = data;
	}
	
	
	
}
