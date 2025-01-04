package Managment_System;

import java.util.*;

// Abstraction it is implemented by concrete book types.
interface Loanable {
    double calculateLoanFee(int days);
    boolean extendLoan();
}

// Abstract base class for Books
abstract class Book implements Loanable {
    // Encapsulation
    private String bookID, title, author, isbn, genre;
    private int publicationYear;
    private boolean loanStatus;
    protected double baseLoanFee; // Protected for use by subclasses

    public Book(String bookID, String title, String author, String isbn, int publicationYear, String genre, double baseLoanFee) {
        this.bookID = bookID;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.publicationYear = publicationYear;
        this.genre = genre;
        this.baseLoanFee = baseLoanFee;
        this.loanStatus = false;
    }

    public String getBookID() {
        return bookID;
    }

    public void setBookID(String bookID) {
        this.bookID = bookID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public int getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(int publicationYear) {
        this.publicationYear = publicationYear;
    }

    public boolean isLoaned() {
        return loanStatus;
    }

    public void setLoanStatus(boolean loanStatus) {
        this.loanStatus = loanStatus;
    }

    public double getBaseLoanFee() {
        return baseLoanFee;
    }

    public void setBaseLoanFee(double baseLoanFee) {
        this.baseLoanFee = baseLoanFee;
    }

    @Override
    public String toString() {
        return String.format("Book[ID=%s, Title=%s, Author=%s, ISBN=%s, Genre=%s, Year=%d]", bookID, title, author, isbn, genre, publicationYear);
    }

    // Abstract methods for subclasses
    public abstract double calculateLoanFee(int days);
    public abstract boolean extendLoan();
    
    public Book(String bookID, String title, String author, String isbn, int publicationYear, String genre, double baseLoanFee, boolean loanStatus) {
        this.bookID = bookID;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.publicationYear = publicationYear;
        this.genre = genre;
        this.baseLoanFee = baseLoanFee;
        this.loanStatus = loanStatus;
    }
}

class Textbook extends Book {
    private boolean extended = false;

    public Textbook(String bookID, String title, String author, String isbn, int publicationYear, String genre, double baseLoanFee) {
        super(bookID, title, author, isbn, publicationYear, genre, baseLoanFee);
    }
    
    public Textbook(String bookID, String title, String author, String isbn, int publicationYear, String genre, double baseLoanFee, boolean loanStatus) {
        super(bookID, title, author, isbn, publicationYear, genre, baseLoanFee, loanStatus);
    }

    @Override
    public double calculateLoanFee(int days) {
        return getBaseLoanFee() + 2.0 * days;
    }

    @Override
    public boolean extendLoan() {
        if (!extended) {
            extended = true;
            return true;
        }
        return false;
    }
}

class Novel extends Book {
    public Novel(String bookID, String title, String author, String isbn, int publicationYear, String genre, double baseLoanFee) {
        super(bookID, title, author, isbn, publicationYear, genre, baseLoanFee);
    }
    public Novel(String bookID, String title, String author, String isbn, int publicationYear, String genre, double baseLoanFee, boolean loanStatus) {
        super(bookID, title, author, isbn, publicationYear, genre, baseLoanFee, loanStatus);
    }

    @Override
    public double calculateLoanFee(int days) {
        return getBaseLoanFee();
    }

    @Override
    public boolean extendLoan() {
        return false; // Novels cannot extend the loan
    }
}

class ReferenceBook extends Book {
    public ReferenceBook(String bookID, String title, String author, String isbn, int publicationYear, String genre, double baseLoanFee) {
        super(bookID, title, author, isbn, publicationYear, genre, baseLoanFee);
    }

    public ReferenceBook(String bookID, String title, String author, String isbn, int publicationYear, String genre, double baseLoanFee, boolean loanStatus) {
        super(bookID, title, author, isbn, publicationYear, genre, baseLoanFee, loanStatus);
    }
    
    @Override
    public double calculateLoanFee(int days) {
        return 0; // Reference books cannot be loaned
    }

    @Override
    public boolean extendLoan() {
        return false; // Reference books cannot extend the loan
    }
}

abstract class User {
    private String userID, name, email, phone, address;
    private List<Book> loanedBooks; // Composition: User has a list of loaned books
    private double totalLoanFees;

    public User(String userID, String name, String email, String phone, String address) {
        this.userID = userID;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.loanedBooks = new ArrayList<>();
        this.totalLoanFees = 0;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<Book> getLoanedBooks() {
        return loanedBooks;
    }

    public void setLoanedBooks(List<Book> loanedBooks) {
        this.loanedBooks = loanedBooks;
    }

    public double getTotalLoanFees() {
        return totalLoanFees;
    }

    public void setTotalLoanFees(double totalLoanFees) {
        this.totalLoanFees = totalLoanFees;
    }

    public void addPenalty(double penalty) {
        this.totalLoanFees += penalty;
        System.out.println("Penalty of $" + penalty + " added to user " + name + ". Total fees now: $" + totalLoanFees);
    }
    public void addLoanFee(double fee) {
        this.totalLoanFees += fee;
    }



    public void returnBook(Book book) {
        if (loanedBooks.contains(book)) {
            loanedBooks.remove(book);
            book.setLoanStatus(false);
            System.out.println("Book returned: " + book.getBookID() + " by user: " + name);
        } else {
            System.out.println("Book not found in loaned books for user: " + name);
        }
    }

    public void loanBook(Book book, int days) {
        loanedBooks.add(book); // Composition: A user has multiple books
        double fee = book.calculateLoanFee(days); // Polymorphism: Calls the correct calculateLoanFee()
        totalLoanFees += fee;
        book.setLoanStatus(true);
        System.out.println("Book loaned: " + book.getBookID() + " to user: " + name + ". Loan fee: $" + fee);
    }

    public abstract int getMaxLoans(); // Abstraction: Must be implemented by subclasses

    public boolean canBorrow() {
        return loanedBooks.size() < getMaxLoans(); // Polymorphism: getMaxLoans() based on user type
    }

    @Override
    public String toString() {
        return String.format("User[ID=%s, Name=%s, Email=%s, Phone=%s, Address=%s, Loaned Books=%d, Total Fees=%.2f]", userID, name, email, phone, address, loanedBooks.size(), totalLoanFees);
    }
}

class Student extends User {
    public Student(String userID, String name, String email, String phone, String address) {
        super(userID, name, email, phone, address);
    }

    @Override
    public int getMaxLoans() {
        return 5; // Students can borrow up to 5 books
    }
}

class Faculty extends User {
    public Faculty(String userID, String name, String email, String phone, String address) {
        super(userID, name, email, phone, address);
    }

    @Override
    public int getMaxLoans() {
        return 10; // Faculty can borrow up to 10 books
    }
}

class PublicMember extends User {
    public PublicMember(String userID, String name, String email, String phone, String address) {
        super(userID, name, email, phone, address);
    }

    @Override
    public int getMaxLoans() {
        return 3; // Public members can borrow up to 3 books
    }
}
