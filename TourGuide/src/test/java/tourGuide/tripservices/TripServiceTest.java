package tourGuide.tripservices;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import tripPricer.Provider;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

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
import tourGuide.model.AttractionNearby;
import tourGuide.model.User;
import tourGuide.model.UserPreferences;
import tourGuide.services.TestHelperService;
import tourGuide.tripServices.TripService;
import tripPricer.TripPricer;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TripServiceTest {
	@MockBean
	private TripPricer tripPricer;
	@MockBean
	private GpsUtil gpsUtil;

	@Autowired
	TestHelperService testHelperService;
	@Autowired
	TripService tripService;

	@Test
	public void givenPriceForDuration4Days_whenCalculateProposalsWithDuration8Days_thenReturnsTwiceThePrice() {
		// GIVEN user, preferences and attractions nearby
		User user = new User(new UUID(11, 12), "user_name", "user_phone", "user_email");
		int adults = 2;
		int children = 3;
		int duration = 4;
		UserPreferences userPreferences = new UserPreferences();
		userPreferences.setNumberOfAdults(adults);
		userPreferences.setNumberOfChildren(children);
		userPreferences.setTripDuration(duration);
		user.setUserPreferences(userPreferences);
		Location location = new Location(0, 0);
		VisitedLocation visitedLocation = new VisitedLocation(user.getUserId(), location, new Date());
		user.addToVisitedLocations(visitedLocation);
		List<AttractionNearby> nearbyAttractions = new ArrayList<>();
		Attraction attraction = new Attraction("", "", "", 0, 0);
		AttractionNearby attractionNearby = new AttractionNearby(attraction, user, 0);
		nearbyAttractions.add(attractionNearby);
		// MOCK getAttractions
		testHelperService.mockGpsUtilGetAttractions();
		// MOCK getPrice
		double priceForDuration4 = 1000;
		List<Provider> givenProvidersSimple = new ArrayList<Provider>();
		givenProvidersSimple.add(new Provider(null, "providerSimple", priceForDuration4));
		List<Provider> givenProvidersDouble = new ArrayList<Provider>();
		givenProvidersDouble.add(new Provider(null, "providerDouble", 2 * priceForDuration4));
		when(tripPricer.getPrice(eq(TripService.TRIP_PRICER_KEY), any(UUID.class), eq(adults), eq(children), eq(duration),
				eq(0))).thenReturn(givenProvidersSimple);
		when(tripPricer.getPrice(eq(TripService.TRIP_PRICER_KEY), any(UUID.class), eq(adults), eq(children),
				eq(2 * duration), eq(0))).thenReturn(givenProvidersDouble);
		// WHEN
		List<Provider> duration4Providers = tripService.calculateProposals(user, nearbyAttractions, 0);
		userPreferences.setTripDuration(2 * duration);
		List<Provider> duration8Providers = tripService.calculateProposals(user, nearbyAttractions, 0);
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
	public void givenPriceFor1Child_whenCalculateProposalsWith2Children_thenReturnsTwiceThePrice() {
		// GIVEN user, preferences and attractions nearby
		User user = new User(new UUID(11, 12), "user_name", "user_phone", "user_email");
		int adults = 0;
		int children = 1;
		int duration = 3;
		UserPreferences userPreferences = new UserPreferences();
		userPreferences.setNumberOfAdults(adults);
		userPreferences.setNumberOfChildren(children);
		userPreferences.setTripDuration(duration);
		user.setUserPreferences(userPreferences);
		Location location = new Location(0, 0);
		VisitedLocation visitedLocation = new VisitedLocation(user.getUserId(), location, new Date());
		user.addToVisitedLocations(visitedLocation);
		List<AttractionNearby> nearbyAttractions = new ArrayList<>();
		Attraction attraction = new Attraction("", "", "", 0, 0);
		AttractionNearby attractionNearby = new AttractionNearby(attraction, user, 0);
		nearbyAttractions.add(attractionNearby);
		// MOCK getAttractions
		testHelperService.mockGpsUtilGetAttractions();
		// MOCK getPrice
		double priceForOneChild = 100;
		List<Provider> givenProvidersSimple = new ArrayList<Provider>();
		givenProvidersSimple.add(new Provider(null, "providerSimple", priceForOneChild));
		List<Provider> givenProvidersDouble = new ArrayList<Provider>();
		givenProvidersDouble.add(new Provider(null, "providerDouble", 2 * priceForOneChild));
		when(tripPricer.getPrice(eq(TripService.TRIP_PRICER_KEY), any(UUID.class), eq(adults), eq(children), eq(duration),
				eq(0))).thenReturn(givenProvidersSimple);
		when(tripPricer.getPrice(eq(TripService.TRIP_PRICER_KEY), any(UUID.class), eq(adults), eq(2 * children),
				eq(duration), eq(0))).thenReturn(givenProvidersDouble);
		// WHEN
		List<Provider> providers1Child = tripService.calculateProposals(user, nearbyAttractions, 0);
		userPreferences.setNumberOfChildren(2 * children);
		List<Provider> providers2Children = tripService.calculateProposals(user, nearbyAttractions, 0);
		// THEN
		assertNotNull(providers1Child);
		assertNotNull(providers2Children);
		assertNotNull(providers1Child.size());
		assertNotNull(providers2Children.size());
		assertNotNull(providers1Child.get(0));
		assertNotNull(providers2Children.get(0));
		assertEquals(providers1Child.get(0).price * 2, providers2Children.get(0).price, 0.0000001);
	}
}