package com.mytunes.services;

import java.io.IOException;
import java.util.ArrayList;

import org.springframework.stereotype.Service;

import com.mytunes.config.SpotifyConfig;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.specification.Album;
import com.wrapper.spotify.model_objects.specification.Artist;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.SavedAlbum;
import com.wrapper.spotify.model_objects.specification.SavedTrack;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.model_objects.specification.TrackSimplified;
import com.wrapper.spotify.requests.data.albums.GetAlbumsTracksRequest;
import com.wrapper.spotify.requests.data.albums.GetSeveralAlbumsRequest;
import com.wrapper.spotify.requests.data.library.GetCurrentUsersSavedAlbumsRequest;
import com.wrapper.spotify.requests.data.library.GetUsersSavedTracksRequest;
import com.wrapper.spotify.requests.data.personalization.simplified.GetUsersTopArtistsRequest;

@Service
public class SpotifyService {

	public SpotifyService() {
	}

	public ArrayList<String> getUsersTopArtists(String accessToken, String timeRange) {
		SpotifyConfig.getInstance().setAccessToken(accessToken);
		ArrayList<String> result = new ArrayList<String>();
		int offSet = 0;
		int resultLength = -1;
		do {
			try {
				GetUsersTopArtistsRequest getUsersTopArtistsRequest = SpotifyConfig.getInstance().getUsersTopArtists()
						.limit(50).offset(offSet).time_range(timeRange)
						.build();
				if (!timeRange.equals("short_term") || !timeRange.equals("medium_term") || !timeRange.equals("long_term")) {
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
	
	public Album[] getSeveralAlbums(String accessToken, String[] albumIds) {
		SpotifyConfig.getInstance().setAccessToken(accessToken);
		GetSeveralAlbumsRequest getSeveralAlbumsRequest = SpotifyConfig.getInstance().getSeveralAlbums(albumIds)
				.build();
		try {
			Album[] albums = getSeveralAlbumsRequest.execute();
			return albums;
		} catch (IOException | SpotifyWebApiException e) {
			System.out.println("Error: " + e.getMessage());
		}
		return null;
	}

	public Integer getAlbumTrackCount(String accessToken, String albumId) {
		SpotifyConfig.getInstance().setAccessToken(accessToken);
		int offSet = 0;
		int resultLength = -1;
		int totalTrackCount = 0;
		do {
			GetAlbumsTracksRequest getAlbumsTracksRequest = SpotifyConfig.getInstance().getAlbumsTracks(albumId)
					.limit(50).offset(offSet).build();
			try {
				Paging<TrackSimplified> trackSimplifiedPaging = getAlbumsTracksRequest.execute();
				resultLength = trackSimplifiedPaging.getTotal();
				totalTrackCount += resultLength;
			} catch (IOException | SpotifyWebApiException e) {
				System.out.println("Error: " + e.getMessage());
			}
			offSet += 50;
		} while (resultLength == 50);
		return totalTrackCount;
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
				.limit(50)
				.offset(offSet)
				.build();
			try {
				Paging<SavedTrack> savedTrackPaging = getUsersSavedTracksRequest.execute();
				resultLength = savedTrackPaging.getItems().length;
				for(SavedTrack savedTrack : savedTrackPaging.getItems()) {
					result.add(savedTrack.getTrack());
				}
			} catch (IOException | SpotifyWebApiException e) {
			    System.out.println("Error: " + e.getMessage());
			}
			offSet += 50;
		} while(resultLength == 50);
		return result;
	}
}