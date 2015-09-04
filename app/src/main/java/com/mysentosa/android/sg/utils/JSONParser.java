package com.mysentosa.android.sg.utils;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class JSONParser {
	static Gson gson = new Gson();
	static JsonParser parser = new JsonParser();

	public static <T> T getResponse(Class<T> cls, String json)
			throws JsonSyntaxException {
		return gson.fromJson(json, cls);
	}

}
