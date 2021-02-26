package tourGuide.tripServices;

import java.util.List;

import tourGuide.model.AttractionNearby;
import tourGuide.model.ProviderData;
import tourGuide.model.User;

public interface TripService {
	final static String TRIP_PRICER_KEY = "test-server-api-key";

	List<ProviderData> calculateProposals(User user, List<AttractionNearby> attractions, int cumulativeRewardPoints);
}