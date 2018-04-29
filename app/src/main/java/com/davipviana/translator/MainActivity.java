package com.davipviana.translator;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.cognitiveservices.speechrecognition.ISpeechRecognitionServerEvents;
import com.microsoft.cognitiveservices.speechrecognition.MicrophoneRecognitionClient;
import com.microsoft.cognitiveservices.speechrecognition.RecognitionResult;
import com.microsoft.cognitiveservices.speechrecognition.RecognitionStatus;
import com.microsoft.cognitiveservices.speechrecognition.SpeechRecognitionMode;
import com.microsoft.cognitiveservices.speechrecognition.SpeechRecognitionServiceFactory;
import com.microsoft.speech.tts.Synthesizer;
import com.microsoft.speech.tts.Voice;

import translatorapi.Language;
import translatorapi.Translate;

public class MainActivity extends AppCompatActivity
        implements ISpeechRecognitionServerEvents {

    public static final String LOG_TAG = "TranslatorApp";
    private MicrophoneRecognitionClient micClient = null;
    private SpeechRecognitionMode speechMode = SpeechRecognitionMode.ShortPhrase;

    private String languageCode = Constants.LANGUAGE_CODES[0];
    private Language languageTranslation = Constants.LANGUAGES[0];
    private String key = Constants.PRIMARY_SUBSCRIPTION_KEY;

    private TextView resultText;
    private FloatingActionButton fab;

    private ItemAdapter itemAdapter = new ItemAdapter(this);
    private View suggestionLayout;

    private int onlineIcon;
    private int busyIcon;

    private boolean hasStartedRecording = false;
    private boolean hasOptionChanged = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(false);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        resultText = (TextView) findViewById(R.id.resultText);
        suggestionLayout = findViewById(R.id.suggestionLayout);

        onlineIcon = getResources().getIdentifier("@android:drawable/presence_audio_online", null, null);
        busyIcon = getResources().getIdentifier("@android:drawable/ic_voice_search", null, null);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (hasInternetConnection()) {
                    resultText.setText("");
                    suggestionLayout.setVisibility(View.GONE);
                    initRecording();
                    if (micClient != null) {
                        if (speechMode.equals(SpeechRecognitionMode.ShortPhrase)) {
                            if (!hasStartedRecording) {
                                micClient.startMicAndRecognition();
                            }
                        } else {
                            if (!hasStartedRecording) {
                                micClient.startMicAndRecognition();
                            } else {
                                micClient.endMicAndRecognition();
                            }
                        }
                    }
                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.check_connection), Toast.LENGTH_LONG).show();
                }
            }
        });

        initLanguageSpinner();
        initSpeechModeSpinner();
        checkPermissions();
    }

    private void checkPermissions() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO}, Constants.PERMISSIONS_REQUEST_RECORD_AUDIO);
            }
        }
    }

    private boolean hasInternetConnection() {
        ConnectivityManager connectivityManager = ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager != null
                && connectivityManager.getActiveNetworkInfo() != null
                && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    private void initLanguageSpinner() {
        final Spinner spinner = (Spinner) findViewById(R.id.language_spinner);
        spinner.setSaveEnabled(true);
        spinner.setSelection(SharedPreferencesUtils.getBaseLanguageIndex(this));
        languageCode = Constants.LANGUAGE_CODES[SharedPreferencesUtils.getBaseLanguageIndex(this)];

        spinner.post(new Runnable() {
            @Override
            public void run() {
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        Log.d(LOG_TAG, "In Language Spinner onItemSelected");
                        languageCode = Constants.LANGUAGE_CODES[position];
                        hasOptionChanged = true;
                        SharedPreferencesUtils.updateBaseLanguageIndex(MainActivity.this, position);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // no action required, but must implement this method
                    }
                });
            }
        });
    }

    private void initSpeechModeSpinner() {
        final Spinner spinner = (Spinner) findViewById(R.id.speech_mode_spinner);
        spinner.setSaveEnabled(true);
        int pref = SharedPreferencesUtils.getSpeechModeIndex(this);
        spinner.setSelection(pref);
        speechMode = pref == 0 ? SpeechRecognitionMode.ShortPhrase : SpeechRecognitionMode.LongDictation;
        spinner.post(new Runnable() {
            @Override
            public void run() {
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        Log.d(LOG_TAG, "In Speech Mode onItemSelected");
                        speechMode = position == 0 ? SpeechRecognitionMode.ShortPhrase : SpeechRecognitionMode.LongDictation;
                        hasOptionChanged = true;
                        SharedPreferencesUtils.updateSpeechModeIndex(MainActivity.this, position);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // no action required, but must implement this method
                    }
                });
            }
        });
    }

    private void initRecording() {
        if ((hasOptionChanged) || (micClient == null)) {
            Log.d(LOG_TAG, "Language id " + languageCode + "\nSpeech mode is " + speechMode);
            if (key.equals(Constants.PRIMARY_SUBSCRIPTION_KEY)) {
                resultText.append(getString(R.string.primary_connect));
            } else {
                resultText.append(getString(R.string.secondary_connect));
            }
            micClient = SpeechRecognitionServiceFactory.createMicrophoneClient(this, speechMode, languageCode, this, key);
            hasOptionChanged = false;
        }
        // discard previous items
        itemAdapter.clear();
        // And hide the speak button
        ImageButton speakButton = (ImageButton) findViewById(R.id.speak_button);
        if(speakButton != null) {
            speakButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPartialResponseReceived(String response) {
        resultText.append("PARTIAL RESULT:\n");
        resultText.append(response + "\n");
    }

    @Override
    public void onFinalResponseReceived(RecognitionResult recognitionResult) {
        // explanation of results at https://msdn.microsoft.com/en-us/library/mt613453.aspx
        resultText.setText("");
        boolean isFinalDictationMessage = (
                speechMode == SpeechRecognitionMode.LongDictation
                    && (recognitionResult.RecognitionStatus == RecognitionStatus.EndOfDictation
                        || recognitionResult.RecognitionStatus == RecognitionStatus.DictationEndSilenceTimeout
                        || recognitionResult.RecognitionStatus == RecognitionStatus.RecognitionSuccess
                    )
        );
        if(speechMode == SpeechRecognitionMode.ShortPhrase || isFinalDictationMessage){
            if(micClient != null) {
                micClient.endMicAndRecognition();
            }

            fab.setEnabled(true);
            fab.setImageResource(onlineIcon);
        }
        if(recognitionResult.Results.length > 0) {
            ListView listView = (ListView) findViewById(R.id.resultList);
            listView.setAdapter(itemAdapter);
            suggestionLayout.setVisibility(View.VISIBLE);
            for(int i = 0; i < recognitionResult.Results.length; i++) {
                itemAdapter.addItem(recognitionResult.Results[i].DisplayText);
            }
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                    final Dialog dialog = new Dialog(MainActivity.this);
                    dialog.setContentView(R.layout.dialog_content);

                    ListView translationList = (ListView) dialog.findViewById(R.id.translation_list);
                    final ItemAdapter translationAdapter = new ItemAdapter(MainActivity.this);
                    translationAdapter.setItems(getResources().getStringArray(R.array.languages));
                    translationList.setAdapter(translationAdapter);
                    translationAdapter.setSelected(SharedPreferencesUtils.getConvertLanguageIndex(MainActivity.this));
                    // Initialise the translation language to the stored preference
                    languageTranslation = Constants.LANGUAGES[SharedPreferencesUtils.getConvertLanguageIndex(MainActivity.this)];
                    translationList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            languageTranslation = Constants.LANGUAGES[position];
                            SharedPreferencesUtils.updateConvertLanguageIndex(MainActivity.this, position);
                            translationAdapter.setSelected(position);
                        }
                    });

                    dialog.findViewById(R.id.translate_button).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            resultText.setText("");
                            new TranslationTask(Constants.LANGUAGES[SharedPreferencesUtils
                                    .getBaseLanguageIndex(MainActivity.this)],
                                    languageTranslation,
                                    (String) itemAdapter.getItem(position)).execute();
                        }
                    });

                    dialog.findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    dialog.setCancelable(true);
                    dialog.setTitle(getString(R.string.dialog_title));
                    dialog.show();
                }
            });
        }
    }

    @Override
    public void onIntentReceived(String response) {
        // not using speech recognition with intent, but must implement all the interface methods
    }

    @Override
    public void onError(int errorCode, String response) {
        fab.setEnabled(true);
        fab.setImageResource(onlineIcon);
        Toast.makeText(this, getString(R.string.internet_error_text), Toast.LENGTH_LONG).show();
        resultText.append("Error " + errorCode + ": " + response + "\n");
        micClient = null; // Force initialization when recording next time
        key = Constants.SECONDARY_SUBSCRIPTION_KEY;
    }

    @Override
    public void onAudioEvent(boolean isRecording) {
        hasStartedRecording = isRecording;
        if(!isRecording) {
            if(micClient != null) {
                micClient.endMicAndRecognition();
            }
            fab.setEnabled(true);
            fab.setImageResource(onlineIcon);
        } else {
            if(speechMode == SpeechRecognitionMode.ShortPhrase) {
                fab.setEnabled(false);
            }
            fab.setImageResource(busyIcon);
        }
        resultText.append(isRecording ? getString(R.string.recording_start) : getString(R.string.recording_end));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("resultText", resultText.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        resultText.setText(savedInstanceState.getString("resultText"));
    }

    private class TranslationTask extends AsyncTask<Void, Void, Void> {
        private final Language baseLanguage;
        private final Language convertLanguage;
        private final String word;
        private String translatedText = "";

        public TranslationTask(Language baseLanguage, Language convertLanguage, String word) {
            this.baseLanguage = baseLanguage;
            this.convertLanguage = convertLanguage;
            this.word = word;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            resultText.append("Word Selected: " + word);
            resultText.append(getString(R.string.translation_start));
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Translate.setKey(Constants.TRANSLATION_KEY);
            try {
                translatedText = Translate.execute(word, baseLanguage, convertLanguage);
            } catch(Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            resultText.setText(getString(R.string.translation_heading));
            resultText.append(translatedText);

            // Set up the click listener for the Speak button
            ImageButton speakButton = (ImageButton) findViewById(R.id.speak_button);
            if(speakButton != null) {
                speakButton.setVisibility(View.VISIBLE);
                speakButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Get the language code that the translation is in
                        String speechLanguage = Constants.LANGUAGE_CODES[SharedPreferencesUtils.getConvertLanguageIndex(MainActivity.this)];
                        Log.d(LOG_TAG, "Speech language is " + speechLanguage);
                        Synthesizer synthesizer = new Synthesizer(getString(R.string.app_name), Constants.PRIMARY_SUBSCRIPTION_KEY);
                        Voice voice = Voices.getVoice(speechLanguage, 0);
                        if(voice != null) {
                            Log.d(LOG_TAG, voice.voiceName);
                            synthesizer.SetVoice(voice, voice);
                            Log.d(LOG_TAG, "Speaking: " + translatedText);
                            synthesizer.SpeakToAudio(translatedText);

                        }
                    }
                });

            }
        }
    }
}
