package com.tts.anuvad.ui.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.mlkit.nl.languageid.LanguageIdentification;
import com.google.mlkit.nl.languageid.LanguageIdentifier;

import com.tts.anuvad.R;
import com.tts.anuvad.models.STSTranscript;
import com.tts.anuvad.models.TTSLanguage;
import com.tts.anuvad.models.TTSResponse;
import com.tts.anuvad.models.TranslateResponse;
import com.tts.anuvad.repositories.STSTranscriptDB;
import com.tts.anuvad.ui.adapters.RvStsTranscriptAdapter;
import com.tts.anuvad.ui.fragments.STSFragment;
import com.tts.anuvad.viewmodels.ViewModelMain;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
EditText editTextToTranslate,editTextTranslatedText;
ImageButton imageButtonTTS;
ViewModelMain viewModelMain ;
    private String langCode;
    private MediaPlayer mediaPlayer;

    private ProgressBar progressBar;
    private int REQUEST_CAMERA_PERMISSION_RESULT = 10;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private static String TAG = "MAIN_ACTIVITY";
    private Spinner spinnerFrom,spinnerTo;
    private List<String> languages;

    private int langFromPosition;
    private int langToPosition;
    private List<TTSLanguage> languageList;
    private RecyclerView recyclerView;
    private String textToTranslate;
    private String translatedText;
    private BottomNavigationView bottomNavigationView;
    private FrameLayout frameLayout;
   ;

    public ViewModelMain getViewModelMain() {
        return viewModelMain;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);
        firebaseAuth = FirebaseAuth.getInstance();
        signInAnonymously();
        viewModelMain = new ViewModelProvider(this).get(ViewModelMain.class);
        editTextToTranslate = findViewById(R.id.editTextTextMultiLine);
        progressBar = findViewById(R.id.progressBarModel);
        editTextTranslatedText  =findViewById(R.id.editTextTextTranslatedText);
        imageButtonTTS = findViewById(R.id.imageButtonTextToSpeech);
        spinnerFrom = findViewById(R.id.spinnerFrom);
        spinnerTo = findViewById(R.id.spinnerTo);
        frameLayout = findViewById(R.id.frameLayoutMain);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        recyclerView = findViewById(R.id.recyclerViewSTSTranscript);
        viewModelMain.getListMutableLiveDataTTSLang().observe(this, new Observer<List<TTSLanguage>>() {
            @Override
            public void onChanged(List<TTSLanguage> ttsLanguages) {
                languages = viewModelMain.getStringLangList(ttsLanguages);
                languageList = ttsLanguages;
                ArrayAdapter<String> arrayAdapter  = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_spinner_dropdown_item, languages);
                spinnerFrom.setAdapter(arrayAdapter);
                spinnerTo.setAdapter(arrayAdapter);
            }
        });
        viewModelMain.fetchLanguages();
        Fragment fragment = new STSFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayoutMain,fragment,"STS").commit();
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.sts:
                        STSFragment stsFragment = new STSFragment();
                        getSupportFragmentManager().beginTransaction().add(R.id.frameLayoutMain,stsFragment,"STS").commit();
                        Log.d(TAG,"STS clicked");
                        return true;
                }
                return false;
            }
        });

        editTextTranslatedText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length()>0){
                    translatedText = s.toString();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            viewModelMain.insertSTSTranscript(textToTranslate,translatedText,getApplicationContext());

                        }
                    }).start();
                }

            }
        });

    }

    private void signInAnonymously() {
        firebaseAuth.signInAnonymously().addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                user = firebaseAuth.getCurrentUser();
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    Log.d(TAG,"Anonymous_Failed");
                    }
                });
    }

    public  void identifyLanguage(String text){

        LanguageIdentifier languageIdentifier = LanguageIdentification.getClient();
        languageIdentifier.identifyLanguage(text)
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

    public void deleteSTS(STSTranscript stsTranscript) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                viewModelMain.deleteSTS(stsTranscript,getApplicationContext());
            }
        }).start();
    }
}