package tourGuide.gpsservices;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.VisitedLocation;
import tourGuide.model.AttractionData;
import tourGuide.model.LocationData;
import tourGuide.model.User;
import tourGuide.model.VisitedLocationData;

@Service
public class GpsServiceImpl implements GpsService {
	private Logger logger = LoggerFactory.getLogger(GpsServiceImpl.class);
	@Autowired
	private GpsUtil gpsUtil;

	@Override
	public List<User> trackAllUserLocations(List<User> userList) {
		logger.debug("trackAllUserLocations with list of size = " + userList.size());
		userList.stream().parallel().forEach(user -> {
			VisitedLocation visitedLocation = gpsUtil.getUserLocation(user.getUserId());
			user.addToVisitedLocations(newVisitedLocationData(visitedLocation));
		});
		return userList;
	}

	@Override
	public VisitedLocationData getCurrentUserLocation(String userIdString) {
		logger.debug("getUserLocation with userId = " + userIdString);
		UUID userId = UUID.fromString(userIdString);
		return newVisitedLocationData(gpsUtil.getUserLocation(userId));
	}

	@Override
	public List<AttractionData> getAllAttractions() {
		logger.debug("getAllAttractions");
		List<AttractionData> dataList = new ArrayList<AttractionData>();
		gpsUtil.getAttractions().stream().forEach(attraction -> {
			AttractionData data = new AttractionData();
			data.name = attraction.attractionName;
			data.city = attraction.city;
			data.state = attraction.state;
			data.latitude = attraction.latitude;
			data.longitude = attraction.longitude;
			dataList.add(data);
		});
		return dataList;
	}

	@Override
	public VisitedLocationData newVisitedLocationData(VisitedLocation visitedLocation) {
		return new VisitedLocationData(visitedLocation.userId,
				new LocationData(visitedLocation.location.latitude, visitedLocation.location.longitude),
				visitedLocation.timeVisited);
	}
}