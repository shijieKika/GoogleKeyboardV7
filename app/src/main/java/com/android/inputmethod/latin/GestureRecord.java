package com.android.inputmethod.latin;

/**
 * Created by johnny on 2017/3/21.
 */

import android.renderscript.ScriptGroup;

import org.json.*;

import com.android.inputmethod.latin.common.ComposedData;
import com.android.inputmethod.latin.common.InputPointers;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Iterator;


public class GestureRecord implements Iterable {

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


    public Iterator iterator() {
        return new DataIterator();
    }

    private class DataIterator implements Iterator {
        private Iterator it = mJson.keys();
        public boolean hasNext() {
            return it.hasNext();
        }

        public PairN2T next() {
            try {
                String name = (String)it.next();
                JSONObject trace = mJson.getJSONObject(name);
                int size_ = trace.getInt("size");
                String[] x_ = trace.getString("x").replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\\s", "").split(",");
                String[] y_ = trace.getString("y").replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\\s", "").split(",");
                String[] time_ = trace.getString("time").replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\\s", "").split(",");
                InputPointers input_ = new InputPointers(48);
                for(int i = 0; i < size_; i++) {
                    input_.addPointer(Integer.parseInt(x_[i]), Integer.parseInt(y_[i]), 0, Integer.parseInt(time_[i]));
                }

                return new PairN2T(name, new ComposedData(input_, true, ""));

            } catch (Exception e){
                e.printStackTrace();
                return new PairN2T("", new ComposedData(new InputPointers(48), true, ""));
            }


        }
    }

    public class PairN2T {
        public String mName;
        public ComposedData mData;

        PairN2T(String name_, ComposedData data_) {
            mName = name_;
            mData = data_;
        }
    }

}
