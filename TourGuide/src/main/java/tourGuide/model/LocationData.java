package tourGuide.model;

public class LocationData {
	public double latitude;
	public double longitude;

	private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;
	private static final double EARTH_RADIUS_IN_NAUTICAL_MILES = 3440.0647948;

	public LocationData(double lat, double lon) {
		latitude = lat;
		longitude = lon;
	}

	public LocationData() {
	}

	/**
	 * Calculates distance in statute miles between locations Uses Spherical Law of
	 * Cosines
	 * 
	 * @param this
	 * @param loc2
	 * @return calculated distance
	 */
	public double getDistance(LocationData that) {
		double lat1 = Math.toRadians(this.latitude);
		double lon1 = Math.toRadians(this.longitude);
		double lat2 = Math.toRadians(that.latitude);
		double lon2 = Math.toRadians(that.longitude);

		double angleDistance = Math
				.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

		double nauticalMilesDistance = EARTH_RADIUS_IN_NAUTICAL_MILES * angleDistance;
		double statuteMilesDistance = STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMilesDistance;
		return statuteMilesDistance;
	}
}