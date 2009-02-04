package com.org.libmetasrv;


import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Tony Ivanov
 */
public abstract class MetaServer extends Thread {
    
    //Options
    public int port = 5525;
    public int maxConnections = -1;
    public int workerThreads = 2;
    
    private boolean alive = false;    
    private ArrayList<Worker> workers = new ArrayList<Worker>();
    private static int workPos=0;
    protected ArrayList<MetaClient> clients = new ArrayList<MetaClient>();
    //protected ArrayList<MetaClient> workQue = new ArrayList<MetaClient>();
    private TaskMaster mTaskMaster;
    public boolean clientMode = false;
    protected static java.net.ServerSocket server = null;  

    protected abstract void resetInstance();
    public boolean clientMode(String address,int port){
        if(alive){
            System.err.println("Server already running!");
            return false;
        }
        clientMode=true;
        workerThreads = 1;
        try {
            this.start();
            connectToRemote(address, port); 
            return true;
        } catch (IOException ex) {
            System.err.println("Failed to connect, resetting server.");
            shutdown();
            //Logger.getLogger(MetaServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;     
    }

    public void run() {
        this.setName(this.getClass().getSimpleName());
        try {
            alive = true;

            //Start the workers
            for (int i = 0; i < workerThreads; i++) {
                Worker w = new Worker();
                w.start();
                workers.add(w);
            }
            mTaskMaster = new TaskMaster();
            
            //Start listening on port
            if (!clientMode) {
                bindPort();                
                mTaskMaster.start(); // spawn a taskmaster.
            }else{
                mTaskMaster.run(); //become the taskmaster yourself.
            }
            
            while (alive) {
                if (!clientMode) {
                    acceptConnections();
                }
                sleep(40);
            }
            if (!clientMode && server.isBound()) {
                server.close();
            }
            
        } catch (SocketException ex) {
            Logger.getLogger(MetaServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MetaServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(MetaServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        // Cleanup before resting in peace.
        int aliveThreads=0;
        while(aliveThreads >0){
            aliveThreads = 0;
            for(Worker w:workers){
                if(w.isAlive()){
                    aliveThreads++;
                }
            }
            if(mTaskMaster.isAlive()){
                aliveThreads++;
            }
            if(this.isAlive()){
                aliveThreads++;
            }
            try {
                sleep(500);
            } catch (InterruptedException ex) {
                Logger.getLogger(MetaServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        workers.clear();
        clients.clear();
        workPos=0;
        resetInstance();
        System.err.println("Bye Bye cruel world!");
    }

    private void acceptConnections() {
        try{
            java.net.Socket Ssock = server.accept();
            if(maxConnections == -1 || clients.size() < maxConnections){
               clients.add(newClient(Ssock));
            }else{
                Ssock.close();
            }            
        }catch(SocketTimeoutException ex){
            //Ignore socket timeouts.
        } catch (IOException ex) {
            Logger.getLogger(MetaServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private void bindPort(){
                 
            // Set socket timeout so it has a chance to die when people press stop server.            
            try {                
                server = new java.net.ServerSocket(port);                
                server.setSoTimeout(1000);
                System.out.println("Server started, listening on port: " + port);           
            } catch (IOException e) {
                System.err.println("Could not listen on port: " + port);
                alive = false;
            }
    }

    public boolean isServerAlive(){
        return alive;
    }  
    /**
     * Is automatically called upon a new connection to the server
     * Override this function and return a Class based on the libmetasrv.MetaClient
     * interface
     * @param sock Socket object of the new connection.
     * @return MetaClient Interface based class
     */
    public abstract MetaClient newClient(java.net.Socket sock);
    
    public void connectToRemote(String address,int port) throws IOException{
        java.net.Socket socker = new java.net.Socket();
        socker.connect(new java.net.InetSocketAddress(address, port));
        if(socker.isConnected()){
            clients.add(newClient(socker));

        }
    }
    public void shutdown() {
        // Die and reset the server to defaults.
        alive = false;        
        
    }
    /**
     * Same as broadcast()
     * @param bytes
     */
    public void toAll(byte[] bytes) {
        broadcast(bytes);
    }
    public void broadcast(byte[] buffer){
        for(MetaClient mc : clients){
            try {
                mc.socket.getOutputStream().write(buffer);
                mc.socket.getOutputStream().flush();
            } catch (IOException ex) {
                Logger.getLogger(MetaServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    public void toAllExcept(MetaClient aThis, byte[] buffer) {
        MetaClient[] mcs = {aThis};
        toAllExcept(mcs,buffer);
    }
    public void toAllExcept(MetaClient[] exmcs,byte[] buffer){
        for(MetaClient mc :clients){
            for(MetaClient exmc : exmcs){
                boolean doit =true;
                if(mc.equals(exmc)){
                   doit = false; 
                }
                if(doit){
                    try {
                        mc.socket.getOutputStream().write(buffer);
                        mc.socket.getOutputStream().flush();
                    } catch (IOException ex) {
                        Logger.getLogger(MetaServer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }
    public void toOnly(MetaClient[] mcs,byte[] buffer){
        for(MetaClient mc: mcs){
            try {
                mc.socket.getOutputStream().write(buffer);
                mc.socket.getOutputStream().flush();
            } catch (IOException ex) {
                Logger.getLogger(MetaServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /** The overseer*/
    class TaskMaster extends Thread{
        private Worker getLivingWorker() throws InterruptedException{             
            while(true){
                ArrayList<Worker> zombies = new ArrayList<Worker>();
                for(Worker w:workers){
                    if(!w.busy){
                        return w;
                    }else if(!w.isAlive()){
                        zombies.add(w);
                    }
                }
                for(Worker w:zombies){
                    workers.remove(w);
                    System.err.println("R.I.P " + w);
                    Worker peon = new Worker();
                    workers.add(peon);
                    peon.start();
                }
                if(zombies.size()<1){
                    sleep(30);
                }
            }
        }
        @Override
        public void run(){
            this.setName(this.getClass().getSimpleName());
            while(alive){
                try {
                    for (MetaClient mc : clients) {                        
                        // Unlock the client if the worker died.
                        if (mc.isLocked() && !mc.mWorker.isAlive()) {
                            mc.unlock();
                        }
                        if(!mc.isLocked()){
                            Worker w=getLivingWorker();
                            w.task=mc;
                            w.busy=true;
                        }

                    }
                    
                    sleep(40);
                } catch (InterruptedException ex) {
                    Logger.getLogger(MetaServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    /** the expendable */
    class Worker extends Thread{
        MetaClient task;
        boolean busy=false;
        @Override
        public void run(){
            this.setName(this.getClass().getSimpleName());
            while(alive){                
                try {
                    if(busy){
                        // Work.                       
                        // Kill the client if connection is dead.
                        if(task.socket.isClosed()){
                            task.killClient();
                            //If this was a single client-mode instance
                            // then shutdown the framework when the socket
                            // dies.
                            if(clientMode){
                                shutdown();
                            }
                        }   
                        task.lock(this); // Lock it with our seal.
                        task.process();
                        task.heartBeat = System.nanoTime();
                        task.unlock();
                        task=null;
                        busy=false;
                    }
                    sleep(30);
                } catch(java.lang.IndexOutOfBoundsException ex){
                    //I suspect this is just a spook of concurrent arraylist modifications.
                    //ex.printStackTrace();
                    //System.out.println("S:"+clients.size()+" P:"+workPos + " T:"+workers.size());
                } catch (InterruptedException ex) {
                    Logger.getLogger(MetaServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
