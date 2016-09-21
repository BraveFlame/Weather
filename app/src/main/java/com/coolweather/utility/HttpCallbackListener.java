package com.coolweather.utility;

public interface HttpCallbackListener {
	void onFinish(String response);

	void onError(Exception e);
}
