package tourGuide;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.json.JSONException;
import org.junit.Test;

import tourGuide.helper.InternalTestHelper;
import tourGuide.model.Attraction;
import tourGuide.model.User;
import tourGuide.model.VisitedLocation;
import tourGuide.service.GpsUtilService;
import tourGuide.service.HTTPRequestService;
import tourGuide.service.RewardCentralService;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.service.TripPricerService;

public class TestPerformance {
//	private ExecutorService executorService = Executors.newFixedThreadPool(1000);
//	private int nbrUsers = 5;

	/*
	 * A note on performance improvements:
	 * 
	 * The number of users generated for the high volume tests can be easily
	 * adjusted via this method:
	 * 
	 * InternalTestHelper.setInternalUserNumber(100000);
	 * 
	 * 
	 * These tests can be modified to suit new solutions, just as long as the
	 * performance metrics at the end of the tests remains consistent.
	 * 
	 * These are performance metrics that we are trying to hit:
	 * 
	 * highVolumeTrackLocation: 100,000 users within 15 minutes:
	 * assertTrue(TimeUnit.MINUTES.toSeconds(15) >=
	 * TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	 *
	 * highVolumeGetRewards: 100,000 users within 20 minutes:
	 * assertTrue(TimeUnit.MINUTES.toSeconds(20) >=
	 * TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	 */

	private String configurationFilePath = "src/main/resources/application.properties";
	private ExecutorService executorService;
	private HTTPRequestService httpRequestService;
	private TripPricerService tripPricerService;
	private RewardCentralService rewardCentralService;
	private GpsUtilService gpsUtilService;
	private RewardsService rewardsService;
	private TourGuideService tourGuideService;

	private void initTest() {
		executorService = Executors.newFixedThreadPool(1000);
		httpRequestService = new HTTPRequestService();
		tripPricerService = new TripPricerService(httpRequestService, configurationFilePath);
		rewardCentralService = new RewardCentralService(httpRequestService, configurationFilePath);
		gpsUtilService = new GpsUtilService(httpRequestService, configurationFilePath);
		rewardsService = new RewardsService(gpsUtilService, rewardCentralService, executorService);
		InternalTestHelper.setInternalUserNumber(100); // Users should be incremented up to 100,000
		tourGuideService = new TourGuideService(gpsUtilService, rewardsService, tripPricerService, executorService);

	}

	private void undefTest() {
		InternalTestHelper.setInternalUserNumber(0);
		executorService.shutdown();
		executorService = null;
		gpsUtilService = null;
		rewardsService = null;
		tourGuideService = null;
	}

	// Must finish less than 15 minutes
	@Test
	public void highVolumeTrackLocation() throws ExecutionException, InterruptedException {
		initTest();

		List<User> allUsers = tourGuideService.getAllUsers();

		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		List<VisitedLocation> locationList = tourGuideService.getLocationsFromUserList(allUsers).get();
		stopWatch.stop();
		tourGuideService.tracker.stopTracking();
		assertTrue(locationList.size() > 0);

		System.out.println("highVolumeTrackLocation: Time Elapsed: "
				+ TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
		assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));

		undefTest();
	}

	// Must finish less than 20 minutes
	@Test
	public void highVolumeGetRewards() throws ExecutionException, InterruptedException, IOException, JSONException {
		initTest();

		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		Attraction attraction = gpsUtilService.getAttractions().get(0);
		List<User> allUsers = tourGuideService.getAllUsers();
		allUsers.forEach(u -> u.addToVisitedLocations(new VisitedLocation(u.getUserId(), attraction, new Date())));

		rewardsService.calculateUsersListReward(allUsers).get();

		for (User user : allUsers) {
			assertTrue(user.getUserRewards().size() > 0);
		}
		stopWatch.stop();
		tourGuideService.tracker.stopTracking();

		System.out.println("highVolumeGetRewards: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime())
				+ " seconds.");
		assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));

		undefTest();
	}
}