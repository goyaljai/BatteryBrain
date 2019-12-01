package com.example.batterylifepredection;

import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;
import com.samsung.lableencoder.EncodedDataMapper;
import com.smasung.scale.*;
import com.smasung.scale.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class ScallerUtils {

    public void Scale(){
        String inputFilePath = Constants.OUT_LABLE_ENCODED_FILE;
        String absPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        Scale scler = new Scale();
        File inputFile = new File(absPath,inputFilePath);
        ScaleStatistics scaleStatistics = getScaleStatistics();
        if(scaleStatistics.hm!=null){
            com.smasung.scale.Pair pair = scaleStatistics.hm.get(0);
            Log.e("JKT",""+pair.mean.length+""+pair.mean[0]);
        }
        Scale.scaleTest(inputFile,absPath+Constants.SCALED_OUT_FILE,scaleStatistics.hm);



    }


    private ScaleStatistics getScaleStatistics() {
        ScaleStatistics scaleStatistics = null;
        String scalerPath = Constants.SCALER_PATH;
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), scalerPath);
        if (!file.exists()) {
            try {
                throw new Exception("encoder is not present at given path");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        String line = "";
        StringBuffer scalerJSON = new StringBuffer();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            while ((line = br.readLine()) != null) {
                scalerJSON.append(line);

            }
            Log.e("JKT","scalerStats::"+scalerJSON.toString());
            scaleStatistics = new Gson().fromJson(scalerJSON.toString(), ScaleStatistics.class);
        } catch (FileNotFoundException e) {
            Log.e("JKT",e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("JKT",e.getMessage());
            e.printStackTrace();
        }

        return scaleStatistics;
    }

}
