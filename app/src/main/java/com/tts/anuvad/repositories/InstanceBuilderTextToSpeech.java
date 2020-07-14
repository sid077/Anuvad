package com.tts.anuvad.repositories;



import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class InstanceBuilderTextToSpeech {
    private static String BASE_URL = "https://texttospeech.googleapis.com/";


   public static Retrofit build(){
       Gson gson = new GsonBuilder().setLenient().create();
       Retrofit retrofit = new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create(gson)).build();

       return retrofit;
   }
}
