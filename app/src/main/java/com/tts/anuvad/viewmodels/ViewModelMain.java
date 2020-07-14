package com.tts.anuvad.viewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.tts.anuvad.models.TTSRequest;
import com.tts.anuvad.models.TTSResponse;
import com.tts.anuvad.models.TranslateResponse;
import com.tts.anuvad.repositories.InstanceBuilderTextToSpeech;
import com.tts.anuvad.repositories.InstanceBuilderTranslate;
import com.tts.anuvad.repositories.TextToSpeechService;
import com.tts.anuvad.repositories.TranslateService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ViewModelMain extends ViewModel {
    public static String API_KEY = "";

    MutableLiveData<TTSResponse> ttsResponseMutableLiveData = new MutableLiveData<>();
    MutableLiveData<TranslateResponse> translateResponseMutableLiveData = new MutableLiveData<>();


    public void convertTTS(String s, String langCode) {
        TTSRequest request = new TTSRequest();
        TTSRequest.TTSVoice ttsVoice=  new TTSRequest.TTSVoice();
        ttsVoice.setLangCode(langCode);
        TTSRequest.TTSInput ttsInput = new TTSRequest.TTSInput();
        ttsInput.setText(s);
        TTSRequest.TTSAudioConfig audioConfig = new TTSRequest.TTSAudioConfig();
        audioConfig.setAudioEncoding("LINEAR16");
        request.setInput(ttsInput);
        request.setAudioConfig(audioConfig);
        request.setVoice(ttsVoice);

        Call<TTSResponse > call = InstanceBuilderTextToSpeech.build().create(TextToSpeechService.class)
                .convertTTS(request,API_KEY);

        call.enqueue(new Callback<TTSResponse>() {
            @Override
            public void onResponse(Call<TTSResponse> call, Response<TTSResponse> response) {
                if(response.isSuccessful()){
                    ttsResponseMutableLiveData.setValue(response.body());
                }
            }

            @Override
            public void onFailure(Call<TTSResponse> call, Throwable t) {

            }
        });



    }
    public void translate (String text,String langCode){
        Call<TranslateResponse> call = InstanceBuilderTranslate.build().create(TranslateService.class)
                .translateText(text,langCode,"text",API_KEY);
        call.enqueue(new Callback<TranslateResponse>() {
            @Override
            public void onResponse(Call<TranslateResponse> call, Response<TranslateResponse> response) {
                if(response.isSuccessful()){
                    translateResponseMutableLiveData.setValue(response.body());
                }
            }

            @Override
            public void onFailure(Call<TranslateResponse> call, Throwable t) {
                int x=0;
            }
        });

    }
    public MutableLiveData<TranslateResponse> getTranslateResponseMutableLiveData() {
        return translateResponseMutableLiveData;
    }

    public void setTranslateResponseMutableLiveData(MutableLiveData<TranslateResponse> translateResponseMutableLiveData) {
        this.translateResponseMutableLiveData = translateResponseMutableLiveData;
    }
    public MutableLiveData<TTSResponse> getTtsResponseMutableLiveData() {
        return ttsResponseMutableLiveData;
    }

    public void setTtsResponseMutableLiveData(MutableLiveData<TTSResponse> ttsResponseMutableLiveData) {
        this.ttsResponseMutableLiveData = ttsResponseMutableLiveData;
    }
}
