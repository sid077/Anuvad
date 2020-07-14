package com.tts.anuvad.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TranslateResponse {


    @SerializedName("data")
    TranslateData data;
    public TranslateData getData() {
        return data;
    }

    public void setData(TranslateData data) {
        this.data = data;
    }


    public static class TranslateTextResponse {
        @SerializedName("detectedSourceLanguage")
        String detectedLang;
        @SerializedName("model")
        String model;
        @SerializedName("translatedText")
        String text;
        public String getDetectedLang() {
            return detectedLang;
        }

        public void setDetectedLang(String detectedLang) {
            this.detectedLang = detectedLang;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }


    }

    public static class TranslateData {
        @SerializedName("translations")
        List<TranslateTextResponse> translateTextResponseList;

        public List<TranslateTextResponse> getTranslateTextResponseList() {
            return translateTextResponseList;
        }

        public void setTranslateTextResponseList(List<TranslateTextResponse> translateTextResponseList) {
            this.translateTextResponseList = translateTextResponseList;
        }
    }
}
