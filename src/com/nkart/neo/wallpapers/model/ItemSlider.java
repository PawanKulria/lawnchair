package com.nkart.neo.wallpapers.model;

public class ItemSlider {
	
	private String id, Name,Image, imageThumb,Link;

	public ItemSlider(String id, String name, String image, String imageThumb, String link) {
		this.id = id;
		Name = name;
		Image = image;
		this.imageThumb = imageThumb;
		Link = link;
	}

	public void setName(String Name) {
		this.Name = Name;
	}

	public String getName() {
		return Name;
	}
	
	public void setImage(String Image) {
		this.Image = Image;
	}

	public String getImage() {
		return Image;
	}
	
	public void setLink(String Link) {
		this.Link = Link;
	}

	public String getLink() {
		return Link;
	}

	public String getId() {
		return id;
	}

	public String getImageThumb() {
		return imageThumb;
	}
}
