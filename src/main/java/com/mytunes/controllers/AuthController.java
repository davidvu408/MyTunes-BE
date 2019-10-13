package com.mytunes.controllers;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.mytunes.config.SpotifyConfig;
import com.mytunes.models.AccessTokenModel;
import com.mytunes.repositories.User;
import com.mytunes.repositories.UserRepository;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;

@RestController
@CrossOrigin
public class AuthController {
	
	@Autowired
	private UserRepository userRepository;

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
	
	@RequestMapping(value = "/register", method = RequestMethod.POST)
	public Map<String, String> register(@RequestBody User user) {
		if(user.getUserId() == null || user.getPassword() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Response Body");
		}
		
		Optional<User> queriedUser = userRepository.findById(user.getUserId());
		if(queriedUser.isPresent()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
		} else {
			userRepository.save(user);
		}
		
		HashMap<String, String> result = new HashMap<String, String>();
		result.put("status", "200");
		result.put("message", "User successfully saved");
		return result;
	}

}
