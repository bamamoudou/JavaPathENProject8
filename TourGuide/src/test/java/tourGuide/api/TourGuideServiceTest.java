package tourGuide.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import tourGuide.model.AttractionNearby;
import tourGuide.model.LocationData;
import tourGuide.model.ProviderData;
import tourGuide.model.User;
import tourGuide.model.UserPreferences;
import tourGuide.model.VisitedLocationData;
import tourGuide.trackerservices.TrackerServiceImpl;
import tourGuide.user.UserServiceImpl;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TourGuideServiceTest {
	@MockBean
	GpsRequestService gpsRequest;
	@MockBean
	RewardRequestService rewardRequest;
	@MockBean
	TripRequestService tripRequest;
	@MockBean
	TrackerServiceImpl tracker;
	@MockBean
	UserServiceImpl userService;

	@Autowired
	TourGuideService tourGuideService;
	@Autowired
	TestHelperService testHelperService;

	@Before
	public void deactivateUnexpectedServices() {
		doNothing().when(tracker).run();
		doNothing().when(userService).initializeInternalUsers(any(Integer.class), any(Boolean.class));
	}

	@Test
	public void givenAttractions_whenGetNearbyAttractions_thenCorrectListReturned() {
		// GIVEN mock UserService & GpsUtil
		User user = testHelperService.mockGetUserAndGetCurrentUserLocation(1, null);
		// MOCK getAttractions
		testHelperService.mockGetAllAttractions();
		// WHEN
		List<AttractionNearby> resultAttractions = tourGuideService.getNearbyAttractions(user.getUserName());
		// THEN
		assertNotNull(resultAttractions);
		assertEquals(TourGuideService.NUMBER_OF_PROPOSED_ATTRACTIONS, resultAttractions.size());
		double resultCheckSum = 0;
		for (AttractionNearby a : resultAttractions) {
			resultCheckSum += a.attractionLocation.longitude;
		}
		double expectedCheckSum = (TourGuideService.NUMBER_OF_PROPOSED_ATTRACTIONS + 1)
				* TourGuideService.NUMBER_OF_PROPOSED_ATTRACTIONS / 2 * TestHelperService.LONGITUDE_ATTRACTION_ONE;
		assertEquals(expectedCheckSum, resultCheckSum, 0.00000000001);
	}

	@Test
	public void givenDuration4Price_whenGetTripDealsForDuration8_thenReturnsDoublePrice() {
		// GIVEN
		int adults = 2;
		int children = 3;
		int duration = 4;
		// MOCK getUser
		User userSimple = generateUserWithPreferences(31, adults, children, duration);
		User userDouble = generateUserWithPreferences(32, adults, children, 2 * duration);
		// MOCK getAttractions
		testHelperService.mockGetAllAttractions();
		// MOCK calculateProposals
		double priceForDuration4 = 1000;
		List<ProviderData> givenProvidersSimple = new ArrayList<ProviderData>();
		givenProvidersSimple.add(new ProviderData("providerSimple", priceForDuration4, new UUID(101, 102)));
		List<ProviderData> givenProvidersDouble = new ArrayList<ProviderData>();
		givenProvidersDouble.add(new ProviderData("providerDouble", 2 * priceForDuration4, new UUID(201, 202)));
		when(tripRequest.calculateProposals(eq(userSimple), any(), anyInt())).thenReturn(givenProvidersSimple);
		when(tripRequest.calculateProposals(eq(userDouble), any(), anyInt())).thenReturn(givenProvidersDouble);
		// WHEN
		List<ProviderData> duration4Providers = tourGuideService.getTripDeals(userSimple);
		List<ProviderData> duration8Providers = tourGuideService.getTripDeals(userDouble);
		// THEN
		assertNotNull(duration4Providers);
		assertNotNull(duration8Providers);
		assertNotNull(duration4Providers.size());
		assertNotNull(duration8Providers.size());
		assertNotNull(duration4Providers.get(0));
		assertNotNull(duration8Providers.get(0));
		assertEquals(duration4Providers.get(0).price * 2, duration8Providers.get(0).price, 0.0000001);
	}

	@Test
	public void given1ChildPrice_whenGetTripDealsWith2Children_thenReturnsDoublePriceForChildren() {
		// GIVEN
		int adults = 0;
		int children = 1;
		int duration = 3;
		// MOCK getUser
		User userSimple = generateUserWithPreferences(31, adults, children, duration);
		User userDouble = generateUserWithPreferences(32, adults, 2 * children, duration);
		// MOCK getAttractions
		testHelperService.mockGetAllAttractions();
		// MOCK getPrice
		double priceForOneChild = 100;
		List<ProviderData> givenProvidersSimple = new ArrayList<ProviderData>();
		givenProvidersSimple.add(new ProviderData("providerSimple", priceForOneChild, new UUID(101, 102)));
		List<ProviderData> givenProvidersDouble = new ArrayList<ProviderData>();
		givenProvidersDouble.add(new ProviderData("providerDouble", 2 * priceForOneChild, new UUID(201, 202)));
		when(tripRequest.calculateProposals(eq(userSimple), any(), anyInt())).thenReturn(givenProvidersSimple);
		when(tripRequest.calculateProposals(eq(userDouble), any(), anyInt())).thenReturn(givenProvidersDouble);
		// WHEN
		List<ProviderData> providers1Child = tourGuideService.getTripDeals(userSimple);
		List<ProviderData> providers2Children = tourGuideService.getTripDeals(userDouble);
		// THEN
		assertNotNull(providers1Child);
		assertNotNull(providers2Children);
		assertNotNull(providers1Child.size());
		assertNotNull(providers2Children.size());
		assertNotNull(providers1Child.get(0));
		assertNotNull(providers2Children.get(0));
		assertEquals(providers1Child.get(0).price * 2, providers2Children.get(0).price, 0.0000001);
	}

	@Test
	public void givenUserList_whenGetLastLocationAllUsers_thenReturnsCorrectList() {
		// MOCK getAllUsers
		List<User> givenUsers = testHelperService.mockGetUsersAndGetCurrentLocations(5);
		// WHEN
		Map<String, LocationData> allUserLocations = tourGuideService.getLastLocationAllUsers();
		// THEN
		assertNotNull(allUserLocations);
		assertEquals(givenUsers.size(), allUserLocations.size()); // CHECK LIST SIZE
		givenUsers.forEach(user -> {
			LocationData resultLocation = allUserLocations.get(user.getUserId().toString());
			assertNotNull(resultLocation);
			VisitedLocationData givenVisitedLocation = user.getLastVisitedLocation();
			assertNotNull(givenVisitedLocation);
			LocationData givenLocation = givenVisitedLocation.location;
			assertNotNull(givenLocation);
			assertEquals(givenLocation.latitude, resultLocation.latitude, 0.0000000001);
			assertEquals(givenLocation.longitude, resultLocation.longitude, 0.0000000001);
		});
	}

	@Test
	public void givenUserWithVisitedLocation_whenGetLastUserLocation_thenReturnsLastVisitedLocation() {
		// GIVEN mock GpsUtil
		User user = testHelperService.mockGetUserCurrentAndVisitedLocation(1, null);
		// WHEN
		VisitedLocationData resultLocation = tourGuideService.getLastUserLocation(user);
		// THEN
		assertNotNull(resultLocation);
		assertTrue(resultLocation.userId.equals(user.getUserId()));
		assertEquals(TestHelperService.LATITUDE_USER_ONE, resultLocation.location.latitude, 0.0000000001);
		assertEquals(TestHelperService.LONGITUDE_USER_ONE, resultLocation.location.longitude, 0.0000000001);
	}

	@Test
	public void givenUserWithoutVisitedLocation_whenGetLastUserLocation_thenReturnsCurrentLocation() {
		// GIVEN mock GpsUtil
		User user = testHelperService.mockGetUserCurrentLocation(1);
		// WHEN
		VisitedLocationData resultLocation = tourGuideService.getLastUserLocation(user);
		// THEN
		assertNotNull(resultLocation);
		assertTrue(resultLocation.userId.equals(user.getUserId()));
		assertEquals(TestHelperService.CURRENT_LATITUDE, resultLocation.location.latitude, 0.0000000001);
		assertEquals(TestHelperService.CURRENT_LONGITUDE, resultLocation.location.longitude, 0.0000000001);
	}

	private User generateUserWithPreferences(int index, int adults, int children, int duration) {
		UserPreferences userPreferences = new UserPreferences();
		userPreferences.setNumberOfAdults(adults);
		userPreferences.setNumberOfChildren(children);
		userPreferences.setTripDuration(duration);
		return testHelperService.mockGetUserAndGetCurrentUserLocation(index, userPreferences);
	}
}