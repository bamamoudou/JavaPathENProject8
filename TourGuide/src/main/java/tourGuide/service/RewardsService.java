package tourGuide.service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import org.json.JSONException;
import org.springframework.stereotype.Service;

import tourGuide.model.Attraction;
import tourGuide.model.Location;
import tourGuide.model.User;
import tourGuide.model.UserReward;
import tourGuide.model.VisitedLocation;

@Service
public class RewardsService {
	private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

	// proximity in miles
	private int defaultProximityBuffer = 10;
	private int proximityBuffer = defaultProximityBuffer;
	private int attractionProximityRange = 200;
	private final GpsUtilService gpsUtilService;
	private final RewardCentralService rewardCentralService;
	private ExecutorService executorService;

	public RewardsService(GpsUtilService gpsUtilService, RewardCentralService rewardCentralService,
			ExecutorService executorService) {
		this.gpsUtilService = gpsUtilService;
		this.rewardCentralService = rewardCentralService;
		this.executorService = executorService;
	}

	public void setProximityBuffer(int proximityBuffer) {
		this.proximityBuffer = proximityBuffer;
	}

	public void setDefaultProximityBuffer() {
		proximityBuffer = defaultProximityBuffer;
	}

	/**
	 * Calculate User Reward
	 * 
	 * @param user
	 * @throws IOException
	 * @throws JSONException
	 */
	public void calculateRewards(User user) throws IOException, JSONException {
		List<VisitedLocation> userLocations = user.getVisitedLocations();
		List<Attraction> attractions = gpsUtilService.getAttractions();

		for (VisitedLocation visitedLocation : userLocations) {
			for (Attraction attraction : attractions) {
				if (user.getUserRewards().stream()
						.noneMatch(r -> r.attraction.attractionName.equals(attraction.attractionName))
						&& nearAttraction(visitedLocation, attraction)) {
					user.addUserReward(new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user)));
				}
			}
		}
	}

	/**
	 * Calculate User reward Asynchronously
	 * 
	 * @param user
	 * @return
	 */
	public CompletableFuture<Void> calculateRewardAsync(User user) {
		return CompletableFuture.runAsync(() -> {
			try {
				this.calculateRewards(user);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}, this.executorService);
	}

	/**
	 * Calculate Users reward Asynchronously
	 * 
	 * @param usersList
	 * @return
	 */
	public CompletableFuture<List<Void>> calculateUsersListReward(List<User> usersList) {
		List<CompletableFuture<Void>> allCalculatedRewardFutures = usersList.stream().map(this::calculateRewardAsync)
				.collect(Collectors.toList());

		CompletableFuture<Void> allFutures = CompletableFuture
				.allOf(allCalculatedRewardFutures.toArray(new CompletableFuture[allCalculatedRewardFutures.size()]));

		return allFutures.thenApply(
				v -> allCalculatedRewardFutures.stream().map(CompletableFuture::join).collect(Collectors.toList()));
	}

	/**
	 * Get if Attraction is near of location
	 * 
	 * @param attraction
	 * @param location
	 * @return
	 */
	public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
		return !(getDistance(attraction, location) > attractionProximityRange);
	}

	/**
	 * Get if Attraction is near of visitedLocation
	 * 
	 * @param visitedLocation
	 * @param attraction
	 * @return
	 */
	private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
		return !(getDistance(attraction, visitedLocation.location) > proximityBuffer);
	}

	public int getRewardPoints(Attraction attraction, User user) throws IOException, JSONException {
		return rewardCentralService.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
	}

	public double getDistance(Location loc1, Location loc2) {
		double lat1 = Math.toRadians(loc1.latitude);
		double lon1 = Math.toRadians(loc1.longitude);
		double lat2 = Math.toRadians(loc2.latitude);
		double lon2 = Math.toRadians(loc2.longitude);

		double angle = Math
				.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

		double nauticalMiles = 60 * Math.toDegrees(angle);
		return STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
	}
}