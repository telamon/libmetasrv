/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.org.libmetasrv;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Tony Ivanov (telamohn@gmail.com)
 */
public class SimpleTest{

    public static void main(String[] argv) throws MetaSrvException{
	if(argv.length<2){ // No address provided, act as server.
	    SimpleServer myServer = new SimpleServer();
	    myServer.start();
	}else{ // Address and port provided as first argument in format "example.com:1234"
	    String[] addrPort = argv[1].split(":");
	    SimpleServer myClient = new SimpleServer();
	    myClient.clientMode(addrPort[0], Integer.parseInt(addrPort[1]));
	}
    }

    static class SimpleClient extends MetaClient{
	BufferedReader input;
	public SimpleClient(MetaServer ms,Socket sock){
	    super(ms,sock);
	    input= new BufferedReader(new java.io.InputStreamReader(iStream));
	}
	@Override
	public void process() {
	    try{
		if(!mServer.clientMode){
		    // I'm a server, I Broadcast incoming messages to all connected clients.
		    mServer.broadcast(input.readLine().getBytes());
		}else{
		    // I'm a client, I print all incoming messages
		    System.out.println(input.readLine());
		}
	    } catch (IOException ex) {
		Logger.getLogger(SimpleTest.class.getName()).log(Level.SEVERE, null, ex);
	    }
	}

	@Override
	public boolean handshake() {
	    try {
		if (!mServer.clientMode) {
		    // This instance is the server
		    String greeting = input.readLine();
		    if(greeting.equalsIgnoreCase("hello")){
			return true;
		    }else{
			return false;
		    }
		} else {
		    // This is instance the client
		    oStream.write("hello\n".getBytes());
		    return true;
		}
	    } catch (IOException ex) {
		Logger.getLogger(SimpleTest.class.getName()).log(Level.SEVERE, null, ex);
	    }
	    return false;
	}

    }

    static class SimpleServer extends MetaServer{

	@Override
	protected void resetInstance() {

	}

	@Override
	public MetaClient newClient(Socket sock) {
	    return new SimpleClient(this,sock);
	}

    }
}
