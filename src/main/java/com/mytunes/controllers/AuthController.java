package com.mytunes.controllers;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mytunes.config.SpotifyConfig;
import com.mytunes.models.AccessTokenModel;
import com.mytunes.models.RankedAlbumModel;
import com.mytunes.services.SpotifyService;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.SpotifyHttpManager;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import com.wrapper.spotify.model_objects.specification.Album;
import com.wrapper.spotify.model_objects.specification.Artist;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.SavedAlbum;
import com.wrapper.spotify.model_objects.specification.SavedTrack;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;
import com.wrapper.spotify.requests.data.library.GetCurrentUsersSavedAlbumsRequest;
import com.wrapper.spotify.requests.data.library.GetUsersSavedTracksRequest;
import com.wrapper.spotify.requests.data.personalization.simplified.GetUsersTopArtistsRequest;
import com.wrapper.spotify.requests.data.personalization.simplified.GetUsersTopTracksRequest;

@RestController
@CrossOrigin
public class AuthController {
	
	@Autowired
	SpotifyService spotifyService;
	  
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
		AuthorizationCodeRequest authorizationCodeRequest = SpotifyConfig.getInstance()
				.authorizationCode(authCode)
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
	
	@RequestMapping(value = "/topTracks", method = RequestMethod.GET)
	public ArrayList<String> getUsersTopTracks(String accessToken, String timeRange) {
		SpotifyConfig.getInstance().setAccessToken(accessToken);
		ArrayList<String> result = new ArrayList<String>();

		GetUsersTopTracksRequest getUsersTopTracksRequest = SpotifyConfig.getInstance().getUsersTopTracks()
		          .limit(50)
		          .offset(0)
		          .time_range(timeRange)
		          .build();
		
		try {
	      Paging<Track> trackPaging = getUsersTopTracksRequest.execute();
	      for(Track track : trackPaging.getItems()) {
	    	  result.add(track.getName());
	      }
	    } catch (IOException | SpotifyWebApiException e) {
	      System.out.println("Error: " + e.getMessage());
	    }
		return result;
	}
	
	@RequestMapping(value = "/topArtists", method = RequestMethod.GET)
	public ArrayList<String> getUsersTopArtists(String accessToken, String timeRange) {
		return spotifyService.getUsersTopArtists(accessToken, timeRange);
	}
	
	@RequestMapping(value = "/trackCount", method = RequestMethod.GET)
	public Integer getTrackCount(String accessToken, String albumId) {
		return spotifyService.getAlbumTrackCount(accessToken, albumId);
	}
	
	@RequestMapping(value = "/topAlbums", method = RequestMethod.GET)
	public Album[] getUserTopAlbums(String accessToken) {
		
		//ArrayList<RankedAlbumModel> rankedAlbums = new ArrayList<RankedAlbumModel>();
		
		HashMap<String, Double> rankedAlbums = new HashMap<String, Double>();
		
		// A saved album has weight of 1.0
		ArrayList<Album> savedAlbums = spotifyService.getUserSavedAlbums(accessToken);
		for(Album album : savedAlbums) {
			Double count = rankedAlbums.getOrDefault(album.getId(), 0.0);
			rankedAlbums.put(album.getId(), count + 1);
		}
		
		// Calculate percentage of saved songs per album (0.0 - 1.0) 
		
		// Key: Album ID, Value: Frequency Count
		HashMap<String, Double> albumFreqCountFromSavedTracks = new HashMap<String, Double>(); 
		ArrayList<Track> savedTracks = spotifyService.getUserSavedTracks(accessToken);
		
		// Get Album Frequency Count from Saved Tracks
		for(Track track : savedTracks) {
			Double count = albumFreqCountFromSavedTracks.getOrDefault(track.getAlbum().getId(), 0.0);
			albumFreqCountFromSavedTracks.put(track.getAlbum().getId(), count + 1);
		}
		
		// Get percentage of Saved Tracks from each Album and add to rankedAlbums
		for(Map.Entry<String, Double> entry : albumFreqCountFromSavedTracks.entrySet()) {
			String albumId = entry.getKey();
			Integer albumCount = spotifyService.getAlbumTrackCount(accessToken, albumId);
			
			Double weightFromAlbumPercentage = entry.getValue() / albumCount; // Ratio of Saved Tracks per Album
			Double albumWeight = rankedAlbums.getOrDefault(albumId, 0.0); // Current weight of Album ID in rankedAlbums
			rankedAlbums.put(albumId, albumWeight + weightFromAlbumPercentage); // Add new weight
		}
		
		
		 String[] albumIds = new String[rankedAlbums.keySet().size()];
		 int i = 0;
		 for(String albumId : rankedAlbums.keySet()) {
			 albumIds[i++] = albumId;
		 }
		 Album[] rankedAlbumsArr = spotifyService.getSeveralAlbums(accessToken, albumIds);
		 
		return rankedAlbumsArr;
	}
	
}

