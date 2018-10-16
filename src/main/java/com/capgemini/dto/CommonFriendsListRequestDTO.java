package com.capgemini.dto;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class CommonFriendsListRequestDTO {

	@NotNull
	@NotEmpty
	@Size(max = 4, message = "{friendArraySize.size}")
	private List<@Valid @NotEmpty @NotNull @Email String> friends;

	public List<String> getFriends() {
		return friends;
	}

	public void setFriends(List<String> friends) {
		this.friends = friends;
	}

}
