package com.willblaschko.android.alexa.interfaces.card;

import com.willblaschko.android.alexa.data.Directive;
import com.willblaschko.android.alexa.interfaces.AvsItem;

import java.util.List;

/**
 * Created by xblu on 2017/6/22.
 */

public class ListTemplate1 extends BaseCard {

	private List<Directive.ListIteam> listIteams;

	public List<Directive.ListIteam> getListIteams() {
		return listIteams;
	}

	public ListTemplate1(String token, Directive.Title title, Directive.ImageStructure skillIcon, List<Directive.ListIteam> listIteams) {
		super(token,title,skillIcon);
		this.listIteams = listIteams;
	}
}
