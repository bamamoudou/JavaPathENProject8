package tourGuide.gpsservices;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import gpsUtil.GpsUtil;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import tourGuide.model.User;
import tourGuide.services.TestHelperService;
import tourGuide.user.UserService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GpsServiceTest {
	@MockBean
	GpsUtil gpsUtil;
	@MockBean
	UserService userService;
	@Autowired
	TestHelperService testHelperService;
	@Autowired
	GpsService gpsService;

	@Test
	public void givenUser_whenTrackUserLocation_thenLocationAddedToUserHistory() {
		// GIVEN mock GpsUtil
		User user = testHelperService.mockUserServiceGetUserAndGpsUtilGetUserLocation(1, null);
		// WHEN
		VisitedLocation visitedLocation = gpsService.trackUserLocation(user);
		// THEN
		assertEquals(2, user.getVisitedLocations().size()); // Current location has been added to the list
		assertNotNull(visitedLocation);
		assertTrue(visitedLocation.userId.equals(user.getUserId()));
		assertEquals(visitedLocation.location.latitude, user.getLastVisitedLocation().location.latitude, 0.0000000001);
		assertEquals(visitedLocation.location.longitude, user.getLastVisitedLocation().location.longitude, 0.0000000001);
	}

	@Test
	public void givenUserWithVisitedLocation_whenGetUserLocation_thenReturnsCurrentLocation() {
		// GIVEN mock GpsUtil
		User user = testHelperService.mockUserWithVisitedLocation(1, null);
		// WHEN
		VisitedLocation resultLocation = gpsService.getUserLocation(user);
		// THEN
		assertNotNull(resultLocation);
		assertTrue(resultLocation.userId.equals(user.getUserId()));
		assertEquals(TestHelperService.CURRENT_LATITUDE, resultLocation.location.latitude, 0.0000000001);
		assertEquals(TestHelperService.CURRENT_LONGITUDE, resultLocation.location.longitude, 0.0000000001);
	}

	@Test
	public void givenUserWithoutVisitedLocation_whenGetUserLocation_thenReturnsCurrentLocation() {
		// GIVEN mock GpsUtil
		User user = testHelperService.mockUserWithoutVisitedLocation(1, null);
		// WHEN
		VisitedLocation resultLocation = gpsService.getUserLocation(user);
		// THEN
		assertNotNull(resultLocation);
		assertTrue(resultLocation.userId.equals(user.getUserId()));
		assertEquals(TestHelperService.CURRENT_LATITUDE, resultLocation.location.latitude, 0.0000000001);
		assertEquals(TestHelperService.CURRENT_LONGITUDE, resultLocation.location.longitude, 0.0000000001);
	}

	@Test
	public void givenUserWithVisitedLocation_whenGetLastUserLocation_thenReturnsPreviousLocation() {
		// GIVEN mock GpsUtil
		User user = testHelperService.mockUserWithVisitedLocation(1, null);
		// WHEN
		VisitedLocation resultLocation = gpsService.getLastUserLocation(user);
		// THEN
		assertNotNull(resultLocation);
		assertTrue(resultLocation.userId.equals(user.getUserId()));
		assertEquals(TestHelperService.LATITUDE_USER_ONE, resultLocation.location.latitude, 0.0000000001);
		assertEquals(TestHelperService.LONGITUDE_USER_ONE, resultLocation.location.longitude, 0.0000000001);
	}

	@Test
	public void givenUserWithoutVisitedLocation_whenGetLastUserLocation_thenReturnsCurrentLocation() {
		// GIVEN mock GpsUtil
		User user = testHelperService.mockUserWithoutVisitedLocation(1, null);
		// WHEN
		VisitedLocation resultLocation = gpsService.getUserLocation(user);
		// THEN
		assertNotNull(resultLocation);
		assertTrue(resultLocation.userId.equals(user.getUserId()));
		assertEquals(TestHelperService.CURRENT_LATITUDE, resultLocation.location.latitude, 0.0000000001);
		assertEquals(TestHelperService.CURRENT_LONGITUDE, resultLocation.location.longitude, 0.0000000001);
	}

	@Test
	public void givenUserList_whenGetLastUsersLocations_thenReturnsCorrectList() {
		// GIVEN mock getAllUsers
		List<User> givenUsers = testHelperService.mockGetAllUsersAndLocations(5);
		// WHEN
		Map<UUID, Location> allUserLocations = gpsService.getLastUsersLocations(givenUsers);
		// THEN
		assertNotNull(allUserLocations);
		assertEquals(givenUsers.size(), allUserLocations.size()); // CHECK LIST SIZE
		User givenUser = givenUsers.get(0);
		assertNotNull(givenUser);
		assertNotNull(givenUser.getUserId());
		Location resultLocation = allUserLocations.get(givenUser.getUserId());
		assertNotNull(resultLocation);
		VisitedLocation givenVisitedLocation = givenUser.getLastVisitedLocation();
		assertNotNull(givenVisitedLocation);
		Location givenLocation = givenVisitedLocation.location;
		assertNotNull(givenLocation);
		assertEquals(givenLocation.latitude, resultLocation.latitude, 0.0000000001); // CHECK LOCATION FOR FIRST GIVEN
																												// USER
		assertEquals(givenLocation.longitude, resultLocation.longitude, 0.0000000001); // CHECK LOCATION FOR FIRST GIVEN
																													// USER
	}

	@Test
	public void givenUserList_whenTrackAllUserLocations_thenAddsVisitedLocationToAllUsers() {
		// GIVEN mock getAllUsers
		List<User> givenUsers = testHelperService.mockGetAllUsersAndLocations(5);
		for (User user : givenUsers) {
			assertNotNull(user);
			assertNotNull(user.getVisitedLocations());
			assertEquals(1, user.getVisitedLocations().size());
		}
		// WHEN
		gpsService.trackAllUserLocations(givenUsers);
		// THEN
		for (User user : givenUsers) {
			assertNotNull(user);
			assertNotNull(user.getVisitedLocations());
			assertEquals(2, user.getVisitedLocations().size());
		}
	}
}