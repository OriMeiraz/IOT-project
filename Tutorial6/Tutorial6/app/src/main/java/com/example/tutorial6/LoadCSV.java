package com.example.tutorial6;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;

import com.opencsv.CSVReader;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import android.os.Handler;

public class LoadCSV extends AppCompatActivity {
    int requestcode = 1;
    String path;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_csv);

        Button choose = (Button) findViewById(R.id.choose_file);
        Button Play = (Button) findViewById(R.id.play);
        choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
                ArrayList<String[]> csvData = new ArrayList<>();
            }
        });

        Play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = "/sdcard/"+path.split("/")[2] + "/" +path.split("/")[3];
                ArrayList<String[]> csvData = CsvRead(name);
                playFile(csvData);
            }
        });

    }
    public synchronized void playFile(ArrayList<String[]> csvData){
        boolean arrived_first_drum = false;
        for(int i = 0; i < csvData.size(); i++) {
            String sound = csvData.get(i)[0];
            if (sound.equals("1"))
                play_drum1();
            if (sound.equals("2"))
                play_drum2();
            if (sound.equals("3"))
                play_drum3();
            if (!sound.equals("0"))
                arrived_first_drum = true;
            if (arrived_first_drum) {
                try {
                    wait(50); // 20Hz
                } catch (Exception e) {
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestcode, int resultcode, Intent data) {
        super.onActivityResult(requestcode, resultcode, data);
        Uri uri = data.getData();
        path = uri.getPath();
    }



    public void openFileChooser(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");

        startActivityForResult(intent, requestcode);
    }

    public void play_drum1(){
        MediaPlayer mp =  MediaPlayer.create(this, R.raw.drum1);
        mp.start();
    }
    public void play_drum2(){
        MediaPlayer mp =  MediaPlayer.create(this, R.raw.drum2);
        mp.start();
    }
    public void play_drum3(){
        MediaPlayer mp =  MediaPlayer.create(this, R.raw.drum3);
        mp.start();
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
}