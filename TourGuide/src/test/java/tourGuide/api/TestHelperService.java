package tourGuide.api;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tourGuide.model.AttractionData;
import tourGuide.model.LocationData;
import tourGuide.model.User;
import tourGuide.model.UserPreferences;
import tourGuide.model.VisitedLocationData;
import tourGuide.user.UserServiceImpl;

@Service
public class TestHelperService {
	public final static int NUMBER_OF_TEST_ATTRACTIONS = TourGuideServiceImpl.NUMBER_OF_PROPOSED_ATTRACTIONS * 2;
	public final static double LATITUDE_USER_ONE = 0.21;
	public final static double LONGITUDE_USER_ONE = -0.00022;
	public final static double LATITUDE_ATTRACTION_ONE = 0.31;
	public final static double LONGITUDE_ATTRACTION_ONE = -0.00032;
	public final static double CURRENT_LATITUDE = 0.111;
	public final static double CURRENT_LONGITUDE = -0.222;

	@Autowired
	GpsRequestService gpsRequest;
	@Autowired
	UserServiceImpl userService;

	public User generateUser(int index) {
		return new User(new UUID(11 * index, 12 * index), "name" + index, "phone" + index, "email" + index);
	}

	public VisitedLocationData generateVisitedLocation(UUID userId, int index) {
		LocationData location = new LocationData(LATITUDE_USER_ONE * index, LONGITUDE_USER_ONE * index);
		VisitedLocationData visitedLocation = new VisitedLocationData(userId, location, new Date(index));
		return visitedLocation;
	}

	public List<User> mockGetUsersAndGetCurrentLocations(int numberOfUsers) {
		List<User> givenUsers = new ArrayList<User>();
		for (int i = 0; i < numberOfUsers; i++) {
			givenUsers.add(mockGetUserAndGetCurrentUserLocation(i + 1, null));
		}
		when(userService.getAllUsers()).thenReturn(givenUsers);
		return givenUsers;
	}

	public User mockGetUserAndGetCurrentUserLocation(int index, UserPreferences userPreferences) {
		User user = mockGetUserCurrentAndVisitedLocation(index, userPreferences);
		when(userService.getUser(user.getUserName())).thenReturn(user);
		when(gpsRequest.getCurrentUserLocation(user)).thenReturn(user.getLastVisitedLocation());
		return user;
	}

	public User mockGetCurrentUserLocation(int index) {
		User user = generateUser(index);
		VisitedLocationData visitedLocation = generateVisitedLocation(user.getUserId(), index);
		when(gpsRequest.getCurrentUserLocation(user)).thenReturn(visitedLocation);
		return user;
	}

	public User mockGetUserCurrentAndVisitedLocation(int index, UserPreferences userPreferences) {
		User user = mockGetUserCurrentLocation(index);
		user.setUserPreferences(userPreferences);
		VisitedLocationData visitedLocation = generateVisitedLocation(user.getUserId(), index);
		user.addToVisitedLocations(visitedLocation);
		return user;
	}

	public User mockGetUserCurrentLocation(int index) {
		User user = generateUser(index);
		LocationData currentLocation = new LocationData(CURRENT_LATITUDE, CURRENT_LONGITUDE);
		VisitedLocationData visitedLocation = new VisitedLocationData(user.getUserId(), currentLocation, new Date());
		when(gpsRequest.getCurrentUserLocation(user)).thenReturn(visitedLocation);
		return user;
	}

	public List<AttractionData> mockGetAllAttractions() {
		return mockGetAllAttractions(NUMBER_OF_TEST_ATTRACTIONS);
	}

	public List<AttractionData> mockGetAllAttractions(int numberOfTestAttractions) {
		List<AttractionData> attractions = new ArrayList<AttractionData>();
		for (int i = 0; i < numberOfTestAttractions; i++) {
			int index = numberOfTestAttractions - i;
			AttractionData attraction = new AttractionData("name" + index, "city" + index, "state" + index,
					LATITUDE_ATTRACTION_ONE * index, LONGITUDE_ATTRACTION_ONE * index);
			attractions.add(attraction);
		}
		when(gpsRequest.getAllAttractions()).thenReturn(attractions);
		return attractions;
	}
}