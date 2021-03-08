package tourGuide.gpsservices;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import tourGuide.model.User;

@Service
public class GpsService {
	Logger logger = LoggerFactory.getLogger(GpsService.class);
	@Autowired
	private GpsUtil gpsUtil;

	public VisitedLocation trackUserLocation(User user) {
		logger.debug("trackUserLocation with userName = " + user.getUserName());
		VisitedLocation visitedLocation = gpsUtil.getUserLocation(user.getUserId());
		user.addToVisitedLocations(visitedLocation);
		return visitedLocation;
	}

	public void trackAllUserLocations(List<User> userList) {
		logger.debug("trackAllUserLocations with list of size = " + userList.size());
		userList.stream().parallel().forEach(user -> {
			VisitedLocation visitedLocation = gpsUtil.getUserLocation(user.getUserId());
			user.addToVisitedLocations(visitedLocation);
		});
	}

	public VisitedLocation getUserLocation(User user) {
		logger.debug("getUserLocation with userName = " + user.getUserName());
		return gpsUtil.getUserLocation(user.getUserId());
	}

	public VisitedLocation getLastUserLocation(User user) {
		logger.debug("getLastUserLocation with userName = " + user.getUserName());
		if (user.getVisitedLocations().size() > 0) {
			return user.getLastVisitedLocation();
		}
		return getUserLocation(user);
	}

	public Map<UUID, Location> getLastUsersLocations(List<User> userList) {
		logger.debug("getLastUsersLocations with list of size = " + userList.size());
		Map<UUID, Location> userLocations = new HashMap<UUID, Location>();
		userList.stream().parallel().forEach(user -> {
			userLocations.put(user.getUserId(), getLastUserLocation(user).location);
		});
		return userLocations;
	}

	public List<Attraction> getAllAttractions() {
		logger.debug("getAllAttractions");
		return gpsUtil.getAttractions();
	}
}