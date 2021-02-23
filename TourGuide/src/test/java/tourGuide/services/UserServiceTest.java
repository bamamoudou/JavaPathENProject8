package tourGuide.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import tourGuide.service.UserService;
import tourGuide.user.User;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserServiceTest {
	@Test
	public void givenUser_whenAddUser_thenUserIsAdded() {
		// GIVEN
		UserService userService = new UserService(false);
		User givenUser = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		// WHEN
		userService.addUser(givenUser);
		User resultUser = userService.getUser(givenUser.getUserName());
		// THEN
		assertNotNull(resultUser);
		assertEquals(givenUser.getUserId(), resultUser.getUserId());
	}

	@Test
	public void givenUserList_whenGetAllUsers_thenReturnsFullList() {
		// GIVEN
		UserService userService = new UserService(false);
		User givenUser1 = new User(UUID.randomUUID(), "jon1", "0001", "jon1@tourGuide.com");
		User givenUser2 = new User(UUID.randomUUID(), "jon2", "0002", "jon2@tourGuide.com");
		userService.addUser(givenUser1);
		userService.addUser(givenUser2);
		// WHEN
		List<User> userList = userService.getAllUsers();
		// THEN
		assertNotNull(userList);
		assertEquals(2, userList.size());
		assertTrue(userList.contains(givenUser1));
		assertTrue(userList.contains(givenUser2));
	}
}