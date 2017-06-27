package com.willblaschko.android.alexa.interfaces.card;

import com.willblaschko.android.alexa.data.Directive;
import com.willblaschko.android.alexa.interfaces.AvsItem;

/**
 * Created by xblu on 2017/6/22.
 */

public class BaseCard extends AvsItem {

	private Directive.Title title;
	private Directive.ImageStructure skillIcon;


	public Directive.Title getTitle() {
		return title;
	}

	public Directive.ImageStructure getSkillIcon() {
		return skillIcon;
	}

	public BaseCard(String token, Directive.Title title, Directive.ImageStructure skillIcon) {
		super(token);
		this.title  = title;
		this.skillIcon = skillIcon;

	}
}
