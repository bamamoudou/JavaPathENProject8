package tourGuide.service;

import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import tourGuide.helper.InternalTestHelper;
import tourGuide.model.Attraction;
import tourGuide.model.Location;
import tourGuide.model.Provider;
import tourGuide.model.User;
import tourGuide.model.UserReward;
import tourGuide.model.VisitedLocation;
import tourGuide.tracker.Tracker;

@Service
public class TourGuideService {
	private Logger logger = LoggerFactory.getLogger(TourGuideService.class);
	private final GpsUtilService gpsUtilService;
	private final RewardsService rewardsService;
	private final TripPricerService tripPricerService;
	public final Tracker tracker;
	boolean testMode = true;
	private ExecutorService executorService;

	/**
	 * Constructor
	 * 
	 * @param gpsUtilService
	 * @param rewardsService
	 * @param tripPricerService
	 * @param executorService
	 */
	public TourGuideService(GpsUtilService gpsUtilService, RewardsService rewardsService,
			TripPricerService tripPricerService, ExecutorService executorService) {
		this.gpsUtilService = gpsUtilService;
		this.rewardsService = rewardsService;
		this.tripPricerService = tripPricerService;
		this.executorService = executorService;
		if (testMode) {
			logger.info("TestMode enabled");
			logger.debug("Initializing users");
			initializeInternalUsers();
			logger.debug("Finished initializing users");
		}
		tracker = new Tracker(this);
		addShutDownHook();
	}

	public List<UserReward> getUserRewards(User user) {
		return user.getUserRewards();
	}

	public VisitedLocation getUserLocation(User user) throws ExecutionException, InterruptedException {
		VisitedLocation visitedLocation = (user.getVisitedLocations().size() > 0) ? user.getLastVisitedLocation()
				: trackUserLocation(user).get();
		return visitedLocation;
	}

	/**
	 * Get user by usernmae
	 * 
	 * @param userName
	 * @return
	 */
	public User getUser(String userName) {
		return internalUserMap.get(userName);
	}

	/**
	 * Get All users
	 * 
	 * @return
	 */
	public List<User> getAllUsers() {
		return new ArrayList<>(internalUserMap.values());
	}

	/**
	 * Add User to the list
	 */
	public void addUser(User user) {
		if (!internalUserMap.containsKey(user.getUserName())) {
			internalUserMap.put(user.getUserName(), user);
		}
	}

	/**
	 * Get Trip Deal
	 * 
	 * @param user
	 * @return
	 * @throws IOException
	 * @throws JSONException
	 */
	public List<Provider> getTripDeals(User user) throws IOException, JSONException {
		int cumulatativeRewardPoints = user.getUserRewards().stream().mapToInt(i -> i.getRewardPoints()).sum();
		List<Provider> providers = tripPricerService.getPrice(tripPricerApiKey, user.getUserId(),
				user.getUserPreferences().getNumberOfAdults(), user.getUserPreferences().getNumberOfChildren(),
				user.getUserPreferences().getTripDuration(), cumulatativeRewardPoints);
		user.setTripDeals(providers);
		return providers;
	}

	/**
	 * Track User location asynchronously, get user location and calculate reward
	 * 
	 * @param user
	 * @return
	 */
	public CompletableFuture<VisitedLocation> trackUserLocation(User user) {
		return CompletableFuture.supplyAsync(() -> {
			VisitedLocation visitedLocation = null;
			try {
				visitedLocation = gpsUtilService.getUserLocation(user.getUserId());
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}
			user.addToVisitedLocations(visitedLocation);
			try {
				rewardsService.calculateRewards(user);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return visitedLocation;
		}, executorService);
	}

	/**
	 * Track Users location asynchronously, get users location and calculate reward
	 * 
	 * @param usersList
	 * @return
	 */
	public CompletableFuture<List<VisitedLocation>> getLocationsFromUserList(List<User> usersList) {
		List<CompletableFuture<VisitedLocation>> allVisitedLocationFutures = usersList.stream()
				.map(this::trackUserLocation).collect(Collectors.toList());

		CompletableFuture<Void> allFutures = CompletableFuture
				.allOf(allVisitedLocationFutures.toArray(new CompletableFuture[allVisitedLocationFutures.size()]));

		return allFutures.thenApply(
				v -> allVisitedLocationFutures.stream().map(CompletableFuture::join).collect(Collectors.toList()));
	}

	/**
	 * Get 5 nearest attraction of a location
	 * 
	 * @param visitedLocation
	 * @return list attractions
	 * @throws IOException
	 * @throws JSONException
	 */
	public List<Attraction> getNearbyAttractions(VisitedLocation visitedLocation) throws IOException, JSONException {
		List<Attraction> nearbyAttractions = new ArrayList<>();
		Map<Double, Attraction> attractionMap = new HashMap<>();

		gpsUtilService.getAttractions().stream()
				.forEach(a -> attractionMap.put(rewardsService.getDistance(a, visitedLocation.location), a));

		int i = 5;
		for (Map.Entry<Double, Attraction> entry : new TreeMap<>(attractionMap).entrySet()) {
			if (i != 0) {
				nearbyAttractions.add(entry.getValue());
				i--;
			} else
				break;
		}

		return nearbyAttractions;
	}

	/**
	 * Get 5 nearest attraction of a location and construct JSON response
	 * 
	 * @param user
	 * @return
	 * @throws ExecutionException
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws JSONException
	 */
	public String getFiveClosestAttractionJSON(User user)
			throws ExecutionException, InterruptedException, IOException, JSONException {
		VisitedLocation userLocation = getUserLocation(user);
		List<Attraction> closestAttractionsLists = getNearbyAttractions(userLocation);
		StringBuffer result = new StringBuffer();

		result.append("{\"userLocation\" : ");
		result.append("{").append("\"longitude\" : ").append(userLocation.location.longitude).append(",");
		result.append("\"latitude\" : ").append(userLocation.location.latitude).append("},");
		result.append("\"closestAttractions\" : ");

		List<CompletableFuture<String>> closestAttractionsListFutures = closestAttractionsLists.stream()
				.map(a -> CompletableFuture.supplyAsync(() -> {
					StringBuffer res = new StringBuffer();
					res.append("{\"attractionName\" : \"").append(a.attractionName).append("\",");
					res.append("\"city\" : \"").append(a.city).append("\",");
					res.append("\"state\" : \"").append(a.state).append("\",");
					res.append("\"distance\" : ").append(rewardsService.getDistance(userLocation.location, a)).append(",");
					try {
						res.append("\"reward\" : ").append(rewardsService.getRewardPoints(a, user)).append("}");
					} catch (IOException e) {
						e.printStackTrace();
					} catch (JSONException e) {
						e.printStackTrace();
					}
					return res.toString();
				}, executorService)).collect(Collectors.toList());

		CompletableFuture<Void> allFutures = CompletableFuture
				.allOf(closestAttractionsListFutures.toArray(new CompletableFuture[closestAttractionsListFutures.size()]));

		CompletableFuture<List<String>> allRes = allFutures.thenApply(
				v -> closestAttractionsListFutures.stream().map(CompletableFuture::join).collect(Collectors.toList()));

		result.append(allRes.get());
		result.append("}");
		return result.toString();
	}

	private void addShutDownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				tracker.stopTracking();
			}
		});
	}

	/**
	 * Return all users LastVisitedLocation
	 * 
	 * @return list of locations
	 */
	public List<VisitedLocation> getAllUsersLocations() {
		List<VisitedLocation> usersLocationsList = new ArrayList<>();
		this.getAllUsers().stream().forEach(u -> usersLocationsList.add(u.getLastVisitedLocation()));
		return usersLocationsList;
	}

	/**
	 * Return all users LastVisitedLocation and construct JSON response
	 * 
	 * @return JSON response
	 */
	public String getAllUsersLocationsJSON() {
		List<VisitedLocation> allUsersLocations = getAllUsersLocations();
		StringBuffer result = new StringBuffer();

		result.append("{");
		for (VisitedLocation visitedLocation : allUsersLocations) {
			result.append("\"").append(visitedLocation.userId.toString()).append("\":");
			result.append("{").append("\"longitude\" : ").append(visitedLocation.location.longitude).append(",");
			result.append("\"latitude\" : ").append(visitedLocation.location.latitude).append("}");
			if (!visitedLocation.equals(allUsersLocations.get(allUsersLocations.size() - 1))) {
				result.append(",");
			}
		}
		result.append("}");
		return result.toString();
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
		IntStream.range(0, 3).forEach(i -> user.addToVisitedLocations(new VisitedLocation(user.getUserId(),
				new Location(generateRandomLatitude(), generateRandomLongitude()), getRandomTime())));
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