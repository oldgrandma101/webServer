package ca.concordia.server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class Bank {
    private final ConcurrentHashMap<Integer, Account> accounts = new ConcurrentHashMap<>();
    //private final Semaphore writeSemaphore = new Semaphore(1); //mutex for writing to accounts.txt
    private final String filename;

    public Bank(String filename) {
        this.filename = filename;
    }
    public ConcurrentHashMap<Integer, Account> getAccounts() {
        return accounts;
    }

    @SuppressWarnings("CallToPrintStackTrace")
    public void initializeAccounts(String filename) {
        //load accounts ids and balances from accounts.txt file
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

    // @SuppressWarnings("CallToPrintStackTrace")
    // private void writeToAccountsFile() {
    //         try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
    //             for (Account account : accounts.values()) {
    //                 writer.write(account.getId() + "," + account.getBalance());
    //                 writer.newLine();
    //             }
    //             System.out.println("Accounts file updated successfully");
    //         } catch (IOException e) {
    //             e.printStackTrace();
    //             System.err.println("Failed to update accounts file");
    //         }
    //     }



    @SuppressWarnings("CallToPrintStackTrace")
    public boolean transfer(int sourceId, int destinationId, int amount) {
        Account source = accounts.get(sourceId);
        Account destination = accounts.get(destinationId);

        if (source == null || destination == null || source.getBalance() < amount) { //invalid transfer of funds
            return false;
        }

        //synchronization to prevent deadlock
        Account lockSource = sourceId < destinationId ? source : destination;
        Account lockDestination = sourceId < destinationId ? destination : source;

        try {
            //get semaphores for source and destination accounts
            lockSource.getSemaphore().acquire();
            lockDestination.getSemaphore().acquire();
            
            //transfer funds
            source.withdraw(amount);
            destination.deposit(amount);

            // writeSemaphore.acquire();
            // writeToAccountsFile();
            // writeSemaphore.release();

            System.out.println("Transfer of: " + amount + "$ from: " + sourceId + " to " + destinationId + " successful!");
            System.out.println("Your account balance in "+ sourceId +" is now: " + source.getBalance());
            System.out.println("Receiver's balance ("+ destinationId +") is now: " + destination.getBalance());
            return true;

        } catch(InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
            return false;
        } finally {
            lockDestination.getSemaphore().release();
            lockSource.getSemaphore().release();
        }
    }
}