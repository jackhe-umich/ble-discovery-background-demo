package org.thaliproject.nativetest.app;

import android.app.Activity;
import android.content.Context;

public class ConnectionManager {

    private  ConnectionEngine mConnectionEngine;
    private  byte[] dataToSend;
    public ConnectionManager(Context context, Activity activity){
        if(mConnectionEngine == null){
            mConnectionEngine = new ConnectionEngine(context, activity);
            mConnectionEngine.bindSettings();
        }
    }

    /*public ConnectionManager(Context context){
        if(mConnectionEngine == null){
            mConnectionEngine = new ConnectionEngine(context);
            mConnectionEngine.bindSettings();
        }
    }*/

    public void startConnection(){
        if(dataToSend.length == 0){
            //setDataToSend(new String("empty").getBytes());
        }
        mConnectionEngine.start();
        //mConnectionEngine.makeDeviceDiscoverable();
        mConnectionEngine.startBluetoothDeviceDiscovery();
    }

    /*public void startConnection(String stringToSend){
        setDataToSend(stringToSend.getBytes());
        startConnection();
    }

    public void startConnection(byte[] dataToSend){
        setDataToSend(dataToSend);
        startConnection();
    }*/

    public void stopConnection(){
        mConnectionEngine.stop();
    }

    public void destroyManager(){
        if(mConnectionEngine!=null){
            mConnectionEngine.dispose();
            mConnectionEngine = null;
        }
    }

    /*public void setDataToSend(byte[] dataToSend) {
        this.dataToSend = dataToSend;
        if(mConnectionEngine != null){
            mConnectionEngine.setDataToSend(this.dataToSend);
        }
    }*/

    public byte[] getDataToSend() {
        return dataToSend;
    }

    /*public byte[] readBytes(){
        return mConnectionEngine.getBytesRead();
    }*/
}
