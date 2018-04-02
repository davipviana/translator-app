package com.davipviana.translator;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
    private String key = Constants.PRIMARY_SUBSCRIPTION_KEY;

    private TextView resultText;
    private FloatingActionButton fab;

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
        onlineIcon = getResources().getIdentifier("@android:drawable/presence_audio_online", null, null);
        busyIcon = getResources().getIdentifier("@android:drawable/ic_voice_search", null, null);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (hasInternetConnection()) {
                    resultText.setText("");
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
                    Toast.makeText(MainActivity.this, "Please check your internet connection", Toast.LENGTH_LONG).show();
                }
            }
        });

        initLanguageSpinner();
        initSpeechModeSpinner();
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
        // TODO spinner.setSelection(SharedPreferencesUtils.getBaseLanguageIndex(this));
        // TODO languageCode = Constants.LANGUAGE_CODES[SharedPreferencesUtils.getBaseLanguageIndex(this));

        spinner.post(new Runnable() {
            @Override
            public void run() {
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        Log.d(LOG_TAG, "In Language Spinner onItemSelected");
                        languageCode = Constants.LANGUAGE_CODES[position];
                        hasOptionChanged = true;
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
        // TODO int pref = SharedPreferencesUtils.getSpeechModeIndex(this);
        // TODO spinner.setSelection(pref);
        // TODO speechMode = pref == 0 ? SpeechRecognitionMode.ShortPhrase : SpeechRecognitionMode.LongDictation;
        spinner.post(new Runnable() {
            @Override
            public void run() {
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        Log.d(LOG_TAG, "In Speech Mode onItemSelected");
                        speechMode = position == 0 ? SpeechRecognitionMode.ShortPhrase : SpeechRecognitionMode.LongDictation;
                        hasOptionChanged = true;
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
                resultText.append("Connecting with PRIMARY key\n");
            } else {
                resultText.append("Connecting with SECONDARY key\n");
            }
            micClient = SpeechRecognitionServiceFactory.createMicrophoneClient(this, speechMode, languageCode, this, key);
            hasOptionChanged = false;
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

        String s = "";
        if(isFinalDictationMessage) {
            s += "Final Dictation Messsage\n";
        }

        if(recognitionResult.Results.length > 0) {
            s += "Text Suggestions\n";
            for(int i = 0; i < recognitionResult.Results.length; i++) {
                s += (i+1) + " " + recognitionResult.Results[i].DisplayText + "\n";
            }
            s += "\n" + resultText.getText().toString();
            resultText.setText(s);
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
        Toast.makeText(this, "Cannot connect to server\nError has ocurred", Toast.LENGTH_LONG).show();
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
        resultText.append(isRecording ? "RECORDING STARTED\n" : "RECORDING ENDED\n");
    }

    //    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
}
