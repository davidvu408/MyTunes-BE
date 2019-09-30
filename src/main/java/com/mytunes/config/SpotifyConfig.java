package com.mytunes.config;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.SpotifyHttpManager;

/**
 * Singleton object to configure Spotify API
 */
public class SpotifyConfig {
	
	private static SpotifyApi spotifyApi = new SpotifyApi.Builder()
	        .setClientId("251226c738e049e2bdb0d35f3dcc8747")
	        .setClientSecret("9d21e44e1a04421f9913d954fb0f5045")
	        .setRedirectUri(SpotifyHttpManager.makeUri("https://davidvu408.github.io/MyTunes-FE/callback.html"))
	        .build(); 
	
	private SpotifyConfig() {}
	
	public static SpotifyApi getInstance() {
		return spotifyApi;
	}
}
