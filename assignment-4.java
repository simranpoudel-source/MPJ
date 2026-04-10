import java.io.*;
import java.util.*;

class InvalidAmountException extends Exception {
    InvalidAmountException(String msg) {
        super(msg);
    }
}

class InvalidCIDException extends Exception {
    InvalidCIDException(String msg) {
        super(msg);
    }
}

class InsufficientBalanceException extends Exception {
    InsufficientBalanceException(String msg) {
        super(msg);
    }
}

class Customer {
    int cid;
    String cname;
    double amount;

    Customer(int cid, String cname, double amount) {
        this.cid = cid;
        this.cname = cname;
        this.amount = amount;
    }

    public String toString() {
        return cid + " " + cname + " " + amount;
    }
}

public class BankingSystem {

    static Scanner sc = new Scanner(System.in);
    static final String FILE_NAME = "customers.txt";

    public static void createAccount() throws Exception {

        System.out.print("Enter CID (1 to 20): ");
        int cid = sc.nextInt();

        if (cid < 1 || cid > 20) {
            throw new InvalidCIDException("CID must be between 1 and 20");
        }

        System.out.print("Enter Name: ");
        String name = sc.next();

        System.out.print("Enter Amount: ");
        double amt = sc.nextDouble();

        if (amt < 1000) {
            throw new InvalidAmountException("Minimum balance is 1000");
        }

        Customer c = new Customer(cid, name, amt);

        FileWriter fw = new FileWriter(FILE_NAME, true);
        fw.write(c.toString() + "\n");
        fw.close();

        System.out.println("Account Created Successfully");
    }

    public static void withdraw() throws Exception {

        System.out.print("Enter Withdrawal Amount: ");
        double wamt = sc.nextDouble();

        if (wamt <= 0) {
            throw new InvalidAmountException("Amount must be positive");
        }

        System.out.print("Enter Current Balance: ");
        double balance = sc.nextDouble();

        if (wamt > balance) {
            throw new InsufficientBalanceException("Insufficient Balance");
        }

        balance = balance - wamt;
        System.out.println("Remaining Balance: " + balance);
    }

    public static void deposit() throws Exception {

        System.out.print("Enter Deposit Amount: ");
        double damt = sc.nextDouble();

        if (damt <= 0) {
            throw new InvalidAmountException("Amount must be positive");
        }

        System.out.print("Enter Current Balance: ");
        double balance = sc.nextDouble();

        balance = balance + damt;

        System.out.println("Updated Balance: " + balance);
    }

    public static void main(String[] args) {

        int choice;

        do {
            System.out.println("\n1. Create Account");
            System.out.println("2. Withdraw");
            System.out.println("3. Deposit");
            System.out.println("4. Exit");
            System.out.print("Enter Choice: ");

            choice = sc.nextInt();

            try {
                switch (choice) {
                    case 1:
                        createAccount();
                        break;

                    case 2:
                        withdraw();
                        break;

                    case 3:
                        deposit();
                        break;

                    case 4:
                        System.out.println("Exiting...");
                        break;

                    default:
                        System.out.println("Invalid Choice");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }

        } while (choice != 4);
    }
}
