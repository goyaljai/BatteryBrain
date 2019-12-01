package com.example.batterylifepredection;
import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;
import com.samsung.lableencoder.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class LableEncoderUtils {
   /*int column[] :: list of column which needed to trransform
   * note:: zero based indexing
   * save new encoded file in output path*/
    public void encodeFile(int column[]){
        EncodedDataMapper encodedDataMapper = getEncodedDataMapper();
        LableEncode lableEncode = new LableEncode();
        String absPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        File inputFile = new File(absPath,Constants.INPUT_TEST_FILE_PATH);
        String outputPath = absPath+Constants.OUT_LABLE_ENCODED_FILE;
        Log.e("JKT",""+outputPath);
        String transformed = lableEncode.transform(encodedDataMapper,column,inputFile, outputPath,false);
        try {
            File file = new File(absPath+"/battery/","output_lable_encoded.txt");
            FileWriter fileWriter = new FileWriter(outputPath);
            //BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            fileWriter.append(transformed);
            //bufferedWriter.write(transformed);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private EncodedDataMapper getEncodedDataMapper(){
        EncodedDataMapper encodedDataMapper = null;
        String encoderPath = Constants.SAVED_ENCODER_PATH;
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),encoderPath);
        if(!file.exists()){
            try {
                throw new Exception("encoder is not present at given path");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        String line = "";
        StringBuffer encoderJSON = new StringBuffer();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            while((line=br.readLine())!=null){
                    encoderJSON.append(line);

            }
            encodedDataMapper = new Gson().fromJson(encoderJSON.toString(),EncodedDataMapper.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return encodedDataMapper;
    }
}
