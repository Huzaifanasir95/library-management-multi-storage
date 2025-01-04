package Managment_System;

import java.util.*;
import java.sql.*;

public class LibraryManagementSystem {
    private static final double LATE_FEE = 3.0;
    private static double totalRevenue = 0.0;
    private static List<User> users = new ArrayList<>();
    private static List<Book> books = new ArrayList<>();

    private static boolean useDatabase;

    // Initialization for Persistence Choice
    public static void initializePersistenceOption(Scanner scanner) {
        System.out.println("Choose data storage method:");
        System.out.println("1. Database");
        System.out.println("2. File System");
        System.out.print("Enter choice (1 or 2): ");
        int choice = scanner.nextInt();
        scanner.nextLine();
        
        useDatabase = choice == 1;
    }

    // Check if a book exists
    public static boolean bookExists(String bookId) {
        return books.stream().anyMatch(book -> book.getBookID().equals(bookId));
    }

    // Check if a user exists
    public static boolean userIdExists(String userId) {
        return users.stream().anyMatch(user -> user.getUserID().equals(userId));
    }

    // Add a new book
    public static void addBook(Book book) {
        if (bookExists(book.getBookID())) {
            System.out.println("A book with the same ID already exists.");
        } else {
            books.add(book);
//            System.out.println("Book added: " + book);
            if (useDatabase) {
                PersistenceHandler.persistAddBook(book);
            } else {
                FileHandler.persistAddBook(book);
            }
        }
    }

    private static void displayAvailableBooks() {
        List<Book> availableBooks;
        if (useDatabase) {
            availableBooks = PersistenceHandler.persistDisplayAvailableBooks();
        } else {
            availableBooks = FileHandler.persistDisplayAvailableBooks();
        }

        if (availableBooks.isEmpty()) {
            System.out.println("No available books.");
        } else {
            System.out.println("\nAvailable Books:");
            availableBooks.forEach(System.out::println);
        }
    }

    private static void displayLoanedBooks() {
        List<Book> loanedBooks;
        if (useDatabase) {
            loanedBooks = PersistenceHandler.persistDisplayLoanedBooks();
        } else {
            loanedBooks = FileHandler.persistDisplayLoanedBooks();
        }

        if (loanedBooks.isEmpty()) {
            System.out.println("No loaned books.");
        } else {
            System.out.println("\nLoaned Books:");
            loanedBooks.forEach(System.out::println);
        }
    }

    public static void removeBook(String bookId) {
        Book bookToRemove = books.stream().filter(book -> book.getBookID().equals(bookId) && !book.isLoaned()).findFirst().orElse(null);
        if (bookToRemove != null) {
            books.remove(bookToRemove);
            System.out.println("Book removed: " + bookToRemove.getTitle());
            if (useDatabase) {
                PersistenceHandler.persistRemoveBook(bookId);
            } else {
                FileHandler.persistRemoveBook(bookId);
            }
        } else {
            System.out.println("Cannot remove the book. It might be loaned or doesn't exist.");
        }
    }

    public static void addUser(User user) {
        if (userIdExists(user.getUserID())) {
            System.out.println("A user with the same ID already exists.");
        } else {
            users.add(user);
//            System.out.println("User added: " + user);
            if (useDatabase) {
                PersistenceHandler.persistAddUser(user);
            } else {
                FileHandler.persistAddUser(user);
            }
        }
    }

    private static void displayUsers() {
        List<User> registeredUsers;
        if (useDatabase) {
            registeredUsers = PersistenceHandler.persistDisplayUsers();
        } else {
            registeredUsers = FileHandler.persistDisplayUsers();
        }

        if (registeredUsers.isEmpty()) {
            System.out.println("No registered users.");
        } else {
            System.out.println("\nRegistered Users:");
            registeredUsers.forEach(System.out::println);
        }
    }

    public static void loanBook(String userId, String bookId, int days) {
        User user;
        Book book;

        if (useDatabase) {
            user = PersistenceHandler.fetchUser(userId);  
            book = PersistenceHandler.fetchBook(bookId);  
        } else {
            user = FileHandler.fetchUser(userId);        
            book = FileHandler.fetchBook(bookId);        
        }

        if (user == null) {
            System.out.println("User not found.");
            return;
        }
        if (book == null) {
            System.out.println("Book not found.");
            return;
        }

        if (book.isLoaned()) {  
            System.out.println("Book is already on loan and cannot be loaned again.");
            return;
        }

        if (user.canBorrow()) {
            double loanFee = book.calculateLoanFee(days);
            user.addLoanFee(loanFee);
            book.setLoanStatus(true); 

            if (useDatabase) {
                PersistenceHandler.persistLoanBook(userId, bookId, days, loanFee);
            } else {
                FileHandler.persistLoanBook(userId, bookId, days, loanFee);
            }
            System.out.println("Book loaned to user: " + userId + ". Loan fee: $" + loanFee);
        } else {
            System.out.println("User has reached the maximum loan limit.");
        }
    }


    private static void sortingMenu(Scanner scanner) {
        System.out.println("Sort by:");
        System.out.println("1. Book");
        System.out.println("2. User");
        System.out.print("Enter your choice: ");
        int choice = scanner.nextInt();
        scanner.nextLine();  // Consume newline

        if (choice == 1) {
            System.out.println("Sort Books by:");
            System.out.println("1. Title ");
            System.out.println("2. Book ID");
            System.out.print("Enter sorting criterion: ");
            int sortCriterion = scanner.nextInt();
            scanner.nextLine();  // Consume newline
            String criterion = sortCriterion == 1 ? "title" : "id";
            List<Book> books = useDatabase ? PersistenceHandler.sortBooks(criterion) : FileHandler.sortBooks(criterion);
            System.out.println("Sorted Books:");
            for (Book book : books) {
                System.out.println(book);
            }
        } else if (choice == 2) {
            System.out.println("Sort Users by:");
            System.out.println("1. Name ");
            System.out.println("2. User ID");
            System.out.print("Enter sorting criterion: ");
            int sortCriterion = scanner.nextInt();
            scanner.nextLine();  // Consume newline
            String criterion = sortCriterion == 1 ? "name" : "id";
            List<User> users = useDatabase ? PersistenceHandler.sortUsers(criterion) : FileHandler.sortUsers(criterion);
            System.out.println("Sorted Users:");
            for (User user : users) {
                System.out.println(user);
            }
        } else {
            System.out.println("Invalid choice.");
        }
    }

    public static void returnBook(String userId, String bookId, int daysLoaned) {
        boolean loanExists;

       
        if (useDatabase) {
            loanExists = PersistenceHandler.isBookLoanedToUser(userId, bookId);
        } else {
            loanExists = FileHandler.isBookLoanedToUser(userId, bookId);
        }

        if (!loanExists) {
            System.out.println("No loan record found for this user and book.");
            return;
        }


        int allowedLoanDays = useDatabase ? PersistenceHandler.getAllowedDaysForBook(bookId)
                                          : FileHandler.getAllowedDaysForBook(bookId);
        int overdueDays = Math.max(0, daysLoaned - allowedLoanDays);
        double penalty = overdueDays * 3.0; 

        totalRevenue += penalty;

        if (useDatabase) {
            PersistenceHandler.persistReturnBook(userId, bookId, overdueDays);
        } else {
            FileHandler.persistReturnBook(userId, bookId, overdueDays);
        }

        System.out.println("Book returned by user: " + userId + ". Penalty for late return: $" + penalty);
    }


    private static void displayLoanedBooksWithUsers() {
        List<String> loanedBooksWithUsers;
        if (useDatabase) {
            loanedBooksWithUsers = PersistenceHandler.persistDisplayLoanedBooksWithUsers();
        } else {
            loanedBooksWithUsers = FileHandler.persistDisplayLoanedBooksWithUsers();
        }

        if (loanedBooksWithUsers.isEmpty()) {
            System.out.println("No loaned books with users.");
        } else {
            System.out.println("\nLoaned Books with Users:");
            loanedBooksWithUsers.forEach(System.out::println);
        }
    }

    public static void extendLoan(String userId, String bookId) {
        Book book = books.stream().filter(b -> b.getBookID().equals(bookId) && b instanceof Textbook).findFirst().orElse(null);

        if (book != null && ((Textbook) book).extendLoan()) {
            double fee = book.calculateLoanFee(10);
            totalRevenue += fee;

            if (useDatabase) {
                PersistenceHandler.persistExtendLoan(bookId);
            } else {
                FileHandler.persistExtendLoan(bookId);
            }
            System.out.println("Loan extended for book: " + bookId);
        } else {
            System.out.println("Loan extension failed or the book has already been extended.");
        }
    }

    private static void displayTotalRevenue() {
        if (useDatabase) {
            totalRevenue = PersistenceHandler.persistDisplayTotalRevenue();
        } else {
            totalRevenue = FileHandler.persistDisplayTotalRevenue();
        }
        System.out.println("\nTotal Revenue from loans and penalties: $" + totalRevenue);
    }


    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        initializePersistenceOption(scanner);
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC Driver not found.");
            e.printStackTrace();
            return;
        }


        int choice;

        do {
            System.out.println("\nLibrary Management System Menu:");
            System.out.println("1. Add New User");
            System.out.println("2. Add New Book");
            System.out.println("3. Display Available Books");
            System.out.println("4. Display Loaned Books");
            System.out.println("5. Display Users");
            System.out.println("6. Loan a Book");
            System.out.println("7. Return a Book");
            System.out.println("8. Extend Loan for Textbook");
            System.out.println("9. Display Total Revenue");
            System.out.println("10. Display Loaned Books with Users");
            System.out.println("11. Search Books and Users");  // New option for search
            System.out.println("12. Delete User or Book");
            System.out.println("13. Sort Books and Users");
            System.out.println("0. Exit");
            System.out.print("Enter your choice: ");
            choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1 -> addNewUser(scanner);
                case 2 -> addNewBook(scanner);
                case 3 -> displayAvailableBooks();
                case 4 -> displayLoanedBooks();
                case 5 -> displayUsers();
                case 6 -> loanBookMenu(scanner);
                case 7 -> returnBookMenu(scanner);
                case 8 -> extendLoanMenu(scanner);
                case 9 -> displayTotalRevenue();
                case 10 -> displayLoanedBooksWithUsers();
                case 11 ->
                    searchMenu(scanner);  // New search functionality
                case 12->
                    deleteMenu(scanner);
                case 13->
                    sortingMenu(scanner); 

                case 0 -> System.out.println("Exiting system...");
                default -> System.out.println("Invalid choice. Try again.");
            }
        } while (choice != 0);

        scanner.close();
    }
    private static void deleteMenu(Scanner scanner) {
        System.out.println("Delete:");
        System.out.println("1. User");
        System.out.println("2. Book");
        System.out.print("Enter your choice: ");
        int choice = scanner.nextInt();
        scanner.nextLine();  // Consume newline

        if (choice == 1) {
            System.out.print("Enter User ID to delete: ");
            String userId = scanner.nextLine();
            boolean success = useDatabase ? PersistenceHandler.deleteUser(userId) : FileHandler.deleteUser(userId);
            if (!success) {
                System.out.println("Deletion failed. User may have active loans.");
            }
        } else if (choice == 2) {
            System.out.print("Enter Book ID to delete: ");
            String bookId = scanner.nextLine();
            boolean success = useDatabase ? PersistenceHandler.deleteBook(bookId) : FileHandler.deleteBook(bookId);
            if (!success) {
                System.out.println("Deletion failed. Book may be currently on loan.");
            }
        } else {
            System.out.println("Invalid choice.");
        }
    }

    private static void addNewUser(Scanner scanner) {
        System.out.println("Enter User Type:\n1. Student\n2. Faculty\n3. Public Member");
        int userType = scanner.nextInt();
        scanner.nextLine();
        System.out.print("Enter User ID: ");
        String userId = scanner.nextLine();
        System.out.print("Enter Name: ");
        String name = scanner.nextLine();
        System.out.print("Enter Email: ");
        String email = scanner.nextLine();
        System.out.print("Enter Phone: ");
        String phone = scanner.nextLine();
        System.out.print("Enter Address: ");
        String address = scanner.nextLine();

        User user = switch (userType) {
            case 1 -> new Student(userId, name, email, phone, address);
            case 2 -> new Faculty(userId, name, email, phone, address);
            case 3 -> new PublicMember(userId, name, email, phone, address);
            default -> throw new IllegalArgumentException("Invalid user type.");
        };

        addUser(user);
    }

    private static void searchMenu(Scanner scanner) {
        System.out.println("Search for:");
        System.out.println("1. Book");
        System.out.println("2. User");
        System.out.print("Enter your choice: ");
        int choice = scanner.nextInt();
        scanner.nextLine();  // Consume newline

        System.out.print("Enter search query: ");
        String query = scanner.nextLine();

        if (choice == 1) {
            // Search for books
            List<Book> books = useDatabase ? PersistenceHandler.searchBook(query) : FileHandler.searchBook(query);
            System.out.println("Search Results for Books:");
            if (books.isEmpty()) {
                System.out.println("No books found matching the query.");
            } else {
                for (Book book : books) {
                    System.out.println(book);
                }
            }
        } else if (choice == 2) {
            // Search for users
            List<User> users = useDatabase ? PersistenceHandler.searchUser(query) : FileHandler.searchUser(query);
            System.out.println("Search Results for Users:");
            if (users.isEmpty()) {
                System.out.println("No users found matching the query.");
            } else {
                for (User user : users) {
                    System.out.println(user);
                }
            }
        } else {
            System.out.println("Invalid choice.");
        }
    }
    private static void addNewBook(Scanner scanner) {
        System.out.println("Enter Book Type:\n1. Textbook\n2. Novel\n3. Reference Book");
        int bookType = scanner.nextInt();
        scanner.nextLine();
        System.out.print("Enter Book ID: ");
        String bookId = scanner.nextLine();
        System.out.print("Enter Title: ");
        String title = scanner.nextLine();
        System.out.print("Enter Author: ");
        String author = scanner.nextLine();
        System.out.print("Enter ISBN: ");
        String isbn = scanner.nextLine();
        System.out.print("Enter Publication Year: ");
        int year = scanner.nextInt();
        scanner.nextLine();
        System.out.print("Enter Genre: ");
        String genre = scanner.nextLine();
        System.out.print("Enter Base Loan Fee: ");
        double baseFee = scanner.nextDouble();
        scanner.nextLine();

        Book book = switch (bookType) {
            case 1 -> new Textbook(bookId, title, author, isbn, year, genre, baseFee);
            case 2 -> new Novel(bookId, title, author, isbn, year, genre, baseFee);
            case 3 -> new ReferenceBook(bookId, title, author, isbn, year, genre, baseFee);
            default -> throw new IllegalArgumentException("Invalid book type.");
        };

        addBook(book);
    }

    private static void loanBookMenu(Scanner scanner) {
        System.out.print("Enter User ID: ");
        String userId = scanner.nextLine();
        System.out.print("Enter Book ID: ");
        String bookId = scanner.nextLine();
        System.out.print("Enter Loan Period (in days): ");
        int days = scanner.nextInt();
        loanBook(userId, bookId, days);
    }

    private static void returnBookMenu(Scanner scanner) {
        System.out.print("Enter User ID: ");
        String userId = scanner.nextLine();
        System.out.print("Enter Book ID: ");
        String bookId = scanner.nextLine();
        System.out.print("Enter number of days the book was borrowed: ");
        int daysLoaned = scanner.nextInt();
        returnBook(userId, bookId, daysLoaned);
    }

    private static void extendLoanMenu(Scanner scanner) {
        System.out.print("Enter Book ID: ");
        String bookId = scanner.nextLine();
        extendLoan(bookId, bookId);
    }
}
