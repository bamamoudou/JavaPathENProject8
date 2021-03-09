package tourGuide.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONException;
import org.junit.Test;

import tourGuide.helper.InternalTestHelper;
import tourGuide.model.Attraction;
import tourGuide.model.User;
import tourGuide.model.UserReward;
import tourGuide.model.VisitedLocation;
import tourGuide.service.GpsUtilService;
import tourGuide.service.HTTPRequestService;
import tourGuide.service.RewardCentralService;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.service.TripPricerService;

public class TestRewardsService {
	private String configurationFilePath = "src/main/resources/application.properties";
	private ExecutorService executorService;
	private HTTPRequestService httpRequestService;
	private GpsUtilService gpsUtilService;
	private TripPricerService tripPricerService;
	private RewardCentralService rewardCentralService;
	private RewardsService rewardsService;
	private TourGuideService tourGuideService;
	private User user;

	private void initTest(Integer internalUser) {
		executorService = Executors.newFixedThreadPool(1000);
		httpRequestService = new HTTPRequestService();
		tripPricerService = new TripPricerService(httpRequestService, configurationFilePath);
		rewardCentralService = new RewardCentralService(httpRequestService, configurationFilePath);
		gpsUtilService = new GpsUtilService(httpRequestService, configurationFilePath);
		rewardsService = new RewardsService(gpsUtilService, rewardCentralService, executorService);
		InternalTestHelper.setInternalUserNumber(internalUser);
		tourGuideService = new TourGuideService(gpsUtilService, rewardsService, tripPricerService, executorService);
		user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
	}

	private void undefTest() {
		executorService.shutdown();
		executorService = null;
		gpsUtilService = null;
		rewardsService = null;
		tourGuideService = null;
		user = null;
	}

	@Test
	public void userGetRewards() throws ExecutionException, InterruptedException, IOException, JSONException {
		initTest(0);
		Attraction attraction = gpsUtilService.getAttractions().get(0);
		user.addToVisitedLocations(new VisitedLocation(user.getUserId(), attraction, new Date()));
		tourGuideService.trackUserLocation(user).get();
		List<UserReward> userRewards = user.getUserRewards();
		tourGuideService.tracker.stopTracking();

		assertEquals(1, userRewards.size());
		undefTest();
	}

	@Test
	public void isWithinAttractionProximity() throws IOException, JSONException {
		initTest(0);
		Attraction attraction = gpsUtilService.getAttractions().get(0);
		assertTrue(rewardsService.isWithinAttractionProximity(attraction, attraction));
		undefTest();
	}

	@Test
	public void nearAllAttractions() throws IOException, JSONException {
		initTest(1);

		rewardsService.setProximityBuffer(Integer.MAX_VALUE);
		rewardsService.calculateRewards(tourGuideService.getAllUsers().get(0));
		List<UserReward> userRewards = tourGuideService.getUserRewards(tourGuideService.getAllUsers().get(0));

		tourGuideService.tracker.stopTracking();

		assertEquals(gpsUtilService.getAttractions().size(), userRewards.size());
		undefTest();
	}
}