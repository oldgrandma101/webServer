package ca.concordia.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


//create the WebServer class to receive connections on port 5000. Each connection is handled by a master thread that puts the descriptor in a bounded buffer. A pool of worker threads take jobs from this buffer if there are any to handle the connection.
public class WebServer {

    //need to add attributes to WebServer for thread pools (number of threads in pool and the queue to hold extra tasks
    private ExecutorService threadPool;
    private ArrayBlockingQueue<Socket> requests;
    private Bank bank; //instantiate bank 


    //also need to make a constructor to initialize attributes
    public WebServer(int poolSize, int queueSize) {
        threadPool = Executors.newFixedThreadPool(poolSize);
        requests = new ArrayBlockingQueue<> (queueSize);

        bank = new Bank(); //initialize bank
        

        bank.initializeAccounts("target\\classes\\accounts.txt"); //initialize accounts with their respective parameters

        // Start IndividualThreads
        for (int i = 0; i < poolSize; i++) {
            threadPool.execute(new IndividualThreads(requests));
        }
    }


    public void start() throws java.io.IOException{
        //Create a server socket
        ServerSocket serverSocket = new ServerSocket(5001);
        while(true){
            System.out.println("Waiting for a client to connect...");
            //Accept a connection from a client
            Socket clientSocket = serverSocket.accept();
            System.out.println("New client...");

            //add client to requests and print error message if unsuccessful
            if(!requests.offer(clientSocket)){
                System.err.println("Queue is full, dropping client");
                clientSocket.close();
            }
        }
    }

    public static void main(String[] args) {
        //Start the server, if an exception occurs, print the stack trace
        WebServer server = new WebServer(1000, 1000);
        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

