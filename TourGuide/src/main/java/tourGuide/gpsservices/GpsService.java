package tourGuide.gpsservices;

import java.util.List;

import gpsUtil.location.VisitedLocation;
import tourGuide.model.AttractionData;
import tourGuide.model.User;
import tourGuide.model.VisitedLocationData;

public interface GpsService {
	List<User> trackAllUserLocations(List<User> userList);

	VisitedLocationData getCurrentUserLocation(String userIdString);

	List<AttractionData> getAllAttractions();

	VisitedLocationData newVisitedLocationData(VisitedLocation visitedLocation);
}