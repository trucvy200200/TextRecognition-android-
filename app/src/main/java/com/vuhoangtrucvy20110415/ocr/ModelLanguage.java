package com.vuhoangtrucvy20110415.ocr;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.util.Log;
import android.widget.EditText;

import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

public class ModelLanguage  {
    String languageCode;
    String languageTitle;
    boolean downloaded = false;
    public Translator englishVietTranslator;

    public ModelLanguage() {
        TranslatorOptions options =
            new TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(TranslateLanguage.VIETNAMESE)
                .build();
        englishVietTranslator = Translation.getClient(options);
        downloadModel();

    }

    public void downloadModel() {
        DownloadConditions conditions = new DownloadConditions.Builder()
            .requireWifi()
            .build();
        downloaded = true;
        englishVietTranslator.downloadModelIfNeeded(conditions)
        .addOnSuccessListener(
                aVoid -> {
                    downloaded = true;
        })
        .addOnFailureListener(
                e -> downloaded = false);
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public String getLanguageTitle() {
        return languageTitle;
    }

    public void setLanguageTitle(String languageTitle) {
        this.languageTitle = languageTitle;
    }

    public String translate(String text, Callback callback) {

        englishVietTranslator.translate(text)
                .addOnSuccessListener(
                        translation -> {
//                            Log.e(TAG, "Translation success: " + translation);
//                            mResult.setText(translation);
                            callback.onTranslationComplete(translation);
                        }
                )
                .addOnFailureListener(
                        e -> {
                            Log.e(TAG, "Translation failed: " + e.getMessage());
                        }
                );

        // Wait for the translation result
        return null;
    }

}
