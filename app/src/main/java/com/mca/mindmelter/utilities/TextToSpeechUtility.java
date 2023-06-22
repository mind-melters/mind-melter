package com.mca.mindmelter.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import java.util.Locale;

public class TextToSpeechUtility {

    private TextToSpeech tts;
    private SharedPreferences sharedPreferences;

    public TextToSpeechUtility(Context context) {
        // Initialize TTS
        tts = new TextToSpeech(context, status -> {
            if (status != TextToSpeech.ERROR) {
                tts.setLanguage(Locale.US);
            } else {
                Toast.makeText(context, "Initialization Fail!", Toast.LENGTH_SHORT).show();
            }
        });

        sharedPreferences = context.getSharedPreferences("MODE", Context.MODE_PRIVATE);
    }

    public void speak(String text) {
        // Check if TTS is enabled
        boolean ttsMode = sharedPreferences.getBoolean("tts", false);
        if (ttsMode) {
            Bundle params = new Bundle();
            String utteranceId = this.hashCode() + "";
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId);
        }
    }

    public void shutdown() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }
}
