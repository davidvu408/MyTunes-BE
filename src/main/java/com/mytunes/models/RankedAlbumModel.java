package com.mytunes.models;


import com.wrapper.spotify.model_objects.specification.Image;

public class RankedAlbumModel implements Comparable<RankedAlbumModel> {

	Double weight;
	String name;
	String albumCoverImgURL;
	
	public RankedAlbumModel() { }
	

	public Double getWeight() {
		return weight;
	}
	
	public String getName() {
		return name;
	}

	public String getAlbumCoverImgURL() {
		return albumCoverImgURL;
	}
	
	public RankedAlbumModel setAlbumCoverImgURL(String url) {
		this.albumCoverImgURL = url;
		return this;
	}
	
	public RankedAlbumModel setName(String name) {
		this.name = name;
		return this;
	}
	
	public RankedAlbumModel setWeight(Double weight) {
		this.weight = weight;
		return this;
	}



	@Override
	public int compareTo(RankedAlbumModel o) {
		return Double.compare(this.weight, o.weight);
	}
	
	
}
