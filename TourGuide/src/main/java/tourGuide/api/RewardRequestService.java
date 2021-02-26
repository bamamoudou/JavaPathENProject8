package tourGuide.api;

import java.util.List;

import tourGuide.model.AttractionData;
import tourGuide.model.User;

public interface RewardRequestService {
	List<User> addAllNewRewardsAllUsers(List<User> userList, List<AttractionData> attractions);

	int sumOfAllRewardPoints(User user);

	int getRewardPoints(AttractionData attraction, User user);
}