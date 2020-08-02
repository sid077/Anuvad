package com.tts.anuvad.models;

import com.google.gson.annotations.SerializedName;

public class TTSLanguage {
    @SerializedName("name")
    String name;
    @SerializedName("country")
    String country;

    @SerializedName("bcp")
    String bcp;
    @SerializedName("iso")
    String iso;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBcp() {
        return bcp;
    }

    public void setBcp(String bcp) {
        this.bcp = bcp;
    }

    public String getIso() {
        return iso;
    }

    public void setIso(String iso) {
        this.iso = iso;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
