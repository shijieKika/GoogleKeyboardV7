package com.android.inputmethod.latin;

/**
 * Created by johnny on 2017/3/21.
 */

import org.json.*;

import com.android.inputmethod.latin.common.ComposedData;
import com.android.inputmethod.latin.common.InputPointers;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;


public class GestureRecord {

    private JSONObject mJson;
    private String mFileName;

    private int mLastSize;
    private String mLastName;
    private JSONObject mLastTrace;

    private JSONObject loadJson() {
        try {
            FileInputStream tmp_fin = new FileInputStream(mFileName);
            int tmp_len = tmp_fin.available();
            byte[] buffer = new byte[tmp_len];
            tmp_fin.read(buffer);
            tmp_fin.close();
            return new JSONObject(new String(buffer));
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public GestureRecord(String name) {
        try{
            mFileName = name;
            mJson = loadJson();
            mLastSize = -1;
        } catch (Exception e){
            e.printStackTrace();
        }

    }


    public void putRecord(String name, ComposedData composedData) {
        final InputPointers inputPointers = composedData.mInputPointers;

        try {
            if(mJson == null) {
                mJson = loadJson();
            }
            int currentSize = inputPointers.getPointerSize();
            if(mLastSize > currentSize) {
                mJson.put(mLastName, mLastTrace);
            }

            JSONObject coor = new JSONObject();
            coor.put("size", currentSize);
            coor.put("x", Arrays.toString(inputPointers.getXCoordinates()));
            coor.put("y", Arrays.toString(inputPointers.getYCoordinates()));
            coor.put("time", Arrays.toString(inputPointers.getTimes()));

            mLastSize = currentSize;
            mLastName = name;
            mLastTrace = coor;

        } catch (Exception e){
            e.printStackTrace();
        }

    }

    public void flush() {
        // flush into file
        try {
            if(mJson != null) {
                mJson.put(mLastName, mLastTrace);
                FileOutputStream fout = new FileOutputStream(mFileName);
                fout.write(mJson.toString().getBytes());
                fout.close();
                mJson = null;
            }
        } catch (Exception e){
            e.printStackTrace();
        }

    }


}
