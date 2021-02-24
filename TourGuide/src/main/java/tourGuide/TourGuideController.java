package tourGuide;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jsoniter.output.JsonStream;

import gpsUtil.location.VisitedLocation;
import tourGuide.gpsservices.GpsService;
import tourGuide.model.User;
import tourGuide.service.TourGuideService;
import tripPricer.Provider;

@RestController
public class TourGuideController {
	@Autowired
	TourGuideService tourGuideService;
	@Autowired
	GpsService gpsService;

	@GetMapping("/")
	public String index() {
		return "Greetings from TourGuide!";
	}

	@GetMapping("/getLastLocation")
	public String getLastLocation(@RequestParam String userName) {
		VisitedLocation visitedLocation = gpsService.getLastUserLocation(getUser(userName));
		return JsonStream.serialize(visitedLocation.location);
	}

	@GetMapping("/getNearbyAttractions")
	public String getNearbyAttractions(@RequestParam String userName) {
		return JsonStream.serialize(tourGuideService.getNearByAttractions(userName));
	}

	@GetMapping("/getRewards")
	public String getRewards(@RequestParam String userName) {
		return JsonStream.serialize(tourGuideService.getUserRewards(getUser(userName)));
	}

	@GetMapping("/getAllCurrentLocations")
	public String getAllCurrentLocations() {
		return JsonStream.serialize(tourGuideService.getLastLocationAllUsers());
	}

	@GetMapping("/getTripDeals")
	public String getTripDeals(@RequestParam String userName) {
		List<Provider> providers = tourGuideService.getTripDeals(getUser(userName));
		return JsonStream.serialize(providers);
	}

	// TODO ???
	private User getUser(String userName) {
		return null;// tourGuideService.getUser(userName);
	}
}