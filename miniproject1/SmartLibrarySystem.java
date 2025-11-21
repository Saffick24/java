package com.miniproject1;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;


class Book {
    private final String id;
    private final String title;
    private final String author;
    private boolean issued;

    public Book(String id, String title, String author) {
        this.id = id.trim();
        this.title = title.trim();
        this.author = author.trim();
        this.issued = false;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public boolean isIssued() { return issued; }
    public void setIssued(boolean issued) { this.issued = issued; }

    @Override
    public String toString() {
        return String.format("[%-6s] %-25s %-20s (%s)",
                id, title, author, issued ? "Issued" : "Available");
    }
}

class Member {
    private final String memberId;
    private final String name;
    private final List<String> borrowedBooks;
    private final int borrowLimit = 3; // NEW FEATURE

    public Member(String memberId, String name) {
        this.memberId = memberId.trim();
        this.name = name.trim();
        this.borrowedBooks = new ArrayList<>();
    }

    public String getMemberId() { return memberId; }
    public String getName() { return name; }
    public List<String> getBorrowedBooks() { return borrowedBooks; }

    public boolean canBorrowMore() {
        return borrowedBooks.size() < borrowLimit;
    }

    public void borrowBook(String bookId) {
        borrowedBooks.add(bookId);
    }

    public void returnBook(String bookId) {
        borrowedBooks.remove(bookId);
    }

    @Override
    public String toString() {
        return String.format("Member[ID=%s, Name=%s, Borrowed=%d/%d]",
                memberId, name, borrowedBooks.size(), borrowLimit);
    }
}

class BookNotAvailableException extends Exception {
    public BookNotAvailableException(String msg) { super(msg); }
}

class InvalidReturnException extends Exception {
    public InvalidReturnException(String msg) { super(msg); }
}

class Library {

    private final HashMap<String, Book> inventory = new HashMap<>();
    private final HashMap<String, Member> members = new HashMap<>();
    private final String LOG_FILE = "smart_library_log.txt";

    public void addBook(Book b) throws IOException {
        inventory.put(b.getId(), b);
        log("Added Book: " + b.getTitle() + " (" + b.getId() + ")");
    }

    public void addMember(Member m) throws IOException {
        members.put(m.getMemberId(), m);
        log("Added Member: " + m.getName() + " (" + m.getMemberId() + ")");
    }

    public void issueBook(String bookId, String memberId)
            throws BookNotAvailableException, IOException {

        Book book = inventory.get(bookId);
        Member member = members.get(memberId);

        if (book == null)
            throw new BookNotAvailableException("Book not found.");

        if (member == null)
            throw new BookNotAvailableException("Member not found.");

        if (book.isIssued())
            throw new BookNotAvailableException("Book is already issued.");

        if (!member.canBorrowMore())
            throw new BookNotAvailableException("Member reached maximum borrowing limit.");

        book.setIssued(true);
        member.borrowBook(bookId);

        log(String.format("Issued Book '%s' (%s) to %s",
                book.getTitle(), bookId, member.getName()));
    }

    public void returnBook(String bookId, String memberId, int daysLate)
            throws InvalidReturnException, IOException {

        Book book = inventory.get(bookId);
        Member member = members.get(memberId);

        if (book == null || member == null)
            throw new InvalidReturnException("Invalid book or member.");

        if (!member.getBorrowedBooks().contains(bookId))
            throw new InvalidReturnException("Book not borrowed by this member.");

        book.setIssued(false);
        member.returnBook(bookId);

        int fine = Math.max(0, daysLate * 2);

        log(String.format("Returned Book '%s' by %s | Late %d days | Fine ₹%d",
                book.getTitle(), member.getName(), daysLate, fine));

        System.out.println("\nBook returned successfully.");
        if (fine > 0)
            System.out.println("Late Fee: ₹" + fine);
    }

    public void showInventory() {
        System.out.println("\n----------- LIBRARY INVENTORY -----------");
        if (inventory.isEmpty()) {
            System.out.println("No books available.");
        } else {
            inventory.values().forEach(System.out::println);
        }
        System.out.println("-----------------------------------------\n");
    }

    private void log(String msg) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(LOG_FILE, true))) {
            bw.write(LocalDateTime.now() + ": " + msg);
            bw.newLine();
        }
    }
}


public class SmartLibrarySystem {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        Library lib = new Library();

        System.out.println("\n====== Smart Library Management System ======\n");

        while (true) {
            System.out.println("1. Add Book");
            System.out.println("2. Add Member");
            System.out.println("3. Issue Book");
            System.out.println("4. Return Book");
            System.out.println("5. Show Inventory");
            System.out.println("6. Exit");
            System.out.print("Select Option: ");

            int option;
            try {
                option = Integer.parseInt(sc.nextLine());
            } catch (Exception e) {
                System.out.println("Invalid input. Try again.\n");
                continue;
            }

            try {
                switch (option) {

                    case 1 -> {
                        System.out.print("Book ID: ");
                        String id = sc.nextLine();
                        System.out.print("Title: ");
                        String title = sc.nextLine();
                        System.out.print("Author: ");
                        String author = sc.nextLine();

                        lib.addBook(new Book(id, title, author));
                        System.out.println("Book added!\n");
                    }

                    case 2 -> {
                        System.out.print("Member ID: ");
                        String memberId = sc.nextLine();
                        System.out.print("Name: ");
                        String name = sc.nextLine();

                        lib.addMember(new Member(memberId, name));
                        System.out.println("Member added!\n");
                    }

                    case 3 -> {
                        System.out.print("Book ID: ");
                        String bookId = sc.nextLine();
                        System.out.print("Member ID: ");
                        String memId = sc.nextLine();

                        lib.issueBook(bookId, memId);
                        System.out.println("Book issued!\n");
                    }

                    case 4 -> {
                        System.out.print("Book ID: ");
                        String bookId = sc.nextLine();
                        System.out.print("Member ID: ");
                        String memId = sc.nextLine();
                        System.out.print("Days Late: ");
                        int late = Integer.parseInt(sc.nextLine());

                        lib.returnBook(bookId, memId, late);
                        System.out.println();
                    }

                    case 5 -> lib.showInventory();

                    case 6 -> {
                        System.out.println("Goodbye!");
                        System.exit(0);
                    }

                    default -> System.out.println("Invalid option.\n");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage() + "\n");
            }
        }
    }
}
