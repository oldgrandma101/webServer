package ca.concordia.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.concurrent.ArrayBlockingQueue;


public class IndividualThreads implements Runnable{
    private ArrayBlockingQueue<Socket> requests;
    private static Bank bank; 
    
        public IndividualThreads(ArrayBlockingQueue<Socket> requests, Bank bank) {
            this.requests = requests;
            this.bank = bank;
    
        }
    
        @Override
        public void run() {
            while(true){
                try{
                    Socket clientSocket = requests.take();
    
                    handleClient(clientSocket);
    
                    clientSocket.close();
    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("\nthread interrupted");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    
        public void handleClient(Socket clientSocket)throws IOException{
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            OutputStream out = clientSocket.getOutputStream();
    
            String request = in.readLine();
            if (request != null) {
                if (request.startsWith("GET")) {
                    // Handle GET request
                    handleGetRequest(out);
                } else if (request.startsWith("POST")) {
                    // Handle POST request
                    handlePostRequest(in, out);
                }
            }
    
    
            in.close();
            out.close();
        
    
        }//end of handle client
    
        private static void handleGetRequest(OutputStream out) throws IOException {
            // Respond with a basic HTML page
            System.out.println("Handling GET request");
            String response = "HTTP/1.1 200 OK\r\n\r\n" +
                    "<!DOCTYPE html>\n" +
                    "<html>\n" +
                    "<head>\n" +
                    "<title>Concordia Transfers</title>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "\n" +
                    "<h1>Welcome to Concordia Transfers</h1>\n" +
                    "<p>Select the account and amount to transfer</p>\n" +
                    "\n" +
                    "<form action=\"/submit\" method=\"post\">\n" +
                    "        <label for=\"account\">Account:</label>\n" +
                    "        <input type=\"text\" id=\"account\" name=\"account\"><br><br>\n" +
                    "\n" +
                    "        <label for=\"value\">Value:</label>\n" +
                    "        <input type=\"text\" id=\"value\" name=\"value\"><br><br>\n" +
                    "\n" +
                    "        <label for=\"toAccount\">To Account:</label>\n" +
                    "        <input type=\"text\" id=\"toAccount\" name=\"toAccount\"><br><br>\n" +
                    "\n" +
                    "        <label for=\"toValue\">To Value:</label>\n" +
                    "        <input type=\"text\" id=\"toValue\" name=\"toValue\"><br><br>\n" +
                    "\n" +
                    "        <input type=\"submit\" value=\"Submit\">\n" +
                    "    </form>\n" +
                    "</body>\n" +
                    "</html>\n";
            out.write(response.getBytes());
            out.flush();
        }
    
        private static void handlePostRequest(BufferedReader in, OutputStream out) throws IOException {
            System.out.println("Handling post request");
            StringBuilder requestBody = new StringBuilder();
            int contentLength = 0;
            String line;
    
            // Read headers to get content length
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                if (line.startsWith("Content-Length")) {
                    contentLength = Integer.parseInt(line.substring(line.indexOf(' ') + 1));
                }
            }
    
            // Read the request body based on content length
            for (int i = 0; i < contentLength; i++) {
                requestBody.append((char) in.read());
            }
    
            System.out.println(requestBody.toString());
            // Parse the request body as URL-encoded parameters
            String[] params = requestBody.toString().split("&");
            int account =  -1, value = -1, toAccount = -1, toValue = -1;
    
            for (String param : params) {
                String[] parts = param.split("=");
                if (parts.length == 2) {
                    String key = URLDecoder.decode(parts[0], "UTF-8");
                    String val = URLDecoder.decode(parts[1], "UTF-8");
    
                    switch (key) {
                        case "account":
                            account = Integer.parseInt(val);
                            break;
                        case "value":
                            value = Integer.parseInt(val);
                            break;
                        case "toAccount":
                            toAccount = Integer.parseInt(val);
                            break;
                        case "toValue":
                            toValue = Integer.parseInt(val);
                            break;
                    }
                    
                }
                
            }
               
            boolean success = bank.transfer(account, toAccount, value);

        // Create the response
        String responseContent = "<html><body><h1>Thank you for using Concordia Transfers</h1>" +
                "<h2>Received Form Inputs:</h2>"+
                "<p>Account: " + account + "</p>" +
                "<p>Value: " + value + "</p>" +
                "<p>To Account: " + toAccount + "</p>" +
                "<p>To Value: " + toValue + "</p>" +
                "</body></html>";
    
        
                
        // Respond with the received form inputs
        String response = "HTTP/1.1 200 OK\r\n" +
                "Content-Length: " + responseContent.length() + "\r\n" +
                "Content-Type: text/html\r\n\r\n" +
                responseContent;

        out.write(response.getBytes());
        out.flush();
    }
}
