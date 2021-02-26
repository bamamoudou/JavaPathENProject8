package tourGuide.api;

import java.util.List;
import java.util.Map;

import tourGuide.model.AttractionNearby;
import tourGuide.model.LocationData;
import tourGuide.model.ProviderData;
import tourGuide.model.User;
import tourGuide.model.UserReward;
import tourGuide.model.VisitedLocationData;

public interface TourGuideService {
	final static int NUMBER_OF_PROPOSED_ATTRACTIONS = 5;

	List<UserReward> getUserRewards(User user);

	VisitedLocationData getLastUserLocation(User user);

	Map<String, LocationData> getLastLocationAllUsers();

	List<ProviderData> getTripDeals(User user);

	List<AttractionNearby> getNearbyAttractions(String userName);
}