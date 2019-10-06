package com.mytunes.models;

public class AccessTokenModel {

	private String accessToken;
	private String refreshToken;
	private boolean hasError;
	
	public AccessTokenModel() {
		this.hasError = true;
	}
	
	public AccessTokenModel(String access_token, String refresh_token) {
		this.accessToken = access_token;
		this.refreshToken = refresh_token;
		this.hasError = false;
	}
	
	public void setAccessToken(String access_token) {
		this.accessToken = access_token;
	}

	public String getAccessToken() {
		return accessToken;
	}
	
	public void setRefreshToken(String refresh_token) {
		this.refreshToken = refresh_token;
	}

	public String getRefreshToken() {
		return refreshToken;
	}
	
	public void setError(boolean has_error) {
		this.hasError = has_error;
	}
	
	public boolean getHasError() {
		return hasError;
	}
	
}
