package tourGuide.services;

import static org.hamcrest.CoreMatchers.any;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.service.UserService;
import tourGuide.tracker.Tracker;
import tourGuide.user.User;
import tourGuide.user.UserReward;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RewardsServiceTest {
	@MockBean
	GpsUtil gpsUtil;
	@MockBean
	Tracker tracker; // TODO replace with interface
	@MockBean
	UserService userService; // TODO replace with interface
	@MockBean
	TourGuideService tourGuideService; // TODO replace with interface
	@MockBean
	RewardCentral rewardCentral;
	@Autowired
	RewardsService rewardsService;
	@Autowired
	TestHelperService testHelperService;

	@Test
	public void givenPrerequisitesToAdd1RewardOk_whenCalculateRewards_thenAddsCorrectReward() {
		// MOCK gpsUtil.getAttractions
		List<Attraction> givenAttractions = testHelperService.mockGpsUtilGetAttractions();
		// GIVEN
		rewardsService.setProximityBuffer(10); // statute miles
		int expectedRewardPoints = 123;
		User user = new User(UUID.randomUUID(), "name", "phone", "email");
		Attraction attraction = givenAttractions.get(0);
		Location location = new Location(attraction.latitude, attraction.longitude);
		VisitedLocation visitedLocation = new VisitedLocation(user.getUserId(), location, new Date(0));
		user.addToVisitedLocations(visitedLocation);
		// MOCK rewardCentral.getAttractionRewardPoints
		when(rewardCentral.getAttractionRewardPoints(any(UUID.class), eq(user.getUserId())))
				.thenReturn(expectedRewardPoints);
		// WHEN
		rewardsService.calculateRewards(user);
		List<UserReward> userRewards = user.getUserRewards();
		// THEN
		assertNotNull(userRewards);
		assertEquals(1, userRewards.size());
		assertNotNull(userRewards.get(0));
		assertEquals(expectedRewardPoints, userRewards.get(0).getRewardPoints());
	}

	@Test
	public void givenTooFarToAddReward_whenCalculateRewards_thenAddsNoReward() {
		// MOCK gpsUtil.getAttractions
		List<Attraction> givenAttractions = testHelperService.mockGpsUtilGetAttractions();
		// GIVEN
		rewardsService.setProximityBuffer(10); // statute miles
		double latitudeDifferenceMakingItTooFar = 0.15; // degrees
		User user = new User(UUID.randomUUID(), "name", "phone", "email");
		Attraction attraction = givenAttractions.get(0);
		Location location = new Location(attraction.latitude + latitudeDifferenceMakingItTooFar, attraction.longitude);
		VisitedLocation visitedLocation = new VisitedLocation(user.getUserId(), location, new Date(0));
		user.addToVisitedLocations(visitedLocation);
		// MOCK rewardCentral.getAttractionRewardPoints
		when(rewardCentral.getAttractionRewardPoints(any(UUID.class), eq(user.getUserId()))).thenReturn(999);
		// WHEN
		rewardsService.calculateRewards(user);
		List<UserReward> userRewards = user.getUserRewards();
		// THEN
		assertNotNull(userRewards);
		assertEquals(0, userRewards.size());
	}

	@Test
	public void givenAlreadyRewardedVisit_whenCalculateRewards_thenAddsNoReward() {
		// MOCK gpsUtil.getAttractions
		List<Attraction> givenAttractions = testHelperService.mockGpsUtilGetAttractions();
		// GIVEN
		rewardsService.setProximityBuffer(10); // statute miles
		int expectedRewardPoints = 123;
		User user = new User(UUID.randomUUID(), "name", "phone", "email");
		Attraction attraction = givenAttractions.get(0);
		Location location = new Location(attraction.latitude, attraction.longitude);
		VisitedLocation visitedLocation = new VisitedLocation(user.getUserId(), location, new Date(0));
		user.addToVisitedLocations(visitedLocation);
		UserReward userReward = new UserReward(visitedLocation, attraction, expectedRewardPoints);
		user.addUserReward(userReward);
		// MOCK rewardCentral.getAttractionRewardPoints
		when(rewardCentral.getAttractionRewardPoints(any(UUID.class), eq(user.getUserId()))).thenReturn(999);
		// WHEN
		rewardsService.calculateRewards(user);
		List<UserReward> userRewards = user.getUserRewards();
		// THEN
		assertNotNull(userRewards);
		assertEquals(1, userRewards.size());
		assertNotNull(userRewards.get(0));
		assertEquals(expectedRewardPoints, userRewards.get(0).getRewardPoints());
	}

	/*
	 * Method not used --> not need to test
	 * 
	 * @Test public void isWithinAttractionProximity() { Attraction attraction =
	 * gpsUtil.getAttractions().get(0);
	 * assertTrue(rewardsService.isWithinAttractionProximity(attraction,
	 * attraction)); }
	 */

	@Test
	public void givenMaximalProximityBuffer_whenCalculateRewards_thenAddsRewardsForAllAttractions() {
		// MOCK gpsUtil.getAttractions
		List<Attraction> givenAttractions = testHelperService.mockGpsUtilGetAttractions();
		// GIVEN
		rewardsService.setProximityBuffer((Integer.MAX_VALUE / 2) - 1);
		int expectedRewardPoints = 1;
		User user = new User(UUID.randomUUID(), "name", "phone", "email");
		for (Attraction attraction : givenAttractions) {
			Location location = new Location(attraction.latitude + 22, attraction.longitude - 33);
			VisitedLocation visitedLocation = new VisitedLocation(user.getUserId(), location, new Date(0));
			user.addToVisitedLocations(visitedLocation);
		}
		// MOCK rewardCentral.getAttractionRewardPoints
		when(rewardCentral.getAttractionRewardPoints(any(UUID.class), eq(user.getUserId())))
				.thenReturn(expectedRewardPoints);
		// WHEN
		rewardsService.calculateRewards(user);
		List<UserReward> userRewards = user.getUserRewards();
		// THEN
		assertNotNull(userRewards);
		assertEquals(TestHelperService.numberOfTestAttractions, userRewards.size());
		assertNotNull(userRewards.get(0));
		assertEquals(expectedRewardPoints, userRewards.get(0).getRewardPoints());
	}

	@Test
	public void givenTwoLocations_whenGetDistance_thenReturnsCorrectDistance() {
		// GIVEN
		Location parisLocation = new Location(48.8534, 2.3488);
		Location londonLocation = new Location(51.5084, -0.1255);
		// WHEN
		double distance = RewardsService.getDistance(parisLocation, londonLocation);
		// THEN
		assertEquals(213, distance, 1);
	}
}