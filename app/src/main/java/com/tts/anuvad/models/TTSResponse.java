package com.tts.anuvad.models;

import com.google.gson.annotations.SerializedName;

public class TTSResponse {
    @SerializedName("audioContent")
    String audio;

    public String getAudio() {
        return audio;
    }

    public void setAudio(String audio) {
        this.audio = audio;
    }
}
