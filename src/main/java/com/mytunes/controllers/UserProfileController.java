package com.mytunes.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mytunes.config.SpotifyConfig;
import com.mytunes.models.RankedAlbumModel;
import com.mytunes.models.RankedGenreModel;
import com.mytunes.services.SpotifyService;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.specification.Album;
import com.wrapper.spotify.model_objects.specification.Artist;
import com.wrapper.spotify.model_objects.specification.ArtistSimplified;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.model_objects.specification.User;
import com.wrapper.spotify.requests.data.personalization.simplified.GetUsersTopTracksRequest;

@RestController
@CrossOrigin
public class UserProfileController {

	@Autowired
	SpotifyService spotifyService;
	
	@RequestMapping(value = "/topTracks", method = RequestMethod.GET)
	public ArrayList<String> getUsersTopTracks(String accessToken, String timeRange) {
		SpotifyConfig.getInstance().setAccessToken(accessToken);
		ArrayList<String> result = new ArrayList<String>();

		GetUsersTopTracksRequest getUsersTopTracksRequest = SpotifyConfig.getInstance().getUsersTopTracks().limit(50)
				.offset(0).time_range(timeRange).build();

		try {
			Paging<Track> trackPaging = getUsersTopTracksRequest.execute();
			for (Track track : trackPaging.getItems()) {
				result.add(track.getName());
			}
		} catch (IOException | SpotifyWebApiException e) {
			System.out.println("Error: " + e.getMessage());
		}
		return result;
	}
	
	@RequestMapping(value = "/topGenres", method = RequestMethod.GET)
	public ArrayList<RankedGenreModel> getUserTopGenres(String accessToken) {
		
		// Get all the Users Saved Tracks
		ArrayList<Track> userSavedTracks = spotifyService.getUserSavedTracks(accessToken);
		// Get frequency count of associated Artists from Users Saved Tracks
		HashMap<String, Integer> artistsFreqCount = new HashMap<String, Integer>();
		for (Track track : userSavedTracks) {
			for (ArtistSimplified artist : track.getArtists()) {
				Integer count = artistsFreqCount.getOrDefault(artist.getId(), 0);
				artistsFreqCount.put(artist.getId(), count + 1);
			}
		}

		// For each Saved Track, get all the associated Artists
		String[] artistIDs = new String[artistsFreqCount.keySet().size()];
		int i = 0;
		for (String artistId : artistsFreqCount.keySet()) {
			artistIDs[i++] = artistId;
		}

		// For each of the associated artists, get their associated genres
		// The weight of each genre is +1 for the genre + the number of times the
		// artists showed up in Users Saved Tracks
		ArrayList<Artist> artists = spotifyService.getSeveralArtists(accessToken, artistIDs);
		HashMap<String, Integer> genreCounts = new HashMap<String, Integer>();
		for (Artist artist : artists) {
			for (String genre : artist.getGenres()) {
				Integer genreCount = genreCounts.getOrDefault(genre, 0);
				Integer artistCount = artistsFreqCount.get(artist.getId());
				genreCounts.put(genre, genreCount + 1 + artistCount);
			}
		}

		// Prepare return result
		ArrayList<RankedGenreModel> rankedGenres = new ArrayList<RankedGenreModel>();
		for (Entry<String, Integer> entry : genreCounts.entrySet()) {
			RankedGenreModel rankedGenreModel = new RankedGenreModel();
			rankedGenreModel.setName(entry.getKey());
			rankedGenreModel.setWeight(entry.getValue());
			rankedGenres.add(rankedGenreModel);
		}

		rankedGenres.sort(Collections.reverseOrder());
		return rankedGenres;
	}

	@RequestMapping(value = "/topArtists", method = RequestMethod.GET)
	public ArrayList<String> getUsersTopArtists(String accessToken, String timeRange) {
		return spotifyService.getUsersTopArtists(accessToken, timeRange);
	}
	
	@RequestMapping(value = "/userProfile", method = RequestMethod.GET)
	public User getUserProfile(String accessToken) {
		return spotifyService.getUserProfile(accessToken);
	}

	/**
	 * Favorite albums are calculated by: 1) User's saved albums (each album saved
	 * has weight of 1.0) 2) From User's saved songs, for each song that belongs to
	 * an Album, we calculate the percentage of songs the User has saved of an Album
	 * (0 - 1.0)
	 */
	@RequestMapping(value = "/topAlbums", method = RequestMethod.GET)
	public List<RankedAlbumModel> getUserTopAlbums(String accessToken, @RequestParam(required = false) Integer limit) {
		if (limit == null) {
			limit = 20;
		}
		HashMap<String, Double> rankedAlbums = new HashMap<String, Double>();
		calculatePercentageOfAlbumSavedFromLikedSongs(rankedAlbums, accessToken);

		for (Album album : spotifyService.getUserSavedAlbums(accessToken)) {
			Double count = rankedAlbums.getOrDefault(album.getId(), 0.0);
			rankedAlbums.put(album.getId(), count + 1.0);
		}

		String[] resultAlbumIds = new String[rankedAlbums.keySet().size()];
		int i = 0;
		for (String id : rankedAlbums.keySet()) {
			resultAlbumIds[i++] = id;
		}

		ArrayList<RankedAlbumModel> result = new ArrayList<RankedAlbumModel>();

		for (Album album : spotifyService.getSeveralAlbums(accessToken, resultAlbumIds)) {
			RankedAlbumModel rankedAlbumModel = new RankedAlbumModel();
			rankedAlbumModel.setWeight(rankedAlbums.get(album.getId()));
			rankedAlbumModel.setName(album.getName());

			// Use 2nd image URL but default onto 1st one
			if (album.getImages().length >= 1) {
				rankedAlbumModel.setAlbumCoverImgURL(album.getImages()[1].getUrl());
			} else {
				rankedAlbumModel.setAlbumCoverImgURL(album.getImages()[0].getUrl());
			}

			// Don't include Albums of type Singles
			if (album.getTracks().getTotal() != 1) {
				result.add(rankedAlbumModel);
			}
		}
		result.sort(Collections.reverseOrder());
		int endPoint = (limit > result.size()) ? result.size() : limit;
		return result.subList(0, endPoint);
	}

	// 1) From Users Saved Tracks, get super set of Album IDs and count of Tracks
	// User has saved from each album
	// 2) From each Album ID, put number of songs user has saved to total album
	// track count
	private void calculatePercentageOfAlbumSavedFromLikedSongs(HashMap<String, Double> rankedAlbums,
			String accessToken) {
		// For each track, add Album ID and increment count
		for (Track track : spotifyService.getUserSavedTracks(accessToken)) {
			Double count = rankedAlbums.getOrDefault(track.getAlbum().getId(), 0.0);
			rankedAlbums.put(track.getAlbum().getId(), count + 1.0);
		}

		// Get all album IDs in map
		String[] albumIds = new String[rankedAlbums.keySet().size()];
		int i = 0;
		for (String id : rankedAlbums.keySet()) {
			albumIds[i++] = id;
		}

		// Get ArrayList of Album objects
		ArrayList<Album> temp = spotifyService.getSeveralAlbums(accessToken, albumIds); // Albums are returned in order  they are requested
		i = 0;
		for (Entry<String, Double> entry : rankedAlbums.entrySet()) {
			rankedAlbums.put(entry.getKey(), entry.getValue() / temp.get(i++).getTracks().getTotal());
		}
	}
}
