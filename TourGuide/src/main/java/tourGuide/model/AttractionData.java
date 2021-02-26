package tourGuide.model;

import java.util.UUID;

public class AttractionData  extends LocationData{
	public String name;
	public String city;
	public String state;
	public UUID id;
	
	public AttractionData(double lat, double lon) {
		super(lat, lon);
	}

	public AttractionData(String name, String city, String state, double latitude, double longitude) {
		this(latitude,longitude);
		this.name=name;
		this.city=city;
		this.state=state;
		id = new UUID(0, 0);
	}

	public AttractionData() {
	}
}