package tourGuide.api;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import tourGuide.model.AttractionNearby;
import tourGuide.model.ProposalForm;
import tourGuide.model.ProviderData;
import tourGuide.model.User;

@Service
public class TripRequestServiceImpl implements TripRequestService {
	private Logger logger = LoggerFactory.getLogger(TripRequestServiceImpl.class);
	private final TripClient tripClient;
	@Autowired
	private ObjectMapper objectMapper;

	public TripRequestServiceImpl(TripClient tripClient) {
		this.tripClient = tripClient;
	}

	@Override
	public List<ProviderData> calculateProposals(User user, List<AttractionNearby> attractions,
			int cumulativeRewardPoints) {
		logListContent("calculateProposals before external call", Collections.singletonList(user));
		logListContent("calculateProposals before external call", attractions);
		logListContent("calculateProposals before external call", Collections.singletonList(cumulativeRewardPoints));
		ProposalForm proposalForm = new ProposalForm(user, attractions, cumulativeRewardPoints);
		List<ProviderData> proposals = tripClient.calculateProposals(proposalForm);
		logListContent("calculateProposals after external call", proposals);
		return proposals;
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