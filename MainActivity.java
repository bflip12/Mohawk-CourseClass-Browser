/* I, Bobby Filippopoulos, verify that this is my work and only my work */
package ca.mohawk.bobbyfilippopoulos.project2;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

//functionality resides in the main activity
public class MainActivity extends AppCompatActivity implements ListView.OnItemClickListener {

    //create the dbhelper class
    MyDbHelper myDbHelp = new MyDbHelper(this);

    //these boolean values are for the inital load of the spinner listeners
    Boolean initialDisplay = true;
    Boolean initialSemesterDisplay = true;

    //functionality that will happen upon start up
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //create and call the DownloadAsync task, reads the gson object and writes to the database
        DownloadAsyncTask dl = new DownloadAsyncTask(this);
        //build call
        String uri = "https://csunix.mohawkcollege.ca/~geczy/mohawkprograms.php";

        dl.execute(uri);

        //set strings
        final String debugMessage = getString(R.string.debugMessage);
        //Read from database to the drop downs


        SQLiteDatabase dbReader = myDbHelp.getReadableDatabase();

        String[] projection = {"_id, program, semesterNum, courseCode, courseTitle, courseDescription, courseOwner, optional, hours"};

        //String selection = "program" + " = ?";
        //String[] selectionArgs = { "My Title" };

        String sortOrder = "program" + " ASC";

        Cursor mycursor = dbReader.query("courseTable", projection, null, null, null, null, sortOrder);


        //on load fill the program drop down spinner with the courses
        Course programObjectRead = new Course();
        List<String> listOfPrograms = new ArrayList<String>();

        final Spinner programDropDown = (Spinner) findViewById(R.id.programDropDown);
        while (mycursor.moveToNext()) {

            programObjectRead._id = mycursor.getString(mycursor.getColumnIndex("_id"));
            programObjectRead.program = mycursor.getString(mycursor.getColumnIndex("program"));
            programObjectRead.semesterNum = mycursor.getString(mycursor.getColumnIndex("semesterNum"));
            programObjectRead.courseCode = mycursor.getString(mycursor.getColumnIndex("courseCode"));
            programObjectRead.courseTitle = mycursor.getString(mycursor.getColumnIndex("courseTitle"));
            programObjectRead.courseDescription = mycursor.getString(mycursor.getColumnIndex("courseDescription"));
            programObjectRead.courseOwner = mycursor.getString(mycursor.getColumnIndex("courseOwner"));
            programObjectRead.optional = mycursor.getString(mycursor.getColumnIndex("optional"));
            programObjectRead.hours = mycursor.getString(mycursor.getColumnIndex("hours"));

            //if the list does not already contain the program, add the program so there are no repeats
            if (!listOfPrograms.contains(programObjectRead.program)) {
                listOfPrograms.add(programObjectRead.program);
            }

        }

        ArrayAdapter<String> programArrayAdapterRead = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, listOfPrograms);

        programDropDown.setAdapter(programArrayAdapterRead);
        mycursor.close();

        //set on item selected listener for the program drop down so semesters can be updated dynamically
        programDropDown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View selectedItemView, int position, long id) {
                //initial load of the spinner fills the semester drop down based on the inital program drop down value
                if (initialDisplay == true) {

                    Spinner semesterDropDown = (Spinner) findViewById(R.id.courseDropDown);

                    String programString = adapterView.getItemAtPosition(0).toString();

                    String[] SemesterProjection = {"_id, semesterNum, program"};

                    String selection = "program" + " = ?";
                    String[] selectionArgs = {programString};

                    String semesterSortOrder = "semesterNum" + " ASC";

                    SQLiteDatabase dbReaderSemester = myDbHelp.getReadableDatabase();

                    Cursor semesterCursor = dbReaderSemester.query("courseTable", SemesterProjection, selection, selectionArgs, null, null, semesterSortOrder);

                    List<Integer> listOfSemesters = new ArrayList<Integer>();

                    while (semesterCursor.moveToNext()) {
                        Integer semesterNum = semesterCursor.getInt(semesterCursor.getColumnIndex("semesterNum"));
                        if (!listOfSemesters.contains(semesterNum)) {
                            listOfSemesters.add(semesterNum);
                        }
                    }
                    ArrayAdapter<Integer> semesterArrayAdapterRead = new ArrayAdapter<Integer>(getApplicationContext(), android.R.layout.simple_spinner_item, listOfSemesters);

                    semesterDropDown.setAdapter(semesterArrayAdapterRead);
                    initialDisplay = false;
                } else { //any load after initial, the semester drop down will update based on user selection of the program drop down
                    try {
                        Spinner semesterDropDown = (Spinner) findViewById(R.id.courseDropDown);

                        String programString = adapterView.getItemAtPosition(position).toString();

                        String[] SemesterProjection = {"_id, semesterNum, program"};

                        //based on the program, fill the cursor with semester numbers pertaining to the program
                        String selection = "program" + " = ?";
                        String[] selectionArgs = {programString};

                        String semesterSortOrder = "semesterNum" + " ASC";

                        SQLiteDatabase dbReaderSemester = myDbHelp.getReadableDatabase();

                        Cursor semesterCursor = dbReaderSemester.query("courseTable", SemesterProjection, selection, selectionArgs, null, null, semesterSortOrder);

                        List<Integer> listOfSemesters = new ArrayList<Integer>();

                        while (semesterCursor.moveToNext()) {
                            Integer semesterNum = semesterCursor.getInt(semesterCursor.getColumnIndex("semesterNum"));
                            if (!listOfSemesters.contains(semesterNum)) {
                                listOfSemesters.add(semesterNum);
                            }
                        }
                        ArrayAdapter<Integer> semesterArrayAdapterRead = new ArrayAdapter<Integer>(getApplicationContext(), android.R.layout.simple_spinner_item, listOfSemesters);

                        semesterDropDown.setAdapter(semesterArrayAdapterRead);

                        //Set List View if program is change

                        ListView programListView = (ListView) findViewById(R.id.programListView);

                        programString = adapterView.getItemAtPosition(position).toString();
                        String semesterString = semesterDropDown.getSelectedItem().toString();

                        String ListProjection[] = {"_id, semesterNum, program, courseTitle"};
                        //based on the initial program and semester number, limit the cursor
                        selection = "program" + " = ?" + " AND " + "semesterNum" + " = ?";
                        String[] selectionArgsList = {programString, semesterString};

                        semesterSortOrder = "courseTitle" + " ASC";

                        SQLiteDatabase dbReaderList = myDbHelp.getReadableDatabase();

                        Cursor courseNameCursor = dbReaderList.query("courseTable", ListProjection, selection, selectionArgsList, null, null, semesterSortOrder);

                        List<String> listOfCourses = new ArrayList<String>();

                        while (courseNameCursor.moveToNext()) {
                            String semesterNum = courseNameCursor.getString(courseNameCursor.getColumnIndex("courseTitle"));
                            if (!listOfCourses.contains(semesterNum)) {
                                listOfCourses.add(semesterNum);
                            }
                        }
                        ArrayAdapter<String> programNameArrayAdapterRead = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, listOfCourses);

                        programListView.setAdapter(programNameArrayAdapterRead);
                    }
                    catch(Exception ex)
                    {
                        Toast toast = Toast.makeText(getApplicationContext(), debugMessage, Toast.LENGTH_SHORT);
                        toast.show();
                        ListView programListView = (ListView) findViewById(R.id.programListView);
                        programListView.setAdapter(null);
                    }
                }
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            }
        });

        Spinner semesterDropDown = (Spinner) findViewById(R.id.courseDropDown);
        final ListView programListView = (ListView) findViewById(R.id.programListView);

       /* Based on the semester chosen, the list view will update with the follow course titles, on load, it will be the first displayed semester*/
        semesterDropDown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View selectedItemView, int position, long id) {
                if (initialSemesterDisplay == true)
                {
                    try {
                        Spinner programDropDown = (Spinner) findViewById(R.id.programDropDown);

                        ListView programListView = (ListView) findViewById(R.id.programListView);

                        String programString = programDropDown.getItemAtPosition(0).toString();
                        String semesterString = adapterView.getItemAtPosition(0).toString();

                        String[] SemesterProjection = {"_id, semesterNum, program, courseTitle"};

                        //based on the initial program and semester number, limit the cursor
                        String selection = "program" + " = ?" + " AND " + "semesterNum" + " = ?";
                        String[] selectionArgs = {programString, semesterString};

                        String semesterSortOrder = "courseTitle" + " ASC";

                        SQLiteDatabase dbReaderList = myDbHelp.getReadableDatabase();

                        Cursor courseNameCursor = dbReaderList.query("courseTable", SemesterProjection, selection, selectionArgs, null, null, semesterSortOrder);

                        List<String> listOfCourses = new ArrayList<String>();

                        while (courseNameCursor.moveToNext()) {
                            String semesterNum = courseNameCursor.getString(courseNameCursor.getColumnIndex("courseTitle"));
                            if (!listOfCourses.contains(semesterNum)) {
                                listOfCourses.add(semesterNum);
                            }
                        }
                        ArrayAdapter<String> programNameArrayAdapterRead = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, listOfCourses);

                        programListView.setAdapter(programNameArrayAdapterRead);
                        initialSemesterDisplay = false;
                    }
                    catch(Exception ex)
                    {
                        Toast toast = Toast.makeText(getApplicationContext(), debugMessage, Toast.LENGTH_SHORT);
                        toast.show();
                        programListView.setAdapter(null);
                    }
                }
                else //after the initial load, the list view will update based on the semester number chosen
                {
                    try
                    {
                        Spinner programDropDown = (Spinner) findViewById(R.id.programDropDown);

                        ListView programListView = (ListView) findViewById(R.id.programListView);

                        String programString = programDropDown.getSelectedItem().toString();
                        String semesterString = adapterView.getItemAtPosition(position).toString();

                        String[] SemesterProjection = {"_id, semesterNum, program, courseTitle"};

                        //based on the initial program and semester number, limit the cursor
                        String selection = "program" + " = ?" + " AND " + "semesterNum" + " = ?";
                        String[] selectionArgs = {programString, semesterString};

                        String semesterSortOrder = "courseTitle" + " ASC";

                        SQLiteDatabase dbReaderList = myDbHelp.getReadableDatabase();

                        Cursor courseNameCursor = dbReaderList.query("courseTable", SemesterProjection, selection, selectionArgs, null, null, semesterSortOrder);

                        List<String> listOfCourses = new ArrayList<String>();

                        while (courseNameCursor.moveToNext()) {
                            String semesterNum = courseNameCursor.getString(courseNameCursor.getColumnIndex("courseTitle"));
                            if (!listOfCourses.contains(semesterNum)) {
                                listOfCourses.add(semesterNum);
                            }
                        }
                        ArrayAdapter<String> programNameArrayAdapterRead = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, listOfCourses);

                        programListView.setAdapter(programNameArrayAdapterRead);
                    }
                    catch(Exception ex)
                    {
                        Toast toast = Toast.makeText(getApplicationContext(), debugMessage, Toast.LENGTH_SHORT);
                        toast.show();
                        programListView.setAdapter(null);
                    }
                }
            }


            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            }
        });

        programListView.setOnItemClickListener(this);

    }

    //based on the item selected from the listview, open a dialog with the course information
    //if the course information has not been set, it will repeat the title
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
        builder1.setTitle("Program Description");
        final String debugMessage = getString(R.string.debugMessage);

        //set dialog message from the database
        try
        {
            Spinner programDropDown = (Spinner) findViewById(R.id.programDropDown);
            Spinner semesterDropDown = (Spinner) findViewById(R.id.courseDropDown);

            ListView programListView = (ListView) findViewById(R.id.programListView);

            String programString = programDropDown.getSelectedItem().toString();
            String semesterString = semesterDropDown.getSelectedItem().toString();
            String courseTitle = programListView.getItemAtPosition(position).toString();

            String[] SemesterProjection = {"_id, semesterNum, program, courseTitle, courseDescription"};

            //based on the program, semester number and course title, limit the cursor
            String selection = "program" + " = ?" + " AND " + "semesterNum" + " = ?" + " AND " + "courseTitle" + " = ?";
            String[] selectionArgs = {programString, semesterString, courseTitle};

            SQLiteDatabase dbReaderList = myDbHelp.getReadableDatabase();

            Cursor courseNameCursor = dbReaderList.query("courseTable", SemesterProjection, selection, selectionArgs, null, null, null);

            List<String> listOfCourses = new ArrayList<String>();

            while (courseNameCursor.moveToNext()) {
                String courseDescription = courseNameCursor.getString(courseNameCursor.getColumnIndex("courseDescription"));
                builder1.setMessage(courseDescription);
            }

        }
        catch(Exception ex)
        {
            builder1.setMessage(debugMessage);

        }

        builder1.setCancelable(true);

        builder1.setNegativeButton(
                "Exit",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }
}
