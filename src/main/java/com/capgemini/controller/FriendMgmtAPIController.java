package com.capgemini.controller;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.capgemini.model.CommonFriendsListResponse;
import com.capgemini.model.EmailsListRecievesUpdatesResponse;
import com.capgemini.model.Subscriber;
import com.capgemini.model.FriendsStatus;
import com.capgemini.service.FriendMangmtService;
import com.capgemini.user.exception.FriendManagementAPIResourceNotFound;
import com.capgemini.validation.FriendManagementValidator;

@RestController
@Validated
@EntityScan(basePackages = { "com.capgemini.model" })
@RequestMapping(value = "/api")
public class FriendMgmtAPIController {

	private final Logger LOG = LoggerFactory.getLogger(getClass());
	private static final String SUCCESS_STATUS = "Success";
	private static final String ERROR_STATUS = "error";

	public FriendMangmtService frndMngtServc;
	FriendManagementValidator fmError;

	@Autowired
	public FriendMgmtAPIController(FriendMangmtService frndMngtServc, FriendManagementValidator fmError) {
		this.frndMngtServc = frndMngtServc;
		this.fmError = fmError;
	}

	/**
	 * 
	 * @param userReq
	 * @param results
	 * @return ResponseEntity
	 * @throws FriendManagementAPIResourceNotFound
	 */

	@RequestMapping(value = "/create", method = RequestMethod.POST)
	public ResponseEntity<FriendManagementValidator> newFriendConnection(
			@Valid @RequestBody com.capgemini.model.AddFriend userReq, BindingResult results)
			throws FriendManagementAPIResourceNotFound {
		LOG.info("newFriendConnection :: ");
		FriendManagementValidator fmResponse = new FriendManagementValidator();
		// BaseResponse baseResponse = new BaseResponse();
		ResponseEntity<FriendManagementValidator> responseEntity = null;
		try {
			fmResponse = frndMngtServc.addNewFriendConnection(userReq);
			String isNewfrndMangmReqSuccess = fmResponse.getStatus();

			// LOG.info("newFriendConnection :: "+isNewfrndMangmReqSuccess);

			if (isNewfrndMangmReqSuccess.equalsIgnoreCase("Success")) {
				fmResponse.setStatus(SUCCESS_STATUS);
				responseEntity = new ResponseEntity<FriendManagementValidator>(fmResponse, HttpStatus.OK);
			} else {
				fmResponse.setStatus(ERROR_STATUS);
			}
			responseEntity = new ResponseEntity<FriendManagementValidator>(fmResponse, HttpStatus.OK);

		} catch (Exception e) {
			LOG.debug(e.getLocalizedMessage());
			responseEntity = new ResponseEntity<FriendManagementValidator>(fmResponse, HttpStatus.SERVICE_UNAVAILABLE);

		}

		return responseEntity;

	}

	/**
	 * 
	 * @param friendListRequest
	 * @param result
	 * @return
	 * @throws FriendManagementAPIResourceNotFound
	 */
	@RequestMapping(value = "/friendlist", method = RequestMethod.POST)
	public ResponseEntity<FriendsStatus> getFriendList(
			@Valid @RequestBody com.capgemini.model.FriendListRequest friendListRequest, BindingResult result)
			throws FriendManagementAPIResourceNotFound {
		LOG.info("--getFriendList :: " + friendListRequest.getEmail());
		FriendsStatus response = frndMngtServc.getFriendList(friendListRequest);
		ResponseEntity<FriendsStatus> friendListResponseEntity = null;
		try {
			if (response.getStatus() == SUCCESS_STATUS) {
				response.setStatus(SUCCESS_STATUS);
				friendListResponseEntity = new ResponseEntity<FriendsStatus>(response, HttpStatus.OK);
			} else {
				response.setStatus(ERROR_STATUS);
				friendListResponseEntity = new ResponseEntity<FriendsStatus>(response,
						HttpStatus.BAD_REQUEST);
			}
		} catch (Exception e) {
			LOG.debug(e.getLocalizedMessage());
			friendListResponseEntity = new ResponseEntity<FriendsStatus>(response,
					HttpStatus.SERVICE_UNAVAILABLE);

		}
		return friendListResponseEntity;

	}

	/**
	 * 
	 * 
	 * @param commonFrndReq
	 * @return
	 * @throws FriendManagementAPIResourceNotFound
	 */

	@RequestMapping(value = "/friends", method = RequestMethod.POST)
	public ResponseEntity<CommonFriendsListResponse> getCommonFriendList(
			@Valid @RequestBody com.capgemini.model.CommonFriendsListRequest commonFrndReq)
			throws FriendManagementAPIResourceNotFound {
		LOG.info("getCommonFriendList");
		ResponseEntity<CommonFriendsListResponse> commonFriendResponseEntity = null;
		CommonFriendsListResponse response = new CommonFriendsListResponse();
		try {
			response = frndMngtServc.retrieveCommonFriendList(commonFrndReq.getFriends().get(0),
					commonFrndReq.getFriends().get(1));

			if (response.getStatus() == SUCCESS_STATUS) {
				response.setStatus(SUCCESS_STATUS);
				commonFriendResponseEntity = new ResponseEntity<CommonFriendsListResponse>(response, HttpStatus.OK);
			} else {
				response.setStatus(ERROR_STATUS);
				commonFriendResponseEntity = new ResponseEntity<CommonFriendsListResponse>(response,
						HttpStatus.BAD_REQUEST);
			}
		} catch (Exception e) {
			LOG.debug(e.getLocalizedMessage());
			commonFriendResponseEntity = new ResponseEntity<CommonFriendsListResponse>(response,
					HttpStatus.SERVICE_UNAVAILABLE);

		}
		return commonFriendResponseEntity;
	}

	/**
	 * @param subscriber
	 * @param result
	 * @return
	 * @throws FriendManagementAPIResourceNotFound
	 */
	@RequestMapping(value = "/subscribe", method = RequestMethod.POST)
	public ResponseEntity<FriendManagementValidator> subscribeFriend(
			@Valid @RequestBody com.capgemini.model.Subscriber subscriber, BindingResult result)
			throws FriendManagementAPIResourceNotFound {
		LOG.info("Calling subscribe Friend ::");
		// Validation
		if (result.hasErrors()) {
			return handleValidation(result);
		}

		ResponseEntity<FriendManagementValidator> subscribeFriendResponseEntity = null;
		FriendManagementValidator fmv = new FriendManagementValidator();

		try {
			fmv = frndMngtServc.subscribeTargetFriend(subscriber);
			if (fmv.getStatus() == SUCCESS_STATUS) {
				subscribeFriendResponseEntity = new ResponseEntity<FriendManagementValidator>(fmv, HttpStatus.OK);
			} else {
				subscribeFriendResponseEntity = new ResponseEntity<FriendManagementValidator>(fmv,
						HttpStatus.BAD_REQUEST);
			}
		} catch (Exception e) {
			LOG.debug(e.getLocalizedMessage());
			subscribeFriendResponseEntity = new ResponseEntity<FriendManagementValidator>(fmv,
					HttpStatus.SERVICE_UNAVAILABLE);
		}

		return subscribeFriendResponseEntity;

	}

	/**
	 * 
	 * @param subscriber
	 * @param result
	 * @return
	 * @throws FriendManagementAPIResourceNotFound
	 */

	@RequestMapping(value = "/unsubscribe", method = RequestMethod.POST)
	public ResponseEntity<FriendManagementValidator> unSubscribeFriend(
			@Valid @RequestBody com.capgemini.model.Subscriber subscriber, BindingResult result)
			throws FriendManagementAPIResourceNotFound {
		// Validation
		ResponseEntity<FriendManagementValidator> unsubscribeFriendResponseEntity = null;
		boolean isValid = validateInput(subscriber);
		LOG.info("::: Caling unSubscribeFriend () ::");
		if (!isValid) {
			unsubscribeFriendResponseEntity = new ResponseEntity<FriendManagementValidator>(HttpStatus.BAD_REQUEST);
		}

		FriendManagementValidator fmv = null;
		try {
			fmv = frndMngtServc.unSubscribeTargetFriend(subscriber);
			if (fmv.getStatus() == SUCCESS_STATUS) {
				unsubscribeFriendResponseEntity = new ResponseEntity<FriendManagementValidator>(fmv, HttpStatus.OK);
			} else {
				unsubscribeFriendResponseEntity = new ResponseEntity<FriendManagementValidator>(fmv,
						HttpStatus.BAD_REQUEST);
			}
		} catch (Exception e) {
			LOG.debug(e.getLocalizedMessage());
			unsubscribeFriendResponseEntity = new ResponseEntity<FriendManagementValidator>(fmv,
					HttpStatus.BAD_REQUEST);
		}

		return unsubscribeFriendResponseEntity;
	}

	/**
	 * 
	 * @param emailsList
	 * @param result
	 * @return
	 * @throws FriendManagementAPIResourceNotFound
	 */

	@RequestMapping(value = "/friends/updatelist", method = RequestMethod.POST)
	public ResponseEntity<EmailsListRecievesUpdatesResponse> emailListRecievesupdates(
			@Valid @RequestBody com.capgemini.model.EmailsListRecievesUpdatesRequest emailsList, BindingResult result)
			throws FriendManagementAPIResourceNotFound {

		LOG.info("::  Calling emailListRecievesupdates ::");

		ResponseEntity<EmailsListRecievesUpdatesResponse> responseEntity = null;
		EmailsListRecievesUpdatesResponse response = new EmailsListRecievesUpdatesResponse();
		try {
			response = frndMngtServc.emailListRecievesupdates(emailsList);
			if (response.getStatus().toString() == SUCCESS_STATUS) {
				response.setStatus(SUCCESS_STATUS);
				responseEntity = new ResponseEntity<EmailsListRecievesUpdatesResponse>(response, HttpStatus.OK);
			} else {
				response.setStatus(ERROR_STATUS);
				responseEntity = new ResponseEntity<EmailsListRecievesUpdatesResponse>(response,
						HttpStatus.BAD_REQUEST);
			}
		} catch (Exception e) {
			LOG.debug(e.getLocalizedMessage());
			responseEntity = new ResponseEntity<EmailsListRecievesUpdatesResponse>(response,
					HttpStatus.SERVICE_UNAVAILABLE);

		}
		return responseEntity;

	}

	/**
	 * This method is used for client validation
	 * 
	 * @param result
	 * @return
	 */
	private ResponseEntity<FriendManagementValidator> handleValidation(BindingResult result) {
		fmError.setStatus("Failed");
		if (result.getFieldError("requestor") != null && result.getFieldError("target") != null) {
			fmError.setDescription(result.getFieldError("requestor").getDefaultMessage() + " "
					+ result.getFieldError("target").getDefaultMessage());
		} else if (result.getFieldError("target") != null) {
			fmError.setDescription(result.getFieldError("target").getDefaultMessage());
		} else {
			fmError.setDescription(result.getFieldError("requestor").getDefaultMessage());

		}
		return new ResponseEntity<FriendManagementValidator>(fmError, HttpStatus.BAD_REQUEST);

	}

	/**
	 * 
	 * 
	 * @param subscriber
	 * @return
	 */

	private boolean validateInput(Subscriber subscriber) {
		final String requestor = subscriber.getRequestor();
		final String target = subscriber.getTarget();
		if (requestor == null || target == null || requestor.equalsIgnoreCase(target)) {
			return false;
		}
		return true;
	}

}
