package com.tts.anuvad.repositories;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.tts.anuvad.models.STSTranscript;

import java.util.List;

@Dao
public interface STSTranscriptDao {
    @Query("select * from STSTranscript")
    List<STSTranscript> getallStSTranscript();
    @Delete
    void deleteSTSTranscript(STSTranscript stsTranscript);
    @Insert
    void insertSTSTranscript(STSTranscript stsTranscript);
}
