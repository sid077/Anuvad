package com.tts.anuvad.repositories;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.tts.anuvad.models.STSTranscript;

@Database(entities = {STSTranscript.class},version = 1,exportSchema = false)
public abstract class STSTranscriptDB extends RoomDatabase {
    public abstract STSTranscriptDao stsTranscriptDao();
}
