package tourGuide.api;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import tourGuide.model.AttractionData;
import tourGuide.model.User;
import tourGuide.model.VisitedLocationData;
import tourGuide.user.UserServiceImpl;

@Service
public class GpsRequestServiceImpl implements GpsRequestService {
	private final GpsClient gpsClient;
	private Logger logger = LoggerFactory.getLogger(GpsRequestServiceImpl.class);
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private UserServiceImpl userService;

	public GpsRequestServiceImpl(GpsClient gpsClient) {
		this.gpsClient = gpsClient;
	}

	@Override
	public List<User> trackAllUserLocations(List<User> userList) {
		logListContent("trackAllUserLocations  before external call", userList);
		List<User> updatedUserList = gpsClient.trackAllUserLocations(userList);
		logListContent("trackAllUserLocations after external call", updatedUserList);
		userService.setAllUsers(updatedUserList);
		return updatedUserList;
	}

	@Override
	public List<AttractionData> getAllAttractions() {
		logger.debug("getAllAttractions before external call");
		List<AttractionData> attractions = gpsClient.getAllAttractions();
		logListContent("getAllAttractions after external call", attractions);
		return attractions;
	}

	@Override
	public VisitedLocationData getCurrentUserLocation(User user) {
		logger.debug("getCurrentUserLocation before external call for User " + user.getUserName());
		VisitedLocationData visitedLocation = gpsClient.getCurrentUserLocation(user.getUserId().toString());
		logListContent("getCurrentUserLocation after external call", Collections.singletonList(visitedLocation));
		return visitedLocation;
	}

	private void logListContent(String methodName, List<?> list) {
		logger.debug(methodName + " number of elements " + list.size() + " : " + list.toString());
		try {
			logger.debug(methodName + " content details : " + objectMapper.writeValueAsString(list));
		} catch (JsonProcessingException e) {
			throw new RuntimeException("logListContent catched a JsonProcessingException");
		}
	}
}