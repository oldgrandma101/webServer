package ca.concordia.server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class Bank {
    private final ConcurrentHashMap<Integer, Account> accounts = new ConcurrentHashMap<>();

    public void initializeAccounts(String filename) {
        //load accounts from file
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                int id = Integer.parseInt(parts[0].trim());
                int balance = Integer.parseInt(parts[1].trim());
                accounts.put(id, new Account(id, balance));

            }
        } catch(IOException e) {
            e.printStackTrace();
        }

    }


    public boolean transfer(int sourceId, int destinationId, int amount) {
        Account source = accounts.get(sourceId);
        Account destination = accounts.get(destinationId);

        if (source == null || destination == null || source.getBalance() < amount) { //invalid transfer of funds
            return false;
        }

        //synchronization to prevent deadlock
        Account lock1 = sourceId < destinationId ? source : destination;
        Account lock2 = sourceId < destinationId ? destination : source;

        try {
            //get semaphores for source and destination accounts
            lock1.getSemaphore().acquire();
            lock2.getSemaphore().acquire();
            
            //transfer
            source.withdraw(amount);
            destination.deposit(amount);
            System.out.println("Transfer of: $" + amount + "from: " + sourceId + "to " + destinationId + "successful!");
            System.out.println("Your account balance is now: " + source.getBalance());
            System.out.println("Receiver's balance is now:" + destination.getBalance());
            return true;

        } catch(InterruptedException e) {
            e.printStackTrace();
            return false;
        } finally {
            lock2.getSemaphore().release();
            lock1.getSemaphore().release();
        }
    }
}