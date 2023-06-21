package com.mca.mindmelter.utilities;

import android.content.Context;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import java.util.Locale;

public class TextToSpeechUtility {

    private TextToSpeech tts;

    public TextToSpeechUtility(Context context) {
        // Initialize TTS
        tts = new TextToSpeech(context, status -> {
            if (status != TextToSpeech.ERROR) {
                tts.setLanguage(Locale.US);
            } else {
                Toast.makeText(context, "Initialization Fail!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void speak(String text) {
        Bundle params = new Bundle();
        String utteranceId = this.hashCode() + "";
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId);
    }

    public void shutdown() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }
}
