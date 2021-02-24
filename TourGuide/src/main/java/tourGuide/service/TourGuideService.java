package tourGuide.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import tourGuide.gpsservices.GpsService;
import tourGuide.model.AttractionDistance;
import tourGuide.model.AttractionNearby;
import tourGuide.model.User;
import tourGuide.model.UserReward;
import tourGuide.rewardservices.RewardService;
import tourGuide.tripServices.TripService;
import tourGuide.user.UserService;
import tripPricer.Provider;

@Service
public class TourGuideService {
	public static final int NUMBER_OF_PROPOSED_ATTRACTIONS = 5;
	Logger logger = LoggerFactory.getLogger(TourGuideService.class);
	@Autowired
	private GpsService gpsService;
	@Autowired
	private RewardService rewardService;
	@Autowired
	private TripService tripService;
	@Autowired
	private UserService userService;

	public TourGuideService() {
//		addShutDownHook(); // WHY ???
	}

	public List<UserReward> getUserRewards(User user) {
		return user.getUserRewards();
	}

	public Map<String, Location> getLastLocationAllUsers() {
		// Get all users within the application
		List<User> allUsers = userService.getAllUsers();
		// Get visited locations for all of them
		Map<UUID, Location> allUserLocationsWithUUID = gpsService.getLastUsersLocations(allUsers);
		// Change the key of the map to match the String format requirement
		Map<String, Location> allUserLocations = allUserLocationsWithUUID.entrySet().stream()
				.collect(Collectors.toMap(entry -> entry.getKey().toString(), entry -> entry.getValue()));
		return allUserLocations;
	}

	public List<Provider> getTripDeals(User user) {
		// Calculate the sum of all reward points for given user
		int cumulativeRewardPoints = rewardService.sumOfAllRewardPoints(user);
		// List attractions in the neighborhood of the user
		List<AttractionNearby> attractions = getNearByAttractions(user.getUserName());
		// Calculate trip proposals matching attractions list, user preferences and
		// reward points
		return tripService.calculateProposals(user, attractions, cumulativeRewardPoints);
	}

	/*
	 * NOT USED ANY MORE public VisitedLocation
	 * trackUserLocationAndCalculateRewards(User user) { // Get current user
	 * location and register it for given user VisitedLocation visitedLocation =
	 * gpsService.trackUserLocation(user); // Update rewards for given user
	 * calculateRewards(user); return visitedLocation; }
	 */

	public List<AttractionNearby> getNearByAttractions(String userName) {
		// Prepare user location as reference to measure attraction distance
		User user = userService.getUser(userName);
		VisitedLocation visitedLocation = gpsService.getLastUserLocation(user);
		Location fromLocation = visitedLocation.location;
		// Prepare list of all attractions to be sorted
		List<AttractionDistance> fullList = new ArrayList<>();
		for (Attraction toAttraction : gpsService.getAllAttractions()) {
			AttractionDistance ad = new AttractionDistance(fromLocation, toAttraction);
			fullList.add(ad);
		}
		// Sort list
		fullList.sort(null);
		// Keep the selection
		List<AttractionNearby> nearbyAttractions = new ArrayList<>();
		for (int i = 0; i < NUMBER_OF_PROPOSED_ATTRACTIONS && i < fullList.size(); i++) {
			Attraction attraction = fullList.get(i);
			int rewardPoints = rewardService.getRewardPoints(attraction, user);
			AttractionNearby nearbyAttraction = new AttractionNearby(attraction, user, rewardPoints);
			nearbyAttractions.add(nearbyAttraction);
		}
		return nearbyAttractions;
	}

	/*
	 * TODO : WHY ? private void addShutDownHook() {
	 * Runtime.getRuntime().addShutdownHook(new Thread() { public void run() {
	 * tracker.stopTracking(); } }); }
	 */

	public void addUserRewards(User user) {
		// Get all existing attractions within the application
		List<Attraction> attractions = gpsService.getAllAttractions();
		// Add all new rewards for given combination of user, visited locations and
		// existing attractions
		rewardService.addAllNewRewards(user, attractions);
	}
}