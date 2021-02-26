package tourGuide.model;

public class AttractionDistance extends AttractionData implements Comparable<AttractionDistance> {
	private LocationData fromLocation;

	public AttractionDistance(LocationData fromLocation, AttractionData toAttraction) {
		super(toAttraction.name, toAttraction.city, toAttraction.state, toAttraction.latitude, toAttraction.longitude);
		this.fromLocation = fromLocation;
	}

	@Override
	public int compareTo(AttractionDistance that) {
		// Check that we are comparing to the same reference
		if (this.fromLocation.latitude != that.fromLocation.latitude
				|| this.fromLocation.longitude != that.fromLocation.longitude) {
			throw new RuntimeException("Trying to compare attractions based on different origins");
		}
		// Return the comparison result
		double distanceToThis = this.fromLocation.getDistance(new LocationData(this.latitude, this.longitude));
		double distanceToThat = that.fromLocation.getDistance(new LocationData(that.latitude, that.longitude));
		return Double.valueOf(distanceToThis).compareTo(Double.valueOf(distanceToThat));
	}
}