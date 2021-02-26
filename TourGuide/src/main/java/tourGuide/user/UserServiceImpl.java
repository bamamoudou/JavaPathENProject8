package tourGuide.user;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import tourGuide.model.LocationData;
import tourGuide.model.User;
import tourGuide.model.VisitedLocationData;

@Service
public class UserServiceImpl implements UserService {
	private static final int DEFAULT_INTERNAL_USER_NUMBER = 100;
	private static final boolean DEFAULT_LOCATION_HISTORY_ACTIVATED = true;
	private Random random;

	private Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
	private Map<String, User> internalUserMap;

	public UserServiceImpl() {
		this(true);
		logger.debug("new instance with empty constructor");
	}

	public UserServiceImpl(boolean fillInternalUserMapWithRandomUsers) {
		logger.debug("new instance of UserService with fillInternalUserMapWithRandomUsers = "
				+ fillInternalUserMapWithRandomUsers);
		internalUserMap = new HashMap<>();
		random = new Random();
		if (fillInternalUserMapWithRandomUsers) {
			logger.debug("Initializing users");
			initializeInternalUsers(DEFAULT_INTERNAL_USER_NUMBER, DEFAULT_LOCATION_HISTORY_ACTIVATED);
			logger.debug("Finished initializing users");
		}
	}

	@Override
	public User getUser(String userName) {
		logger.debug("getUser with userName = " + userName);
		return internalUserMap.get(userName);
	}

	@Override
	public List<User> getAllUsers() {
		logger.debug("getAllUsers returns list of size = " + internalUserMap.size());
		return internalUserMap.values().stream().collect(Collectors.toList());
	}

	@Override
	public void setAllUsers(List<User> userList) {
		logger.debug("setAllUsers with list of size = " + userList.size());
		internalUserMap = new HashMap<>();
		userList.stream().forEach(user -> {
			internalUserMap.put(user.getUserName(), user);
		});
	}

	@Override
	public void addUser(User user) {
		logger.debug("addUser with userName = " + user.getUserName());
		if (!internalUserMap.containsKey(user.getUserName())) {
			internalUserMap.put(user.getUserName(), user);
		}
	}

	@Override
	public void initializeInternalUsers(int expectedNumberOfUsers, boolean withLocationHistory) {
		logger.debug("initializeInternalUsers with InternalUserNumber = " + expectedNumberOfUsers);
		internalUserMap = new HashMap<>();
		IntStream.range(0, expectedNumberOfUsers).forEach(i -> {
			String userName = "internalUser" + i;
			String phone = "000" + i;
			String email = userName + "@tourGuide.com";
			User user = new User(UUID.randomUUID(), userName, phone, email);
			if (withLocationHistory) {
				generateUserLocationHistory(user);
			}
			internalUserMap.put(userName, user);
		});
		logger.debug("initializeInternalUsers terminated");
	}

	private void generateUserLocationHistory(User user) {
		logger.debug("generateUserLocationHistory with userName = " + user.getUserName());
		IntStream.range(0, 3).forEach(i -> {
			user.addToVisitedLocations(new VisitedLocationData(user.getUserId(),
					new LocationData(generateRandomLatitude(), generateRandomLongitude()), getRandomTime()));
		});
	}

	private double generateRandomLongitude() {
		double leftLimit = -180;
		double rightLimit = 180;
		return leftLimit + random.nextDouble() * (rightLimit - leftLimit);
	}

	private double generateRandomLatitude() {
		double leftLimit = -85.05112878;
		double rightLimit = 85.05112878;
		return leftLimit + random.nextDouble() * (rightLimit - leftLimit);
	}

	private Date getRandomTime() {
		LocalDateTime localDateTime = LocalDateTime.now().minusDays(new Random().nextInt(30));
		return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
	}
}