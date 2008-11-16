package com.org.libmetasrv;


import java.io.IOException;
import java.net.Socket;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author lordtelamon
 */
public class TestServer {
    ChatSRV srv;
    public TestServer(){
        srv = new ChatSRV();
        srv.start();
    }
    public static void main(String[] argv) {
        TestServer t = new TestServer(); 
         
    }
    
    class ChatCLNT extends MetaClient{
      public ChatCLNT(MetaServer aThis,Socket s){
            try {
                server = aThis;
                socket = s;
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
                         if(iStream.available()>0){
                            byte[] buffer = readAvailable();
                            
                            System.out.println(Macros.byteArrayToHexView(buffer));
                            server.toAllExcept(this, buffer);
                            oStream.write("processing! OK!\n".getBytes());
                            oStream.flush();
                            
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
                oStream.write("Hello and Welcome to the Underworld!".getBytes());
                oStream.flush();
                return true;
            } catch (IOException ex) {
                Logger.getLogger(TestServer.class.getName()).log(Level.SEVERE, null, ex);
            }
            return false;
        }
        
    }
    
    class ChatSRV extends MetaServer{
                
        @Override
        public MetaClient newClient(Socket sock) {
            return new ChatCLNT(this,sock);
        }
        
    }
}
