package com.blanke.ankireader.utils;

import android.content.Context;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

public class TtsUtils {
    private Context context;

    private static TtsUtils singleton;

    private TextToSpeech textToSpeech; // TTS对象

    public static TtsUtils getInstance(Context context) {
        if (singleton == null) {
            synchronized (TtsUtils.class) {
                if (singleton == null) {
                    singleton = new TtsUtils(context);
                }
            }
        }
        return singleton;
    }

    public TtsUtils(Context context) {
        this.context = context.getApplicationContext();
        textToSpeech = new TextToSpeech(this.context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i == TextToSpeech.SUCCESS) {
                    textToSpeech.setLanguage(Locale.US);
                    textToSpeech.setPitch(1.0f);// 设置音调，值越大声音越尖（女生），值越小则变成男声,1.0是常规
                    textToSpeech.setSpeechRate(1.0f);
                }
            }
        });
    }

    public void speakText(String text) {
        if (textToSpeech != null) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, null);
        }
    }

    public boolean isSpeaking() {
        if (textToSpeech != null) {
            return textToSpeech.isSpeaking();
        }
        return false;
    }

    public void shutdown() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

    public TextToSpeech getTextToSpeech() {
        return textToSpeech;
    }
}