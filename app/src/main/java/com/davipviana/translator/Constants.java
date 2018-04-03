package com.davipviana.translator;

import com.memetix.mst.language.Language;

public class Constants {
    public static final String PRIMARY_SUBSCRIPTION_KEY = "TBA";
    public static final String SECONDARY_SUBSCRIPTION_KEY = "TBA";

    public static final String[] LANGUAGE_CODES = {"en-us", "en-gb", "fr-fr", "de-de", "it-it", "zh-cn", "es-es" };

    public static final String SPEECH_TO_TEXT_PREFERENCES = "SpeechToTextPreferences";
    public static final String SPEECH_MODE_INDEX = "SpeechModeIndex";
    public static final String BASE_LANGUAGE_INDEX = "BaseLanguageIndex";
    public static final String CONVERT_LANGUAGE_INDEX = "ConvertLanguageIndex";

    public static final String CLIENT_ID_VALUE = "TBA";
    public static final String CLIENT_SECRET_VALUE = "TBA";

    public static final Language[] LANGUAGES = {
            Language.ENGLISH,
            Language.ENGLISH,
            Language.FRENCH,
            Language.GERMAN,
            Language.ITALIAN,
            Language.CHINESE_TRADITIONAL,
            Language.SPANISH
    };


    public static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
}
