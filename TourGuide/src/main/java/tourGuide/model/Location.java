package tourGuide.model;

public class Location {
	public final double longitude;
	public final double latitude;

	/**
	 * Constructor
	 * 
	 * @param latitude
	 * @param longitude
	 */
	public Location(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}
}