package tourGuide.tripServices;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import tourGuide.model.ProposalForm;
import tourGuide.model.ProviderData;

@RestController
public class TripController {
	private Logger logger = LoggerFactory.getLogger(TripController.class);
	@Autowired
	private TripService tripService;

	@GetMapping("/calculateProposals")
	public List<ProviderData> calculateProposals(@RequestBody ProposalForm proposalForm) {
		logger.debug("calculateProposals for User " + proposalForm.user.getUserName() + " with Attraction list of size "
				+ proposalForm.attractions.size() + " and " + proposalForm.cumulativeRewardPoints + "reward points");
		return tripService.calculateProposals(proposalForm.user, proposalForm.attractions,
				proposalForm.cumulativeRewardPoints);
	}
}