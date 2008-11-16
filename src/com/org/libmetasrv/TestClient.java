/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.org.libmetasrv;

import com.org.libmetasrv.TestServer.ChatCLNT;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Administrator
 */
public class TestClient {
    public TestClient(){
        MySocketHandler msh = new MySocketHandler();
        msh.workerThreads=1;
        msh.noServer=true;
        msh.start();
        msh.connectToRemote("irc.efnet.pl", 6667);
        while(msh.isServerAlive()){
            
        }
        
    };
    public static void main(String[] argv) {
        TestClient tc = new TestClient(); 
  
    }
    class MyClient extends MetaClient{

        private MyClient(MySocketHandler aThis, Socket sock) {
            try {
                server = aThis;
                socket = sock;
                oStream = socket.getOutputStream();
                iStream = socket.getInputStream();
            } catch (IOException ex) {
                Logger.getLogger(TestServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        @Override
        public void process() {
            try {
                switch(state){
                    case 0:                        
                        if(handshake()){
                            state=1;
                        }else{
                            killClient();
                        }
                        break;
                    case 1:
                         if(!socket.isConnected())
                             server.shutdown();
                         if(iStream.available()>0){
                            byte[] buffer = readAvailable();
                            
                            System.out.println(Macros.byteArrayToHexView(buffer));
                            server.toAllExcept(this, buffer);
                            //oStream.write("processing! OK!\n".getBytes());
                            //oStream.flush();
                            
                        }   
                        break;
                }                
            } catch (IOException ex) {
                Logger.getLogger(TestServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        @Override
        public boolean handshake() {
            try {
                //oStream.write("PASS \n".getBytes());
                oStream.write("NICK Strombleh\n".getBytes());
                oStream.write("USER guest tolmoon tolsun :Ronnie \n".getBytes());
                oStream.flush();
                return true;
            } catch (IOException ex) {
                Logger.getLogger(TestClient.class.getName()).log(Level.SEVERE, null, ex);
            }
            return false;
        }
        
        
    }
    class MySocketHandler extends MetaServer{
                
        @Override
        public MetaClient newClient(Socket sock) {
            return new MyClient(this,sock);
        }
        
    }
}
