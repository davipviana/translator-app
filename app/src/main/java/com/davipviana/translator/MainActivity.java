package com.davipviana.translator;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.memetix.mst.language.Language;
import com.microsoft.cognitiveservices.speechrecognition.ISpeechRecognitionServerEvents;
import com.microsoft.cognitiveservices.speechrecognition.MicrophoneRecognitionClient;
import com.microsoft.cognitiveservices.speechrecognition.RecognitionResult;
import com.microsoft.cognitiveservices.speechrecognition.RecognitionStatus;
import com.microsoft.cognitiveservices.speechrecognition.SpeechRecognitionMode;
import com.microsoft.cognitiveservices.speechrecognition.SpeechRecognitionServiceFactory;

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
        }

//        String s = "";
//        if(isFinalDictationMessage) {
//            s += "Final Dictation Message\n";
//        }
//
//        if(recognitionResult.Results.length > 0) {
//            s += "Text Suggestions\n";
//            for(int i = 0; i < recognitionResult.Results.length; i++) {
//                s += (i+1) + " " + recognitionResult.Results[i].DisplayText + "\n";
//            }
//            s += "\n" + resultText.getText().toString();
//            resultText.setText(s);
//        }
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
}
