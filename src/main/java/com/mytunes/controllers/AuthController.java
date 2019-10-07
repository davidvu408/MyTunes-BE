package com.mytunes.controllers;

import java.io.IOException;
import java.net.URI;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mytunes.config.SpotifyConfig;
import com.mytunes.models.AccessTokenModel;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;

@RestController
@CrossOrigin
public class AuthController {

	@RequestMapping("/")
	public String index() {
		return "HELLO WORLD!";
	}

	@RequestMapping(value = "/spotify-api/prompt-user", method = RequestMethod.GET)
	public String promptUser() {
		AuthorizationCodeUriRequest authorizationCodeUriRequest = SpotifyConfig.getInstance().authorizationCodeUri()
				.scope("user-library-read,user-top-read").build();
		final URI uri = authorizationCodeUriRequest.execute();
		return uri.toString();
	}

	@RequestMapping(value = "/spotify-api/access-token", method = RequestMethod.GET)
	public AccessTokenModel accessToken(String authCode) {
		AuthorizationCodeRequest authorizationCodeRequest = SpotifyConfig.getInstance().authorizationCode(authCode)
				.build();
		AccessTokenModel accessTokenModel = new AccessTokenModel();
		try {
			AuthorizationCodeCredentials authorizationCodeCredentials = authorizationCodeRequest.execute();
			accessTokenModel.setAccessToken(authorizationCodeCredentials.getAccessToken());
			accessTokenModel.setRefreshToken(authorizationCodeCredentials.getRefreshToken());
			accessTokenModel.setError(false);
		} catch (IOException | SpotifyWebApiException e) {
			accessTokenModel.setError(true);
			System.out.println("Error: " + e.getMessage());
		}
		return accessTokenModel;
	}

}
