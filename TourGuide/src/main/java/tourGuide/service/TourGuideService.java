package tourGuide.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
<<<<<<< HEAD
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
=======
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
>>>>>>> 108fb4f... Split app to service

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
<<<<<<< HEAD
import tourGuide.helper.InternalTestHelper;
import tourGuide.tracker.Tracker;
import tourGuide.user.User;
import tourGuide.user.UserReward;
=======
import tourGuide.gpsservices.GpsService;
import tourGuide.model.AttractionDistance;
import tourGuide.model.AttractionNearby;
import tourGuide.model.User;
import tourGuide.model.UserReward;
import tourGuide.rewardservices.RewardService;
import tourGuide.tripServices.TripService;
import tourGuide.user.UserService;
>>>>>>> 108fb4f... Split app to service
import tripPricer.Provider;

@Service
public class TourGuideService {
<<<<<<< HEAD
	private Logger logger = LoggerFactory.getLogger(TourGuideService.class);
	private final GpsUtil gpsUtil;
	private final RewardsService rewardsService;
	private final TripPricer tripPricer = new TripPricer();
	public final Tracker tracker;
	boolean testMode = true;
	private ExecutorService executorService;

	public TourGuideService(GpsUtil gpsUtil, RewardsService rewardsService, ExecutorService executorService) {
		this.gpsUtil = gpsUtil;
		this.rewardsService = rewardsService;
		this.executorService = executorService;
		if (testMode) {
			logger.info("TestMode enabled");
			logger.debug("Initializing users");
			initializeInternalUsers();
			logger.debug("Finished initializing users");
		}
		tracker = new Tracker(this);
		addShutDownHook();
=======
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
>>>>>>> 108fb4f... Split app to service
	}

	public List<UserReward> getUserRewards(User user) {
		return user.getUserRewards();
	}

<<<<<<< HEAD
	public VisitedLocation getUserLocation(User user) throws ExecutionException, InterruptedException {
		VisitedLocation visitedLocation = (user.getVisitedLocations().size() > 0) ? user.getLastVisitedLocation()
				: trackUserLocation(user).get();
		return visitedLocation;
	}

	public User getUser(String userName) {
		return internalUserMap.get(userName);
	}

	public List<User> getAllUsers() {
		return internalUserMap.values().stream().collect(Collectors.toList());
	}

	public void addUser(User user) {
		if (!internalUserMap.containsKey(user.getUserName())) {
			internalUserMap.put(user.getUserName(), user);
		}
	}

	public List<Provider> getTripDeals(User user) {
		int cumulatativeRewardPoints = user.getUserRewards().stream().mapToInt(i -> i.getRewardPoints()).sum();
		List<Provider> providers = tripPricer.getPrice(tripPricerApiKey, user.getUserId(),
				user.getUserPreferences().getNumberOfAdults(), user.getUserPreferences().getNumberOfChildren(),
				user.getUserPreferences().getTripDuration(), cumulatativeRewardPoints);
		user.setTripDeals(providers);
		return providers;
	}

	public CompletableFuture<VisitedLocation> trackUserLocation(User user) {
		return CompletableFuture.supplyAsync(() -> {
			VisitedLocation visitedLocation = gpsUtil.getUserLocation(user.getUserId());
			user.addToVisitedLocations(visitedLocation);
			CompletableFuture.runAsync(() -> rewardsService.calculateRewards(user));
			return visitedLocation;
		}, executorService);
	}

	public CompletableFuture<List<VisitedLocation>> getLocationsFromUserList(List<User> usersList) {
		List<CompletableFuture<VisitedLocation>> allVisitedLocationFutures = usersList.stream()
				.map(this::trackUserLocation).collect(Collectors.toList());

		CompletableFuture<Void> allFutures = CompletableFuture
				.allOf(allVisitedLocationFutures.toArray(new CompletableFuture[allVisitedLocationFutures.size()]));

		return allFutures.thenApply(
				v -> allVisitedLocationFutures.stream().map(CompletableFuture::join).collect(Collectors.toList()));
	}

	public List<Attraction> getNearByAttractions(VisitedLocation visitedLocation) {
		List<Attraction> nearbyAttractions = new ArrayList<>();
		for (Attraction attraction : gpsUtil.getAttractions()) {
			if (rewardsService.isWithinAttractionProximity(attraction, visitedLocation.location)) {
				nearbyAttractions.add(attraction);
			}
=======
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
>>>>>>> 108fb4f... Split app to service
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

	/**********************************************************************************
	 * 
	 * Methods Below: For Internal Testing
	 * 
	 **********************************************************************************/
	private static final String tripPricerApiKey = "test-server-api-key";
	// Database connection will be used for external users, but for testing purposes
	// internal users are provided and stored in memory
	private final Map<String, User> internalUserMap = new HashMap<>();

	private void initializeInternalUsers() {
		IntStream.range(0, InternalTestHelper.getInternalUserNumber()).forEach(i -> {
			String userName = "internalUser" + i;
			String phone = "000";
			String email = userName + "@tourGuide.com";
			User user = new User(UUID.randomUUID(), userName, phone, email);
			generateUserLocationHistory(user);

			internalUserMap.put(userName, user);
		});
		logger.debug("Created " + InternalTestHelper.getInternalUserNumber() + " internal test users.");
	}

	private void generateUserLocationHistory(User user) {
		IntStream.range(0, 3).forEach(i -> {
			user.addToVisitedLocations(new VisitedLocation(user.getUserId(),
					new Location(generateRandomLatitude(), generateRandomLongitude()), getRandomTime()));
		});
	}

	private double generateRandomLongitude() {
		double leftLimit = -180;
		double rightLimit = 180;
		return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
	}

	private double generateRandomLatitude() {
		double leftLimit = -85.05112878;
		double rightLimit = 85.05112878;
		return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
	}

	private Date getRandomTime() {
		LocalDateTime localDateTime = LocalDateTime.now().minusDays(new Random().nextInt(30));
		return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
	}
}