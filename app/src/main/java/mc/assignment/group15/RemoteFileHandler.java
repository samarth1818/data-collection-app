package mc.assignment.group15;

import android.os.Environment;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static android.content.ContentValues.TAG;


public class RemoteFileHandler {
    private String SERVER_URL = "";
    private String DOWNLOAD_PATH = "/Android/data/CSE535_ASSIGNMENT2_DOWNLOAD/";

    public RemoteFileHandler(String serverURL) {
        //must pass server url in constructor
        SERVER_URL = serverURL;
    }


    public int uploadFile(final String selectedFilePath) {

        int serverResponseCode = 0;

        HttpURLConnection connection;
        DataOutputStream dataOutputStream;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";


        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File selectedFile = new File(selectedFilePath);


        String[] parts = selectedFilePath.split("/");
        final String fileName = parts[parts.length - 1];

        if (!selectedFile.isFile()) {
            Log.d(TAG, "uploadFile: File does not exists!");
            return 0;
        } else {
            try {
                FileInputStream fileInputStream = new FileInputStream(selectedFile);
                URL url = new URL(SERVER_URL + "UploadToServer.php");
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);//Allow Inputs
                connection.setDoOutput(true);//Allow Outputs
                connection.setUseCaches(false);//Don't use a cached Copy
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("ENCTYPE", "multipart/form-data");
                connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                connection.setRequestProperty("uploaded_file", selectedFilePath);

                //creating new dataoutputstream
                dataOutputStream = new DataOutputStream(connection.getOutputStream());

                //writing bytes to data outputstream
                dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
                dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                        + selectedFilePath + "\"" + lineEnd);

                dataOutputStream.writeBytes(lineEnd);

                //returns no. of bytes present in fileInputStream
                bytesAvailable = fileInputStream.available();
                //selecting the buffer size as minimum of available bytes or 1 MB
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                //setting the buffer as byte array of size of bufferSize
                buffer = new byte[bufferSize];

                //reads bytes from FileInputStream(from 0th index of buffer to buffersize)
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                //loop repeats till bytesRead = -1, i.e., no bytes are left to read
                while (bytesRead > 0) {
                    //write the bytes read from inputstream
                    dataOutputStream.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }

                dataOutputStream.writeBytes(lineEnd);
                dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                serverResponseCode = connection.getResponseCode();
                String serverResponseMessage = connection.getResponseMessage();

                Log.i(TAG, "Server Response is: " + serverResponseMessage + ": " + serverResponseCode);

                //response code of 200 indicates the server status OK
                if (serverResponseCode == 200) {
                    // upload successful...
                }

                //closing the input and output streams
                fileInputStream.close();
                dataOutputStream.flush();
                dataOutputStream.close();


            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.d(TAG, "uploadFile: File not found!");
            } catch (MalformedURLException e) {
                e.printStackTrace();
                Log.d(TAG, "uploadFile: URL error!");
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "uploadFile: Read or write file error");
            }
            return serverResponseCode;
        }
    }

    public File downloadFile(String fileToDownload) {
        File Storage = null;
//        File outputFile = null;
        //HttpURLConnection connection;
        DataOutputStream dataOutputStream;
        if (isSDCardPresent()) {
            Log.i(TAG,"In SD CARD SECTION \n \n " + Environment.getExternalStorageDirectory() + "\n\n\n");
            Storage = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + DOWNLOAD_PATH);
        } else {
            Log.e(TAG, "downloadFile: No SD card!");
            return null;
        }

        //If File is not present create directory
        if (!Storage.exists()) {
            boolean res = Storage.mkdirs();
            res = Storage.mkdirs();
            Log.i(TAG, "Directory Created." + res + Storage.exists());
        }

        File outputFile = null;

        try {
            URL url = new URL(SERVER_URL + fileToDownload);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            // connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.connect();
            connection.setReadTimeout(3*1000);
            connection.setConnectTimeout(3*1000);

            //If Connection response is not OK then show Logs
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.e(TAG, "Server returned HTTP " + connection.getResponseCode()
                        + " " + connection.getResponseMessage());
            }

            //+   dataInputStream = new DataOutputStream(connection.getOutputStream());
            //Log.i(TAG, "input stream = "+isSDCardPresent());
            //Get File if SD card is present


            outputFile = new File(Storage, fileToDownload);//Create Output file in Main File
            outputFile.setWritable(true);

            Log.i(TAG, "output file "+outputFile.getName() + "  "+outputFile.exists());

            //Create New File if not present
            if (!outputFile.exists()) {
                outputFile.createNewFile();
                outputFile.canExecute();
                Log.i(TAG, "File Created");
            }

            InputStream fileinput = connection.getInputStream();//Get InputStream for connection
            FileOutputStream dataOutputStrea = new FileOutputStream(outputFile);
            Log.i(TAG, "file output");

            byte[] buffer = new byte[1024];
            int bufferLength = 0;
            while ((bufferLength = fileinput.read(buffer)) != -1) {
                dataOutputStrea.write(buffer, 0, bufferLength);//Write new file
            }

            //Close all connection after doing task
            dataOutputStrea.close();
            fileinput.close();

        } catch (Exception e) {

            e.printStackTrace();
            //outputFile = null;
            Log.e(TAG, "Download Error Exception " + e.getMessage());
        }

        return outputFile;
    }


    public boolean isSDCardPresent() {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            return true;
        }
        return false;

    }
}