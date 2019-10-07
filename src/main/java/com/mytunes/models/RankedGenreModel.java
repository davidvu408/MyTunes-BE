package com.mytunes.models;

public class RankedGenreModel implements Comparable<RankedGenreModel> {

	Integer weight;
	String name;

	public Integer getWeight() {
		return weight;
	}

	public void setWeight(Integer weight) {
		this.weight = weight;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int compareTo(RankedGenreModel o) {
		return Double.compare(this.weight, o.weight);
	}

}
