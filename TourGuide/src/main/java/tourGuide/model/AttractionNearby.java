package tourGuide.model;

import java.util.UUID;

/**
 * AttractionNearby is the data format exchanged in JSON with the client for the
 * getNearbyAttractions controller
 * 
 * Members are public as tolerated for basic data structure
 * 
 */
public class AttractionNearby {
	public final UUID id; // Basically not requested but required for further reuse of object instances
	public final String name;
	public LocationData attractionLocation;
	public LocationData userLocation;
	public double distance;
	public int rewardPoints;

	public AttractionNearby(AttractionData attraction, User user, int rewardPoints) {
		id = attraction.id;
		name = attraction.name;
		attractionLocation = new LocationData(attraction.latitude, attraction.longitude);
		VisitedLocationData visitedLocation = user.getLastVisitedLocation();
		userLocation = new LocationData(visitedLocation.location.latitude, visitedLocation.location.longitude);
		distance = userLocation.getDistance(attractionLocation);
		this.rewardPoints = rewardPoints;
	}

	public AttractionNearby() {
		id = new UUID(0, 0);
		name = new String();
	}
}