package com.nafarya.oktest;

public class PhotoItem {
	private String mUrlSmall;
	private String mUrlMedium;
	private String mUrlLarge;
	//TODO: maybe Long instead String ?
	private String id;
	private String userId;
	
	public String getUrl() {
		return mUrlLarge;
	}
	
	public String getUrlSmall() {
		return mUrlSmall;
	}
	
	public void setUrlSmall(String urlSmall) {
		mUrlSmall = urlSmall;
	}
	
	public String getUrlMedium() {
		return mUrlMedium;
	}
	
	public void setUrlMedium(String urlMedium) {
		mUrlMedium = urlMedium;
	}
	
	public String getUrlLarge() {
		return mUrlLarge;
	}
	
	public void setUrlLarge(String urlLarge) {
		mUrlLarge = urlLarge;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getUserId() {
		return userId;
	}
	
	public void setUserId(String userId) {
		this.userId = userId;
	}

}
