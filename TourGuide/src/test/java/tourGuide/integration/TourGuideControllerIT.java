package tourGuide.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import tourGuide.api.GpsRequestService;
import tourGuide.api.TourGuideService;
import tourGuide.model.AttractionData;
import tourGuide.model.AttractionNearby;
import tourGuide.model.LocationData;
import tourGuide.model.ProviderData;
import tourGuide.model.User;
import tourGuide.model.UserReward;
import tourGuide.model.VisitedLocationData;
import tourGuide.user.UserService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TourGuideControllerIT {
	@Autowired
	private UserService userService;
	@Autowired
	private GpsRequestService gpsRequest;
	@Autowired
	private WebApplicationContext wac;
	@Autowired
	private ObjectMapper objectMapper;
	private MockMvc mockMvc;

	@Before
	public void setup() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
		userService.initializeInternalUsers(100, true);
	}

	@Test
	public void givenUser1_whenGetLastLocation_thenReturnsCorrectLocation() throws Exception {
		// GIVEN
		String userName = "internalUser1";
		User user = userService.getUser(userName);
		// WHEN
		String responseString = mockMvc.perform(get("/getLastLocation?userName=" + userName)).andDo(print()).andReturn()
				.getResponse().getContentAsString();
		LocationData responseObject = objectMapper.readValue(responseString, LocationData.class);
		// THEN
		assertNotNull(responseObject);
		assertEquals(user.getLastVisitedLocation().location.latitude, responseObject.latitude, 0.000001);
		assertEquals(user.getLastVisitedLocation().location.longitude, responseObject.longitude, 0.000001);
	}

	@Test
	public void givenUser1_whenGetNearbyAttractions_thenReturnsCorrectAttractions() throws Exception {
		// GIVEN
		String userName = "internalUser1";
		// WHEN
		String responseString = mockMvc.perform(get("/getNearbyAttractions?userName=" + userName)).andDo(print())
				.andReturn().getResponse().getContentAsString();
		JavaType expectedResultType = objectMapper.getTypeFactory().constructCollectionType(List.class,
				AttractionNearby.class);
		List<AttractionNearby> responseObject = objectMapper.readValue(responseString, expectedResultType);
		// THEN
		assertNotNull(responseObject);
		assertThat(responseObject.size() > 0);
		for (AttractionNearby a : responseObject) {
			assertNotNull(a);
		}
	}

	@Test
	public void givenUser1_whenGetRewards_thenReturnsExpectedReward() throws Exception {
		// GIVEN
		String userName = "internalUser1";
		int rewardPoints = 12345;
		User user = userService.getUser(userName);
		VisitedLocationData visitedLocation = new VisitedLocationData(user.getUserId(),
				user.getLastVisitedLocation().location, new Date());
		AttractionData attraction = gpsRequest.getAllAttractions().get(0);
		user.addUserReward(new UserReward(visitedLocation, attraction, rewardPoints));
		// WHEN
		String responseString = mockMvc.perform(get("/getRewards?userName=" + userName)).andDo(print()).andReturn()
				.getResponse().getContentAsString();
		JavaType expectedResultType = objectMapper.getTypeFactory().constructCollectionType(List.class, UserReward.class);
		List<UserReward> responseObject = objectMapper.readValue(responseString, expectedResultType);
		// THEN
		assertNotNull(responseObject);
		assertEquals(1, responseObject.size());
		boolean rewardFound = false;
		for (UserReward r : responseObject) {
			assertNotNull(r);
			assertNotNull(r.visitedLocation);
			assertNotNull(r.visitedLocation.userId);
			if (r.visitedLocation.userId.equals(user.getUserId()) && r.rewardPoints == 12345) {
				rewardFound = true;
			}
		}
		assertTrue(rewardFound);
	}

	@Test
	public void givenUserList_whenGetAllLastLocations_thenReturnsFullList() throws Exception {
		// GIVEN
		List<User> userList = userService.getAllUsers();
		int numberOfUsers = userList.size();
		// WHEN
		String responseString = mockMvc.perform(get("/getAllLastLocations")).andDo(print()).andReturn().getResponse()
				.getContentAsString();
		JavaType expectedResultType = objectMapper.getTypeFactory().constructMapLikeType(Map.class, String.class,
				LocationData.class);
		Map<String, LocationData> responseObject = objectMapper.readValue(responseString, expectedResultType);
		// THEN
		assertNotNull(responseObject);
		assertEquals(numberOfUsers, responseObject.size());
		for (int i = 0; i < numberOfUsers; i++) {
			LocationData location = responseObject.get(userList.get(i).getUserId().toString());
			assertNotNull(location);
		}
	}

	@Test
	public void givenUser1_whenGetTripDeals_thenReturnsCorrectSomeDeals() throws Exception {
		// GIVEN
		String userName = "internalUser1";
		// WHEN
		String responseString = mockMvc.perform(get("/getTripDeals?userName=" + userName)).andDo(print()).andReturn()
				.getResponse().getContentAsString();
		JavaType expectedResultType = objectMapper.getTypeFactory().constructCollectionType(List.class,
				ProviderData.class);
		List<ProviderData> responseObject = objectMapper.readValue(responseString, expectedResultType);
		// THEN
		assertNotNull(responseObject);
		assertThat(responseObject.size() >= TourGuideService.NUMBER_OF_PROPOSED_ATTRACTIONS);
	}
}