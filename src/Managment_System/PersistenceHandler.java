package Managment_System;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PersistenceHandler {
    private static final String url = "jdbc:sqlserver://PAVILION\\\\SQLEXPRESS01:65096;databaseName=LMS;integratedSecurity=true;trustServerCertificate=true";

    public static void persistAddUser(User user) {
        String insertUserSQL = "INSERT INTO Users (userID, name, email, phone, address, userType, totalLoanFees) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement pstmt = connection.prepareStatement(insertUserSQL)) {

            pstmt.setString(1, user.getUserID());
            pstmt.setString(2, user.getName());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getPhone());
            pstmt.setString(5, user.getAddress());
            pstmt.setString(6, user instanceof Student ? "Student" : user instanceof Faculty ? "Faculty" : "PublicMember");
            pstmt.setDouble(7, user.getTotalLoanFees());

            pstmt.executeUpdate();
            System.out.println("User added to the database: " + user);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void persistAddBook(Book book) {
        String insertBookSQL = "INSERT INTO Books (bookID, title, author, isbn, publicationYear, genre, baseLoanFee, bookType) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement pstmt = connection.prepareStatement(insertBookSQL)) {

            pstmt.setString(1, book.getBookID());
            pstmt.setString(2, book.getTitle());
            pstmt.setString(3, book.getAuthor());
            pstmt.setString(4, book.getIsbn());
            pstmt.setInt(5, book.getPublicationYear());
            pstmt.setString(6, book.getGenre());
            pstmt.setDouble(7, book.getBaseLoanFee());
            pstmt.setString(8, book instanceof Textbook ? "Textbook" : book instanceof Novel ? "Novel" : "ReferenceBook");

            pstmt.executeUpdate();
            System.out.println("Book added to the database: " + book);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Book> persistDisplayAvailableBooks() {
        List<Book> availableBooks = new ArrayList<>();
        String selectBooksSQL = 
            "SELECT b.* FROM Books b " +
            "LEFT JOIN Loans l ON b.bookID = l.bookID " +
            "WHERE l.bookID IS NULL"; 

        try (Connection connection = DriverManager.getConnection(url);
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(selectBooksSQL)) {

            while (rs.next()) {
                Book book = createBookFromResultSet(rs);
                availableBooks.add(book);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return availableBooks;
    }

    public static List<Book> persistDisplayLoanedBooks() {
        List<Book> loanedBooks = new ArrayList<>();
        String selectLoanedBooksSQL = 
            "SELECT b.* FROM Books b " +
            "INNER JOIN Loans l ON b.bookID = l.bookID";  

        try (Connection connection = DriverManager.getConnection(url);
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(selectLoanedBooksSQL)) {

            while (rs.next()) {
                Book book = createBookFromResultSet(rs);
                loanedBooks.add(book);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return loanedBooks;
    }

    public static List<User> persistDisplayUsers() {
        List<User> users = new ArrayList<>();
        String selectUsersSQL = "SELECT * FROM Users";

        try (Connection connection = DriverManager.getConnection(url);
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(selectUsersSQL)) {

            while (rs.next()) {
                User user = createUserFromResultSet(rs);
                users.add(user);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public static void persistLoanBook(String userID, String bookID, int days, double fee) {
        String checkLoanSQL = "SELECT COUNT(*) FROM Loans WHERE bookID = ?";
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement checkStmt = connection.prepareStatement(checkLoanSQL)) {

            checkStmt.setString(1, bookID);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("Book is already on loan and cannot be loaned again.");
                return; 
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        String insertLoanSQL = "INSERT INTO Loans (userID, bookID, loanDate, returnDate, loanFee, loanExtended) VALUES (?, ?, ?, ?, ?, 0)";
        String updateBookStatusSQL = "UPDATE Books SET loanStatus = 1 WHERE bookID = ?";
        String insertRevenueSQL = "INSERT INTO Revenue (amount, revenueType, transactionDate) VALUES (?, 'Loan', ?)";

        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement insertLoanStmt = connection.prepareStatement(insertLoanSQL);
             PreparedStatement updateBookStmt = connection.prepareStatement(updateBookStatusSQL);
             PreparedStatement insertRevenueStmt = connection.prepareStatement(insertRevenueSQL)) {

            LocalDate loanDate = LocalDate.now();
            LocalDate returnDate = loanDate.plusDays(days);

            insertLoanStmt.setString(1, userID);
            insertLoanStmt.setString(2, bookID);
            insertLoanStmt.setDate(3, Date.valueOf(loanDate));
            insertLoanStmt.setDate(4, Date.valueOf(returnDate));
            insertLoanStmt.setDouble(5, fee);
            insertLoanStmt.executeUpdate();

            updateBookStmt.setString(1, bookID);
            updateBookStmt.executeUpdate();

            insertRevenueStmt.setDouble(1, fee);
            insertRevenueStmt.setDate(2, Date.valueOf(loanDate));
            insertRevenueStmt.executeUpdate();

            System.out.println("Book loaned to user: " + userID + ". Loan fee: $" + fee);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    


    public static boolean isBookLoanedToUser(String userID, String bookID) {
        String queryLoanSQL = "SELECT COUNT(*) FROM Loans WHERE userID = ? AND bookID = ?";

        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement pstmt = connection.prepareStatement(queryLoanSQL)) {

            pstmt.setString(1, userID);
            pstmt.setString(2, bookID);
            ResultSet rs = pstmt.executeQuery();

            return rs.next() && rs.getInt(1) > 0; 

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void persistReturnBook(String userID, String bookID, int overdueDays) {
        String deleteLoanSQL = "DELETE FROM Loans WHERE userID = ? AND bookID = ?";
        String updateBookStatusSQL = "UPDATE Books SET loanStatus = 0 WHERE bookID = ?";
        String insertPenaltySQL = "INSERT INTO Penalties (userID, penaltyAmount, penaltyDate) VALUES (?, ?, ?)";
        String insertRevenueSQL = "INSERT INTO Revenue (amount, revenueType, transactionDate) VALUES (?, 'Penalty', ?)";

        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement deleteLoanStmt = connection.prepareStatement(deleteLoanSQL);
             PreparedStatement updateBookStmt = connection.prepareStatement(updateBookStatusSQL);
             PreparedStatement insertPenaltyStmt = connection.prepareStatement(insertPenaltySQL);
             PreparedStatement insertRevenueStmt = connection.prepareStatement(insertRevenueSQL)) {

            if (overdueDays > 0) {
                double penaltyAmount = overdueDays * 3.0; 

                insertPenaltyStmt.setString(1, userID);
                insertPenaltyStmt.setDouble(2, penaltyAmount);
                insertPenaltyStmt.setDate(3, Date.valueOf(LocalDate.now()));
                insertPenaltyStmt.executeUpdate();

                insertRevenueStmt.setDouble(1, penaltyAmount);
                insertRevenueStmt.setDate(2, Date.valueOf(LocalDate.now()));
                insertRevenueStmt.executeUpdate();

                System.out.println("Penalty of $" + penaltyAmount + " applied for late return.");
            }

            deleteLoanStmt.setString(1, userID);
            deleteLoanStmt.setString(2, bookID);
            deleteLoanStmt.executeUpdate();

            // Reset the book status to available
            updateBookStmt.setString(1, bookID);
            updateBookStmt.executeUpdate();

            System.out.println("Book returned and status updated for bookID: " + bookID);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static boolean persistExtendLoan(String bookID) {
        String updateLoanSQL = "UPDATE Loans SET loanExtended = 1 WHERE bookID = ?";

        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement pstmt = connection.prepareStatement(updateLoanSQL)) {

            pstmt.setString(1, bookID);
            int rowsAffected = pstmt.executeUpdate();

            return rowsAffected > 0; 

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static double persistDisplayTotalRevenue() {
        String totalRevenueSQL = "SELECT SUM(amount) AS totalRevenue FROM Revenue";
        double totalRevenue = 0;

        try (Connection connection = DriverManager.getConnection(url);
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(totalRevenueSQL)) {

            if (rs.next()) {
                totalRevenue = rs.getDouble("totalRevenue");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return totalRevenue;
    }

    public static List<String> persistDisplayLoanedBooksWithUsers() {
        List<String> loanedBooksWithUsers = new ArrayList<>();
        String selectLoanedBooksSQL = 
            "SELECT u.userID, u.name, b.bookID, b.title " +
            "FROM Users u " +
            "INNER JOIN Loans l ON u.userID = l.userID " +
            "INNER JOIN Books b ON l.bookID = b.bookID";

        try (Connection connection = DriverManager.getConnection(url);
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(selectLoanedBooksSQL)) {

            while (rs.next()) {
                String userID = rs.getString("userID");
                String userName = rs.getString("name");
                String bookID = rs.getString("bookID");
                String bookTitle = rs.getString("title");

                loanedBooksWithUsers.add("User: " + userID + " (" + userName + ") has loaned Book: " + bookID + " (" + bookTitle + ")");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return loanedBooksWithUsers;
    }

    private static Book createBookFromResultSet(ResultSet rs) throws SQLException {
        String bookID = rs.getString("bookID");
        String title = rs.getString("title");
        String author = rs.getString("author");
        String isbn = rs.getString("isbn");
        int publicationYear = rs.getInt("publicationYear");
        String genre = rs.getString("genre");
        double baseLoanFee = rs.getDouble("baseLoanFee");
        String bookType = rs.getString("bookType");

        return switch (bookType) {
            case "Textbook" -> new Textbook(bookID, title, author, isbn, publicationYear, genre, baseLoanFee);
            case "Novel" -> new Novel(bookID, title, author, isbn, publicationYear, genre, baseLoanFee);
            case "ReferenceBook" -> new ReferenceBook(bookID, title, author, isbn, publicationYear, genre, baseLoanFee);
            default -> throw new IllegalArgumentException("Invalid book type.");
        };
    }

    private static User createUserFromResultSet(ResultSet rs) throws SQLException {
        String userID = rs.getString("userID");
        String name = rs.getString("name");
        String email = rs.getString("email");
        String phone = rs.getString("phone");
        String address = rs.getString("address");
        String userType = rs.getString("userType");
        double totalLoanFees = rs.getDouble("totalLoanFees");

        return switch (userType) {
            case "Student" -> new Student(userID, name, email, phone, address);
            case "Faculty" -> new Faculty(userID, name, email, phone, address);
            case "PublicMember" -> new PublicMember(userID, name, email, phone, address);
            default -> throw new IllegalArgumentException("Invalid user type.");
        };
    }
    public static void persistRemoveBook(String bookID) {
        String deleteBookSQL = "DELETE FROM Books WHERE bookID = ? AND bookID NOT IN (SELECT bookID FROM Loans)";
        
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement pstmt = connection.prepareStatement(deleteBookSQL)) {

            pstmt.setString(1, bookID);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Book removed from database: " + bookID);
            } else {
                System.out.println("Book cannot be removed, it may be on loan or doesn't exist.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static int getAllowedDaysForBook(String bookID) {
        String selectBookTypeSQL = "SELECT bookType FROM Books WHERE bookID = ?";
        int allowedDays = 0;

        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement pstmt = connection.prepareStatement(selectBookTypeSQL)) {

            pstmt.setString(1, bookID);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String bookType = rs.getString("bookType");

                switch (bookType) {
                    case "Textbook" -> allowedDays = 14; 
                    case "Novel" -> allowedDays = 7;      
                    case "ReferenceBook" -> allowedDays = 0; 
                    default -> System.out.println("Unknown book type.");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return allowedDays;
    }
    public static User fetchUser(String userId) {
        String selectUserSQL = "SELECT * FROM Users WHERE userID = ?";
        
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement pstmt = connection.prepareStatement(selectUserSQL)) {

            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String name = rs.getString("name");
                String email = rs.getString("email");
                String phone = rs.getString("phone");
                String address = rs.getString("address");
                String userType = rs.getString("userType");
                double totalLoanFees = rs.getDouble("totalLoanFees");

                return switch (userType) {
                    case "Student" -> new Student(userId, name, email, phone, address);
                    case "Faculty" -> new Faculty(userId, name, email, phone, address);
                    case "PublicMember" -> new PublicMember(userId, name, email, phone, address);
                    default -> null;
                };
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; 
    }

    public static Book fetchBook(String bookID) {
        String selectBookSQL = "SELECT * FROM Books WHERE bookID = ?";

        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement pstmt = connection.prepareStatement(selectBookSQL)) {

            pstmt.setString(1, bookID);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String title = rs.getString("title");
                String author = rs.getString("author");
                String isbn = rs.getString("isbn");
                int publicationYear = rs.getInt("publicationYear");
                String genre = rs.getString("genre");
                double baseLoanFee = rs.getDouble("baseLoanFee");
                String bookType = rs.getString("bookType");

                return switch (bookType) {
                    case "Textbook" -> new Textbook(bookID, title, author, isbn, publicationYear, genre, baseLoanFee);
                    case "Novel" -> new Novel(bookID, title, author, isbn, publicationYear, genre, baseLoanFee);
                    case "ReferenceBook" -> new ReferenceBook(bookID, title, author, isbn, publicationYear, genre, baseLoanFee);
                    default -> null;
                };
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; 
    }
    public static List<Book> searchBook(String query) {
        List<Book> books = new ArrayList<>();
        String searchSQL = "SELECT * FROM Books WHERE bookID LIKE ? OR title LIKE ? OR author LIKE ?";

        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement pstmt = connection.prepareStatement(searchSQL)) {
             
            pstmt.setString(1, "%" + query + "%");
            pstmt.setString(2, "%" + query + "%");
            pstmt.setString(3, "%" + query + "%");

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                books.add(createBookFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return books;
    }

    // Search user by ID, name, or email
    public static List<User> searchUser(String query) {
        List<User> users = new ArrayList<>();
        String searchSQL = "SELECT * FROM Users WHERE userID LIKE ? OR name LIKE ? OR email LIKE ?";

        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement pstmt = connection.prepareStatement(searchSQL)) {

            pstmt.setString(1, "%" + query + "%");
            pstmt.setString(2, "%" + query + "%");
            pstmt.setString(3, "%" + query + "%");

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                users.add(createUserFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }
    public static boolean deleteUser(String userId) {
        String loanCheckSQL = "SELECT COUNT(*) FROM Loans WHERE userID = ?";
        String deleteUserSQL = "DELETE FROM Users WHERE userID = ?";
        
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement loanCheckStmt = connection.prepareStatement(loanCheckSQL);
             PreparedStatement deleteUserStmt = connection.prepareStatement(deleteUserSQL)) {

            loanCheckStmt.setString(1, userId);
            ResultSet rs = loanCheckStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("Cannot delete user with active loans.");
                return false;
            }

            deleteUserStmt.setString(1, userId);
            deleteUserStmt.executeUpdate();
            System.out.println("User deleted: " + userId);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Delete book if it is not currently loaned out
    public static boolean deleteBook(String bookId) {
        String loanStatusCheckSQL = "SELECT loanStatus FROM Books WHERE bookID = ?";
        String deleteBookSQL = "DELETE FROM Books WHERE bookID = ?";

        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement loanStatusCheckStmt = connection.prepareStatement(loanStatusCheckSQL);
             PreparedStatement deleteBookStmt = connection.prepareStatement(deleteBookSQL)) {

            loanStatusCheckStmt.setString(1, bookId);
            ResultSet rs = loanStatusCheckStmt.executeQuery();
            if (rs.next() && rs.getBoolean("loanStatus")) {
                System.out.println("Cannot delete a book that is currently on loan.");
                return false;
            }

            deleteBookStmt.setString(1, bookId);
            deleteBookStmt.executeUpdate();
            System.out.println("Book deleted: " + bookId);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public static List<Book> sortBooks(String criterion) {
        List<Book> books = new ArrayList<>();
        String sortSQL = switch (criterion) {
            case "title" -> "SELECT * FROM Books ORDER BY title ASC";
            case "id" -> "SELECT * FROM Books ORDER BY bookID ASC";
            default -> throw new IllegalArgumentException("Invalid sorting criterion: " + criterion);
        };

        try (Connection connection = DriverManager.getConnection(url);
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sortSQL)) {

            while (rs.next()) {
                books.add(createBookFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return books;
    }

    // Sort users by name (alphabetical order) or by userID
    public static List<User> sortUsers(String criterion) {
        List<User> users = new ArrayList<>();
        String sortSQL = switch (criterion) {
            case "name" -> "SELECT * FROM Users ORDER BY name ASC";
            case "id" -> "SELECT * FROM Users ORDER BY userID ASC";
            default -> throw new IllegalArgumentException("Invalid sorting criterion: " + criterion);
        };

        try (Connection connection = DriverManager.getConnection(url);
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sortSQL)) {

            while (rs.next()) {
                users.add(createUserFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }
}



//-- Books table
//CREATE TABLE Books (
//    bookID VARCHAR(20) PRIMARY KEY,
//    title VARCHAR(100),
//    author VARCHAR(100),
//    isbn VARCHAR(20),
//    publicationYear INT,
//    genre VARCHAR(50),
//    baseLoanFee FLOAT,
//    bookType VARCHAR(20),
//    loanStatus BIT DEFAULT 0  -- 0 means available, 1 means on loan
//);
//
//-- Create the Users table
//CREATE TABLE Users (
//    userID VARCHAR(20) PRIMARY KEY,
//    name VARCHAR(100),
//    email VARCHAR(100),
//    phone VARCHAR(20),
//    address VARCHAR(100),
//    userType VARCHAR(20),
//    totalLoanFees FLOAT DEFAULT 0
//);
//
//-- Create the Loans table to track active loans
//CREATE TABLE Loans (
//    loanID INT PRIMARY KEY IDENTITY(1,1),
//    userID VARCHAR(20) FOREIGN KEY REFERENCES Users(userID),
//    bookID VARCHAR(20) FOREIGN KEY REFERENCES Books(bookID),
//    loanDate DATE,
//    returnDate DATE,
//    loanFee FLOAT,
//    loanExtended BIT DEFAULT 0  -- 0 means not extended, 1 means extended
//);
//
//-- Create the Penalties table for managing overdue penalties
//CREATE TABLE Penalties (
//    penaltyID INT PRIMARY KEY IDENTITY(1,1),
//    userID VARCHAR(20) FOREIGN KEY REFERENCES Users(userID),
//    penaltyAmount FLOAT,
//    penaltyDate DATE
//);
//
//-- Create the Revenue table to store historical revenue data
//CREATE TABLE Revenue (
//    revenueID INT PRIMARY KEY IDENTITY(1,1),
//    amount FLOAT,
//    revenueType VARCHAR(20),  -- "Loan" or "Penalty"
//    transactionDate DATE
//);
