package com.davipviana.translator;

import com.microsoft.speech.tts.Voice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Voices {
    private static final Map<String, List<Voice>> voices = new HashMap<>();

    private static void addVoice(Voice v) {
        List<Voice> lv = voices.get(v.lang);
        if(lv == null) {
            lv = new ArrayList<>();
            voices.put(v.lang, lv);
        }
        lv.add(v);
    }

    static {
        addVoice(new Voice("de-de", "Microsoft Server Speech Text to Speech Voice (de-DE, Hedda)", Voice.Gender.Female, true));
        addVoice(new Voice("de-de", "Microsoft Server Speech Text to Speech Voice (de-DE, Stefan, Apollo)", Voice.Gender.Male, true));
        addVoice(new Voice("en-gb", "Microsoft Server Speech Text to Speech Voice (en-GB, Susan, Apollo)", Voice.Gender.Female, true));
        addVoice(new Voice("en-gb", "Microsoft Server Speech Text to Speech Voice (en-GB, George, Apollo)", Voice.Gender.Male, true));
        addVoice(new Voice("en-us", "Microsoft Server Speech Text to Speech Voice (en-US, ZiraRUS)", Voice.Gender.Female, true));
        addVoice(new Voice("en-us", "Microsoft Server Speech Text to Speech Voice (en-US, BenjaminRUS)", Voice.Gender.Male, true));
        addVoice(new Voice("es-es", "Microsoft Server Speech Text to Speech Voice (es-ES, Laura, Apollo)", Voice.Gender.Female, true));
        addVoice(new Voice("es-es", "Microsoft Server Speech Text to Speech Voice (es-ES, Pablo, Apollo)", Voice.Gender.Male, true));
        addVoice(new Voice("fr-fr", "Microsoft Server Speech Text to Speech Voice (fr-FR, Julie, Apollo)", Voice.Gender.Female, true));
        addVoice(new Voice("fr-fr", "Microsoft Server Speech Text to Speech Voice (fr-FR, Paul, Apollo)", Voice.Gender.Male, true));
        addVoice(new Voice("it-it", "Microsoft Server Speech Text to Speech Voice (it-IT, Cosimo, Apollo)", Voice.Gender.Male, true));
        addVoice(new Voice("zh-cn", "Microsoft Server Speech Text to Speech Voice (zh-CN, HuihuiRUS)", Voice.Gender.Female, true));
        addVoice(new Voice("zh-cn", "Microsoft Server Speech Text to Speech Voice (zh-CN, Yaoyao, Apollo)", Voice.Gender.Female, true));
        addVoice(new Voice("zh-cn", "Microsoft Server Speech Text to Speech Voice (zh-CN, Kangkang, Apollo)", Voice.Gender.Male, true));
    }

    public static Voice getVoice(String lang, int index) {
        // returns a voice for the specified language
        List<Voice> lv = voices.get(lang);
        if ((lv != null) && lv.size() > index) {
            return lv.get(index);
        }
        return null;
    }

    public static int voiceCount(String lang) {
        List<Voice> lv = voices.get(lang);
        if(lv != null) {
            return lv.size();
        }
        return 0;
    }
}
