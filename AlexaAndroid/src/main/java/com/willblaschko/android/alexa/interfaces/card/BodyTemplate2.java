package com.willblaschko.android.alexa.interfaces.card;

import com.willblaschko.android.alexa.data.Directive;

/**
 * Created by xblu on 2017/6/22.
 */

public class BodyTemplate2 extends BaseCard {

	private Directive.ImageStructure image;
	private String textField;

	public BodyTemplate2(String token, Directive.Title title, Directive.ImageStructure skillIcon, String textField,Directive.ImageStructure image) {
		super(token, title, skillIcon);
		this.image = image;
		this.textField = textField;
	}

	public Directive.ImageStructure getImage() {
		return image;
	}
	public String getTextField() {
		return textField;
	}
}
