package com.capgemini.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class SubscriberDTO {

	@NotNull
	@NotEmpty(message = "{requestorEmail.notempty}")
	@Email(message = "{requestorEmail.valid}")
	@Size(max = 30, message = "{requestorEmail.size}")
	private String requestor;

	@NotNull
	@NotEmpty(message = "{targetEmail.notempty}")
	@Email(message = "{targetEmail.valid}")
	@Size(max = 30, message = "{targetEmail.size}")
	private String target;

	public SubscriberDTO() {

	}

	public String getRequestor() {
		return requestor;
	}

	public void setRequestor(String requestor) {
		this.requestor = requestor;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

}
