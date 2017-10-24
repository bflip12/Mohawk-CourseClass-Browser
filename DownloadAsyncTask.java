package ca.mohawk.bobbyfilippopoulos.project2;

import android.app.Activity;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import com.google.gson.Gson;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import ca.mohawk.bobbyfilippopoulos.project2.Course;
import ca.mohawk.bobbyfilippopoulos.project2.MyDbHelper;
import ca.mohawk.bobbyfilippopoulos.project2.courseSpinner;

/**
 * Created by BobbyFilippopoulos on 2017-04-15.
 */

//DownloadAsyncTask downloads the course website file, creating a gson object which can be read to the database
public class DownloadAsyncTask extends AsyncTask<String, Void, String> {

    private Activity myActivity;


    public DownloadAsyncTask(Activity inActivity){
        myActivity = inActivity;
    }

    //doInBackground reads the course file and builds a string upon the content
    @Override
    protected String doInBackground(String... params) {

        Log.d("log", "Starting background task");

        String results = null;

        HttpGet httpGet = new HttpGet(params[0]);

        StringBuilder sb = new StringBuilder();

        try
        {
            DefaultHttpClient httpClient = new DefaultHttpClient(new BasicHttpParams());
            HttpResponse response = httpClient.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();

            if(statusCode == 200)
            {
                HttpEntity entity = response.getEntity();
                InputStream inputStream = entity.getContent();

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
                String line = null;
                while((line = bufferedReader.readLine()) != null)
                {
                    sb.append(line);
                }
            }
        }
        catch(IOException ex)
        {

        }

        results = sb.toString();
        return results;
    }

    //onPostExecute converts reads gson object to the database, resets eachtime the program is loaded incase of changes
    protected void onPostExecute (String result)
    {
        MyDbHelper myDbHelp = new MyDbHelper(myActivity);


        Gson gson = new Gson();
        courseSpinner courseSpinnerCl = gson.fromJson(result, courseSpinner.class);

        ArrayAdapter<Course> courseArrayAdapter =
                new ArrayAdapter<Course>(myActivity, android.R.layout.simple_list_item_1, courseSpinnerCl);

        SQLiteDatabase db = myDbHelp.getWritableDatabase();
        db.beginTransaction();
        try {
            //reset the database each upload incase of changes
            db.execSQL("DELETE FROM courseTable");
            //inserts the course object into the table for each record
            for (int i = 0; i < courseArrayAdapter.getCount(); i++) {


                ContentValues values = new ContentValues();

                Course courseObject;

                courseObject = courseArrayAdapter.getItem(i);
                values.put("program", courseObject.program);
                values.put("semesterNum", courseObject.semesterNum);
                values.put("courseCode", courseObject.courseCode);
                values.put("courseTitle", courseObject.courseTitle);
                values.put("courseDescription", courseObject.courseDescription);
                values.put("courseOwner", courseObject.courseOwner);
                values.put("optional", courseObject.optional);
                values.put("hours", courseObject.hours);

                long newRowID = db.insert("courseTable", null, values);

                Log.d("log", "New ID " + newRowID);
            }
            db.setTransactionSuccessful();
        }
        catch(Exception ex)
        {
            Log.d("log", ex.toString());
        }
        finally{db.endTransaction();}
        db.close();
        // End retrieving and setting content
/*
        Spinner courseSpinn
        r = (Spinner) myActivity.findViewById(R.id.courseDropDown);
        lv.setAdapter(adapter);

        Spinner courseDropDown = (Spinner) MainActivity.find(R.id.courseDropDown);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.list, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        myspinner.setAdapter(adapter);
        myspinner.setSelection(0, false);
        myspinner.setOnItemSelectedListener(this);

*/
    }

}

