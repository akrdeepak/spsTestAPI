package com.capgemini.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.capgemini.model.CommonFriendsListResponse;
import com.capgemini.model.EmailsListRecievesUpdatesResponse;
import com.capgemini.model.Subscriber;
import com.capgemini.model.FriendsStatus;
import com.capgemini.repository.FriendMangementRepository;
import com.capgemini.user.exception.FriendManagementAPIResourceNotFound;
import com.capgemini.validation.FriendManagementValidator;

@Service
public class FriendMangmtService {

	private final Logger LOG = LoggerFactory.getLogger(getClass());

	@Autowired
	FriendMangementRepository friendMangmtRepository;

	@Autowired
	public FriendMangmtService(FriendMangementRepository friendMangmtRepo) {
		this.friendMangmtRepository = friendMangmtRepo;
	}

	public FriendManagementValidator addNewFriendConnection(com.capgemini.model.AddFriend userReq)
			throws FriendManagementAPIResourceNotFound {
		LOG.info(":: In Service class .. addNewFriendConnection()");
		FriendManagementValidator fmResponse = friendMangmtRepository.addNewFriendConnection(userReq);
		return fmResponse;
	}

	public FriendManagementValidator subscribeTargetFriend(com.capgemini.model.Subscriber subscriber)
			throws FriendManagementAPIResourceNotFound {
		LOG.info(":: In Service class .. subscribeTargetFriend()");

		return friendMangmtRepository.subscribeTargetFriend(subscriber);

	}

	public FriendManagementValidator unSubscribeTargetFriend(Subscriber subscriber) throws FriendManagementAPIResourceNotFound {
		LOG.info(":: In Service class .. unSubscribeTargetFriend()");
		return friendMangmtRepository.unSubscribeTargetFriend(subscriber);
	}


	public FriendsStatus getFriendList(com.capgemini.model.FriendListRequest friendListRequest)
			throws FriendManagementAPIResourceNotFound {
		LOG.info(":: In Service class .. getFriendList()");

		return friendMangmtRepository.getFriendsList(friendListRequest);

	}

	public CommonFriendsListResponse retrieveCommonFriendList(final String email1, final String email2)
			throws FriendManagementAPIResourceNotFound {
		LOG.info(":: In Service class .. retrieveCommonFriendList()");

		return friendMangmtRepository.retrieveCommonFriendList(email1, email2);
	}

	public EmailsListRecievesUpdatesResponse emailListRecievesupdates(
			com.capgemini.model.EmailsListRecievesUpdatesRequest emailsList) throws FriendManagementAPIResourceNotFound {
		LOG.info(":: In Service class .. EmailsListRecievesUpdatesRequest()");

		return friendMangmtRepository.emailListRecievesupdates(emailsList);
	}

}
