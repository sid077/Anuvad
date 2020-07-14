package com.tts.anuvad;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.nl.languageid.LanguageIdentification;
import com.google.mlkit.nl.languageid.LanguageIdentifier;

import com.tts.anuvad.models.TTSResponse;
import com.tts.anuvad.models.TranslateResponse;
import com.tts.anuvad.viewmodels.ViewModelMain;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
EditText editTextToTranslate,editTextTranslatedText;
ImageButton imageButtonTTS;
ViewModelMain viewModelMain ;
    private String langCode;
    private MediaPlayer mediaPlayer;

    private ProgressBar progressBar;
    private int REQUEST_CAMERA_PERMISSION_RESULT = 10;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewModelMain = new ViewModelProvider(this).get(ViewModelMain.class);
        editTextToTranslate = findViewById(R.id.editTextTextMultiLine);
        progressBar = findViewById(R.id.progressBarModel);
        editTextTranslatedText  =findViewById(R.id.editTextTextTranslatedText);
        imageButtonTTS = findViewById(R.id.imageButtonTextToSpeech);


        viewModelMain.getTranslateResponseMutableLiveData().observe(this, new Observer<TranslateResponse>() {
            @Override
            public void onChanged(TranslateResponse translateResponse) {
                editTextTranslatedText.setText(translateResponse.getData().getTranslateTextResponseList().get(0).getText());
                viewModelMain.convertTTS(translateResponse.getData().getTranslateTextResponseList().get(0).getText(),"hi-IN");

            }
        });
        imageButtonTTS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isRecordPermissionGranted()){
                    return;
                }
                else{
                    Intent intent=  new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"en_US");
                    startActivityForResult(intent,1);

                }

            }
        });
        viewModelMain.getTtsResponseMutableLiveData().observe(this, new Observer<TTSResponse>() {
            @Override
            public void onChanged(TTSResponse ttsResponse) {
                byte[] bytes = Base64.decode(ttsResponse.getAudio(),Base64.DEFAULT);
                mediaPlayer = new MediaPlayer();
                try {

                    File file = new File(getCacheDir()+"/ttsfile.mp3");
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    fileOutputStream.write(bytes);
                    fileOutputStream.close();
                    mediaPlayer.setDataSource(getCacheDir()+"/ttsfile.mp3");
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
        editTextToTranslate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length()>0){
                    identifyLanguage();
                }
            }
        });
//       Credential credential =
//                GoogleAccountCredential.usingOAuth2(this, Collections.singleton(TasksScopes.TASKS));
//        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
//        credential.setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, null));
//        // Tasks client
//        service =
//                new com.google.api.services.tasks.Tasks.Builder(httpTransport, jsonFactory, credential)
//                        .setApplicationName("Google-TasksAndroidSample/1.0").build();

    }
    public  void identifyLanguage(){

        LanguageIdentifier languageIdentifier = LanguageIdentification.getClient();
        languageIdentifier.identifyLanguage(editTextToTranslate.getText().toString())
                .addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        Log.d("Lang identified",s);
                        Locale locale = new Locale(s);
                        langCode = locale.getLanguage();
                        int x=0;
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Lang detect failed",e.getMessage());
                    }
                });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1&&resultCode==RESULT_OK&&data !=null){
            ArrayList<String>s = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            editTextToTranslate.setText(s.get(0));
            viewModelMain.translate(s.get(0),"hi");

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==REQUEST_CAMERA_PERMISSION_RESULT&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
            imageButtonTTS.callOnClick();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mediaPlayer !=null){
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }
    public boolean isRecordPermissionGranted(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED) {
           return true;
        } else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
                Toast.makeText(this,
                        "App required access to audio", Toast.LENGTH_SHORT).show();
            }
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO
            }, REQUEST_CAMERA_PERMISSION_RESULT);
            return  false;
        }
    }
}