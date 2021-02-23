package tourGuide.model;

import java.util.UUID;

import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import tourGuide.service.RewardsService;
import tourGuide.user.User;

/**
 * AttractionNearby is the data format exchanged in JSON with the client for the
 * getNearbyAttractions controller
 * 
 * Members are public as tolerated for basic data structure
 * 
 */
public class AttractionNearby {
	public UUID id; // Basically not requested but required for further reuse of object instances
	public String name;
	public Location attractionLocation;
	public Location userLocation;
	public double distance;
	public int rewardPoints;

	public AttractionNearby(Attraction attraction, User user, int rewardPoints) {
		id = attraction.attractionId;
		name = attraction.attractionName;
		attractionLocation = new Location(attraction.latitude, attraction.longitude);
		Location userCurrentLocation = user.getLastVisitedLocation().location;
		userLocation = new Location(userCurrentLocation.latitude, userCurrentLocation.longitude);
		distance = RewardsService.getDistance(attractionLocation, userLocation);
		this.rewardPoints = rewardPoints;
	}
}