package tourGuide.rewardservices;

import java.util.List;

import tourGuide.model.AttractionData;
import tourGuide.model.User;
import tourGuide.model.VisitedLocationData;

public interface RewardService {
	void setProximityMaximalDistance(int proximityBuffer);

	int getProximityMaximalDistance();

	boolean nearAttraction(VisitedLocationData visitedLocation, AttractionData attractionData);

	int getRewardPoints(AttractionData attractionData, User user);

	void addAllNewRewards(User user, List<AttractionData> attractions);

	List<User> addAllNewRewardsAllUsers(List<User> userList, List<AttractionData> attractions);

	int sumOfAllRewardPoints(User user);
}