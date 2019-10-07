package com.mytunes.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.springframework.stereotype.Service;

import com.mytunes.config.SpotifyConfig;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.specification.Album;
import com.wrapper.spotify.model_objects.specification.Artist;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.SavedAlbum;
import com.wrapper.spotify.model_objects.specification.SavedTrack;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.model_objects.specification.User;
import com.wrapper.spotify.requests.data.albums.GetSeveralAlbumsRequest;
import com.wrapper.spotify.requests.data.artists.GetSeveralArtistsRequest;
import com.wrapper.spotify.requests.data.library.GetCurrentUsersSavedAlbumsRequest;
import com.wrapper.spotify.requests.data.library.GetUsersSavedTracksRequest;
import com.wrapper.spotify.requests.data.personalization.simplified.GetUsersTopArtistsRequest;
import com.wrapper.spotify.requests.data.users_profile.GetCurrentUsersProfileRequest;

@Service
public class SpotifyService {

	public SpotifyService() {
	}

	public User getUserProfile(String accessToken) {
		SpotifyConfig.getInstance().setAccessToken(accessToken);
		GetCurrentUsersProfileRequest getCurrentUsersProfileRequest = SpotifyConfig.getInstance()
				.getCurrentUsersProfile().build();
		try {
			User user = getCurrentUsersProfileRequest.execute();
			return user;
		} catch (IOException | SpotifyWebApiException e) {
			System.out.println("Error: " + e.getMessage());
		}
		return null;
	}

	public ArrayList<String> getUsersTopArtists(String accessToken, String timeRange) {
		SpotifyConfig.getInstance().setAccessToken(accessToken);
		ArrayList<String> result = new ArrayList<String>();
		int offSet = 0;
		int resultLength = -1;
		do {
			try {
				GetUsersTopArtistsRequest getUsersTopArtistsRequest = SpotifyConfig.getInstance().getUsersTopArtists()
						.limit(50).offset(offSet).time_range(timeRange).build();
				if (!timeRange.equals("short_term") || !timeRange.equals("medium_term")
						|| !timeRange.equals("long_term")) {
					throw new SpotifyWebApiException("Invalid Time Range");
				}
				Paging<Artist> artistPaging = getUsersTopArtistsRequest.execute();
				resultLength = artistPaging.getTotal();
				for (Artist artist : artistPaging.getItems()) {
					result.add(artist.getName());
				}
			} catch (IOException | SpotifyWebApiException e) {
				System.out.println("Error: " + e.getMessage());
			}
			offSet += 50;
		} while (resultLength == 50);
		return result;
	}

	public ArrayList<Album> getSeveralAlbums(String accessToken, String[] albumIds) {
		ArrayList<Album> result = new ArrayList<Album>();
		SpotifyConfig.getInstance().setAccessToken(accessToken);
		int from = 0;
		int to = 20;

		int resultLength = -1;
		do {
			String[] subArr;
			if (albumIds.length - from >= 20) {
				subArr = Arrays.copyOfRange(albumIds, from, to);
			} else {
				subArr = Arrays.copyOfRange(albumIds, from, albumIds.length);
			}
			GetSeveralAlbumsRequest getSeveralAlbumsRequest = SpotifyConfig.getInstance().getSeveralAlbums(subArr)
					.build();
			try {
				Album[] albums = getSeveralAlbumsRequest.execute();
				resultLength = albums.length;
				for (Album album : albums) {
					result.add(album);
				}
			} catch (IOException | SpotifyWebApiException e) {
				System.out.println("Error: " + e.getMessage());
			}
			from += 20;
			to += 20;
		} while (resultLength == 20);
		return result;
	}

	public ArrayList<Artist> getSeveralArtists(String accessToken, String[] artistIDs) {
		ArrayList<Artist> result = new ArrayList<Artist>();
		SpotifyConfig.getInstance().setAccessToken(accessToken);

		int from = 0;
		int to = 50;
		int resultLength = -1;
		do {
			String[] subArr;
			if (artistIDs.length - from >= 50) {
				subArr = Arrays.copyOfRange(artistIDs, from, to);
			} else {
				subArr = Arrays.copyOfRange(artistIDs, from, artistIDs.length);
			}
			GetSeveralArtistsRequest getSeveralArtistsRequest = SpotifyConfig.getInstance().getSeveralArtists(subArr)
					.build();
			try {
				Artist[] artists = getSeveralArtistsRequest.execute();
				resultLength = artists.length;
				for (Artist artist : artists) {
					result.add(artist);
				}
			} catch (IOException | SpotifyWebApiException e) {
				System.out.println("Error: " + e.getMessage());
			}
			from += 20;
			to += 20;
		} while (resultLength == 50);
		return result;
	}

	public ArrayList<Album> getUserSavedAlbums(String accessToken) {
		SpotifyConfig.getInstance().setAccessToken(accessToken);
		ArrayList<Album> result = new ArrayList<Album>();
		int offSet = 0;
		int resultLength = -1;
		do {
			GetCurrentUsersSavedAlbumsRequest getCurrentUsersSavedAlbumsRequest = SpotifyConfig.getInstance()
					.getCurrentUsersSavedAlbums().limit(50).offset(offSet).build();
			try {
				Paging<SavedAlbum> savedAlbumPaging = getCurrentUsersSavedAlbumsRequest.execute();
				for (SavedAlbum savedAlbum : savedAlbumPaging.getItems()) {
					result.add(savedAlbum.getAlbum());
				}
			} catch (IOException | SpotifyWebApiException e) {
				System.out.println("Error: " + e.getMessage());
			}
			offSet += 50;
		} while (resultLength == 50);

		return result;
	}

	public ArrayList<Track> getUserSavedTracks(String accessToken) {
		SpotifyConfig.getInstance().setAccessToken(accessToken);
		ArrayList<Track> result = new ArrayList<Track>();
		int offSet = 50;
		int resultLength = -1;
		do {
			GetUsersSavedTracksRequest getUsersSavedTracksRequest = SpotifyConfig.getInstance().getUsersSavedTracks()
					.limit(50).offset(offSet).build();
			try {
				Paging<SavedTrack> savedTrackPaging = getUsersSavedTracksRequest.execute();
				resultLength = savedTrackPaging.getItems().length;
				for (SavedTrack savedTrack : savedTrackPaging.getItems()) {
					result.add(savedTrack.getTrack());
				}
			} catch (IOException | SpotifyWebApiException e) {
				System.out.println("Error: " + e.getMessage());
			}
			offSet += 50;
		} while (resultLength == 50);
		return result;
	}

}
