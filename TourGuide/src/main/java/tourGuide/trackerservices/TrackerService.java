package tourGuide.trackerservices;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gpsUtil.location.Attraction;
import tourGuide.gpsservices.GpsService;
import tourGuide.model.User;
import tourGuide.rewardservices.RewardService;
import tourGuide.user.UserService;

@Service
public class TrackerService extends Thread {
	private Logger logger = LoggerFactory.getLogger(TrackerService.class);
	private static final long trackingPollingInterval = TimeUnit.MINUTES.toSeconds(5);
	private final ExecutorService executorService = Executors.newSingleThreadExecutor();
	private boolean stop = false;

	@Autowired
	private UserService userService;
	@Autowired
	private GpsService gpsService;
	@Autowired
	private RewardService rewardService;

	public TrackerService() {
		logger.debug("new instance with empty constructor");
		executorService.submit(this);
	}

	/**
	 * Tells the Tracker thread to stop now and ensures it stops latest after next
	 * iteration over all users
	 */
	public void stopTracking() {
		stop = true;
		executorService.shutdownNow();
	}

	@Override
	public void run() {
		logger.debug("run begins");
		while (true) {
			if (Thread.currentThread().isInterrupted() || stop) {
				logger.debug("run has been told to stop");
				break;
			}
			trackAllUsers();
			try {
				logger.debug("run starts to sleep");
				TimeUnit.SECONDS.sleep(trackingPollingInterval);
			} catch (InterruptedException e) {
				logger.error("run has catched InterruptedException");
				break;
			}
		}
		logger.debug("run has reached the end");
	}

	public long trackAllUsers() {
		logger.debug("trackAllUsers starts iteration over all users");
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		// Get All users
		List<User> allUsers = userService.getAllUsers();
		// Get and register current location for all users
		gpsService.trackAllUserLocations(allUsers);
		// Get all attractions
		List<Attraction> allAttractions = gpsService.getAllAttractions();
		// Update rewards for all users
		rewardService.addAllNewRewardsAllUsers(allUsers, allAttractions);
		stopWatch.stop();
		long duration = TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime());
		logger.info("trackAllUsers required " + duration + " seconds for " + allUsers.size() + " users");
		return duration;
	}

}
