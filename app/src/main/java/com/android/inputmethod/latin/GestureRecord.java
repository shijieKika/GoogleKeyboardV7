package com.android.inputmethod.latin;

/**
 * Created by johnny on 2017/3/21.
 */



import com.android.inputmethod.latin.common.InputPointers;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class GestureRecord implements Iterable {

    private Gson mGson;
    private String mFileName;

    private List<Trace> mTrace;

    private Trace mLastTrace;

    public GestureRecord(String fileName) {
        mGson = new Gson();
        mFileName = fileName;
        mLastTrace = new Trace();
        mTrace = loadTrace();
    }

    private List<Trace> loadTrace() {
        List<Trace> rel = null;
        try {
            FileInputStream fin = new FileInputStream(mFileName);
            int tmp_len = fin.available();
            byte[] buffer = new byte[tmp_len];
            fin.read(buffer);
            fin.close();
            rel =  mGson.fromJson(new String(buffer), new TypeToken<List<Trace>>(){}.getType());
        } catch (Exception e){
            e.printStackTrace();
        }
        if(rel == null){
            rel = new ArrayList<Trace>();
        }
        return rel;
    }

    private void initTrace() {
        mTrace = null;
        mLastTrace = new Trace();
    }

    // 用于记录不相同的结果
    public void putTrace(String word, Trace t) {
        if(mTrace == null) {
            mTrace = loadTrace();
        }
        mTrace.add(new Trace(word, t));
    }

    public void putTrace(ArrayList<SuggestedWords.SuggestedWordInfo> rel, WordComposer data, NgramContext prev) {
        final InputPointers inputPointers = data.getComposedDataSnapshot().mInputPointers;
        int currentSize = inputPointers.getPointerSize();
        if(currentSize < mLastTrace.mInputSize) {
            if(mTrace == null) {
                mTrace = loadTrace();
            }
            mTrace.add(mLastTrace);
        }
        String[] word = new String[3];
        for(int i = 0; i < 3 && i < rel.size(); i++) {
            word[i] = rel.get(i).mWord;
        }
        int prevSize = prev.getPrevWordCount();
        CharSequence[] prevWord = new String[prevSize];
        for(int i = 0; i < prevSize; i++) {
            prevWord[i] = prev.getNthPrevWord(i + 1);
        }
        mLastTrace = new Trace(word, currentSize,
                inputPointers.getXCoordinates(),
                inputPointers.getYCoordinates(),
                inputPointers.getTimes(),
                data.wasShiftedNoLock(), data.isAllUpperCase(),
                prevSize, prevWord);
    }

    public void flushTrace() {
        if(mTrace == null) {
            return;
        }
        if(mLastTrace != null && !mLastTrace.isEmpty()) {
            mTrace.add(mLastTrace);
        }
        String jsonTrace = mGson.toJson(mTrace);
        try {
            FileOutputStream fout = new FileOutputStream(mFileName);
            fout.write(jsonTrace.getBytes());
            fout.close();
        } catch (Exception e){
            e.printStackTrace();
        }
        initTrace();
    }

    public class Trace {

        public String[] word;
        public String mKikaWord;

        public int mInputSize;
        public int[] mInputX;
        public int[] mInputY;
        public int[] mInputTime;

        boolean mIsFirstUp;
        boolean mIsAllUp;

        public int mPrevSize;
        public String[] mPrevWord;

        public Trace() {
            word = new String[0];
            mKikaWord = new String();
            mInputSize = 0;
            mInputX = new int[0];
            mInputY = new int[0];
            mInputTime = new int[0];
            mIsFirstUp = false;
            mIsAllUp = false;
            mPrevSize = 0;
            mPrevWord = new String[0];
        }

        public Trace(String[] word_, int size_, int[] x_, int[] y_, int[] time_, boolean first_, boolean all_, int prevSize_, CharSequence[] prevWord_) {
            word = word_;
            mInputSize = size_;
            mInputX = x_;
            mInputY = y_;
            mInputTime = time_;
            mIsFirstUp = first_;
            mIsAllUp = all_;
            mPrevSize = prevSize_;
            mKikaWord = new String();
            mPrevWord = new String[prevSize_];
            for(int i = 0; i < prevSize_; i++) {
                CharSequence s = prevWord_[i];
                if(s == null) {
                    continue;
                }
                mPrevWord[i] = s.toString();
            }
        }

        public Trace(String word_, Trace t) {
            mKikaWord = word_;
            word = t.word;
            mInputSize = t.mInputSize;
            mInputX = t.mInputX;
            mInputY = t.mInputY;
            mInputTime = t.mInputTime;
            mIsFirstUp = t.mIsFirstUp;
            mIsAllUp = t.mIsAllUp;
            mPrevSize = t.mPrevSize;
            mPrevWord = t.mPrevWord;
        }

        private boolean isEmpty() {
            return mInputSize == 0;
        }

    }

    public Iterator iterator() {
        if(mTrace == null) {
            mTrace = loadTrace();
        }
        return new TraceIterator();
    }

    private class TraceIterator implements Iterator {
        private Iterator it = mTrace.iterator();

        public boolean hasNext() {
            return it.hasNext();
        }

        public Trace next() {
            return (Trace)it.next();
        }
    }


}
