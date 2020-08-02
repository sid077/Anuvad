package com.tts.anuvad.viewmodels;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tts.anuvad.models.STSTranscript;
import com.tts.anuvad.models.TTSLanguage;
import com.tts.anuvad.models.TTSRequest;
import com.tts.anuvad.models.TTSResponse;
import com.tts.anuvad.models.TranslateResponse;
import com.tts.anuvad.repositories.InstanceBuilderTextToSpeech;
import com.tts.anuvad.repositories.InstanceBuilderTranslate;
import com.tts.anuvad.repositories.STSTranscriptDB;
import com.tts.anuvad.repositories.TextToSpeechService;
import com.tts.anuvad.repositories.TranslateService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ViewModelMain extends ViewModel {
    public static String API_KEY = "";

    MutableLiveData<TTSResponse> ttsResponseMutableLiveData = new MutableLiveData<>();
    MutableLiveData<TranslateResponse> translateResponseMutableLiveData = new MutableLiveData<>();



    MutableLiveData<List<TTSLanguage>> listMutableLiveDataTTSLang = new MutableLiveData<>();
    private STSTranscriptDB stsTranscriptDB;
    MutableLiveData<List<STSTranscript>> listMutableLiveDataStsTranscripts = new MutableLiveData<>();

    public MutableLiveData<List<STSTranscript>> getListMutableLiveDataStsTranscripts() {
        return listMutableLiveDataStsTranscripts;
    }

    public void setListMutableLiveDataStsTranscripts(MutableLiveData<List<STSTranscript>> listMutableLiveDataStsTranscripts) {
        this.listMutableLiveDataStsTranscripts = listMutableLiveDataStsTranscripts;
    }

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

    public void fetchLanguages() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<TTSLanguage> ttsLanguages = new ArrayList<>();

              for(DataSnapshot snapshot:  dataSnapshot.child("tts").child("languages").getChildren()){
                  TTSLanguage ttsLanguage = snapshot.getValue(TTSLanguage.class);
                  ttsLanguages.add(ttsLanguage);

              }
              listMutableLiveDataTTSLang.setValue(ttsLanguages);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    public MutableLiveData<List<TTSLanguage>> getListMutableLiveDataTTSLang() {
        return listMutableLiveDataTTSLang;
    }

    public void setListMutableLiveDataTTSLang(MutableLiveData<List<TTSLanguage>> listMutableLiveDataTTSLang) {
        this.listMutableLiveDataTTSLang = listMutableLiveDataTTSLang;
    }

    public List<String> getStringLangList(List<TTSLanguage> ttsLanguages) {
        List<String> stringList = new ArrayList<>();
        for(int i=0;i<ttsLanguages.size();i++){
            stringList.add(ttsLanguages.get(i).getName());
        }
        return stringList;


    }
    public STSTranscriptDB getSTSDB(Context context){
        stsTranscriptDB = Room.databaseBuilder(context,STSTranscriptDB.class,"STS_DB").build();
        return stsTranscriptDB;
    }

    public void getSTSTranscripts() {
        if(stsTranscriptDB!=null){
            listMutableLiveDataStsTranscripts.postValue( stsTranscriptDB.stsTranscriptDao().getallStSTranscript());

        }

    }

    public void insertSTSTranscript(String textToTranslate, String translatedText,Context context) {
        if(stsTranscriptDB!=null){
            STSTranscript stsTranscript = new STSTranscript();
            stsTranscript.setFrom(textToTranslate);
            stsTranscript.setTo(translatedText);
            stsTranscriptDB.stsTranscriptDao().insertSTSTranscript(stsTranscript);
            stsTranscriptDB.stsTranscriptDao().getallStSTranscript();
        }
        else{
            stsTranscriptDB = getSTSDB(context);
            STSTranscript stsTranscript = new STSTranscript();
            stsTranscript.setFrom(textToTranslate);
            stsTranscript.setTo(translatedText);
            stsTranscriptDB.stsTranscriptDao().insertSTSTranscript(stsTranscript);
            stsTranscriptDB.stsTranscriptDao().getallStSTranscript();

        }
    }

    public void deleteSTS(STSTranscript stsTranscript,Context context) {
        if(stsTranscriptDB!=null){
            stsTranscriptDB.stsTranscriptDao().deleteSTSTranscript(stsTranscript);
            stsTranscriptDB.stsTranscriptDao().getallStSTranscript();
        }
        else{
            stsTranscriptDB = getSTSDB(context);
            stsTranscriptDB.stsTranscriptDao().deleteSTSTranscript(stsTranscript);
            stsTranscriptDB.stsTranscriptDao().getallStSTranscript();
        }
    }
}
