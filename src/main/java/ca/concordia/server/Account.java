package ca.concordia.server;

import java.util.concurrent.Semaphore;

public class Account {
    //represent a bank account with a balance and withdraw and deposit methods
    private int balance;
    private int id;
    private final Semaphore semaphore = new Semaphore(1); //mutex

    public Account(int balance, int id){

        this.balance = balance;
        this.id = id;
    }

    public int getBalance(){
        return balance;
    }

    public int getId(){
        return id;
    }


    public void withdraw(int amount){
        balance -= amount;
    }

    public void deposit(int amount){
        balance += amount;
    }

    public Semaphore getSemaphore(){
        return semaphore;
    }
}
