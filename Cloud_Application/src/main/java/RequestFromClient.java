import java.io.Serializable;
/**
 * 
 * @author rnocerino
 * Class which implement the Request that the user will sent through the network to the CloudApplication
 * speicifying the action that he wants to perform and some other parameters like room,resource(temp,light)
 */
public class RequestFromClient implements Serializable{
	
	private String room;
	private String action;
	private int temperature;
	private int threshTemp;
	private int brightness;
	private int brightMax;
	private int brightStep;
	private int hour;
	
	public int getHour() {
		return hour;
	}

	public void setHour(int hour) {
		this.hour = hour;
	}
	/**
	 * 
	 * @param room specifies the room where the action will be performed
	 * @param action specifies the action that the user wants to perform
	 * @param temperature if the actions are related to a temperature action this field will be used to send the request temperature
	 * @param threshTemp if the action=="AutomaticTempControlON" this field will contain the temperature threshold value
	 * @param brightness if the actions are related to the light brightness this field will contain the requested light brightness
	 * @param brightMax if the action=="progressiveLightON" this field will contain the max desired brightness of the light
	 * @param brightStep if the action=="progressiveLightON" this field will contain the progressive increase of brightness
	 * @param hour if action=="programAirCond" or action=="progressiveLightON" this field will contain the specified hour on which the user wants to activate those 2 modes
	 */
	public RequestFromClient( String room, String action, int temperature, int threshTemp,
			int brightness, int brightMax, int brightStep,int hour) {
		
		super();
		this.room = room;
		this.action = action;
		this.temperature = temperature;
		this.threshTemp = threshTemp;
		this.brightness = brightness;
		this.brightMax = brightMax;
		this.brightStep = brightStep;
		this.hour = hour;
		
	}

	public String getRoom() {
		return room;
	}

	public void setRoom(String room) {
		this.room = room;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public int getTemperature() {
		return temperature;
	}

	public void setTemperature(int temperature) {
		this.temperature = temperature;
	}

	public int getThreshTemp() {
		return threshTemp;
	}

	public void setThreshTemp(int threshTemp) {
		this.threshTemp = threshTemp;
	}

	public int getBrightness() {
		return brightness;
	}

	public void setBrightness(int brightness) {
		this.brightness = brightness;
	}

	public int getBrightMax() {
		return brightMax;
	}

	public void setBrightMax(int brightMax) {
		this.brightMax = brightMax;
	}

	public int getBrightStep() {
		return brightStep;
	}

	public void setBrightStep(int brightStep) {
		this.brightStep = brightStep;
	}
	
	
	

}

