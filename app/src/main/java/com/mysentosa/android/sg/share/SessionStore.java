/*
 * Copyright 2010 Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mysentosa.android.sg.share;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import twitter4j.Twitter;
import twitter4j.auth.AccessToken;

public class SessionStore {

    private static final String TWITTER_SESSION = "twitter-session";
    private static final String TWITTER_ACCESS_TOKEN = "twitter_access_token";
    private static final String TWITTER_ACCESS_TOKEN_SECRET = "twitter_access_token_secret";

    public static boolean saveTwitterAuthorizationToken(AccessToken at, Context context) {
        Editor editor = context.getSharedPreferences(TWITTER_SESSION, Context.MODE_PRIVATE).edit();
        editor.putString(TWITTER_ACCESS_TOKEN, at.getToken());
        editor.putString(TWITTER_ACCESS_TOKEN_SECRET, at.getTokenSecret());
        return editor.commit();
    }

    /*
     * Restore the access token and the expiry date from the shared preferences.
     */
    public static boolean restoreTwitterAuthorizationToken(Twitter session, Context context) {
        SharedPreferences savedSession = context.getSharedPreferences(TWITTER_SESSION, Context.MODE_PRIVATE);
        String token = savedSession.getString(TWITTER_ACCESS_TOKEN, null);
        String token_secret = savedSession.getString(TWITTER_ACCESS_TOKEN_SECRET, null);
        if (token != null && token_secret != null) {
            AccessToken at = new AccessToken(token, token_secret);
            session.setOAuthAccessToken(at);
            return true;
        }
        return false;
    }

    public static void clearTwitterAuthorizationToken(Context context) {
        Editor editor = context.getSharedPreferences(TWITTER_SESSION, Context.MODE_PRIVATE).edit();
        editor.clear();
        editor.commit();
    }
}