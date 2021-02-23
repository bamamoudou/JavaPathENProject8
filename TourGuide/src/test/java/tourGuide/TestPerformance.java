package tourGuide;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import tourGuide.helper.InternalTestHelper;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.service.UserService;
import tourGuide.tracker.Tracker;
import tourGuide.user.User;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestPerformance {

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
	Logger logger = LoggerFactory.getLogger(TestPerformance.class);

	@Autowired
	GpsUtil gpsUtil;
	@Autowired
	TourGuideService tourGuideService;
	@Autowired
	RewardsService rewardsService;
	@MockBean
	Tracker tracker;

	@Before
	public void setup() {
		doNothing().when(tracker).run(); // In order to have a reproductible test
	}

	@Test
	public void highVolumeTrackLocation() {
		// Locale.setDefault(Locale.US);
		// GIVEN
		InternalTestHelper.setInternalUserNumber(100);
		UserService userService = new UserService();
		List<User> allUsers = userService.getAllUsers();
		StopWatch stopWatch = new StopWatch();
		// WHEN
		stopWatch.start();
		for (User user : allUsers) {
			tourGuideService.trackUserLocation(user);
		}
		stopWatch.stop();
		// THEN
		logger.info("highVolumeTrackLocation: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime())
				+ " seconds.");
		assertTrue(TimeUnit.SECONDS.toSeconds(7) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	}

	@Test
	public void highVolumeGetRewards() {
		// GIVEN
		InternalTestHelper.setInternalUserNumber(100);
		UserService userService = new UserService();
		StopWatch stopWatch = new StopWatch();
		// WHEN
		Attraction attraction = gpsUtil.getAttractions().get(0);
		List<User> allUsers = userService.getAllUsers();
		stopWatch.start();
		for (User user : allUsers) {
			user.addToVisitedLocations(new VisitedLocation(user.getUserId(), attraction, new Date()));
			rewardsService.calculateRewards(user);
			assertTrue(user.getUserRewards().size() > 0);
		}
		stopWatch.stop();
		// THEN
		System.out.println("highVolumeGetRewards: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime())
				+ " seconds.");
		assertTrue(TimeUnit.SECONDS.toSeconds(58) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	}
}