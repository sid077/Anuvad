package com.tts.anuvad.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.tts.anuvad.ui.activities.MainActivity;
import com.tts.anuvad.ui.adapters.RvStsTranscriptAdapter;
import com.tts.anuvad.viewmodels.ViewModelMain;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;

public class STSFragment extends Fragment {
    MainActivity mainActivity ;
    EditText editTextToTranslate,editTextTranslatedText;
    ImageButton imageButtonTTS;
    ViewModelMain viewModelMain ;
    private String langCode;
    private MediaPlayer mediaPlayer;

    private ProgressBar progressBar;
//    private int REQUEST_CAMERA_PERMISSION_RESULT = 10;
//    private FirebaseAuth firebaseAuth;
//    private FirebaseUser user;
    private static String TAG = "MAIN_ACTIVITY";
    private Spinner spinnerFrom,spinnerTo;
    private List<String> languages;
    private Switch switchStoreSts;
    private int langFromPosition;
    private int langToPosition;
    private List<TTSLanguage> languageList;
    private RecyclerView recyclerView;
    private String textToTranslate;
    private String translatedText;
    public boolean toStore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mainActivity = (MainActivity) getActivity();
        viewModelMain = mainActivity.getViewModelMain();
        View view=inflater.inflate(R.layout.fragment_sts,container,false);
       viewModelMain = new ViewModelProvider(this).get(ViewModelMain.class);
        editTextToTranslate = view.findViewById(R.id.editTextTextMultiLine);
        progressBar = view.findViewById(R.id.progressBarModel);
        editTextTranslatedText  = view.findViewById(R.id.editTextTextTranslatedText);
        imageButtonTTS = view.findViewById(R.id.imageButtonTextToSpeech);
        spinnerFrom = view.findViewById(R.id.spinnerFrom);
        spinnerTo = view.findViewById(R.id.spinnerTo);
        recyclerView = view.findViewById(R.id.recyclerViewSTSTranscript);
        switchStoreSts = view.findViewById(R.id.switchStoreSts);

        return view;

    }

    @Override
    public void onStart() {
        super.onStart();
        viewModelMain.getListMutableLiveDataTTSLang().observe(this, new Observer<List<TTSLanguage>>() {
            @Override
            public void onChanged(List<TTSLanguage> ttsLanguages) {
                languages = viewModelMain.getStringLangList(ttsLanguages);
                languageList = ttsLanguages;
                ArrayAdapter<String> arrayAdapter  = new ArrayAdapter<String>(getContext(),android.R.layout.simple_spinner_dropdown_item, languages);
                spinnerFrom.setAdapter(arrayAdapter);
                spinnerTo.setAdapter(arrayAdapter);
            }
        });
        viewModelMain.fetchLanguages();


        viewModelMain.getTranslateResponseMutableLiveData().observe(this, new Observer<TranslateResponse>() {
            @Override
            public void onChanged(TranslateResponse translateResponse) {

                editTextTranslatedText.setText(translateResponse.getData().getTranslateTextResponseList().get(0).getText());
                viewModelMain.convertTTS(translateResponse.getData().getTranslateTextResponseList().get(0).getText(),languageList.get(langToPosition).getBcp());

            }
        });
        viewModelMain.getListMutableLiveDataStsTranscripts().observe(this, new Observer<List<STSTranscript>>() {
            @Override
            public void onChanged(List<STSTranscript> stsTranscripts) {
                if(stsTranscripts!=null&&stsTranscripts.size()>0){
                    RvStsTranscriptAdapter adapter = new RvStsTranscriptAdapter(stsTranscripts,mainActivity);
                    recyclerView.setAdapter(adapter);
                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL,false));
                }


            }
        });
        imageButtonTTS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mainActivity.isRecordPermissionGranted()){
                    return;
                }
                else{
                    Intent intent=  new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,languageList.get(langFromPosition).getBcp());//TODO:Exception to be resolved
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

                    File file = new File(getActivity().getCacheDir()+"/ttsfile.mp3");
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    fileOutputStream.write(bytes);
                    fileOutputStream.close();
                    mediaPlayer.setDataSource(getActivity().getCacheDir()+"/ttsfile.mp3");
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
        SharedPreferences sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        toStore = sharedPreferences.getBoolean("toStore",false);
        switchStoreSts.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    recyclerView.setVisibility(View.VISIBLE);
                    sharedPreferences.edit().putBoolean("toStore",true).apply();

                }
                else{
                    sharedPreferences.edit().putBoolean("toStore",false).apply();
                    recyclerView.setVisibility(View.GONE);
                }
            }
        });

        if(toStore){
            switchStoreSts.setChecked(true);
        }
        if(switchStoreSts.isChecked()){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    STSTranscriptDB stsTranscriptDB = viewModelMain.getSTSDB(getContext());
                    viewModelMain.getSTSTranscripts();

                }
            }).start();
        }
        spinnerFrom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                langFromPosition = position;

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinnerTo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                langToPosition =position;


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

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
                   identifyLanguage(s.toString());
                    textToTranslate = s.toString();
                }
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
                    if(switchStoreSts.isChecked()){
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                viewModelMain.insertSTSTranscript(textToTranslate,translatedText,getContext());
                                viewModelMain.getListMutableLiveDataStsTranscripts();

                            }
                        }).start();
                    }

                }

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
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("frag","on activity of frag");
        if(requestCode==1&&resultCode==RESULT_OK&&data !=null){
            ArrayList<String> s = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            editTextToTranslate.setText(s.get(0));
            viewModelMain.translate(s.get(0),languageList.get(langToPosition).getIso());

        }

    }
}
