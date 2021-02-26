package tourGuide.api;

import java.util.List;

import tourGuide.model.AttractionData;
import tourGuide.model.User;
import tourGuide.model.VisitedLocationData;

public interface GpsRequestService {
	List<User> trackAllUserLocations(List<User> userList);

	List<AttractionData> getAllAttractions();

	VisitedLocationData getCurrentUserLocation(User user);
}
