package tourGuide.integration;

import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import tourGuide.api.GpsRequestService;
import tourGuide.api.RewardRequestService;
import tourGuide.model.AttractionData;
import tourGuide.model.User;
import tourGuide.model.VisitedLocationData;
import tourGuide.trackerservices.TrackerService;
import tourGuide.user.UserService;


@RunWith(SpringRunner.class)
@SpringBootTest
public class TourGuidePerformanceIT {
	Logger logger = LoggerFactory.getLogger(TourGuidePerformanceIT.class);

	@Autowired
	private TrackerService trackerService;
	@Autowired
	private UserService userService;
	@Autowired
	private RewardRequestService rewardRequest;
	@Autowired
	private GpsRequestService gpsRequest;

//	@Ignore
	@Test // Performance before optimization
	public void given100Users_whenTrackAllUsers_thenTimeElapsedBelow7Seconds() {
		givenUsers_whenTrackAllUsers_thenTimeElapsedBelowLimit(100, 7);
	}

	@Ignore
	@Test // Performance after optimization
	public void given100000Users_whenTrackAllUsers_thenTimeElapsedBelow15Minutes() {
		givenUsers_whenTrackAllUsers_thenTimeElapsedBelowLimit(100 * 1000, 15 * 60);
	}

//	@Ignore
	@Test // Performance before optimization
	public void given100Users_whenAddAllNewRewardsAllUsers_thenTimeElapsedBelow58Seconds() {
		givenUsers_whenAddAllNewRewardsAllUsers_thenTimeElapsedBelowLimit(100, 58);
	}

	@Ignore
	@Test // Performance after optimization
	public void given100000Users_whenAddAllNewRewardsAllUsers_thenTimeElapsedBelow20Minutes() {
		givenUsers_whenAddAllNewRewardsAllUsers_thenTimeElapsedBelowLimit(100 * 1000, 20 * 60);
	}

	private void givenUsers_whenTrackAllUsers_thenTimeElapsedBelowLimit(int numberOfUsers,
			long maximalExpectedDuration) {
		logger.info("givenUsers_whenTrackAllUsers_thenTimeElapsedBelowLimit expected for " + numberOfUsers + " users in "
				+ maximalExpectedDuration + " seconds");
		// GIVEN
		userService.initializeInternalUsers(numberOfUsers, true);
		// WHEN
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		trackerService.trackAllUsers();
		stopWatch.stop();
		long duration = TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime());
		logger.info("trackAllUsers required " + duration + " seconds for " + numberOfUsers + " users");
		// THEN
		assertTrue(duration <= maximalExpectedDuration);
	}

	private void givenUsers_whenAddAllNewRewardsAllUsers_thenTimeElapsedBelowLimit(int numberOfUsers,
			long maximalExpectedDuration) {
		logger.info("givenUsers_whenAddAllNewRewardsAllUsers_thenTimeElapsedBelowLimit expected for " + numberOfUsers
				+ " users in " + maximalExpectedDuration + " seconds");
		// GIVEN
		userService.initializeInternalUsers(numberOfUsers, false);
		List<User> givenUsers = userService.getAllUsers();
		List<AttractionData> allAttractions = gpsRequest.getAllAttractions();
		AttractionData anyExistingAttraction = allAttractions.get(0);
		for (User user : givenUsers) {
			user.addToVisitedLocations(new VisitedLocationData(user.getUserId(), anyExistingAttraction, new Date()));
		}
		// WHEN
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		List<User> resultUsers = rewardRequest.addAllNewRewardsAllUsers(givenUsers, allAttractions);
		stopWatch.stop();
		long duration = TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime());
		logger.info("addAllNewRewardsAllUsers required " + duration + " seconds for " + numberOfUsers + " users");
		// THEN
		for (User user : resultUsers) {
			assertTrue(user.getUserRewards().size() > 0);
		}
		assertTrue(duration <= maximalExpectedDuration);
	}
}