package com.yifang.ivoice;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.willblaschko.android.alexa.interfaces.AvsItem;
import com.willblaschko.android.alexa.interfaces.card.BodyTemplate1;
import com.willblaschko.android.alexa.interfaces.card.BodyTemplate2;

/**
 * Created by xblu on 2017/6/22.
 */

public class BodyTemplateActivity extends Activity {

	private final String TAG = "BodyTemplateActivity";
	private TextView  mainTitle;
	private TextView  subTitle;
	private TextView  textField;
	private ImageView skillIcon;
	private ImageView image;
	private AvsItem   card;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activitya_body);
		card = (AvsItem) getIntent().getSerializableExtra(MainActivity.ITEM);
		initView();
		setView();
	}

	private void initView() {
		mainTitle = (TextView) findViewById(R.id.card_main_title);
		subTitle = (TextView) findViewById(R.id.card_subtitle);
		textField = (TextView) findViewById(R.id.text_field);
		skillIcon = (ImageView) findViewById(R.id.skill_icon);
		image = (ImageView) findViewById(R.id.image);
	}

	private void setView() {

		if (card instanceof BodyTemplate1) {
			Log.d(TAG, "BodyTemplate1");
			BodyTemplate1 bodyTemplate1 = (BodyTemplate1) card;
			if(bodyTemplate1.getSkillIcon() != null){
				Picasso.with(this).load(bodyTemplate1.getSkillIcon().getSources().get(0).getUrl()).into(skillIcon);
			}else {
				skillIcon.setVisibility(View.GONE);
			}
			mainTitle.setText(bodyTemplate1.getTitle().getMainTitle());
			subTitle.setText(bodyTemplate1.getTitle().getSubTitle());
			textField.setText(bodyTemplate1.getTextField());
		} else if (card instanceof BodyTemplate2) {
			Log.d(TAG, "BodyTemplate2");
			BodyTemplate2 bodyTemplate2 = (BodyTemplate2) card;
			if(bodyTemplate2.getSkillIcon() != null){
				Picasso.with(this).load(bodyTemplate2.getSkillIcon().getSources().get(0).getUrl()).into(skillIcon);
			}else {
				skillIcon.setVisibility(View.GONE);
			}
			mainTitle.setText(bodyTemplate2.getTitle().getMainTitle());
			subTitle.setText(bodyTemplate2.getTitle().getSubTitle());
			textField.setText(bodyTemplate2.getTextField());
			image.setVisibility(View.VISIBLE);
			Picasso.with(this).load(bodyTemplate2.getImage().getSources().get(1).getUrl()).into(image);
		}
	}
}
