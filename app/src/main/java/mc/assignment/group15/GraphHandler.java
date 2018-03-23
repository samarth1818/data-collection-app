package mc.assignment.group15;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.provider.ContactsContract;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;

public class GraphHandler {
    private GraphView graph;
    private LineGraphSeries<DataPoint> seriesX;  //data points associated with graph
    private LineGraphSeries<DataPoint> seriesY;  //data points associated with graph
    private LineGraphSeries<DataPoint> seriesZ;  //data points associated with graph

    private ArrayList<DataPoint> historyX;   //remember data when stop pressed
    private ArrayList<DataPoint> historyY;   //remember data when stop pressed
    private ArrayList<DataPoint> historyZ;   //remember data when stop pressed

    private double lastXvalue = 0;  //latest data point on X axis

    public GraphHandler(Context ctx, SQLiteDatabase db) {
        //instantiate graph
        graph = new GraphView(ctx);

        seriesX = new LineGraphSeries<>();   //instantiate series of data points
        seriesY = new LineGraphSeries<>();   //instantiate series of data points
        seriesZ = new LineGraphSeries<>();   //instantiate series of data points
        seriesX.setColor(Color.GREEN);
        seriesY.setColor(Color.RED);
        seriesZ.setColor(Color.BLUE);
        historyY = new ArrayList<>();    //instantiate historical data array
        historyX = new ArrayList<>();    //instantiate historical data array
        historyZ = new ArrayList<>();    //instantiate historical data array

        historyX.add(new DataPoint(0, 0));
        historyY.add(new DataPoint(0, 0));
        historyZ.add(new DataPoint(0, 0));

        updateFromDB(db, 10);

        graph.addSeries(seriesX);    //associate series with graph
        graph.addSeries(seriesY);    //associate series with graph
        graph.addSeries(seriesZ);    //associate series with graph

        clearGraph();

        //control amount of data (X-axis) shown on the screen
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(9);
    }

    public GraphView getGraph() {
        return graph;
    }

    public void clearGraph() {
        //reset graph's data series
        DataPoint[] resetArray = new DataPoint[1];
        resetArray[0] = new DataPoint(0, 0);
        seriesX.resetData(resetArray);
        seriesY.resetData(resetArray);
        seriesZ.resetData(resetArray);
    }

    public void restoreGraph() {
        //retrieve historical data from ArrayList into array
        DataPoint[] oldDataX = new DataPoint[historyX.size()];
        historyX.toArray(oldDataX);

        //feed array of historical data to the data series
        seriesX.resetData(oldDataX);

        //retrieve historical data from ArrayList into array
        DataPoint[] oldDataY = new DataPoint[historyY.size()];
        historyX.toArray(oldDataY);

        //feed array of historical data to the data series
        seriesX.resetData(oldDataY);

        //retrieve historical data from ArrayList into array
        DataPoint[] oldDataZ = new DataPoint[historyZ.size()];
        historyX.toArray(oldDataZ);

        //feed array of historical data to the data series
        seriesZ.resetData(oldDataZ);
    }

    public void updateSeries(float x, float y, float z) {

        //this method appends a new data point to the graph's data series
        boolean scrollToEnd = false;

        //new point's coordinate on X-axis
        lastXvalue++;

        //scroll viewport to the last X value only on this condition...
        if (lastXvalue >= 9) scrollToEnd = true;

        //fetch new random value for data-point
        DataPoint newPointX = new DataPoint(lastXvalue, x);
        DataPoint newPointY = new DataPoint(lastXvalue, y);
        DataPoint newPointZ = new DataPoint(lastXvalue, z);

        //remember this data point
        historyX.add(newPointX);
        historyY.add(newPointY);
        historyZ.add(newPointZ);

        //update graph's data series
        seriesX.appendData(newPointX, scrollToEnd, 10);
        seriesY.appendData(newPointY, scrollToEnd, 10);
        seriesZ.appendData(newPointZ, scrollToEnd, 10);

    }

    public void updateFromDB(SQLiteDatabase db, int maxCount) {
        int totalEntries = DatabaseHandler.countEntries(db);
        int count = (totalEntries < maxCount) ? totalEntries : maxCount;
        float[][] values = new float[count][3];
        values = DatabaseHandler.readData(db, maxCount);

        for (int i = 0; i < count; i++) {
            updateSeries(values[i][0], values[i][1], values[i][2]);
        }
    }
}
