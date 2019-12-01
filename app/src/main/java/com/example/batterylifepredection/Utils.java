package com.example.batterylifepredection;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Utils {

    private static double[] getCurrentUserContext(File inputFile){
        FileReader fileReader = null;
        double currentContext[] = null;
        try {
            Log.e("JKT","inside getCurrentUserContext");
            fileReader = new FileReader(inputFile);
            BufferedReader br = new BufferedReader(fileReader);

            String line = br.readLine();
            String columnData[] = line.split(",");
            currentContext = new double[columnData.length];
            for(int i=0;i<columnData.length;i++){
                currentContext[i] = Double.parseDouble(columnData[i]);
                Log.e("JKT","currentContext[i]"+currentContext[i]);
            }
        } catch (FileNotFoundException e) {
            Log.e("JKT","fof");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return currentContext;
    }

    public static int getDrain(){
        String absPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        File inputFile = new File(absPath,Constants.INPUT_TEST_FILE_PATH);
        int drain=0;
        try {
            FileReader fileReader = new FileReader(inputFile);
            BufferedReader bufferedReader=new BufferedReader(fileReader);
            String line= bufferedReader.readLine();
            String[] columnData=line.split(",");
            drain=Integer.parseInt(columnData[15]);
        }
        catch(IOException e){
            Log.e("JKT","Could not get drain");
        }
        return drain;
    }
    public static String predict(Context context) {
        String res = "";
        Log.e("JKT","predict start");
        LableEncoderUtils lableEncoderUtils = new LableEncoderUtils();

        lableEncoderUtils.encodeFile(Constants.COLUMN_LIST);
        //TODO- scale the output of encoded file and save it at Constants.SCALED_OUT_FILE
        ScallerUtils scallerUtils = new ScallerUtils();
        scallerUtils.Scale();
        String absSDPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        File scaledFile = new File(absSDPath,Constants.SCALED_OUT_FILE);
        double[] currentUserContext = getCurrentUserContext(scaledFile);
        KNNFinder knnFinder = new KNNFinder();
        Neighbours neighbours = knnFinder.getNeighbours(Constants.K,currentUserContext);
        Log.e("JKT","KNNFINDER worked Fine");
        int row = neighbours.nbrList[0];
        Log.e("JKT","row::"+row);
        try {
            Log.e("JKT","predicting");
            res = new TimeCalculator().predict(row,absSDPath+Constants.MAPPING_FILE_PATH,absSDPath+Constants.SECONDARY_FILE,getDrain());
            Log.e("JKT","final res::"+res);
            Toast.makeText(context," "+res,Toast.LENGTH_LONG).show();
            Log.e("JKT","predict end");
        } catch (Exception e) {
            Log.e("JKT","main exception"+" "+e.getMessage());
            e.printStackTrace();
        }
        return res;
    }

    public static void startService(Context mContext) {

    }
}
