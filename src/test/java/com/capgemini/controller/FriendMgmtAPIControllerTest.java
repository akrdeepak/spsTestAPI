package com.capgemini.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.validation.BindingResult;

import com.capgemini.model.CommonFriendsListRequest;
import com.capgemini.model.CommonFriendsListResponse;
import com.capgemini.model.FriendListRequest;
import com.capgemini.model.Subscriber;
import com.capgemini.model.FriendsStatus;
import com.capgemini.model.AddFriend;
import com.capgemini.repository.FriendMangementRepository;
import com.capgemini.service.FriendMangmtService;
import com.capgemini.user.exception.FriendManagementAPIResourceNotFound;
import com.capgemini.validation.FriendManagementValidator;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FriendMgmtAPIControllerTest {

	FriendMgmtAPIController friendManagementController;
	private Subscriber subscriber;
	private AddFriend userRequest;
	private FriendListRequest friendListRequest;
	private CommonFriendsListRequest commonFriendsListRequest;
	private CommonFriendsListResponse commonFriendsListResponse;
	private FriendsStatus userFriendsListResponse;
	@Mock
	private BindingResult result;
	FriendManagementValidator fmError;
	@Mock
	JdbcTemplate jdbcTemplate;
	@Mock
	FriendMangementRepository friendMangmtRepo;
	@InjectMocks
	FriendMangmtService frndMngtServc;
	@Mock
	BindingResult bindingResult;

	@Before
	public void setUp() throws Exception {
		subscriber = new Subscriber();
		userRequest = new AddFriend();
		fmError = new FriendManagementValidator();
		friendManagementController = new FriendMgmtAPIController(frndMngtServc, fmError);
		friendListRequest = new FriendListRequest();
		commonFriendsListRequest = new CommonFriendsListRequest();
		commonFriendsListResponse = new CommonFriendsListResponse();
		userFriendsListResponse = new FriendsStatus();
	}

	@Test
	public void testRequestorUnsubscribeWithNull() throws FriendManagementAPIResourceNotFound {
		subscriber.setTarget("ravi@gmail.com");
		subscriber.setRequestor(null);
		when(this.result.hasErrors()).thenReturn(false);
		ResponseEntity<FriendManagementValidator> responseEntity = friendManagementController
				.unSubscribeFriend(subscriber, result);
		assertThat(responseEntity.getStatusCode()).isEqualByComparingTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	public void testSubscriberUnsubscribeWithNull() throws FriendManagementAPIResourceNotFound {
		when(this.result.hasErrors()).thenReturn(false);
		subscriber.setRequestor("ravi@gmail.com");
		subscriber.setTarget(null);
		ResponseEntity<FriendManagementValidator> responseEntity = friendManagementController
				.unSubscribeFriend(subscriber, result);
		assertThat(responseEntity.getStatusCode()).isEqualByComparingTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	public void testSamesubreqUnsubscribe() throws FriendManagementAPIResourceNotFound {
		subscriber.setRequestor("ravi@gmail.com");
		subscriber.setTarget("ravi@gmail.com");
		when(this.result.hasErrors()).thenReturn(false);
		ResponseEntity<FriendManagementValidator> responseEntity = friendManagementController
				.unSubscribeFriend(subscriber, result);
		assertThat(responseEntity.getStatusCode()).isEqualByComparingTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	public void testUnSubscribeHappy() throws FriendManagementAPIResourceNotFound {
		subscriber.setRequestor("ravi@gmail.com");
		subscriber.setTarget("arvi@gmail.com");
		when(this.result.hasErrors()).thenReturn(false);
		// List<Object> obj = new ArrayList<Object>();
		fmError.setStatus("Success");
		when(frndMngtServc.unSubscribeTargetFriend(subscriber)).thenReturn(fmError);
		ResponseEntity<FriendManagementValidator> responseEntity = friendManagementController
				.unSubscribeFriend(subscriber, result);
		assertThat(responseEntity.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
	}

	@Test
	public void testAddFriendHappyFlow() throws FriendManagementAPIResourceNotFound {
		fmError.setStatus("Success");
		userRequest.setRequestor("raga@gmail.com");
		userRequest.setTarget("raju@gmail.com");
		when(frndMngtServc.addNewFriendConnection(userRequest)).thenReturn(fmError);
		ResponseEntity<FriendManagementValidator> responseEntity = friendManagementController
				.newFriendConnection(userRequest, bindingResult);
		assertThat(responseEntity.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
	}

	@Test
	public void testGetFriendListHappyFlow() throws FriendManagementAPIResourceNotFound {
		friendListRequest.setEmail("ranga@gmail.com");
		userFriendsListResponse.setStatus("Success");
		when(frndMngtServc.getFriendList(friendListRequest)).thenReturn(userFriendsListResponse);
		ResponseEntity<FriendsStatus> responseEntity = friendManagementController
				.getFriendList(friendListRequest, bindingResult);
		assertThat(responseEntity.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
	}

	@Test
	public void testGetCommonFriendListHappyFlow() throws FriendManagementAPIResourceNotFound {
		List<String> friends = new ArrayList<String>();
		friends.add("ranga@gmail.com");
		friends.add("ranga1@gmail.com");
		commonFriendsListRequest.setFriends(friends);
		commonFriendsListResponse.setStatus("Success");
		when(frndMngtServc.retrieveCommonFriendList("ranga@gmail.com", "ranga1@gmail.com"))
				.thenReturn(commonFriendsListResponse);
		ResponseEntity<CommonFriendsListResponse> responseEntity = friendManagementController
				.getCommonFriendList(commonFriendsListRequest);
		assertThat(responseEntity.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
	}

	@Test
	public void testSubscribeHappyFlow() throws FriendManagementAPIResourceNotFound {
		subscriber.setRequestor("ravi@gmail.com");
		subscriber.setTarget("arvi@gmail.com");
		fmError.setStatus("Success");
		when(frndMngtServc.subscribeTargetFriend(subscriber)).thenReturn(fmError);
		ResponseEntity<FriendManagementValidator> responseEntity = friendManagementController
				.subscribeFriend(subscriber, bindingResult);
		assertThat(responseEntity.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
	}
}
