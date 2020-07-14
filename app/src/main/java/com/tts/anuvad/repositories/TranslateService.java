package com.tts.anuvad.repositories;

import com.tts.anuvad.models.TranslateResponse;

import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface TranslateService {

    @POST("language/translate/v2")
    Call<TranslateResponse> translateText(@Query("q") String inputText, @Query("target") String targetLang, @Query("format")String format, @Query("key") String key);
}
