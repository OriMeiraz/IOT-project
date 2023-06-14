package com.example.tutorial6;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import com.github.mikephil.charting.utils.ColorTemplate;
import com.opencsv.CSVReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import java.util.List;


public class LoadCSV extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_csv);
        Button BackButton = (Button) findViewById(R.id.button_back);
        Button display = (Button) findViewById(R.id.load_csv_btn);
        EditText name = findViewById(R.id.load_csv_file);
        LineChart lineChart = (LineChart) findViewById(R.id.line_chart);
        /*


         */


        BackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClickBack();
            }
        });

        display.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String[]> csvData = new ArrayList<>();

                csvData= CsvRead("/sdcard/csv_dir/"+name.getText());
                csvData= CsvRead("/sdcard/csv_dir/"+name.getText());
                LineDataSet xDataSet =  new LineDataSet(DataValues(csvData, 1),"x");
                xDataSet.setColor(ColorTemplate.rgb("ff0000"));
                xDataSet.setCircleColor((ColorTemplate.rgb("ff0000")));

                LineDataSet yDataSet =  new LineDataSet(DataValues(csvData, 2),"y");
                yDataSet.setColor(ColorTemplate.rgb("00ff00"));
                yDataSet.setCircleColor(ColorTemplate.rgb("00ff00"));

                LineDataSet zDataSet =  new LineDataSet(DataValues(csvData, 3),"z");
                zDataSet.setColor(ColorTemplate.rgb("0000ff"));
                zDataSet.setCircleColor(ColorTemplate.rgb("0000ff"));

                ArrayList<ILineDataSet> dataSets = new ArrayList<>();

                dataSets.add(xDataSet);
                dataSets.add(yDataSet);
                dataSets.add(zDataSet);

                LineData data = new LineData(dataSets);
                lineChart.setData(data);
                lineChart.invalidate();
            }
        });
    }

    private void ClickBack(){
        finish();
    }

    private ArrayList<String[]> CsvRead(String path){
        ArrayList<String[]> CsvData = new ArrayList<>();
        try {
            File file = new File(path);
            CSVReader reader = new CSVReader(new FileReader(file));
            String[] nextline;
            while((nextline = reader.readNext())!= null){
                if(nextline != null){
                    CsvData.add(nextline);
                }
            }

        }catch (Exception e){}
        return CsvData;
    }

    private ArrayList<Entry> DataValues(ArrayList<String[]> csvData, int index){
        ArrayList<Entry> dataVals = new ArrayList<Entry>();
        for (int i = 6; i < csvData.size(); i++){
            dataVals.add(new Entry(Float.parseFloat(csvData.get(i)[0]),
                    Float.parseFloat(csvData.get(i)[index])));


        }

        return dataVals;
    }

}