package com.capgemini.controller;

import static org.assertj.core.api.Assertions.assertThat;

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

import com.capgemini.exceptionhandling.ResourceNotFoundException;
import com.capgemini.model.CommonFriendsListRequest;
import com.capgemini.model.FriendListRequest;
import com.capgemini.model.Subscriber;
import com.capgemini.model.UserRequest;
import com.capgemini.repository.FriendMangmtRepo;
import com.capgemini.service.FriendMangmtService;
import com.capgemini.validation.FriendManagementValidation;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FriendManagementControllerTest {
	// @Mock
	FriendManagementController friendManagementController;
	private Subscriber subscriber;
	private UserRequest userRequest;
	private FriendListRequest friendListRequest;
	private CommonFriendsListRequest commonFriendsListRequest;
	// @InjectMocks
	@Mock
	private BindingResult result;
	// @Mock
	FriendManagementValidation fmError;
	@Mock
	JdbcTemplate jdbcTemplate;
	 @InjectMocks
	FriendMangmtRepo friendMangmtRepo;
	// @InjectMocks
	FriendMangmtService frndMngtServc;
	@Mock
	BindingResult bindingResult;

	@Before
	public void setUp() throws Exception {
		subscriber = new Subscriber();
		userRequest = new UserRequest();
		fmError = new FriendManagementValidation();
		friendMangmtRepo = new FriendMangmtRepo(fmError, jdbcTemplate);
		frndMngtServc = new FriendMangmtService(friendMangmtRepo);
		friendManagementController = new FriendManagementController(frndMngtServc, fmError);
		friendListRequest = new FriendListRequest();
		commonFriendsListRequest = new CommonFriendsListRequest();
	}


	@Test
	public void test_addFriend_success() throws ResourceNotFoundException {
		fmError.setStatus("success");
		userRequest.setRequestor("raga@gmail.com");
		userRequest.setTarget("raju@gmail.com");
		ResponseEntity<FriendManagementValidation> responseEntity = null;
		responseEntity = friendManagementController.newFriendConnection(userRequest, bindingResult);
		assertThat(responseEntity.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
	}


}
