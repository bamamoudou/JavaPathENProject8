package tourGuide.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.money.CurrencyUnit;
import javax.money.Monetary;

import org.javamoney.moneta.Money;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import tourGuide.user.User;
import tourGuide.user.UserPreferences;
import tourGuide.user.UserReward;
import tripPricer.Provider;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestModel {
	@Test
	public void testUserReward() {
		VisitedLocation visitedLocation = new VisitedLocation(UUID.randomUUID(), new Location(0, 0),
				Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()));
		Attraction attraction = new Attraction("Disneyland", "Anaheim", "CA", 33.817595D, -117.922008D);
		UserReward userReward = new UserReward(visitedLocation, attraction, 12);

		assertEquals(12, userReward.getRewardPoints());
		userReward.setRewardPoints(24);
		assertEquals(24, userReward.getRewardPoints());
		assertNotNull(userReward.visitedLocation);
		assertEquals(visitedLocation, userReward.visitedLocation);
		assertNotNull(userReward.attraction);
		assertEquals(attraction, userReward.attraction);

		visitedLocation = null;
		attraction = null;
		userReward = null;
	}

	@Test
	public void testUserPreference() {
		CurrencyUnit currency = Monetary.getCurrency("USD");
		UserPreferences userPreferences = new UserPreferences();
		assertEquals(Integer.MAX_VALUE, userPreferences.getAttractionProximity());
		assertEquals(Money.of(0, currency), userPreferences.getLowerPricePoint());
		assertEquals(Money.of(Integer.MAX_VALUE, currency), userPreferences.getHighPricePoint());
		assertEquals(1, userPreferences.getTripDuration());
		assertEquals(1, userPreferences.getTicketQuantity());
		assertEquals(1, userPreferences.getNumberOfAdults());
		assertEquals(0, userPreferences.getNumberOfChildren());

		userPreferences.setAttractionProximity(100);
		userPreferences.setLowerPricePoint(Money.of(100, currency));
		userPreferences.setHighPricePoint(Money.of(200, currency));
		userPreferences.setTripDuration(10);
		userPreferences.setTicketQuantity(9);
		userPreferences.setNumberOfAdults(8);
		userPreferences.setNumberOfChildren(7);

		assertEquals(100, userPreferences.getAttractionProximity());
		assertEquals(Money.of(100, currency), userPreferences.getLowerPricePoint());
		assertEquals(Money.of(200, currency), userPreferences.getHighPricePoint());
		assertEquals(10, userPreferences.getTripDuration());
		assertEquals(9, userPreferences.getTicketQuantity());
		assertEquals(8, userPreferences.getNumberOfAdults());
		assertEquals(7, userPreferences.getNumberOfChildren());

		currency = null;
		userPreferences = null;
	}

	@Test
	public void testUser() {
		UUID uuid = UUID.randomUUID();
		User user = new User(uuid, "jon", "000", "jon@tourGuide.com");

		assertEquals(uuid, user.getUserId());
		assertEquals("jon", user.getUserName());
		assertEquals("000", user.getPhoneNumber());
		assertEquals("jon@tourGuide.com", user.getEmailAddress());
		assertNull(user.getLatestLocationTimestamp());
		assertEquals(0, user.getUserRewards().size());
		assertNotNull(user.getUserPreferences());
		assertEquals(0, user.getTripDeals().size());
		assertEquals(0, user.getVisitedLocations().size());

		Date date = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());

		user.setPhoneNumber("111");
		user.setEmailAddress("jon2@tourGuide.com");
		user.setLatestLocationTimestamp(date);
		List<Provider> providerList = new ArrayList<>();
		providerList.add(new Provider(UUID.randomUUID(), "name", 10.0));
		user.setTripDeals(providerList);
		user.setUserPreferences(null);
		user.addToVisitedLocations(null);
		user.addUserReward(null);

		assertEquals("111", user.getPhoneNumber());
		assertEquals("jon2@tourGuide.com", user.getEmailAddress());
		assertEquals(date, user.getLatestLocationTimestamp());
		assertEquals(1, user.getUserRewards().size());
		assertNull(user.getUserPreferences());
		assertEquals(1, user.getTripDeals().size());
		assertEquals(1, user.getVisitedLocations().size());
		user.clearVisitedLocations();
		assertEquals(0, user.getVisitedLocations().size());

		uuid = null;
		user = null;
	}
}