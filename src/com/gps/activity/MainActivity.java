package com.gps.activity;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gps.receiver.NetBroadcastReceiver;
import com.gps.receiver.NetBroadcastReceiver.netEventHandler;
import com.gps.receiver.NetUtil;
import com.gps.util.SendUdp;
import com.gps.util.Utils;

public class MainActivity extends Activity implements netEventHandler {

	private TextView speed;
	private TextView bearing;
	private TextView latitude;
	private TextView longitude;
	private TextView time;

	private TextView gps_state;
	private TextView net_state;

	private ImageView toControl;
	private boolean isStart = false;

	private ImageView toSetting;

	private Location location;
	private LocationManager lm;
	private LocationListener locationListener;
	private boolean isExistlocationListener = false;
	// ͨ��network��ȡlocation
//	private String networkProvider = LocationManager.NETWORK_PROVIDER;
	// ͨ��gps��ȡlocation
	private String GpsProvider = LocationManager.GPS_PROVIDER;

	private SimpleDateFormat sdf;
	private DecimalFormat df;

	private SendUdp su;

	private String str = "";
	private boolean bb = true;
	private boolean cc = true;
	private boolean isExistUdpObject = false;
	private boolean isFirstConnectGps = true;
	private boolean netState = true;
	private boolean gpsState = true;

	private SharedPreferences spf;
	private String id;
	private String ipAddress;
	private int timePre;

	private Handler mHandle;
	private final int GPS_OPEN = 6660;
	private final int GPS_CLOSE = 6661;

	private final ContentObserver mGpsMonitor = new ContentObserver(null) {
		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);

			boolean enabled = lm
					.isProviderEnabled(LocationManager.GPS_PROVIDER);
			if (enabled) {
				gpsState = true;
				mHandle.sendEmptyMessage(GPS_OPEN);
			} else {
				gpsState = false;
				mHandle.sendEmptyMessage(GPS_CLOSE);
			}
		}
	};

	@SuppressLint({ "SimpleDateFormat", "HandlerLeak" })
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);

		mHandle = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case GPS_OPEN:
					gps_state.setText(getResources().getString(
							R.string.gps_pause));
					gps_state.setTextColor(getResources().getColor(
							R.color.state_pause));
					break;
				case GPS_CLOSE:
					gps_state
							.setText(getResources().getString(R.string.gps_no));
					gps_state.setTextColor(getResources().getColor(
							R.color.state_no));

					cc = false;
					if (isExistlocationListener) {
						lm.removeUpdates(locationListener);
					}

					// toControl.setText(getResources().getText(R.string.startG));
					toControl.setImageResource(R.drawable.btn_start_selector);
					// toControl.setBackgroundResource(R.drawable.btn_start_selector);
					isStart = true;
					break;

				default:
					break;
				}
			}
		};

		NetBroadcastReceiver.mListeners.add(this);

		lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

		sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		df = new DecimalFormat("###.000000");

		gps_state = (TextView) this.findViewById(R.id.gps_state);
		net_state = (TextView) this.findViewById(R.id.net_state);

		speed = (TextView) this.findViewById(R.id.speed);
		bearing = (TextView) this.findViewById(R.id.bearing);
		latitude = (TextView) this.findViewById(R.id.latitude);
		longitude = (TextView) this.findViewById(R.id.longitude);
		time = (TextView) this.findViewById(R.id.time);

		toControl = (ImageView) this.findViewById(R.id.toControl);
		toSetting = (ImageView) this.findViewById(R.id.toSetting);

		toControl.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// bb = true;
				if (!isStart) {
					if (!netState) {
						AlertDialog.Builder builder = new Builder(
								MainActivity.this);
						builder.setTitle("����������");
						builder.setMessage("������wifi������ƶ�����");
						builder.setPositiveButton("ȷ��", new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						});
						builder.create().show();
					} else if (!gpsState) {
						AlertDialog.Builder builder = new Builder(
								MainActivity.this);
						builder.setTitle("δ��GPS�豸");
						builder.setMessage("���GPS�豸");
						builder.setPositiveButton("ȷ��", new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						});
						builder.create().show();
					} else if (!isExistUdpObject) {
						AlertDialog.Builder builder = new Builder(
								MainActivity.this);
						builder.setTitle("�������쳣");
						builder.setMessage("��ȷ����������Ϣ");
						builder.setPositiveButton("֪����", new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						});
						builder.setNegativeButton("ȥȷ��", new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
								startActivityForResult(new Intent(
										Utils.SETTING_ACTIVTY), 10012);
							}
						});
						builder.create().show();
					} else {
						gps_state.setText(getResources().getText(
								R.string.gps_find));
						gps_state.setTextColor(getResources().getColor(
								R.color.state_find));
						initLocation(MainActivity.this);
					}
				} else {
					cc = false;
					lm.removeUpdates(locationListener);

					gps_state.setText(getResources()
							.getText(R.string.gps_pause));
					gps_state.setTextColor(getResources().getColor(
							R.color.state_pause));

					// toControl.setText(getResources().getText(R.string.startG));
					toControl.setImageResource(R.drawable.btn_start_selector);
					// toControl
					// .setBackgroundResource(R.drawable.btn_start_selector);
					isStart = false;
				}
			}
		});

		toSetting.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!isStart) {
					startActivityForResult(new Intent(Utils.SETTING_ACTIVTY),
							10012);
				} else {
					Toast.makeText(MainActivity.this, "����ֹͣ��������",
							Toast.LENGTH_SHORT).show();
				}
			}
		});

		initCheckNetAndGps();

		spf = getSharedPreferences(Utils.SPF, MODE_PRIVATE);
		id = spf.getString(Utils.SPF_ID, "");
		if (id == "") {
			spf = null;
			startActivityForResult(new Intent(Utils.SETTING_ACTIVTY), 10012);
		} else {
			ipAddress = spf.getString(Utils.SPF_IP, "");
			timePre = Integer.parseInt(spf.getString(Utils.SPF_TIME, "60"));

			su = new SendUdp(ipAddress);
			isExistUdpObject = true;

			// ��ʼִ�л�ȡlocation����
			// initLocation(MainActivity.this);
		}

	}

	@Override
	public void onNetChange() {
		// Message msg=new Message();
		if (NetUtil.getNetworkState(this) == NetUtil.NETWORN_NONE) {
			net_state.setText(getResources().getString(R.string.net_no));
			net_state.setTextColor(getResources().getColor(R.color.state_no));
			netState = false;

			cc = false;

			if (isExistlocationListener) {
				lm.removeUpdates(locationListener);
			}

			// toControl.setText(getResources().getText(R.string.startG));
			toControl.setImageResource(R.drawable.btn_start_selector);
			// toControl.setBackgroundResource(R.drawable.btn_start_selector);
			isStart = false;

		} else {
			net_state.setText(getResources().getString(R.string.net_yes));
			net_state.setTextColor(getResources().getColor(R.color.state_yes));
			netState = true;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			exitBy2Click();
		}
		return false;
	}

	private static boolean isExit = false;

	@SuppressLint("ShowToast")
	private void exitBy2Click() {
		Timer tExit = null;
		if (!isExit) {
			isExit = true;
			Toast.makeText(this, "�ٰ�һ���˳�", 0).show();
			tExit = new Timer();
			tExit.schedule(new TimerTask() {
				@Override
				public void run() {
					isExit = false;
				}
			}, 2000);
		} else {
			finish();
		}
	}

	@Override
	protected void onActivityResult(int arg0, int arg1, Intent arg2) {
		super.onActivityResult(arg0, arg1, arg2);

		if (arg1 == RESULT_OK) {
			spf = getSharedPreferences(Utils.SPF, MODE_PRIVATE);
			id = spf.getString(Utils.SPF_ID, "");
			ipAddress = spf.getString(Utils.SPF_IP, "");
			timePre = Integer.parseInt(spf.getString(Utils.SPF_TIME, "60"));

			if (!isExistUdpObject) {
				su = new SendUdp(ipAddress);
				isExistUdpObject = true;
			} else {
				su.updateAddress(ipAddress);
			}
		} else {
		}

		// initLocation(MainActivity.this);
	}

	private void initCheckNetAndGps() {
		if (isNetworkConnected(getApplicationContext())
				|| isWifiEnable(getApplicationContext())) {
			net_state.setText(getResources().getString(R.string.net_yes));
			net_state.setTextColor(getResources().getColor(R.color.state_yes));
			netState = true;
		} else {
			net_state.setText(getResources().getString(R.string.net_no));
			net_state.setTextColor(getResources().getColor(R.color.state_no));
			netState = false;
		}
		if (isGpsEnable(getApplicationContext())) {
			gps_state.setText(getResources().getString(R.string.gps_pause));
			gps_state
					.setTextColor(getResources().getColor(R.color.state_pause));
			gpsState = true;
		} else {
			gps_state.setText(getResources().getString(R.string.gps_no));
			gps_state.setTextColor(getResources().getColor(R.color.state_no));
			gpsState = false;
		}
	}

	// �Ƿ��п�������
	private boolean isNetworkConnected(Context mContext) {
		ConnectivityManager cm = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo network = cm.getActiveNetworkInfo();
		if (network != null) {
			return network.isAvailable();
		}
		return false;
	}

	// Wifi�Ƿ����
	private boolean isWifiEnable(Context mContext) {
		WifiManager wifiManager = (WifiManager) mContext
				.getSystemService(Context.WIFI_SERVICE);
		return wifiManager.isWifiEnabled();
	}

	// Gps�Ƿ����
	private boolean isGpsEnable(Context mContext) {
		LocationManager locationManager = ((LocationManager) mContext
				.getSystemService(Context.LOCATION_SERVICE));
		return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}

	// ��ȡlocation����
	private void initLocation(Context mContext) {
//		// ���ȼ�� ͨ��network �ܷ���location����
//		if (startLocation(networkProvider, mContext)) {
//			cc = true;
//
//			// toControl.setText(getResources().getText(R.string.stopG));
//			// toControl.setBackgroundResource(R.drawable.btn_stop_selector);
//			toControl.setImageResource(R.drawable.btn_stop_selector);
//			isStart = true;
//
//			updateLocation(location, mContext);
//		} else
		// ͨ��gps �ܷ���location����
		if (startLocation(GpsProvider, mContext)) {
			cc = true;

			// toControl.setText(getResources().getText(R.string.stopG));
			toControl.setImageResource(R.drawable.btn_stop_selector);
			// toControl.setBackgroundResource(R.drawable.btn_stop_selector);
			isStart = true;

			updateLocation(location, mContext);
			if (isFirstConnectGps) {
				AlertDialog.Builder builder = new Builder(MainActivity.this);
				builder.setTitle("�������ӡ�");
				builder.setMessage("��������GPS�����ܻᷢ����ʷλ����Ϣ�����Ժ�");
				builder.setPositiveButton("ȷ��", new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				builder.create().show();
			}
		} else {
			isFirstConnectGps = false;
			AlertDialog.Builder builder = new Builder(MainActivity.this);
			builder.setTitle("�������ӡ�");
			builder.setMessage("�״�����GPS��Ҫ��ʱ�䣬���Ժ�����");
			builder.setPositiveButton("ȷ��", new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			builder.create().show();
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onStart() {
		super.onStart();
		getContentResolver()
				.registerContentObserver(
						Settings.Secure
								.getUriFor(Settings.System.LOCATION_PROVIDERS_ALLOWED),
						false, mGpsMonitor);
	}

	@Override
	protected void onStop() {
		super.onStop();
		getContentResolver().unregisterContentObserver(mGpsMonitor);
	}

	/**
	 * ͨ������ ��ȡLocation���� ���Location����Ϊ�� �򷵻� true ���Ҹ�ֵ��ȫ�ֱ��� location ���Ϊ��
	 * ����false����ֵ��ȫ�ֱ���location
	 * 
	 * @param provider
	 * @param mContext
	 * @return
	 */
	private boolean startLocation(String provider, final Context mContext) {
		Location location = lm.getLastKnownLocation(provider);
		isExistlocationListener = true;

		// λ�ü�����
		locationListener = new LocationListener() {
			// ��λ�øı�ʱ����
			@Override
			public void onLocationChanged(Location location) {

				gps_state.setText(getResources().getText(R.string.gps_yes));
				gps_state.setTextColor(getResources().getColor(
						R.color.state_yes));

				updateLocation(location, mContext);
			}

			// ProviderʧЧʱ����
			@Override
			public void onProviderDisabled(String arg0) {
				System.out.println("ProviderʧЧ");
			}

			// Provider����ʱ����
			@Override
			public void onProviderEnabled(String arg0) {
				System.out.println("Provider����");
			}

			// Provider״̬�ı�ʱ����
			@Override
			public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
				System.out.println("onStatusChanged");
			}
		};

		// 500�������һ�Σ�����λ�ñ仯
		lm.requestLocationUpdates(provider, 500, 0, locationListener);

		// ���Location����Ϊ�� �򷵻� true ���Ҹ�ֵ��ȫ�ֱ��� location
		// ���Ϊ�� ����false ����ֵ��ȫ�ֱ���location
		if (location != null) {
			this.location = location;
			return true;
		}
		return false;

	}

	private void updateLocation(Location location, Context mContext) {
		if (location != null) {
			// ά��
			double str_latitude = location.getLatitude();
			// ����
			double str_longitude = location.getLongitude();
			// �ٶ�
			float str_speed = (float) (location.getSpeed() * 3.6);
			// ����
			String str_bearing = location.getBearing() + "";
			// float str_bearing = (float) (location.getBearing());
			// ʱ��
			String str_time = sdf.format(new Date(location.getTime()));

			longitude.setText(str_longitude + "");
			latitude.setText(str_latitude + "");
			speed.setText(str_speed + " km/h");
			bearing.setText(str_bearing);
			time.setText(str_time);

			str = "*" + id + "," + df.format(str_latitude) + ","
					+ df.format(str_longitude) + "," + str_speed + ","
					+ str_bearing + "," + str_time + ",";

			if (bb) {
				bb = false;
				new Thread() {
					@Override
					public void run() {
						super.run();
						while (cc) {
							try {
								sleep(timePre * 1000);
								if (cc) {
									su.connectServerWithUDPSocket(str
											+ sdf.format(new Date()) + "#");
								}
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						bb = true;
					}
				}.start();
			}
		} else {
			System.out.println("û�л�ȡ����λ����Location");
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		cc = false;
		if (isExistUdpObject) {
			su.socket.close();
		}
		if (isExistlocationListener) {
			lm.removeUpdates(locationListener);
		}
		System.exit(0);
	}
}
