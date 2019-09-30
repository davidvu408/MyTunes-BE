package com.mytunes.models;

public class RankedAlbumModel {

	String id;
	String name;
	//ImageModel[] imageObjects;
	Double weight;
	
	public RankedAlbumModel() { }
	
	public RankedAlbumModel(String id, String name, Double weight) {
		this.id = id;
		this.name = name;
		this.weight = weight;
	}
	
	public RankedAlbumModel setId(String id) {
		this.id = id;
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
	
	// Sort by weight
	// Identified by id
}
