package tourGuide;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import tourGuide.service.TourGuideService;

@RunWith(SpringRunner.class)
@ComponentScan({ "tourGuide" })
@ContextConfiguration(classes = { TourGuideModule.class })
@WebMvcTest(TourGuideController.class)
public class TestTourGuideController {

	@Autowired
	MockMvc mockMvc;

	@MockBean
	TourGuideService tourGuideService;

	@Autowired
	private WebApplicationContext webApplicationContext;

	private void runPath(String path) throws Exception {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
		mockMvc.perform(get(path)).andExpect(status().is2xxSuccessful());
		mockMvc = null;
	}

	@Test
	public void getIndexTest() throws Exception {
		runPath("/");
	}

	@Test
	public void getLocationTest() throws Exception {
		runPath("/getLocation?userName=internalUser0");
	}

	@Test
	public void getNearbyAttractionsTest() throws Exception {
		runPath("/getNearbyAttractions?userName=internalUser0");
	}

	@Test
	public void getRewardsTest() throws Exception {
		runPath("/getRewards?userName=internalUser0");
	}

	@Test
	public void getAllCurrentLocationsTest() throws Exception {
		runPath("/getAllCurrentLocations");
	}

	@Test
	public void getTripDealsTest() throws Exception {
		runPath("/getTripDeals?userName=internalUser0");
	}
}