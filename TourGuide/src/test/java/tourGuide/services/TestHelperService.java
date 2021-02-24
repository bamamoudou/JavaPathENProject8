package tourGuide.services;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import tourGuide.model.User;
import tourGuide.model.UserPreferences;
import tourGuide.service.TourGuideService;
import tourGuide.user.UserService;

@Service
public class TestHelperService {
	public final static int NUMBER_OF_TEST_ATTRACTIONS = TourGuideService.NUMBER_OF_PROPOSED_ATTRACTIONS * 2;
	public final static double LATITUDE_USER_ONE = 0.21;
	public final static double LONGITUDE_USER_ONE = -0.00022;
	public final static double LATITUDE_ATTRACTION_ONE = 0.31;
	public final static double LONGITUDE_ATTRACTION_ONE = -0.00032;
	public final static double CURRENT_LATITUDE = 0.111;
	public final static double CURRENT_LONGITUDE = -0.222;

	@Autowired
	GpsUtil gpsUtil;
	@Autowired
	UserService userService;

	public List<User> mockGetAllUsersAndLocations(int numberOfUsers) {
		List<User> givenUsers = new ArrayList<User>();
		for (int i = 0; i < numberOfUsers; i++) {
			givenUsers.add(mockUserServiceGetUserAndGpsUtilGetUserLocation(i + 1, null));
		}
		when(userService.getAllUsers()).thenReturn(givenUsers);
		return givenUsers;
	}

	public User mockGpsUtilGetUserLocation(int index) {
		User user = new User(new UUID(11 * index, 12 * index), "name" + index, "phone" + index, "email" + index);
		Location location = new Location(LATITUDE_USER_ONE * index, LONGITUDE_USER_ONE * index);
		VisitedLocation visitedLocation = new VisitedLocation(user.getUserId(), location, new Date(index));
		when(gpsUtil.getUserLocation(user.getUserId())).thenReturn(visitedLocation);
		return user;
	}

	public User mockUserServiceGetUserAndGpsUtilGetUserLocation(int index, UserPreferences userPreferences) {
		User user = mockUserWithVisitedLocation(index, userPreferences);
		when(userService.getUser(user.getUserName())).thenReturn(user);
		when(gpsUtil.getUserLocation(user.getUserId())).thenReturn(user.getLastVisitedLocation());
		return user;
	}

	public User mockUserWithVisitedLocation(int index, UserPreferences userPreferences) {
		User user = mockUserWithoutVisitedLocation(index, userPreferences);
		Location location = new Location(LATITUDE_USER_ONE * index, LONGITUDE_USER_ONE * index);
		VisitedLocation visitedLocation = new VisitedLocation(user.getUserId(), location, new Date(index));
		user.addToVisitedLocations(visitedLocation);
		return user;
	}

	public User mockUserWithoutVisitedLocation(int index, UserPreferences userPreferences) {
		User user = new User(new UUID(11 * index, 12 * index), "name" + index, "phone" + index, "email" + index);
		user.setUserPreferences(userPreferences);
		Location currentLocation = new Location(CURRENT_LATITUDE, CURRENT_LONGITUDE);
		VisitedLocation visitedLocation = new VisitedLocation(user.getUserId(), currentLocation, new Date());
		when(gpsUtil.getUserLocation(user.getUserId())).thenReturn(visitedLocation);
		return user;
	}

	public List<Attraction> mockGpsUtilGetAttractions() {
		return mockGpsUtilGetAttractions(NUMBER_OF_TEST_ATTRACTIONS);
	}

	public List<Attraction> mockGpsUtilGetAttractions(int numberOfTestAttractions) {
		List<Attraction> givenAttractions = new ArrayList<Attraction>();
		for (int i = 0; i < numberOfTestAttractions; i++) {
			int index = numberOfTestAttractions - i;
			Attraction attraction = new Attraction("name" + index, "city" + index, "state" + index,
					LATITUDE_ATTRACTION_ONE * index, LONGITUDE_ATTRACTION_ONE * index);
			givenAttractions.add(attraction);
		}
		when(gpsUtil.getAttractions()).thenReturn(givenAttractions);
		return givenAttractions;
	}
}