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

import com.capgemini.exceptionhandling.ResourceNotFoundException;
import com.capgemini.model.Subscriber;
import com.capgemini.service.FriendMangmtService;
import com.capgemini.validation.FriendManagementValidation;

@RestController
@Validated
@EntityScan(basePackages = { "com.capgemini.entity" })
@RequestMapping(value = "/api")
public class FriendManagementController {

	private final Logger LOG = LoggerFactory.getLogger(getClass());
	private static final String SUCCESS_STATUS = "Success";
	private static final String ERROR_STATUS = "error";

	public FriendMangmtService frndMngtServc;
	FriendManagementValidation fmError;

	@Autowired
	public FriendManagementController(FriendMangmtService frndMngtServc, FriendManagementValidation fmError) {
		this.frndMngtServc = frndMngtServc;
		this.fmError = fmError;
	}

	/**
	 * 
	 * @param userReq
	 * @param results
	 * @return ResponseEntity
	 * @throws ResourceNotFoundException
	 */

	@RequestMapping(value = "/create", method = RequestMethod.POST)
	public ResponseEntity<FriendManagementValidation> newFriendConnection(
			@Valid @RequestBody com.capgemini.model.UserRequest userReq, BindingResult results)
			throws ResourceNotFoundException {
		LOG.info("newFriendConnection :: ");
		FriendManagementValidation fmResponse = new FriendManagementValidation();
		// BaseResponse baseResponse = new BaseResponse();
		ResponseEntity<FriendManagementValidation> responseEntity = null;
		try {
			fmResponse = frndMngtServc.addNewFriendConnection(userReq);
			String isNewfrndMangmReqSuccess = fmResponse.getStatus();

			// LOG.info("newFriendConnection :: "+isNewfrndMangmReqSuccess);

			if (isNewfrndMangmReqSuccess.equalsIgnoreCase("Success")) {
				fmResponse.setStatus(SUCCESS_STATUS);
				responseEntity = new ResponseEntity<FriendManagementValidation>(fmResponse, HttpStatus.OK);
			} else {
				fmResponse.setStatus(ERROR_STATUS);
			}
			responseEntity = new ResponseEntity<FriendManagementValidation>(fmResponse, HttpStatus.OK);

		} catch (Exception e) {
			LOG.error(e.getLocalizedMessage());
			responseEntity = new ResponseEntity<FriendManagementValidation>(fmResponse, HttpStatus.SERVICE_UNAVAILABLE);

		}

		return responseEntity;

	}
	
	

	

	

}
