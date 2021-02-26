package tourGuide.api;

import java.util.List;

import tourGuide.model.AttractionNearby;
import tourGuide.model.ProviderData;
import tourGuide.model.User;

public interface TripRequestService {
	List<ProviderData> calculateProposals(User user, List<AttractionNearby> attractions, int cumulativeRewardPoints);
}