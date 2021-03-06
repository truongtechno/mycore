package com.simicart.plugins.barcode;

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;

import com.simicart.MainActivity;
import com.simicart.core.base.block.SimiBlock;
import com.simicart.core.base.delegate.ModelDelegate;
import com.simicart.core.base.manager.SimiManager;
import com.simicart.core.base.model.SimiModel;
import com.simicart.core.catalog.product.fragment.ProductDetailParentFragment;
import com.simicart.core.common.Utils;
import com.simicart.core.config.Config;
import com.simicart.core.config.Constants;
import com.simicart.core.config.Rconfig;
import com.simicart.core.event.slidemenu.SlideMenuData;
import com.simicart.core.slidemenu.entity.ItemNavigation;
import com.simicart.core.slidemenu.entity.ItemNavigation.TypeItem;

public class ScanCode {

	Context mContext;
	SlideMenuData mSlideMenuData;
	ArrayList<ItemNavigation> mItems;
	private String type = "";
	private ScanCodeModel mModel;

	public ScanCode(String method, SlideMenuData menuData) {
		this.mSlideMenuData = menuData;
		mContext = SimiManager.getIntance().getCurrentActivity();
		mItems = mSlideMenuData.getItemNavigations();
		if (method.equals("addbarcodetoleftmenu")) {
			ItemNavigation item = new ItemNavigation();
			item.setType(TypeItem.NORMAL);
			item.setName(Config.getInstance().getText(
					ConstantBarcode.QR_BAR_CODE));
			int id_icon = Rconfig.getInstance().drawable("ic_barcode");
			Drawable icon = mContext.getResources().getDrawable(id_icon);
			icon.setColorFilter(Color.parseColor("#ffffff"),
					PorterDuff.Mode.SRC_ATOP);
			item.setIcon(icon);
			mItems.add(item);
		}
	}

	public ScanCode(String method) {
		if (method.equals("resultbarcode")) {
			checkResultBarcode();
		}
		if (method.equals("checkentrycount")) {
			checkEntryCount();
		}
		if (method.equals("clickitemleftmenu")) {
			String nameItem = Constants.itemName;
			clickItemLeftMenuCode(nameItem);
		}
		if (method.equals("backtoscan")) {
			backToScan();
		}
		if (method.equals("checkdirectdetail")) {
			String nameFragment = Constants.NAME_FRAGMENT;
			if (nameFragment
					.contains("com.simicart.core.catalog.product.fragment.ProductDetailParentFragment")) {
				Constants.DIRECT_TODETAIL = true;
			} else {
				Constants.DIRECT_TODETAIL = false;
			}
		}
	}

	private void checkResultBarcode() {
		String content = MainActivity.instance.getData().getStringExtra(
				"SCAN_RESULT");
		final String fomat = MainActivity.instance.getData().getStringExtra(
				"SCAN_RESULT_FORMAT");
		if (fomat.equals("QR_CODE")) {
			type = "1";
		} else {
			type = "0";
		}
		if (type != "" && content != "") {
			mModel = new ScanCodeModel();
			final ProgressDialog pd_loading = ProgressDialog
					.show(SimiManager.getIntance().getCurrentActivity(), null,
							null, true, false);
			pd_loading.setContentView(Rconfig.getInstance().layout(
					"core_base_loading"));
			pd_loading.setCanceledOnTouchOutside(false);
			pd_loading.setCancelable(false);
			pd_loading.show();
			mModel.setDelegate(new ModelDelegate() {

				@Override
				public void callBack(String message, boolean isSuccess) {
					pd_loading.dismiss();
					if (isSuccess) {
						ArrayList<String> listID = new ArrayList<String>();
						String product_id = mModel.getProductID();
						if (!Utils.validateString(product_id)) {
							SimiManager.getIntance().showToast(
									"Result products is empty");
							return;
						}
						listID.add(product_id);
						ProductDetailParentFragment fragment = ProductDetailParentFragment
								.newInstance();
						fragment.setTargetFragment(fragment,
								Constants.TARGET_PRODUCTDETAIL);
						fragment.setProductID(product_id);
						fragment.setListIDProduct(listID);
						SimiManager.getIntance().addFragment(fragment);
						MainActivity.mCheckToDetailAfterScan = true;
						MainActivity.mBackEntryCountDetail = SimiManager
								.getIntance().getManager()
								.getBackStackEntryCount();
					} else {
						SimiManager.getIntance().showToast(
								"Result products is empty");
						return;
						// Intent intent = new Intent(Constants.SCANNER);
						// intent.putExtra("SCAN_MODE", Constants.SCAN_MODE);
						// intent.putExtra("SCAN_MODE",
						// Constants.SCAN_MODE_BARCODE);
						// intent.putExtra("QR_CODE_ERROR", message);
						// SimiManager.getIntance().getCurrentActivity()
						// .startActivityForResult(intent, 1111);
					}
				}
			});
			mModel.addParam("code", content);
			mModel.addParam("type", type);
			mModel.request();
		}
	}

	private void clickItemLeftMenuCode(String nameItem) {
		if (nameItem.equals(Config.getInstance().getText(
				ConstantBarcode.QR_BAR_CODE))) {
			Intent intent = new Intent(Constants.SCANNER);
			intent.putExtra("SCAN_MODE", Constants.SCAN_MODE);
			intent.putExtra("SCAN_MODE", Constants.SCAN_MODE_BARCODE);
			SimiManager.getIntance().getCurrentActivity()
					.startActivityForResult(intent, Constants.RESULT_BARCODE);
		}
	}

	private void checkEntryCount() {
		int backEntryCountDetail = SimiManager.getIntance().getManager()
				.getBackStackEntryCount();
		if (MainActivity.mBackEntryCountDetail == backEntryCountDetail) {
			if (Constants.DIRECT_TODETAIL == true) {
				if (SimiManager.getIntance().getManager().getFragments().size() > 0) {
					try {
						for (Fragment fragment : SimiManager.getIntance()
								.getManager().getFragments()) {
							if (fragment == null) {
								// SimiManager.getIntance().getManager()
								// .getFragments().remove(fragment);
								break;
							} else {
								if (fragment.getTargetRequestCode() == Constants.TARGET_PRODUCTDETAIL
										&& MainActivity.mCheckToDetailAfterScan == true) {
									MainActivity.checkBackScan = true;
								}
							}
						}
					} catch (Exception e) {
						System.out.println(e.getMessage());
					}
				}
			}
		}
	}

	private void backToScan() {
		Intent intent = new Intent(Constants.SCANNER);
		intent.putExtra("SCAN_MODE", Constants.SCAN_MODE);
		intent.putExtra("SCAN_MODE", Constants.SCAN_MODE_BARCODE);
		SimiManager.getIntance().getCurrentActivity()
				.startActivityForResult(intent, Constants.RESULT_BARCODE);
		MainActivity.mCheckToDetailAfterScan = false;
	}

	@SuppressWarnings("unused")
	private void addItemLeftMenu(boolean theme) {
		// if (theme == true) {
		// // parent
		// ItemNavigation parentBarcode = new ItemNavigation();
		// parentBarcode.setSparator(true);
		// parentBarcode.setName(ConstantBarcode.SCAN_NOW);
		// parentBarcode.setType(ItemNavigation.BASIC_TYPE);
		// mItems.add(parentBarcode);
		// // child
		// ItemNavigation mItemNavigation = new ItemNavigation();
		// mItemNavigation.setType(ItemNavigation.PLUGIN_TYPE);
		// Drawable icon = SimiManager.getIntance().getCurrentContext()
		// .getResources()
		// .getDrawable(Rconfig.getInstance().drawable("ic_barcode"));
		// icon.setColorFilter(Config.getInstance().getColorMenu(),
		// PorterDuff.Mode.SRC_ATOP);
		// mItemNavigation.setName(ConstantBarcode.QR_BAR_CODE);
		// mItemNavigation.setIcon(icon);
		// mItems.add(mItemNavigation);
		// } else {
		// mItems = mSlideMenuData.getItemNavigations();
		// Drawable ic_barcode = mContext.getResources().getDrawable(
		// Rconfig.getInstance().drawable("ic_barcode"));
		// ic_barcode.setColorFilter(Config.getInstance().getColorMenu(),
		// PorterDuff.Mode.SRC_ATOP);
		// ItemNavigation itemCategory = new ItemNavigation();
		// itemCategory.setIcon(ic_barcode);
		// itemCategory.setName(ConstantBarcode.QR_BAR_CODE);
		// itemCategory.setType(ItemNavigation.BASIC_TYPE);
		// mItems.add(itemCategory);
		// }
	}

}
