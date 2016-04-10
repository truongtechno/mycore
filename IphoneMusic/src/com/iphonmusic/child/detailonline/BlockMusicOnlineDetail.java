package com.iphonmusic.child.detailonline;

import java.util.ArrayList;
import java.util.Random;

import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.iphonmusic.R;
import com.iphonmusic.base.manager.BaseManager;
import com.iphonmusic.config.Config;
import com.iphonmusic.config.Instance;
import com.iphonmusic.config.Rconfig;
import com.iphonmusic.config.Utilities;
import com.iphonmusic.entity.EntitySong;
import com.iphonmusic.entity.EntityZingMp3;
import com.iphonmusic.style.floatingbutton.FloatingActionButton;
import com.iphonmusic.style.floatingbutton.FloatingActionsMenu;

public class BlockMusicOnlineDetail implements DelegateMusicOnlineDetail,
		OnSeekBarChangeListener, OnCompletionListener {

	private View rootView;

	private TextView txt_name;
	private ImageView img_play;
	private ImageView img_next;
	private ImageView img_previous;
	private TextView txt_start;
	private TextView txt_end;
	private SeekBar seekBar;
	private ImageView img_detail;

	private Handler mHandler = new Handler();;
	private Utilities utilities = new Utilities();

	protected FloatingActionsMenu mMultipleActions;
	private Context mContext;

	protected FloatingActionButton btn_share;
	protected FloatingActionButton btn_repeat;
	protected ArrayList<FloatingActionButton> mListButton;

	public BlockMusicOnlineDetail(View view) {
		this.rootView = view;
		mContext = BaseManager.getIntance().getCurrentContext();
		initView();
	}

	public void setOnPlayListener(OnClickListener listener) {
		img_play.setOnClickListener(listener);
	}

	public void setOnNextListener(OnClickListener listener) {
		img_next.setOnClickListener(listener);
	}

	public void setOnPreviousListener(OnClickListener listener) {
		img_previous.setOnClickListener(listener);
	}

	private void initView() {
		txt_name = (TextView) rootView.findViewById(Rconfig.getInstance().id(
				"txt_name"));
		txt_start = (TextView) rootView.findViewById(Rconfig.getInstance().id(
				"txt_time_start"));
		txt_end = (TextView) rootView.findViewById(Rconfig.getInstance().id(
				"txt_time_end"));
		img_play = (ImageView) rootView.findViewById(Rconfig.getInstance().id(
				"img_play"));
		img_next = (ImageView) rootView.findViewById(Rconfig.getInstance().id(
				"img_next"));
		img_previous = (ImageView) rootView.findViewById(Rconfig.getInstance()
				.id("img_previous"));
		seekBar = (SeekBar) rootView.findViewById(Rconfig.getInstance().id(
				"seekbar"));
		img_detail = (ImageView) rootView.findViewById(Rconfig.getInstance()
				.id("img_view"));
		initFloatButton();
		BaseManager.getIntance().getPlayer().setOnCompletionListener(this);
		seekBar.setOnSeekBarChangeListener(this);
		seekBar.setProgress(0);
		seekBar.setMax(100);

		// Updating progress bar
		updateProgressBar();
	}

	private void initFloatButton() {
		mMultipleActions = (FloatingActionsMenu) rootView.findViewById(Rconfig
				.getInstance().id("more_plugins_action"));
		mMultipleActions.createButton(mContext, Color.parseColor("#3498DB"),
				Color.parseColor("#3498DB"), Color.parseColor("#FFFFFF"));
		mListButton = new ArrayList<FloatingActionButton>();
		// btn share
		btn_share = new FloatingActionButton(mContext);
		btn_share.setColorNormal(Color.parseColor("#FFFFFF"));
		btn_share.setColorPressed(Color.parseColor("#f4f4f4"));
		btn_share.setIcon(Rconfig.getInstance().drawable("ic_share"));
		mListButton.add(btn_share);
		// btn repeat
		btn_repeat = new FloatingActionButton(mContext);
		btn_repeat.setColorNormal(Color.parseColor("#FFFFFF"));
		btn_repeat.setColorPressed(Color.parseColor("#f4f4f4"));
		if (Config.getInstance().getIsRepeat()) {
			btn_repeat.setIcon(Rconfig.getInstance().drawable("ic_repeated"));
		} else {
			btn_repeat.setIcon(Rconfig.getInstance().drawable("ic_repeat"));
		}
		mListButton.add(btn_repeat);

		for (int i = 0; i < mListButton.size(); i++) {
			mMultipleActions.addButton(mListButton.get(i));
		}

		btn_share.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

			}
		});
		btn_repeat.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (Config.getInstance().getIsRepeat()) {
					btn_repeat.setIcon(Rconfig.getInstance().drawable(
							"ic_repeat"));
					Config.getInstance().setRepeat(false);
				} else {
					btn_repeat.setIcon(Rconfig.getInstance().drawable(
							"ic_repeated"));
					Config.getInstance().setRepeat(true);
				}
			}
		});
	}

	@Override
	public void updateView(EntityZingMp3 zingMp3,boolean isPlay) {
		txt_name.setText(zingMp3.getzTitle() + " - " + zingMp3.getzArtist());
		if(isPlay){
			img_play.setImageResource(Rconfig.getInstance().drawable("ic_play"));
		}else{
			img_play.setImageResource(Rconfig.getInstance().drawable("ic_pause"));
		}
		Glide.with(mContext).load(zingMp3.getzAvatar()).centerCrop()
		.placeholder(R.drawable.ic_detail)
		.into(img_detail);
	}

	@Override
	public ImageView getImageViewPlay() {
		return img_play;
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {

	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		mHandler.removeCallbacks(mUpdateTimeTask);
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		mHandler.removeCallbacks(mUpdateTimeTask);
		int totalDuration = BaseManager.getIntance().getPlayer().getDuration();
		int currentPosition = utilities.progressToTimer(seekBar.getProgress(),
				totalDuration);

		// forward or backward to certain seconds
		BaseManager.getIntance().getPlayer().seekTo(currentPosition);

		// update timer progress again
		updateProgressBar();
	}

	private Runnable mUpdateTimeTask = new Runnable() {
		public void run() {
			long totalDuration = BaseManager.getIntance().getPlayer()
					.getDuration();
			long currentDuration = BaseManager.getIntance().getPlayer()
					.getCurrentPosition();

			// Displaying Total Duration time
			txt_end.setText("" + utilities.milliSecondsToTimer(totalDuration));
			// Displaying time completed playing
			txt_start.setText(""
					+ utilities.milliSecondsToTimer(currentDuration));

			// Updating progress bar
			int progress = (int) (utilities.getProgressPercentage(
					currentDuration, totalDuration));
			// Log.d("Progress", ""+progress);
			seekBar.setProgress(progress);

			// Running this thread after 100 milliseconds
			mHandler.postDelayed(this, 100);
		}
	};

	public void updateProgressBar() {
		mHandler.postDelayed(mUpdateTimeTask, 100);
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		if (Config.getInstance().getIsRepeat()) {
			BaseManager.getIntance().playMusic();
		} else if (Config.getInstance().getShuffle()) {
			Random rand = new Random();
			int currentSongIndex = rand.nextInt((Instance.LISTSONG_FOR_PLAY
					.size() - 1) - 0 + 1) + 0;
			BaseManager.getIntance().setCurrentSong(
					Instance.LISTSONG_FOR_PLAY.get(currentSongIndex));
			BaseManager.getIntance().playMusic();
		} else {
			BaseManager.getIntance().nextSong();
		}
	}

}
