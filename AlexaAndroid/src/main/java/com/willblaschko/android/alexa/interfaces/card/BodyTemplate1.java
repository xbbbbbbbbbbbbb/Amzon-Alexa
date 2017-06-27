package com.willblaschko.android.alexa.interfaces.card;

import com.willblaschko.android.alexa.data.Directive.Title;
import com.willblaschko.android.alexa.data.Directive.ImageStructure;
import com.willblaschko.android.alexa.interfaces.AvsItem;

/**
 * Created by xblu on 2017/6/22.
 */

public class BodyTemplate1 extends BaseCard {

	private String textField;

	public BodyTemplate1(String token,Title title,ImageStructure skillIcon,String textField) {
		super(token,title,skillIcon);
		this.textField = textField;
	}

	public String getTextField() {
		return textField;
	}
}
