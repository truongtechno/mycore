package com.simicart.core.checkout.block;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.OnWheelScrollListener;
import kankan.wheel.widget.WheelView;

import org.json.JSONArray;
import org.json.JSONException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.simicart.core.base.block.SimiBlock;
import com.simicart.core.checkout.adapter.CreditCardAdapter;
import com.simicart.core.checkout.adapter.DateArrayAdapter;
import com.simicart.core.checkout.adapter.DateNumericAdapter;
import com.simicart.core.checkout.delegate.CreditCardDelegate;
import com.simicart.core.checkout.entity.CreditcardEntity;
import com.simicart.core.checkout.entity.PaymentMethod;
import com.simicart.core.config.Config;
import com.simicart.core.config.DataLocal;
import com.simicart.core.config.Rconfig;

public class CreditCardBlock extends SimiBlock implements CreditCardDelegate {

	protected boolean isCheckedMethod;
	protected PaymentMethod mPaymentMethod;
	protected CreditCardAdapter cAdapter;

	protected EditText et_type;
	protected EditText et_cvv;
	protected WheelView wv_card;
	protected WheelView wv_month;
	protected WheelView wv_year;
	protected Button bt_save;
	protected EditText et_expired;
	protected EditText et_card_number;
	protected HashMap<String, ArrayList<CreditcardEntity>> hashMapCreditCardAuthorize;
	protected HashMap<String, ArrayList<CreditcardEntity>> hashMapCreditCardSaved;
	protected int click = 0;
	protected int click_date = 0;
	protected boolean scrolling = false;
	protected boolean scrollingDate = false;
	String email;

	public CreditCardBlock(View view, Context context) {
		super(view, context);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void initView() {
		wv_card = (WheelView) mView.findViewById(Rconfig.getInstance().id(
				"select_cart"));
		wv_card.setVisibleItems(3);
		cAdapter = new CreditCardAdapter(mContext, mPaymentMethod);
		wv_card.setViewAdapter(cAdapter);

		et_card_number = (EditText) mView.findViewById(Rconfig.getInstance()
				.id("card_number"));
		et_card_number.setHint(Config.getInstance().getText("Card Number ")
				+ ":");
		et_cvv = (EditText) mView.findViewById(Rconfig.getInstance().id("cvv"));

		et_expired = (EditText) mView.findViewById(Rconfig.getInstance().id(
				"expired"));
		et_expired.setText(Config.getInstance().getText("Expired") + ": MM/YY");
		Log.e("CreditCardBlock : ", " Previous NUMBER "
				+ PaymentMethod.getInstance().getPlace_cc_number());

		if (isCheckedMethod) {
			et_expired.setText(PaymentMethod.getInstance()
					.getPlace_cc_exp_month()
					+ "/"
					+ PaymentMethod.getInstance().getPlace_cc_exp_year());
			Log.e("CreditCardBlock : ", "setText "
					+ PaymentMethod.getInstance().getPlace_cc_number());

			et_card_number.setText(PaymentMethod.getInstance()
					.getPlace_cc_number());
		}

		et_type = (EditText) mView.findViewById(Rconfig.getInstance().id(
				"card_type"));
		if (cAdapter.getItemText(0) != null) {
			et_type.setText(cAdapter.getItemText(0));
		}
		if (com.simicart.core.common.Utils.validateString(PaymentMethod
				.getInstance().getPlace_cc_type())) {
			et_type.setText(PaymentMethod.getInstance().getPlace_cc_type_name());
		}

		wv_month = (WheelView) mView.findViewById(Rconfig.getInstance().id(
				"month"));
		wv_year = (WheelView) mView.findViewById(Rconfig.getInstance().id(
				"year"));

		setClickCardType();

		setWheelScroll();
		wv_card.setCurrentItem(0);

		bt_save = (Button) mView.findViewById(Rconfig.getInstance().id(
				"card_save"));
		bt_save.setText(Config.getInstance().getText("Save"));
		bt_save.setTextColor(Color.WHITE);
		GradientDrawable gdDefault = new GradientDrawable();
		gdDefault.setColor(Config.getInstance().getColorMain());
		gdDefault.setCornerRadius(15);
		bt_save.setBackgroundDrawable(gdDefault);

		if (mPaymentMethod.getData("useccv").equals("1")) {
			EditText cvv = (EditText) mView.findViewById(Rconfig.getInstance()
					.id("cvv"));
			cvv.setText(PaymentMethod.getInstance().getPlacecc_id());
			cvv.setVisibility(View.VISIBLE);
		}

		setPickerDate();

		// get cc da luu
		email = DataLocal.getEmailCreditCart();
		if (isSavedCC(mPaymentMethod.getPayment_method())) {
			CreditcardEntity creditcardEntity = DataLocal
					.getHashMapCreditCart().get(DataLocal.getEmailCreditCart())
					.get(mPaymentMethod.getPayment_method());
			et_type.setText(creditcardEntity.getPaymentType());
			et_card_number.setText(creditcardEntity.getPaymentNumber());
			et_expired.setText(creditcardEntity.getPaymentMonth() + "/"
					+ creditcardEntity.getPaymentYear());
			et_cvv.setText(creditcardEntity.getPaymentCvv());
		}
	}

	private boolean isSavedCC(String paymentMethodCode) {
		HashMap<String, HashMap<String, CreditcardEntity>> hashMap = DataLocal
				.getHashMapCreditCart();
		if (hashMap == null || hashMap.size() == 0) {
			return false;
		} else {
			if (hashMap.containsKey(DataLocal.getEmailCreditCart())) {
				HashMap<String, CreditcardEntity> creditcard = hashMap
						.get(DataLocal.getEmailCreditCart());
				if (creditcard.containsKey(paymentMethodCode)) {
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		}
	}

	public void setClickCardType() {
		et_type.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				kankan.wheel.widget.WheelView picker = (kankan.wheel.widget.WheelView) mView
						.findViewById(Rconfig.getInstance().id("select_cart"));
				if (click % 2 == 0) {
					picker.setVisibility(View.VISIBLE);
				} else {
					picker.setVisibility(View.GONE);
				}
				click++;
			}
		});
	}

	public void setWheelScroll() {
		wv_card.addChangingListener(new OnWheelChangedListener() {
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				if (!scrolling) {
					et_type.setText(cAdapter.getItemText(wv_card
							.getCurrentItem()));
					setCcType(wv_card.getCurrentItem());
				}
			}
		});

		wv_card.addScrollingListener(new OnWheelScrollListener() {
			public void onScrollingStarted(WheelView wheel) {
				scrolling = true;
			}

			public void onScrollingFinished(WheelView wheel) {
				scrolling = false;
				et_type.setText(cAdapter.getItemText(wv_card.getCurrentItem()));
				setCcType(wv_card.getCurrentItem());
			}
		});
	}

	public void setPickerDate() {
		Calendar calendar = Calendar.getInstance();

		int curMonth = calendar.get(Calendar.MONTH);

		int curYear = calendar.get(Calendar.YEAR);

		if (!PaymentMethod.getInstance().getPlace_cc_exp_month().equals("")
				&& !PaymentMethod.getInstance().getPlace_cc_exp_year()
						.equals("") && isCheckedMethod) {
			this.setExDate(PaymentMethod.getInstance().getPlace_cc_exp_month(),
					PaymentMethod.getInstance().getPlace_cc_exp_year());
		} else {
			this.setExDate("" + (curMonth + 1), "" + curYear);
		}
		et_expired.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				LinearLayout picker = (LinearLayout) mView.findViewById(Rconfig
						.getInstance().id("card_date"));
				if (click_date % 2 == 0) {
					picker.setVisibility(View.VISIBLE);
				} else {
					picker.setVisibility(View.GONE);
				}
				click_date++;
			}
		});

		// OnWheelChangedListener listener = new OnWheelChangedListener() {
		// public void onChanged(WheelView wheel, int oldValue, int newValue) {
		// updateWheelView();
		// }
		// };

		OnWheelScrollListener listenerScroll = new OnWheelScrollListener() {

			public void onScrollingStarted(WheelView wheel) {
			}

			public void onScrollingFinished(WheelView wheel) {
				updateWheelView();
			}
		};

		String months[] = new String[] { "January - 01", "February - 02",
				"March - 03", "April - 04", "May - 05", "June - 06",
				"July - 07", "August -  08", "September - 09", "October - 10",
				"November - 11", "December - 12" };

		wv_month.setViewAdapter(new DateArrayAdapter(mContext, months, curMonth));
		wv_month.setCurrentItem(curMonth);
		wv_month.addScrollingListener(listenerScroll);

		wv_year.setViewAdapter(new DateNumericAdapter(mContext, curYear, 2099,
				0));

		wv_year.setCurrentItem(curYear);
		wv_year.addScrollingListener(listenerScroll);
	}

	private void updateWheelView() {
		String m = "" + (wv_month.getCurrentItem() + 1);
		String show = m;
		if (m.length() == 1) {
			show = "0" + (m);
		}

		Calendar calendar = Calendar.getInstance();

		this.setExDate(show,
				"" + (calendar.get(Calendar.YEAR) + wv_year.getCurrentItem()));
	}

	public void setExDate(String month, String year) {
		String m = month;
		if (month.length() == 1) {
			m = "0" + (month);
		}
		et_expired.setText(m + "/" + year);
	}

	public void setClickSave(OnTouchListener onTouchListener) {
		bt_save.setOnTouchListener(onTouchListener);
	}

	public void setPaymentMethod(PaymentMethod mPaymentMethod) {
		this.mPaymentMethod = mPaymentMethod;
	}

	public void setCheckedMethod(boolean isCheckedMethod) {
		this.isCheckedMethod = isCheckedMethod;
	}

	public void setCcType(int position) {
		JSONArray cc_types = null;
		try {
			cc_types = new JSONArray(mPaymentMethod.getData("cc_types"));
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		if (cc_types != null) {
			try {
				PaymentMethod.getInstance().setPlace_cc_type(
						cc_types.getJSONObject(position).getString("cc_code"));
				PaymentMethod.getInstance().setPlace_cc_type_name(
						cc_types.getJSONObject(position).getString("cc_name"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	@SuppressLint("NewApi")
	@Override
	public void onCLickSave() {
		String my = "" + et_expired.getText();
		String[] split = my.split("/");
		String number = "" + et_card_number.getText();
		String ccid = "" + et_cvv.getText();
		String cart_type = "" + et_type.getText().toString();

		String email = DataLocal.getEmailCreditCart();
		if (DataLocal.isSignInComplete()) {
			HashMap<String, HashMap<String, CreditcardEntity>> hashMap = DataLocal
					.getHashMapCreditCart();
			if (hashMap == null) {
				hashMap = new HashMap<String, HashMap<String, CreditcardEntity>>();
			}
			CreditcardEntity creditCard = new CreditcardEntity(cart_type,
					number, split[0], split[1], ccid);
			HashMap<String, CreditcardEntity> creditCardHashMap = new HashMap<String, CreditcardEntity>();
			if (hashMap.containsKey(email)) {
				creditCardHashMap = hashMap.get(email);
			}
			creditCardHashMap.put(mPaymentMethod.getPayment_method(),
					creditCard);
			hashMap.put(email, creditCardHashMap);
			DataLocal.saveHashMapCreditCart(hashMap);
		}

		PaymentMethod.getInstance().setPlace_cc_exp_month(split[0]);
		PaymentMethod.getInstance().setPlace_cc_exp_year(split[1]);
		Log.e("CreditCartBlock onClickSave : ", "NUMBER : " + number);
		PaymentMethod.getInstance().setPlace_cc_number(number);
		PaymentMethod.getInstance().setPlacecc_id(ccid);
	}
}
