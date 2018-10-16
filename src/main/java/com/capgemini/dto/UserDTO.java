package com.capgemini.dto;

public class UserDTO {

	private Integer id;
	private String user_name;
	private String user_email;

	public UserDTO() {
		super();
	}

	public UserDTO(Integer id, String user_name, String user_email) {
		super();
		this.id = id;
		this.user_name = user_name;
		this.user_email = user_email;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getUser_name() {
		return user_name;
	}

	public void setUser_name(String user_name) {
		this.user_name = user_name;
	}

	public String getUser_email() {
		return user_email;
	}

	public void setUser_email(String user_email) {
		this.user_email = user_email;
	}

}
