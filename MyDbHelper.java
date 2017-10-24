package ca.mohawk.bobbyfilippopoulos.project2;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by BobbyFilippopoulos on 2017-04-15.
 */

//MyDbHelper class, this creates the table for sure
public class MyDbHelper extends SQLiteOpenHelper {

    private static final String SQL_CREATE =
            "CREATE TABLE courseTable (_id INTEGER PRIMARY KEY, program INTEGER, semesterNum INTEGER, courseCode TEXT," +
                    "courseTitle TEXT, courseDescription TEXT, courseOwner TEXT,optional INTEGER, hours INTEGER )";

    //name of the database
    private static final String DATABASE_NAME = "Project.db";

    private static final int DATABASE_VERSION = 2;

    //constructor
    public MyDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //executes the create on program create
    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(SQL_CREATE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        //onCreate(db);

    }
}
