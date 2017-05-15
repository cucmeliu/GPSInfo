package com.gps.activity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.gps.util.Utils;

public class SettingActivity extends Activity {

	private LinearLayout back;

	private EditText addId;
	private EditText addInetAddress;
	private EditText addTimePre;

	private Button configBtn;

	private Intent mIntent;

	private SharedPreferences spf;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.setting);

		mIntent = getIntent();

		spf = getSharedPreferences(Utils.SPF, MODE_PRIVATE);
		String id = spf.getString(Utils.SPF_ID, "");
		String ipAddress = spf.getString(Utils.SPF_IP, "");
		String timePret = spf.getString(Utils.SPF_TIME, "");
//		int timePret = Integer.parseInt(spf.getString(Utils.SPF_TIME, ""));

		back = (LinearLayout) this.findViewById(R.id.back);

		addId = (EditText) this.findViewById(R.id.addId);
		addInetAddress = (EditText) this.findViewById(R.id.inetAddress);
		addTimePre = (EditText) this.findViewById(R.id.time_per);

		addId.setText(id);
		addInetAddress.setText(ipAddress);
		addTimePre.setText(timePret + "");

		configBtn = (Button) this.findViewById(R.id.configUpdate);

		back.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				tipSaveAndBack();
			}
		});

		configBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				saveAndBack();
			}
		});

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			tipSaveAndBack();
		}
		return false;
	}

	private void tipSaveAndBack() {
		AlertDialog.Builder builder = new Builder(SettingActivity.this);
		builder.setTitle("提示：");
		builder.setMessage("确定不保存返回吗？");
		builder.setPositiveButton("保存", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				saveAndBack();
			}
		});
		builder.setNeutralButton("不保存", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});
		builder.create().show();
	}

	private void saveAndBack() {
		String idMsg = addId.getText().toString();
		String inetAddress = addInetAddress.getText().toString();
		String timePre = addTimePre.getText().toString();

		if (idMsg == null || idMsg.length() == 0) {
			Toast.makeText(getApplicationContext(), "请输入ID", Toast.LENGTH_LONG)
					.show();
		} else if (inetAddress == null || inetAddress.length() == 0) {
			Toast.makeText(getApplicationContext(), "请输入网络地址",
					Toast.LENGTH_LONG).show();
		} else if (timePre == null || timePre.length() == 0) {
			Toast.makeText(getApplicationContext(), "请输入时间间隔",
					Toast.LENGTH_LONG).show();
		} else if (isContainChinese(idMsg)) {
			Toast.makeText(getApplicationContext(), "请输入非中文ID",
					Toast.LENGTH_LONG).show();
		} else if (!isCorrectNet(inetAddress)) {
			Toast.makeText(getApplicationContext(), "请检查网络地址及端口是否合法",
					Toast.LENGTH_LONG).show();
		} else if (!isInteger(timePre)) {
			Toast.makeText(getApplicationContext(), "请输入非0正整数", Toast.LENGTH_LONG)
					.show();
		} else {

			Editor edt = spf.edit();
			edt.putString(Utils.SPF_ID, idMsg);
			edt.putString(Utils.SPF_IP, inetAddress);
			edt.putString(Utils.SPF_TIME, timePre);
			edt.commit();
			edt = null;
			spf = null;
			setResult(RESULT_OK, mIntent);
			finish();
		}
	}

	private boolean isContainChinese(String str) {

		Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
		Matcher m = p.matcher(str);
		if (m.find()) {
			return true;
		}
		return false;
	}

	private boolean isCorrectNet(String str) {
		if (str.contains(":")) {
			String[] cs = str.split(":");
			if (isIp(cs[0]) && isPort(cs[1])) {
				return true;
			}
		}
		return false;
	}

	private boolean isInteger(String str) {
		Pattern p = Pattern.compile("^\\+?[1-9][0-9]*$");
		Matcher m = p.matcher(str);
		if (m.find()) {
			return true;
		}
		return false;
	}

	private boolean isIp(String str) {

		Pattern p = Pattern
				.compile("^(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9])\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[0-9])$");
		Matcher m = p.matcher(str);
		if (m.find()) {
			return true;
		}
		return false;
	}

	private boolean isPort(String str) {

		Pattern p = Pattern
				.compile("^([0-9]|[1-9]\\d|[1-9]\\d{2}|[1-9]\\d{3}|[1-5]\\d{4}|6[0-4]\\d{3}|65[0-4]\\d{2}|655[0-2]\\d|6553[0-5])$");
		Matcher m = p.matcher(str);
		if (m.find()) {
			return true;
		}
		return false;
	}
}
