package mc.assignment.group15;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Group15.db";

    public static String tablename = "Name_ID_Age_Sex";
    public static final String timestamp = "timestamp";
    public static final String x_values = "xvalues";
    public static final String y_values = "yvalues";
    public static final String z_values = "zvalues";

    public DatabaseHandler(Context ctx) {
        super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS"+tablename+"( "+
                timestamp+" INTEGER PRIMARY KEY, "+ x_values +" REAL, "+
                y_values +" REAL, "+ z_values +" REAL);");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + tablename);
        // Create tables again
        onCreate(db);
    }

    public static void insertData(SQLiteDatabase db, float x, float y, float z) {
        ContentValues values = new ContentValues();
        values.put(x_values, x);
        values.put(y_values, y);
        values.put(z_values, z);
        db.insert(tablename, null, values);
    }

    public static float[][] readData(SQLiteDatabase db, int count) {
        Cursor cursor = db.rawQuery("select "+x_values+","+y_values+","+z_values+" from " + tablename
                + " order by timestamp DESC limit " + (count+1), null);
        cursor.moveToFirst();
        float values[][] = new float[count][3];
        int i = 0;

        while (cursor.moveToNext() && i!=count) {
            values[i][0] = Float.parseFloat(cursor.getString(cursor.getColumnIndex(x_values)));
            values[i][1] = Float.parseFloat(cursor.getString(cursor.getColumnIndex(y_values)));
            values[i][2] = Float.parseFloat(cursor.getString(cursor.getColumnIndex(z_values)));
            i++;
        }
        cursor.close();
        return values;
    }

    public static int countEntries(SQLiteDatabase db) {
        String countQuery = "SELECT * FROM " + tablename;
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }
}
