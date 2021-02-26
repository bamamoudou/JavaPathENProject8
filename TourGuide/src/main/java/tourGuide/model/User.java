package tourGuide.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class User {
	private final UUID userId;
	private final String userName;
	private String phoneNumber;
	private String emailAddress;
	private List<VisitedLocationData> visitedLocations;
	private List<UserReward> userRewards;
	private UserPreferences userPreferences;

	public User(UUID userId, String userName, String phoneNumber, String emailAddress) {
		this.userId = userId;
		this.userName = userName;
		this.phoneNumber = phoneNumber;
		this.emailAddress = emailAddress;
		visitedLocations = new ArrayList<>();
		userRewards = new ArrayList<>();
		userPreferences = new UserPreferences();
	}

	public User() {
		this(new UUID(0, 0), new String(), new String(), new String());
	}

	public UUID getUserId() {
		return userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void addToVisitedLocations(VisitedLocationData visitedLocation) {
		visitedLocations.add(visitedLocation);
	}

	public List<VisitedLocationData> getVisitedLocations() {
		return visitedLocations;
	}

	public void clearVisitedLocations() {
		visitedLocations.clear();
	}

	public void addUserReward(UserReward userReward) {
		if (userRewards.stream().filter(r -> r.attraction.name.equals(userReward.attraction.name)).count() == 0) {
			userRewards.add(userReward);
		}
	}

	public List<UserReward> getUserRewards() {
		return userRewards;
	}

	public UserPreferences getUserPreferences() {
		return userPreferences;
	}

	public void setUserPreferences(UserPreferences userPreferences) {
		this.userPreferences = userPreferences;
	}

	public VisitedLocationData getLastVisitedLocation() {
		int listLength = visitedLocations.size();
		if (listLength == 0) {
			return null;
		}
		return visitedLocations.get(listLength - 1);
	}
}