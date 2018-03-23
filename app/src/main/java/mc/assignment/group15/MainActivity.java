package mc.assignment.group15;

import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

//package imported from http://www.android-graphview.org
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;


public class MainActivity extends AppCompatActivity {

    Thread updateThread = null; //thread to update graph

    boolean runPressed = false; //variable to keep the thread running
    boolean downloadPressed = false; //variable to avoid concurrent downloads


    private GraphHandler myGraph = null;
    private AccelerometerHandler accel = null;
    private DatabaseHandler myDB = null;
    private RemoteFileHandler client = null;
    private String SERVER_URL = "http://impact.asu.edu/CSE535Spring18Folder/";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText Name = (EditText) findViewById(R.id.name);
        final RadioGroup Sex = (RadioGroup) findViewById(R.id.sex);
        final EditText Age = (EditText) findViewById(R.id.age);
        final EditText ID = (EditText) findViewById(R.id.id);

        Button runBtn = (Button) findViewById(R.id.RunBtn);     //run button
        Button stopBtn = (Button) findViewById(R.id.StopBtn);   //stop button
        Button uploadBtn = (Button) findViewById(R.id.UploadBtn);   //Upload button
        Button downloadBtn = (Button) findViewById(R.id.DownloadBtn);   //Download button
        FrameLayout graphFrame = (FrameLayout) findViewById(R.id.graphFrame);

        //database handler
        myDB = new DatabaseHandler(this);
        myDB.insertData(myDB.getWritableDatabase(), 0.0f, 0.0f, 0.0f);

        //remote file handler
        client = new RemoteFileHandler(SERVER_URL);
        //accelerometer handler
        accel = new AccelerometerHandler(this);

        //graph handler
        myGraph = new GraphHandler(this, myDB.getReadableDatabase());
        myDB.close();
        //add graph to its (frame) layout
        graphFrame.addView(myGraph.getGraph());


        //when run button is clicked:
        runBtn.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                if (!inputValidation(ID, Name, Age, Sex)) return;

                //ignore this click event if Run button was previously clicked.
                if (runPressed) return;

                //remember this click
                runPressed = true;

                //register accelerometer event listener
                accel.registerListener();

                //restore graph data
                myGraph.restoreGraph();

                //define new thread for updating graph
                updateThread = new Thread(new Runnable() {
                    public void run() {
                        //thread runs until "runPressed" is reset by clicking the Stop button
                        while(runPressed) {
                            //sleep
                            try {
                                Thread.sleep(500);
                            } catch(InterruptedException e) {
                                e.printStackTrace();
                            }

                            float x = accel.readX();
                            float y = accel.readY();
                            float z = accel.readZ();

                            //update the graph data
                            myGraph.updateSeries(x, y, z);

                            //insert into data base
                            myDB.insertData(myDB.getWritableDatabase(), x, y, z);
                            myDB.close();
                        }
                    }
                });

                //start thread
                updateThread.start();

            }
        });

        //when Stop button is clicked
        stopBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                runPressed = false;

                if (updateThread != null) {
                    //wait for the thread to exit, before making changes to the graph's data series
                    try {
                        updateThread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                myGraph.clearGraph();
                accel.unregisterListener();
            }
        });

        //when upload button is clicked:
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!inputValidation(ID, Name, Age, Sex)) return;

                new Thread(new Runnable() {
                    public void run() {
                        client.uploadFile(myDB.getReadableDatabase().getPath());
                        myDB.close();
                    }
                }).start();
            }
        });

        //when download button is clicked:
        downloadBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (!inputValidation(ID, Name, Age, Sex)) return;

                if (downloadPressed) return;

                downloadPressed = true;

                new Thread(new Runnable() {
                    public void run() {

                        File downloadedFile = client.downloadFile("Group15.db");
                        if (downloadedFile != null) {
                            SQLiteDatabase downloadedDB = SQLiteDatabase.openDatabase(downloadedFile.getAbsolutePath(), null,0);
                            myGraph.updateFromDB(downloadedDB, 10);
                            downloadedDB.close();
                            downloadPressed = false;
                        }
                    }
                }).start();
            }
        });


    }

    private boolean inputValidation(EditText patient_id, EditText patient_name,
                                    EditText patient_age, RadioGroup patient_sex) {

        if (patient_id.getText().toString().trim().equals("")) {
            Toast.makeText(MainActivity.this, "Patient Id cannot be null", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (patient_name.getText().toString().equals("")) {
            Toast.makeText(MainActivity.this, "Patient name cannot be null", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (patient_age.getText().toString().equals("")) {
            Toast.makeText(MainActivity.this, "Age cannot be null", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (patient_sex.getCheckedRadioButtonId() != R.id.male && patient_sex.getCheckedRadioButtonId() != R.id.female) {
            Toast.makeText(MainActivity.this, "Please select Male or Female", Toast.LENGTH_SHORT).show();
            return false;
        }
        String tablename = patient_name.getText().toString() + "_" + patient_id.getText().toString() + "_" + patient_age.getText().toString();
        if (patient_sex.getCheckedRadioButtonId() == R.id.male) {
            tablename = tablename + "_male";
        } else
            tablename = tablename + "_female";

        return true;
    }


    @Override
    public void onDestroy() {

        //make sure that the thread exits before closing this activity
        if (updateThread != null) {
            runPressed = false;
            try {
                updateThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        super.onDestroy();
    }

}