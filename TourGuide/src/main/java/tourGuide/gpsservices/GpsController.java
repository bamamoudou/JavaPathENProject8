package tourGuide.gpsservices;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tourGuide.model.AttractionData;
import tourGuide.model.User;
import tourGuide.model.VisitedLocationData;

@RestController
public class GpsController {
	private Logger logger = LoggerFactory.getLogger(GpsController.class);
	@Autowired private GpsService gpsService;

	@PatchMapping("/trackAllUserLocations")
	public List<User> trackAllUserLocations(@RequestBody List<User> userList) {
		logger.debug("trackAllUserLocations with list of size = " + userList.size());
		return gpsService.trackAllUserLocations(userList);
	}
	
	@GetMapping("/getAllAttractions")
	public List<AttractionData> getAllAttractions() {
		logger.debug("getAllAttractions");
		return gpsService.getAllAttractions();
	}

	@GetMapping("/getCurrentUserLocation")
	public VisitedLocationData getCurrentUserLocation(@RequestParam String userId) {
		logger.debug("getCurrentUserLocation for User " + userId);
		return gpsService.getCurrentUserLocation(userId);
	}
}