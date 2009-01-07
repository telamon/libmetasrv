package com.org.libmetasrv;


import com.org.libmetasrv.MetaServer.Worker;
import java.io.IOException;
import java.util.logging.Level;



/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.util.logging.Logger;

/**
 *
 * @author lordtelamon
 */
 public abstract class MetaClient {
    protected Worker mWorker;
    protected MetaServer mServer;
    public java.net.Socket socket = null;
    public java.io.OutputStream oStream;
    public java.io.InputStream iStream;    
    public long heartBeat = 0;
    private boolean locked = false;
    public int state =0;

    public MetaClient(){
        
    }
    public MetaClient(MetaServer aThis,java.net.Socket s){
        try {
            mServer = aThis;
            socket = s;
            oStream = socket.getOutputStream();
            iStream = socket.getInputStream();
        } catch (IOException ex) {
            Logger.getLogger(TestServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    

    protected void unlock(){
        locked=false;
    }
    protected boolean isLocked(){
        return locked;
    }
    public abstract void process();
    public abstract boolean handshake();
    public void killClient() {
        try {
            mServer.clients.remove(this);
            socket.close();
        } catch (java.io.IOException ex) {
            Logger.getLogger(TestServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public byte[] readAvailable(){
        try {
            byte[] buffer = new byte[iStream.available()];
            iStream.read(buffer);
            //System.out.println(Macros.byteArrayToHexView(buffer));
            return(buffer);
        } catch (IOException ex) {
            Logger.getLogger(MetaClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    void lock(Worker aThis) {
        locked=true;
        mWorker = aThis;
    }
      
    
}
