package com.example.bibliothequeapp;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

// Version 3 : ajout du champ anneePublication dans Livre
@Database(entities = {Livre.class}, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract LivreDao livreDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "bibliotheque_database"
                    ).fallbackToDestructiveMigration(false).build();
                }
            }
        }
        return INSTANCE;
    }
}