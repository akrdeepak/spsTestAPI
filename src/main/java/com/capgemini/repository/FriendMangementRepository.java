package com.capgemini.repository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.capgemini.model.CommonFriendsListResponse;
import com.capgemini.model.EmailsListRecievesUpdatesResponse;
import com.capgemini.model.FriendsStatus;
import com.capgemini.user.exception.FriendManagementAPIResourceNotFound;
import com.capgemini.validation.FriendManagementValidator;

@Repository
public class FriendMangementRepository {

	private final Logger LOG = LoggerFactory.getLogger(getClass());

	@Autowired
	FriendManagementValidator friendMgmtValidation;

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Autowired
	NamedParameterJdbcTemplate namedParamJdbcTemplate;

	// final FriendManagementtUtils friendManagementUtils = new
	// FriendManagementtUtils ();

	@Autowired
	public FriendMangementRepository(FriendManagementValidator fmError, JdbcTemplate jdbcTemplate) {
		this.friendMgmtValidation = fmError;
		this.jdbcTemplate = jdbcTemplate;
	}

	/**
	 * This API is invoked to create friend connection between two emails {userReq}
	 * 
	 * @param userReq
	 * @return
	 */
	public FriendManagementValidator addNewFriendConnection(com.capgemini.model.AddFriend userReq)
			throws FriendManagementAPIResourceNotFound {
		try {

			final String requestor = userReq.getRequestor();
			final String target = userReq.getTarget();

			final String query = "SELECT email FROM friendmanagement";

			final List<String> emails = jdbcTemplate.queryForList(query, String.class);
			friendMgmtValidation.setStatus("Success");
			friendMgmtValidation.setDescription("Successfully connected");
			if (requestor.equals(target)) {
				friendMgmtValidation.setStatus("Failed");
				friendMgmtValidation.setDescription("Requestor and target should not be same");
				return friendMgmtValidation;
			}

			if (emails.contains(requestor) && emails.contains(target)) {

				if (!isBlocked(requestor, target)) {
					if (isAlreadyFriend(requestor, target)) {
						friendMgmtValidation.setStatus("Failed");
						friendMgmtValidation.setDescription("Already friends");
					} else {
						connectFriend(requestor, target);
						connectFriend(target, requestor);
					}
				} else {
					friendMgmtValidation.setStatus("Failed");
					friendMgmtValidation.setDescription("target blocked");
				}
			} else if (!emails.contains(requestor) && !emails.contains(target)) {
				insertEmail(requestor);
				insertEmail(target);
				connectFriend(requestor, target);
				connectFriend(target, requestor);
			} else if (emails.contains(requestor)) {
				insertEmail(target);
				connectFriend(requestor, target);
				connectFriend(target, requestor);
			} else {
				insertEmail(requestor);
				connectFriend(requestor, target);
				connectFriend(target, requestor);
			}
		} catch (Exception e) {
			LOG.debug(e.getLocalizedMessage());
			;
		}

		return friendMgmtValidation;

	}

	/**
	 * This API is invoked to get the friend list of connected friends of given email address {friendListRequest}
	 * friend
	 * 
	 * @param email
	 * @return
	 * @throws FriendManagementAPIResourceNotFound
	 */
	public FriendsStatus getFriendsList(com.capgemini.model.FriendListRequest friendListRequest)
			throws FriendManagementAPIResourceNotFound {

		final List<String> friendListEmails = getConnectedFriendList();
		final FriendsStatus emailListresponse = new FriendsStatus();
		if (friendListEmails.contains(friendListRequest.getEmail())) {
			LOG.info("----getFriendList-----" + friendListRequest.getEmail());
			final String friendList = getFriendList(friendListRequest.getEmail());
			if ("".equals(friendList) || friendList == null) {
				emailListresponse.setStatus("Failed");
				emailListresponse.setCount(0);
			} else {
				final String[] friendListQueryParam = friendList.split(",");
				final List<String> friends = getEmailByIds(Arrays.asList(friendListQueryParam));
				if (friends.size() == 0) {

				} else {
					emailListresponse.setStatus("Success");
					emailListresponse.setCount(friends.size());
					for (String friend : friends) {
						emailListresponse.getFriends().add(friend);
					}
				}
			}
		} else {
			LOG.info(":: No friend list available for provided e-mail address ::");
			emailListresponse.setStatus("Failed");

		}
		return emailListresponse;

	}

	/**
	 * This API is invoked to get the common friends between two friends
	 * 
	 * @param email1
	 * @param email2
	 * @return
	 */
	public CommonFriendsListResponse retrieveCommonFriendList(final String email1, final String email2)
			throws FriendManagementAPIResourceNotFound {
		CommonFriendsListResponse commonFrndListresponse = new CommonFriendsListResponse();
		
		List<String> emails = getConnectedFriendList();
		
		if(emails.contains(email1) &&  emails.contains(email2)) {

		final String friendList1 = getFriendList(email1);
		final String friendList2 = getFriendList(email2);
		final String[] friendList1Container = friendList1.split(",");
		final String[] friendList2Container = friendList2.split(",");

		final Set<String> friend1Set = new HashSet<String>(Arrays.asList(friendList1Container));
		final Set<String> friend2Set = new HashSet<String>(Arrays.asList(friendList2Container));
		friend1Set.retainAll(friend2Set);

		final List<String> friends = getEmailByIds(new ArrayList<String>(friend1Set));
		if (friends.size() == 0) {
			commonFrndListresponse.setStatus("No common friend list available for this email address.");

		} else {
			commonFrndListresponse.setStatus("Success");
			commonFrndListresponse.setCount(friends.size());
			for (String friend : friends) {
				commonFrndListresponse.getFriends().add(friend);
			}
		}
		}else {
			commonFrndListresponse.setStatus("Failed");
		}
		return commonFrndListresponse;
	}

	/**
	 * This API is invoked to subscribe to updates from an email address
	 * 
	 * @param subscriber
	 * @return
	 * @throws FriendManagementAPIResourceNotFound
	 */
	public FriendManagementValidator subscribeTargetFriend(com.capgemini.model.Subscriber subscriber)
			throws FriendManagementAPIResourceNotFound {

		final String requestor = subscriber.getRequestor();
		final String target = subscriber.getTarget();

		final String query = "SELECT email FROM friendmanagement";
		final List<String> emails = jdbcTemplate.queryForList(query, String.class);

		if (requestor.equals(target)) {
			friendMgmtValidation.setStatus("Failed");
			friendMgmtValidation.setDescription("Requestor and target should not be same");
			return friendMgmtValidation;
		}
		friendMgmtValidation.setStatus("Success");
		friendMgmtValidation.setDescription("Subscribed successfully");
		boolean isBlocked = isBlocked(requestor, target);
		if (!isBlocked) {
			if (emails.contains(target) && emails.contains(requestor)) {
				final String subscribers = getSubscriptionList(requestor);
				String targetId = getId(target);
				if (subscribers.isEmpty()) {
					updateQueryForSubscriber(targetId, requestor);

					updateSubscribedBy(requestor, target);

				} else {
					final String[] subs = subscribers.split(",");
					final ArrayList<String> al = new ArrayList<String>(Arrays.asList(subs));

					if (!al.contains(targetId)) {
						targetId = subscribers + "," + targetId;
						updateQueryForSubscriber(targetId, requestor);

						updateSubscribedBy(requestor, target);

					} else {
						friendMgmtValidation.setStatus("Failed");
						friendMgmtValidation.setDescription("Target already subscribed");
					}
				}

			} else {
				friendMgmtValidation.setStatus("Failed");
				friendMgmtValidation.setDescription("Check Target or Requestor email id");
			}
		} else {
			friendMgmtValidation.setStatus("Failed");
			friendMgmtValidation.setDescription("target blocked");
		}
		return friendMgmtValidation;
	}

	/**
	 * This API is invoked to retrive all email address that can receive updates
	 * from an email address
	 * 
	 * @param emailsList
	 * @return
	 */
	public EmailsListRecievesUpdatesResponse emailListRecievesupdates(
			com.capgemini.model.EmailsListRecievesUpdatesRequest emailsList) throws FriendManagementAPIResourceNotFound {

		final EmailsListRecievesUpdatesResponse EmailsList = new EmailsListRecievesUpdatesResponse();

		final String query = "SELECT email FROM friendmanagement";
		final List<String> emails = jdbcTemplate.queryForList(query, String.class);
		final String sender = emailsList.getSender();
		String text = emailsList.getText();
		text = text.trim();
		final String reciever = text.substring(text.lastIndexOf(' ') + 1).substring(1);

		if (emails.contains(sender)) {
			if (emails.contains(reciever)) {
				final String recieverId = getId(reciever);
				updateQueryForUpdated(recieverId, sender);
			} else {
				insertEmail(reciever);
				final String recieverId = getId(reciever);
				updateQueryForUpdated(recieverId, sender);
			}

			final String friendList = getFriendList(sender);
			final String[] senderFriends = friendList.split(",");

			final String subscribedBy = getSubscribedByList(sender);
			final String[] subscribedFriends = subscribedBy.split(",");

			final Set<String> set = new HashSet<String>();
			
			if(senderFriends[0].equals("") && subscribedFriends[0].equals("")) {

			}else if(senderFriends[0].equals("")) {
				set.addAll(Arrays.asList(subscribedFriends));
			}else if(subscribedFriends[0].equals("")){
				set.addAll(Arrays.asList(senderFriends));
			}else {
				set.addAll(Arrays.asList(senderFriends));
				set.addAll(Arrays.asList(subscribedFriends));
			}
			
			final List<String> emailsUnion = new ArrayList<String>(set);
			final List<String> commonEmails = getEmailByIds(emailsUnion);

			if (!commonEmails.contains(reciever)) {
				commonEmails.add(reciever);
			}

			EmailsList.setStatus("Success");
			for (String email : commonEmails) {
				EmailsList.getFriends().add(email);
			}
		} else {
			EmailsList.setStatus("Failed");
		}
		return EmailsList;
	}

	/**
	 * This API is invoked to unsubscribe and remove id from subscribe and
	 * subscribedBy column
	 * 
	 * @param subscriber
	 * @return
	 * @throws FriendManagementAPIResourceNotFound
	 */
	public FriendManagementValidator unSubscribeTargetFriend(final com.capgemini.model.Subscriber subscriber)
			throws FriendManagementAPIResourceNotFound {
		String requestor = subscriber.getRequestor();
		String target = subscriber.getTarget();

		final String query = "SELECT email FROM friendmanagement";
		final List<String> emails = jdbcTemplate.queryForList(query, String.class);

		if (emails.contains(requestor) && emails.contains(target)) {
			final String sql = "SELECT subscriber FROM friendmanagement WHERE email=?";
			final String subscribers = (String) jdbcTemplate.queryForObject(sql, new Object[] { requestor },
					String.class);
			if (subscribers == null || subscribers.isEmpty()) {
				friendMgmtValidation.setStatus("Failed");
				friendMgmtValidation.setDescription("Requestor does not subscribe to any email");
			} else {
				// unsubscribeTarget(email);
				final String[] subs = subscribers.split(",");
				final ArrayList<String> subscriberList = new ArrayList<>(Arrays.asList(subs));
				final String targetId = getId(target);
				if (subscriberList.contains(targetId)) {
					final StringJoiner sjTarget = new StringJoiner(",");
					for (String sub : subscriberList) {
						if (!sub.equals(targetId)) {
							sjTarget.add(sub);
						}
					}
					updateQueryForSubscriber(sjTarget.toString(), requestor);

					// This section is used to remove requestor id from subscribedBy column
					final String sqlQuery = "SELECT subscribedBy FROM friendmanagement WHERE email=?";
					final String subscribedBys = (String) jdbcTemplate.queryForObject(sqlQuery, new Object[] { target },
							String.class);
					final String[] subscribedBy = subscribedBys.split(",");
					final ArrayList<String> subscribedByList = new ArrayList<>(Arrays.asList(subscribedBy));
					final String requestorId = getId(requestor);
					if (subscribedByList.contains(requestorId)) {
						final StringJoiner sjRequestor = new StringJoiner(",");
						for (String sub : subscribedByList) {
							if (!sub.equals(requestorId)) {
								sjRequestor.add(sub);
							}
						}
						updateQueryForSubscribedBy(sjRequestor.toString(), target);
					}

					updateUnsubscribeTable(requestor, target, "Blocked");

					friendMgmtValidation.setStatus("Success");
					friendMgmtValidation.setDescription("Unsubscribed successfully");
				} else {
					friendMgmtValidation.setStatus("Failed");
					friendMgmtValidation.setDescription("No Target available");
				}
			}
		} else {
			friendMgmtValidation.setStatus("Failed");
			friendMgmtValidation.setDescription("Please provide valid Requestor and Target email");
		}
		return friendMgmtValidation;
	}

	/**
	 * This method is invoked to insert new record in a friendmanagement table
	 * 
	 * @param email
	 * @return
	 */
	private int insertEmail(final String email) {
		try {
			return jdbcTemplate.update(
					"insert into friendmanagement(email, friend_list, subscriber, subscribedBy, updated, updated_timestamp) values(?,?,?,?,?,?)",
					new Object[] { email, "", "", "", "", new Timestamp((new Date()).getTime()) });
		} catch (Exception e) {
			e.getCause();
			// LOG.info(e.getLocalizedMessage());
		}
		return 0;

	}

	/**
	 * This method is used to get the all the email by ID
	 * 
	 * @param friendListQueryParam
	 * @return
	 */
	private List<String> getEmailByIds(final List<String> friendListQueryParam) {

		final StringJoiner email_Ids = new StringJoiner(",", "SELECT email FROM friendmanagement WHERE id in (", ")");

		for (String friendId : friendListQueryParam) {
			email_Ids.add(friendId);
		}
		final String query = email_Ids.toString();
		return (List<String>) jdbcTemplate.queryForList(query, new Object[] {}, String.class);
	}

	/**
	 * This method is used to get all the subscriber for an email
	 * 
	 * @param email
	 * @return
	 */
	private String getSubscriptionList(final String email) {
		final String sqlrFriendList = "SELECT subscriber FROM friendmanagement WHERE email=?";
		final String friendList = (String) jdbcTemplate.queryForObject(sqlrFriendList, new Object[] { email },
				String.class);
		return friendList;
	}

	/**
	 * This method is used to get the subscribedBy Ids for a particular email
	 * 
	 * @param email
	 * @return
	 */
	private String getSubscribedByList(final String email) {
		final String sqlrFriendList = "SELECT subscribedBy FROM friendmanagement WHERE email=?";
		final String friendList = (String) jdbcTemplate.queryForObject(sqlrFriendList, new Object[] { email },
				String.class);
		return friendList;
	}

	/**
	 * This method is invoked to connect friend
	 * 
	 * @param firstEmail
	 * @param secondEmail
	 */
	private void connectFriend(final String firstEmail, final String secondEmail) {
		final String requestorId = getId(firstEmail);
		String connectedFriendList = getFriendList(secondEmail);

		connectedFriendList = connectedFriendList.isEmpty() ? requestorId : connectedFriendList + "," + requestorId;

		jdbcTemplate.update("update friendmanagement " + " set friend_list = ?" + " where email = ?",
				new Object[] { connectedFriendList, secondEmail });
	}

	/**
	 * This method is invoked to check whether the friend is already connected
	 * 
	 * @param requestor
	 * @param targetFriend
	 * @return
	 */
	private boolean isAlreadyFriend(final String requestor, final String targetFriend) {
		boolean alreadyFriend = false;

		final String requestorId = getId(requestor);
		final String targetId = getId(targetFriend);

		final String requestorFriendList = getFriendList(requestor);
		final String[] requestorFriends = requestorFriendList.split(",");

		final String targetFirendList = getFriendList(targetFriend);
		final String[] targetFriends = targetFirendList.split(",");

		if (Arrays.asList(requestorFriends).contains(targetId) && Arrays.asList(targetFriends).contains(requestorId)) {
			alreadyFriend = true;
		}
		 LOG.info("alreadyFriend ... " + alreadyFriend);
		return alreadyFriend;

	}

	/**
	 * this method is invoked to get Id of particular email
	 * 
	 * @param email
	 * @return
	 */
	private String getId(final String email) {
		String sql = "SELECT id FROM friendmanagement WHERE email=?";
		String requestorId = (String) jdbcTemplate.queryForObject(sql, new Object[] { email }, String.class);
		return requestorId;
	}

	/**
	 * This method is invoked to get the list of friends
	 * 
	 * @param email
	 * @return
	 */
	private String getFriendList(String email) {
		String sqlrFriendList = "SELECT friend_list FROM friendmanagement WHERE email=?";
		String friendList = (String) jdbcTemplate.queryForObject(sqlrFriendList, new Object[] { email }, String.class);
		return friendList;

	}

	/**
	 * This method is invoked to check whether the target is blocked or not
	 * 
	 * @param requestor_email
	 * @param target_email
	 * @return
	 */
	private boolean isBlocked(String requestor_email, String target_email) {
		boolean status = false;
		try {
			String sqlrFriendList = "SELECT Subscription_Status FROM unsubscribe WHERE Requestor_email=? AND Target_email=?";
			String Subscription_Status = (String) jdbcTemplate.queryForObject(sqlrFriendList,
					new Object[] { requestor_email, target_email }, String.class);
			 LOG.info(":: Subscription_Status " + Subscription_Status);
			if (Subscription_Status.equalsIgnoreCase("Blocked")) {
				status = true;
			}
		} catch (Exception e) {
			 LOG.debug(e.getLocalizedMessage());

		}
		return status;
	}

	private void updateUnsubscribeTable(final String requestor, final String target, final String status) {
		jdbcTemplate.update(
				"insert into UNSUBSCRIBE(Requestor_email, Target_email, Subscription_Status) values(?, ?, ?)",
				new Object[] { requestor, target, status });
	}

	/**
	 * This method is used to update the subscribedBy column
	 * 
	 * @param requestor
	 * @param target
	 */
	private void updateSubscribedBy(final String requestor, final String target) {

		final String subscribedList = getSubscribedByList(target);
		String requestorId = getId(requestor);
		if (subscribedList.isEmpty()) {
			updateQueryForSubscribedBy(requestorId, target);
		} else {
			final String[] subscr = subscribedList.split(",");
			final ArrayList<String> subscrList = new ArrayList<String>(Arrays.asList(subscr));

			if (!subscrList.contains(requestorId)) {
				requestorId = subscribedList + "," + requestorId;
				updateQueryForSubscribedBy(requestorId, target);
			}
		}
	}

	private void updateQueryForSubscriber(final String targetId, final String requestor) {
		jdbcTemplate.update("update friendmanagement " + " set subscriber = ? " + " where email = ?",
				new Object[] { targetId, requestor });
	}

	private void updateQueryForSubscribedBy(final String requestorId, final String target) {
		jdbcTemplate.update("update friendmanagement " + " set subscribedBy = ? " + " where email = ?",
				new Object[] { requestorId, target });
	}

	private void updateQueryForUpdated(final String recieverId, final String senderEmail) {
		jdbcTemplate.update("update friendmanagement " + " set updated = ? " + " where email = ?",
				new Object[] { recieverId, senderEmail });
	}

	private List<String> getConnectedFriendList() {
		List<String> friendList = new ArrayList<>();
		final String friendListQuery = "Select email from friendmanagement";
		friendList = jdbcTemplate.queryForList(friendListQuery, String.class);
		return friendList;
	}

}