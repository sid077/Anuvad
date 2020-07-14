package com.tts.anuvad.models;

import com.google.gson.annotations.SerializedName;

public class TTSRequest {
@SerializedName("input")
    TTSInput input;
@SerializedName("voice")
    TTSVoice voice;
@SerializedName("audioConfig")
TTSAudioConfig audioConfig;

    public TTSInput getInput() {
        return input;
    }

    public void setInput(TTSInput input) {
        this.input = input;
    }

    public TTSVoice getVoice() {
        return voice;
    }

    public void setVoice(TTSVoice voice) {
        this.voice = voice;
    }

    public TTSAudioConfig getAudioConfig() {
        return audioConfig;
    }

    public void setAudioConfig(TTSAudioConfig audioConfig) {
        this.audioConfig = audioConfig;
    }

    public static class TTSInput {
        @SerializedName("text")
        String text;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

    public static class TTSVoice {
        @SerializedName("languageCode")
        String langCode;
        @SerializedName("ssmlGender")
        String gender;

        public String getLangCode() {
            return langCode;
        }

        public void setLangCode(String langCode) {
            this.langCode = langCode;
        }

        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }
    }

    public static class TTSAudioConfig {
        @SerializedName("audioEncoding")
        String audioEncoding;

        public String getAudioEncoding() {
            return audioEncoding;
        }

        public void setAudioEncoding(String audioEncoding) {
            this.audioEncoding = audioEncoding;
        }
    }
}
