package com.sziti.easysocketlib.client.delegate.io;


import com.sziti.easysocketlib.interfaces.io.IIOCoreOptions;
import com.sziti.easysocketlib.interfaces.io.IReader;
import com.sziti.easysocketlib.interfaces.io.IStateSender;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Tony on 2017/12/26.
 */

public abstract class AbsReader implements IReader<IIOCoreOptions> {

    protected volatile IIOCoreOptions mOkOptions;

    protected IStateSender mStateSender;

    protected InputStream mInputStream;

    protected String mThreadName;

    public AbsReader() {
    }
    public AbsReader(String threadName){
    	this.mThreadName = threadName;
	}
    @Override
    public void initialize(InputStream inputStream, IStateSender stateSender) {
        mStateSender = stateSender;
        mInputStream = inputStream;
    }

    @Override
    public void setOption(IIOCoreOptions option) {
        mOkOptions = option;
    }


    @Override
    public void close() {
        if (mInputStream != null) {
            try {
                mInputStream.close();
            } catch (IOException e) {
                //ignore
            }
        }
    }
}
