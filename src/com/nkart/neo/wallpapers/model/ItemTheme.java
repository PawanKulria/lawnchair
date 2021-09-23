package com.nkart.neo.wallpapers.model;

public class ItemTheme {

	private String ThemeId;
	private String ThemUrl; 
	private String ThemeImag;
	private String ThemeImagThumb;
	private String ThemeView;
	private String ThemeName;

	public ItemTheme(String themeId, String themeName, String themUrl, String themeImag, String themeImagThumb) {
		ThemeId = themeId;
		ThemUrl = themUrl;
		ThemeImag = themeImag;
		ThemeImagThumb = themeImagThumb;
		ThemeName = themeName;
	}
	public String getName() {
		return ThemeName;
	}

	public void setName(String ThemeName) {
		this.ThemeName = ThemeName;
	}

	public String getThemeId() {
		return ThemeId;
	}

	public void setThemeId(String ThemeId) {
		this.ThemeId = ThemeId;
	}

	public String getThemUrl() {
		return ThemUrl;
	}

	public void setThemUrl(String ThemUrl) {
		this.ThemUrl = ThemUrl;
	}

	public String getThemeImag() {
		return ThemeImag;
	}

	public void setThemeImag(String ThemeImag) {
		this.ThemeImag = ThemeImag;
	}
	
	public String getThemeView() {
		return ThemeView;
	}

	public void setThemeView(String ThemeView) {
		this.ThemeView = ThemeView;
	}

	public String getThemeImagThumb() {
		return ThemeImagThumb;
	}
}