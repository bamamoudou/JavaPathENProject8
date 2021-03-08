package tourGuide.rewardservices;

import static org.hamcrest.CoreMatchers.any;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
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
import tourGuide.model.User;
import tourGuide.model.UserReward;
import tourGuide.services.TestHelperService;
import tourGuide.user.UserService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RewardsServiceTest {
	@Autowired
	RewardService rewardService;
	@Autowired
	TestHelperService testHelperService;
	@MockBean
	RewardCentral rewardCentral;
	@MockBean
	UserService userService;
	@MockBean
	GpsUtil gpsUtil;

	private static final int REWARD_POINTS_PER_ATTRACTION = 123;
	private static User user;

	@Before
	public void prepareUser() {
		user = new User(new UUID(11, 12), "user_name", "user_phone", "user_email");
	}

	@Test
	public void givenTwoLocations_whenGetDistance_thenReturnsCorrectDistance() {
		// GIVEN
		Location parisLocation = new Location(48.8534, 2.3488);
		Location londonLocation = new Location(51.5084, -0.1255);
		// WHEN
		double distance = RewardService.getDistance(parisLocation, londonLocation);
		// THEN
		assertEquals(213, distance, 1);
	}

	@Test
	public void givenPrerequisitesToAdd1RewardOk_whenAddAllNewRewards_thenAddsCorrectReward() {
		// MOCK rewardCentral
		Attraction expectedAttraction = new Attraction("attraction_name", "attraction_city", "attraction_state", 0.31,
				-0.32);
		Attraction tooFarAttraction = new Attraction("far_name", "far_city", "far_state", 99, -99);
		when(rewardCentral.getAttractionRewardPoints(eq(expectedAttraction.attractionId), eq(user.getUserId())))
				.thenReturn(REWARD_POINTS_PER_ATTRACTION);
		List<Attraction> attractions = Arrays.asList(expectedAttraction, tooFarAttraction);
		assertEquals(2, attractions.size());
		// GIVEN user was close enough to the attraction
		rewardService.setProximityMaximalDistance(10); // statute miles
		double latitudeDifferenceMakingItCloseEnough = 0.14; // degrees
		Location location = new Location(expectedAttraction.latitude + latitudeDifferenceMakingItCloseEnough,
				expectedAttraction.longitude);
		VisitedLocation visitedLocation = new VisitedLocation(user.getUserId(), location, new Date(0));
		user.addToVisitedLocations(visitedLocation);
		// WHEN
		rewardService.addAllNewRewards(user, attractions);
		List<UserReward> userRewards = user.getUserRewards();
		// THEN
		assertNotNull(userRewards);
		assertEquals(1, userRewards.size());
		assertNotNull(userRewards.get(0));
		assertEquals(REWARD_POINTS_PER_ATTRACTION, userRewards.get(0).getRewardPoints());
	}

	@Test
	public void givenTooFarToAddReward_whenAddAllNewRewards_thenAddsNoReward() {
		// MOCK rewardCentral
		Attraction attraction = new Attraction("attraction_name", "attraction_city", "attraction_state", 0.31, -0.32);
		when(rewardCentral.getAttractionRewardPoints(eq(attraction.attractionId), eq(user.getUserId())))
				.thenReturn(REWARD_POINTS_PER_ATTRACTION);
		List<Attraction> attractions = Arrays.asList(attraction);
		// GIVEN user was close enough to the attraction
		rewardService.setProximityMaximalDistance(10); // statute miles
		double latitudeDifferenceMakingItTooFar = 0.15; // degrees
		Location location = new Location(attraction.latitude + latitudeDifferenceMakingItTooFar, attraction.longitude);
		VisitedLocation visitedLocation = new VisitedLocation(user.getUserId(), location, new Date(0));
		user.addToVisitedLocations(visitedLocation);
		// WHEN
		rewardService.addAllNewRewards(user, attractions);
		List<UserReward> userRewards = user.getUserRewards();
		// THEN
		assertNotNull(userRewards);
		assertEquals(0, userRewards.size());
	}

	@Test
	public void givenAlreadyRewardedVisit_whenAddAllNewRewards_thenAddsNoReward() {
		// MOCK rewardCentral
		Attraction expectedAttraction = new Attraction("attraction_name", "attraction_city", "attraction_state", 0.31,
				-0.32);
		Attraction tooFarAttraction = new Attraction("far_name", "far_city", "far_state", 99, -99);
		when(rewardCentral.getAttractionRewardPoints(eq(expectedAttraction.attractionId), eq(user.getUserId())))
				.thenReturn(999 + REWARD_POINTS_PER_ATTRACTION);
		List<Attraction> attractions = Arrays.asList(expectedAttraction, tooFarAttraction);
		assertEquals(2, attractions.size());
		// GIVEN user has already been rewarded for this attraction
		rewardService.setProximityMaximalDistance(10); // statute miles
		double latitudeDifferenceMakingItCloseEnough = 0.14; // degrees
		Location location = new Location(expectedAttraction.latitude + latitudeDifferenceMakingItCloseEnough,
				expectedAttraction.longitude);
		VisitedLocation visitedLocation = new VisitedLocation(user.getUserId(), location, new Date(0));
		user.addToVisitedLocations(visitedLocation);
		UserReward userReward = new UserReward(visitedLocation, expectedAttraction, REWARD_POINTS_PER_ATTRACTION);
		user.addUserReward(userReward);
		// WHEN
		rewardService.addAllNewRewards(user, attractions);
		List<UserReward> userRewards = user.getUserRewards();
		// THEN
		assertNotNull(userRewards);
		assertEquals(1, userRewards.size());
		assertNotNull(userRewards.get(0));
		assertEquals(REWARD_POINTS_PER_ATTRACTION, userRewards.get(0).getRewardPoints());
	}

	//@Test
	public void givenMaximalProximityBuffer_whenAddAllNewRewards_thenAddsRewardsForAllAttractions() {
		// GIVEN test Attractions
		int numberOfTestAttractions = 999;
		List<Attraction> givenAttractions = testHelperService.mockGpsUtilGetAttractions(numberOfTestAttractions);
		// MOCK rewardCentral
		//when(rewardCentral.getAttractionRewardPoints(any(UUID.class), eq(user.getUserId())))
			//	.thenReturn(REWARD_POINTS_PER_ATTRACTION);
		// GIVEN user is close enough to all attractions
		rewardService.setProximityMaximalDistance((Integer.MAX_VALUE / 2) - 1);
		Location location = new Location(0, 0);
		VisitedLocation visitedLocation = new VisitedLocation(user.getUserId(), location, new Date(0));
		user.addToVisitedLocations(visitedLocation);
		// WHEN
		rewardService.addAllNewRewards(user, givenAttractions);
		List<UserReward> userRewards = user.getUserRewards();
		// THEN
		assertNotNull(userRewards);
		assertEquals(numberOfTestAttractions, userRewards.size());
		assertNotNull(userRewards.get(0));
		assertEquals(REWARD_POINTS_PER_ATTRACTION, userRewards.get(0).getRewardPoints());
	}

	@Test
	public void givenUserRewards_whenSumOfAllRewardPoints_thenReturnsCorrectTotal() {
		// GIVEN
		givenMaximalProximityBuffer_whenAddAllNewRewards_thenAddsRewardsForAllAttractions();
		int numberOfRewards = user.getUserRewards().size();
		// WHEN
		int totalPoints = rewardService.sumOfAllRewardPoints(user);
		// THEN
		assertEquals(numberOfRewards * REWARD_POINTS_PER_ATTRACTION, totalPoints);
	}

	//@Test
	public void givenPrerequisitesToAddRewardsOk_whenAddAllNewRewardsAllUsers_thenAddsRewardsForAllUsers() {
		// GIVEN test Users
		List<User> givenUsers = testHelperService.mockGetAllUsersAndLocations(2);
		// GIVEN test Attractions
		List<Attraction> givenAttractions = testHelperService.mockGpsUtilGetAttractions(2);
		// MOCK rewardCentral
		//when(rewardCentral.getAttractionRewardPoints(any(UUID.class), eq(givenUsers.get(0).getUserId())))
			//	.thenReturn(REWARD_POINTS_PER_ATTRACTION);
	//	when(rewardCentral.getAttractionRewardPoints(any(UUID.class), eq(givenUsers.get(1).getUserId())))
		//		.thenReturn(2 * REWARD_POINTS_PER_ATTRACTION);
		// GIVEN user was close enough to the attraction
		rewardService.setProximityMaximalDistance(8); // statute miles
		// WHEN
		rewardService.addAllNewRewardsAllUsers(givenUsers, givenAttractions);
		// THEN
		int totalRewardPoints = 0;
		for (User u : givenUsers) {
			assertNotNull(u);
			assertNotNull(u.getUserRewards());
			assertEquals(1, u.getUserRewards().size());
			totalRewardPoints += u.getUserRewards().get(0).getRewardPoints();
		}
		assertEquals(3 * REWARD_POINTS_PER_ATTRACTION, totalRewardPoints);
	}
}