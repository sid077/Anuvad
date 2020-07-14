package com.tts.anuvad.repositories;



import com.tts.anuvad.models.TTSRequest;
import com.tts.anuvad.models.TTSResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface TextToSpeechService {

    @POST("v1/text:synthesize/")
    Call<TTSResponse> convertTTS(@Body TTSRequest ttsRequest, @Query("key") String key);
}
