package com.davipviana.translator;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesUtils {
    public static int getSpeechModeIndex(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                Constants.SPEECH_TO_TEXT_PREFERENCES, Context.MODE_PRIVATE
        );

        return sharedPreferences.getInt(Constants.SPEECH_MODE_INDEX, 0);
    }

    public static void updateSpeechModeIndex(Context context, int speechModeIndex) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                Constants.SPEECH_TO_TEXT_PREFERENCES, Context.MODE_PRIVATE
        );

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(Constants.SPEECH_MODE_INDEX, speechModeIndex);
        editor.apply();
    }

    public static int getBaseLanguageIndex(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                Constants.SPEECH_TO_TEXT_PREFERENCES, Context.MODE_PRIVATE
        );

        return sharedPreferences.getInt(Constants.BASE_LANGUAGE_INDEX, 0);
    }

    public static void updateBaseLanguageIndex(Context context, int languageIndex) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                Constants.SPEECH_TO_TEXT_PREFERENCES, Context.MODE_PRIVATE
        );
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(Constants.BASE_LANGUAGE_INDEX, languageIndex);
        editor.apply();
    }
}
