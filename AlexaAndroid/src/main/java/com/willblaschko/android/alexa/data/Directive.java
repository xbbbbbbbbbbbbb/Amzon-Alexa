package com.willblaschko.android.alexa.data;

import android.text.TextUtils;
import android.text.style.LineHeightSpan;


import java.io.Serializable;
import java.util.List;

/**
 * A catch-all Directive to classify return responses from the Amazon Alexa v20160207 API Will handle calls to: <a
 * href="https://developer.amazon.com/public/solutions/alexa/alexa-voice-service/reference/speechrecognizer">Speech Recognizer</a> <a
 * href="https://developer.amazon.com/public/solutions/alexa/alexa-voice-service/reference/alerts">Alerts</a> <a
 * href="https://developer.amazon.com/public/solutions/alexa/alexa-voice-service/reference/audioplayer">Audio Player</a> <a
 * href="https://developer.amazon.com/public/solutions/alexa/alexa-voice-service/reference/playbackcontroller">Playback Controller</a> <a
 * href="https://developer.amazon.com/public/solutions/alexa/alexa-voice-service/reference/speaker">Speaker</a> <a
 * href="https://developer.amazon.com/public/solutions/alexa/alexa-voice-service/reference/speechsynthesizer">Speech Synthesizer</a> <a
 * href="https://developer.amazon.com/public/solutions/alexa/alexa-voice-service/reference/system">System</a>
 *
 * @author wblaschko on 5/6/16.
 */
public class Directive {

	private Header  header;
	private Payload payload;

	private static final String TYPE_SPEAK           = "Speak";
	private static final String TYPE_PLAY            = "Play";
	private static final String TYPE_SET_ALERT       = "SetAlert";
	private static final String TYPE_DELETE_ALERT    = "DeleteAlert";
	private static final String TYPE_SET_VOLUME      = "SetVolume";
	private static final String TYPE_ADJUST_VOLUME   = "AdjustVolume";
	private static final String TYPE_SET_MUTE        = "SetMute";
	private static final String TYPE_EXPECT_SPEECH   = "ExpectSpeech";
	private static final String TYPE_MEDIA_PLAY      = "PlayCommandIssued";
	private static final String TYPE_MEDIA_PAUSE     = "PauseCommandIssued";
	private static final String TYPE_MEDIA_NEXT      = "NextCommandIssued";
	private static final String TYPE_MEDIA_PREVIOUS  = "PreviousCommandIssue";
	private static final String TYPE_EXCEPTION       = "Exception";
	private static final String TYPE_OPEN            = "open";
	private static final String TYPE_RENDER_TEMPLATE = "RenderTemplate";

	private static final String PLAY_BEHAVIOR_REPLACE_ALL      = "REPLACE_ALL";
	private static final String PLAY_BEHAVIOR_ENQUEUE          = "ENQUEUE";
	private static final String PLAY_BEHAVIOR_REPLACE_ENQUEUED = "REPLACE_ENQUEUED";

	//DIRECTIVE TYPES

	public boolean isTypeSpeak() {
		return TextUtils.equals(header.getName(), TYPE_SPEAK);
	}

	public boolean isTypePlay() {
		return TextUtils.equals(header.getName(), TYPE_PLAY);
	}

	public boolean isTypeSetAlert() {
		return TextUtils.equals(header.getName(), TYPE_SET_ALERT);
	}

	public boolean isTypeDeleteAlert() {
		return TextUtils.equals(header.getName(), TYPE_DELETE_ALERT);
	}

	public boolean isTypeSetVolume() {
		return TextUtils.equals(header.getName(), TYPE_SET_VOLUME);
	}

	public boolean isTypeAdjustVolume() {
		return TextUtils.equals(header.getName(), TYPE_ADJUST_VOLUME);
	}

	public boolean isTypeSetMute() {
		return TextUtils.equals(header.getName(), TYPE_SET_MUTE);
	}

	public boolean isTypeExpectSpeech() {
		return TextUtils.equals(header.getName(), TYPE_EXPECT_SPEECH);
	}

	public boolean isTypeMediaPlay() {
		return TextUtils.equals(header.getName(), TYPE_MEDIA_PLAY);
	}

	public boolean isTypeMediaPause() {
		return TextUtils.equals(header.getName(), TYPE_MEDIA_PAUSE);
	}

	public boolean isTypeMediaNext() {
		return TextUtils.equals(header.getName(), TYPE_MEDIA_NEXT);
	}

	public boolean isTypeMediaPrevious() {
		return TextUtils.equals(header.getName(), TYPE_MEDIA_PREVIOUS);
	}

	public boolean isTypeException() {
		return TextUtils.equals(header.getName(), TYPE_EXCEPTION);
	}

	public boolean isTypeOpen() {
		return TextUtils.equals(header.getName(), TYPE_OPEN);
	}

	public boolean isTypeRenderTemplate() {
		return TextUtils.equals(header.getName(), TYPE_RENDER_TEMPLATE);
	}

	//PLAY BEHAVIORS

	public boolean isPlayBehaviorReplaceAll() {
		return TextUtils.equals(payload.getPlayBehavior(), PLAY_BEHAVIOR_REPLACE_ALL);
	}

	public boolean isPlayBehaviorEnqueue() {
		return TextUtils.equals(payload.getPlayBehavior(), PLAY_BEHAVIOR_ENQUEUE);
	}

	public boolean isPlayBehaviorReplaceEnqueued() {
		return TextUtils.equals(payload.getPlayBehavior(), PLAY_BEHAVIOR_REPLACE_ENQUEUED);
	}


	public Header getHeader() {
		return header;
	}

	public Payload getPayload() {
		return payload;
	}

	public static class Header {

		String namespace;
		String name;
		String messageId;
		String dialogRequestId;

		public String getNamespace() {
			return namespace;
		}

		public String getName() {
			return name;
		}

		public String getMessageId() {
			return messageId;
		}

		public String getDialogRequestId() {
			return dialogRequestId;
		}
	}

	public static class Payload {


		String    url;
		String    format;
		String    token;
		String    type;
		String    scheduledTime;
		String    playBehavior;
		AudioItem audioItem;
		long      volume;
		boolean   mute;
		long      timeoutInMilliseconds;
		String    description;
		String    code;
		String    activityName;
		String    packageName;
		Title     title;
		ImageStructure skillIcon;
		String    textField;
		ImageStructure image;
		List<ListIteam> listItems;
		String currentWeather;
		ImageStructure currentWeatherIcon;
		Temperature highTemperature;
		Temperature lowTemperature;
		List<WeatherForecast> weatherForecast;
		MusicContent content;
		List<MusicControl> controls;

		public List<MusicControl> getControls() {
			return controls;
		}

		public MusicContent getContent() {
			return content;
		}

		public String getCurrentWeather(){
			return currentWeather;
		}

		public ImageStructure getCurrentWeatherIcon(){
			return currentWeatherIcon;
		}

		public Temperature getHighTemperature(){
			return highTemperature;
		}

		public Temperature getLowTemperature(){
			return  lowTemperature;
		}

		public List<WeatherForecast> getWeatherForecast(){
			return weatherForecast;
		}

		public String getActivityName() {
			return activityName;
		}

		public String getPackageName() {
			return packageName;
		}

		public String getUrl() {
			return url;
		}

		public String getFormat() {
			return format;
		}

		public String getToken() {
			if (token == null) {
				//sometimes we need to return the stream tokens, not the top level tokens
				if (audioItem != null && audioItem.getStream() != null) {
					return audioItem.getStream().getToken();
				}
			}
			return token;
		}

		public String getType() {
			return type;
		}

		public String getScheduledTime() {
			return scheduledTime;
		}

		public String getPlayBehavior() {
			return playBehavior;
		}

		public AudioItem getAudioItem() {
			return audioItem;
		}

		public long getVolume() {
			return volume;
		}

		public boolean isMute() {
			return mute;
		}

		public long getTimeoutInMilliseconds() {
			return timeoutInMilliseconds;
		}

		public String getDescription() {
			return description;
		}

		public String getCode() {
			return code;
		}

		public String getTextField() {
			return textField;
		}

		public Title getTitle() {
			return title;
		}

		public ImageStructure getSkillIcon() {
			return skillIcon;
		}

		public ImageStructure getImage(){
			return image;
		}

		public List<ListIteam> getListItems(){
			return listItems;
		}
	}

	public static class MusicControl{
		String type;
		String name;
		boolean enabled;
		boolean selected;

		public String getType() {
			return type;
		}

		public String getName() {
			return name;
		}

		public boolean isEnabled() {
			return enabled;
		}

		public boolean isSelected() {
			return selected;
		}
	}

	public static  class MusicContent{
		String title;
		String titleSubtext1;
		String titleSubtext2;
		String header;
		String headerSubtext1;
		long mediaLengthInMilliseconds;
		ImageStructure art;
		MusicProvider provider;

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getTitleSubtext1() {
			return titleSubtext1;
		}

		public void setTitleSubtext1(String titleSubtext1) {
			this.titleSubtext1 = titleSubtext1;
		}

		public String getTitleSubtext2() {
			return titleSubtext2;
		}

		public void setTitleSubtext2(String titleSubtext2) {
			this.titleSubtext2 = titleSubtext2;
		}

		public String getHeader() {
			return header;
		}

		public void setHeader(String header) {
			this.header = header;
		}

		public String getHeaderSubtext1() {
			return headerSubtext1;
		}

		public void setHeaderSubtext1(String headerSubtext1) {
			this.headerSubtext1 = headerSubtext1;
		}

		public long getMediaLengthInMilliseconds() {
			return mediaLengthInMilliseconds;
		}

		public void setMediaLengthInMilliseconds(long mediaLengthInMilliseconds) {
			this.mediaLengthInMilliseconds = mediaLengthInMilliseconds;
		}

		public ImageStructure getArt() {
			return art;
		}

		public void setArt(ImageStructure art) {
			this.art = art;
		}

		public MusicProvider getProvider() {
			return provider;
		}

		public void setProvider(MusicProvider provider) {
			this.provider = provider;
		}
	}

	public static class MusicProvider{
		String name;
		ImageStructure logo;

		public String getName(){
			return name;
		}

		public ImageStructure getLogo(){
			return logo;
		}
	}

	public static class WeatherForecast implements Serializable{
		ImageStructure image;
		String day;
		String date;
		String highTemperature;
		String lowTemperature;

		public ImageStructure getImage(){
			return image;
		}

		public String getDay(){
			return day;
		}

		public String getDate(){
			return date;
		}

		public String getHighTemperature(){
			return highTemperature;
		}

		public String getLowTemperature(){
			return lowTemperature;
		}
	}

	public static class Temperature implements Serializable{
		String value;
		ImageStructure arrow;

		public String getValue(){
			return value;
		}

		public ImageStructure getArrow(){
			return arrow;
		}
	}

	public static class ListIteam implements Serializable{

		String leftTextField;
		String rightTextField;
		public String getLeftTextField(){
			return leftTextField;
		}

		public String getRightTextField(){
			return rightTextField;
		}
	}

	public static class Title implements Serializable{

		String mainTitle;
		String subTitle;

		public String getMainTitle() {
			return mainTitle;
		}

		public String getSubTitle() {
			return subTitle;
		}

		public void setMainTitle(String mainTitle) {
			this.mainTitle = mainTitle;
		}

		public void setSubTitle(String subTitle) {
			this.subTitle = subTitle;
		}
	}

	public static class ImageStructure implements Serializable{

		String        contentDescription;
		List<Sources> sources;

		public String getContentDescription() {
			return contentDescription;
		}

		public List<Sources> getSources() {
			return sources;
		}
	}

	public static class Sources implements Serializable{

		String url;
		String size;
		long   widthPixels;
		long   heightPixels;

		public void setUrl(String url) {
			this.url = url;
		}

		public void setSize(String size) {
			this.size = size;
		}

		public void setWidthPixels(long widthPixels) {
			this.widthPixels = widthPixels;
		}

		public void setHeightPixels(long heightPixels) {
			this.heightPixels = heightPixels;
		}

		public String getUrl() {
			return url;
		}

		public String getSize() {
			return size;
		}

		public long getWidthPixels() {
			return widthPixels;
		}

		public long getHeightPixels() {
			return heightPixels;
		}
	}

	public static class AudioItem {

		String audioItemId;
		Stream stream;

		public String getAudioItemId() {
			return audioItemId;
		}

		public Stream getStream() {
			return stream;
		}
	}

	public static class Stream {

		String url;
		String streamFormat;
		long   offsetInMilliseconds;
		String expiryTime;
		String token;
		String expectedPreviousToken;
		//todo progressReport


		public String getUrl() {
			return url;
		}

		public String getStreamFormat() {
			return streamFormat;
		}

		public long getOffsetInMilliseconds() {
			return offsetInMilliseconds;
		}

		public String getExpiryTime() {
			return expiryTime;
		}

		public String getToken() {
			return token;
		}

		public String getExpectedPreviousToken() {
			return expectedPreviousToken;
		}
	}

	public static class DirectiveWrapper {

		Directive directive;

		public Directive getDirective() {
			return directive;
		}
	}
}
