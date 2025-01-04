package Managment_System;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


import java.util.Comparator;

public class FileHandler {
	private static final String FILE_DIRECTORY = "D:\\SEM5\\SDA\\Files\\";

	private static final String LOANS_FILE = FILE_DIRECTORY + "loans.txt";
	private static final String BOOKS_FILE = FILE_DIRECTORY + "books.txt";
	private static final String USERS_FILE = FILE_DIRECTORY + "users.txt";
	private static final String REVENUE_FILE = FILE_DIRECTORY + "revenue.txt";
//	private static final String PENALTIES_FILE = FILE_DIRECTORY + "penalties.txt";


	  private static void ensureFileExists(String filePath) {
	        try {
	            File file = new File(filePath);
	            File directory = new File(FILE_DIRECTORY);

	            if (!directory.exists()) {
	                directory.mkdirs();
	            }

	            if (!file.exists()) {
	                file.createNewFile();
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }

    public static void persistAddUser(User user) {
    	
    	    ensureFileExists(USERS_FILE);

    	    if (userExists(user.getUserID())) {
    	        System.out.println("A user with the same ID already exists: " + user.getUserID());
    	        return; 
    	    }

    	    try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE, true))) {
    	        String userType = user instanceof Student ? "Student" :
    	                          user instanceof Faculty ? "Faculty" : "PublicMember";
    	        writer.write(String.format("%s,%s,%s,%s,%s,%s,%.2f%n",
    	                user.getUserID(), user.getName(), user.getEmail(),
    	                user.getPhone(), user.getAddress(), userType, user.getTotalLoanFees()));
    	        System.out.println("User added: " + user);
    	    } catch (IOException e) {
    	        e.printStackTrace();
    	    }
    	
}
    
    public static boolean userExists(String userId) {
        ensureFileExists(USERS_FILE);

        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields[0].equals(userId)) { 
                    return true; 
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;  
    }


    public static void persistAddBook(Book book) {
    	 ensureFileExists(BOOKS_FILE);

    	    if (bookExists(book.getBookID())) {
    	        System.out.println("A book with the same ID already exists: " + book.getBookID());
    	        return; 
    	    }

    	    try (BufferedWriter writer = new BufferedWriter(new FileWriter(BOOKS_FILE, true))) {
    	        String bookType = book instanceof Textbook ? "Textbook" :
    	                          book instanceof Novel ? "Novel" : "ReferenceBook";
    	        writer.write(String.format("%s,%s,%s,%s,%d,%s,%.2f,%s,%b%n",
    	                book.getBookID(), book.getTitle(), book.getAuthor(), book.getIsbn(),
    	                book.getPublicationYear(), book.getGenre(), book.getBaseLoanFee(),
    	                bookType, book.isLoaned()));
    	        System.out.println("Book added: " + book);
    	    } catch (IOException e) {
    	        e.printStackTrace();
    	    }
    }

    public static boolean bookExists(String bookId) {
        ensureFileExists(BOOKS_FILE);

        try (BufferedReader reader = new BufferedReader(new FileReader(BOOKS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields[0].equals(bookId)) { 
                    return true;  
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false; 
    }

    public static List<Book> persistDisplayAvailableBooks() {
        ensureFileExists(BOOKS_FILE);
        List<Book> availableBooks = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(BOOKS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                boolean loanStatus = Boolean.parseBoolean(fields[8]);
                if (!loanStatus) {
                    availableBooks.add(createBookFromFields(fields));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return availableBooks;
    }

    public static List<Book> persistDisplayLoanedBooks() {
        List<Book> loanedBooks = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(BOOKS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                boolean loanStatus = Boolean.parseBoolean(fields[8]);
                if (loanStatus) {
                    loanedBooks.add(createBookFromFields(fields));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return loanedBooks;
    }

    public static List<User> persistDisplayUsers() {
        List<User> users = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                users.add(createUserFromFields(fields));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return users;
    }


    public static void persistLoanBook(String userID, String bookID, int days, double fee) {
        ensureFileExists(LOANS_FILE);
        ensureFileExists(REVENUE_FILE);

        LocalDate loanDate = LocalDate.now();
        LocalDate returnDate = loanDate.plusDays(days);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOANS_FILE, true))) {
            writer.write(String.format("%s,%s,%s,%s,%.2f,%d%n",
                    userID, bookID, loanDate, returnDate, fee, 0));
        } catch (IOException e) {
            e.printStackTrace();
        }

        updateBookLoanStatus(bookID, true);

        try (BufferedWriter revenueWriter = new BufferedWriter(new FileWriter(REVENUE_FILE, true))) {
            revenueWriter.write(String.format("%.2f,Loan,%s%n", fee, loanDate));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public static void persistReturnBook(String userID, String bookID, int overdueDays) {
        ensureFileExists(LOANS_FILE);
        ensureFileExists(BOOKS_FILE);
        ensureFileExists(REVENUE_FILE);

        List<String> loans = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(LOANS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                if (!fields[0].equals(userID) || !fields[1].equals(bookID)) {
                    loans.add(line); 
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOANS_FILE))) {
            for (String loan : loans) {
                writer.write(loan + System.lineSeparator());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        updateBookLoanStatus(bookID, false);

        if (overdueDays > 0) {
            double penalty = overdueDays * 3.0;  
            try (BufferedWriter revenueWriter = new BufferedWriter(new FileWriter(REVENUE_FILE, true))) {
                revenueWriter.write(String.format("%.2f,Penalty,%s%n", penalty, LocalDate.now()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Penalty of $" + penalty + " applied for late return.");
        }
    }


    public static boolean persistExtendLoan(String bookID) {
        List<String> loans = new ArrayList<>();
        boolean extended = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(LOANS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields[1].equals(bookID) && fields[5].equals("0")) {
                    fields[5] = "1"; 
                    extended = true;
                    line = String.join(",", fields);
                }
                loans.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (extended) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOANS_FILE))) {
                for (String loan : loans) {
                    writer.write(loan + System.lineSeparator());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return extended;
    }

    public static double persistDisplayTotalRevenue() {
        ensureFileExists(REVENUE_FILE);
        double totalRevenue = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(REVENUE_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                double amount = Double.parseDouble(fields[0]);
                totalRevenue += amount;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return totalRevenue;
    }

    public static List<String> persistDisplayLoanedBooksWithUsers() {
        List<String> loanedBooksWithUsers = new ArrayList<>();
        File loanFile = new File(LOANS_FILE);

        try {
            if (!loanFile.exists()) {
                loanFile.createNewFile();  
            }
        } catch (IOException e) {
            e.printStackTrace();
            return loanedBooksWithUsers;
        }

        try (BufferedReader loanReader = new BufferedReader(new FileReader(loanFile));
             BufferedReader bookReader = new BufferedReader(new FileReader(BOOKS_FILE));
             BufferedReader userReader = new BufferedReader(new FileReader(USERS_FILE))) {

            String loanLine;
            while ((loanLine = loanReader.readLine()) != null) {
                String[] loanFields = loanLine.split(",");
                String userID = loanFields[0];
                String bookID = loanFields[1];

                String bookTitle = getFieldFromFile(bookID, 1, BOOKS_FILE);  
                String userName = getFieldFromFile(userID, 1, USERS_FILE);  

                loanedBooksWithUsers.add("User: " + userID + " (" + userName + ") has loaned Book: " + bookID + " (" + bookTitle + ")");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return loanedBooksWithUsers;
    }


    private static String getFieldFromFile(String id, int fieldIndex, String fileName) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields[0].equals(id)) {
                    return fields[fieldIndex];
                }
            }
        }
        return "";
    }

    public static void updateBookLoanStatus(String bookID, boolean isLoaned) {
        ensureFileExists(BOOKS_FILE);
        
        List<String> books = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(BOOKS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields[0].equals(bookID)) { 
                    fields[8] = Boolean.toString(isLoaned); 
                    line = String.join(",", fields);
                }
                books.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(BOOKS_FILE))) {
            for (String bookLine : books) {
                writer.write(bookLine + System.lineSeparator());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static List<Book> loadBooks() {
        ensureFileExists(BOOKS_FILE);
        List<Book> books = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(BOOKS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                books.add(createBookFromFields(fields));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return books;
    }

    public static void saveBooks(List<Book> books) {
        ensureFileExists(BOOKS_FILE);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(BOOKS_FILE))) {
            for (Book book : books) {
                writer.write(String.format("%s,%s,%s,%s,%d,%s,%.2f,%s,%b%n",
                        book.getBookID(), book.getTitle(), book.getAuthor(), book.getIsbn(),
                        book.getPublicationYear(), book.getGenre(), book.getBaseLoanFee(),
                        book instanceof Textbook ? "Textbook" :
                        book instanceof Novel ? "Novel" : "ReferenceBook",
                        book.isLoaned()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Helper Method: Create Book from fields in file
    private static Book createBookFromFields(String[] fields) {
        String bookID = fields[0];
        String title = fields[1];
        String author = fields[2];
        String isbn = fields[3];
        int publicationYear = Integer.parseInt(fields[4]);
        String genre = fields[5];
        double baseLoanFee = Double.parseDouble(fields[6]);
        String bookType = fields[7];
        boolean loanStatus = Boolean.parseBoolean(fields[8]);

        return switch (bookType) {
            case "Textbook" -> new Textbook(bookID, title, author, isbn, publicationYear, genre, baseLoanFee);
            case "Novel" -> new Novel(bookID, title, author, isbn, publicationYear, genre, baseLoanFee);
            case "ReferenceBook" -> new ReferenceBook(bookID, title, author, isbn, publicationYear, genre, baseLoanFee);
            default -> throw new IllegalArgumentException("Invalid book type.");
        };
    }

    // Helper Method: Create User from fields in file
    private static User createUserFromFields(String[] fields) {
        String userID = fields[0];
        String name = fields[1];
        String email = fields[2];
        String phone = fields[3];
        String address = fields[4];
        String userType = fields[5];
        double totalLoanFees = Double.parseDouble(fields[6]);

        return switch (userType) {
            case "Student" -> new Student(userID, name, email, phone, address);
            case "Faculty" -> new Faculty(userID, name, email, phone, address);
            case "PublicMember" -> new PublicMember(userID, name, email, phone, address);
            default -> throw new IllegalArgumentException("Invalid user type.");
        };
    }

    // Helper Method: Remove loan entry from loans file
    private static void removeLoanEntry(String bookID) {
        List<String> loans = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(LOANS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                if (!fields[1].equals(bookID)) {  // Keep only loans that don't match bookID
                    loans.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Write back all remaining loans
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOANS_FILE))) {
            for (String loan : loans) {
                writer.write(loan + System.lineSeparator());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void persistRemoveBook(String bookID) {
        boolean isLoaned = false;

        // Check if the book is on loan by reading LOANS_FILE
        try (BufferedReader loanReader = new BufferedReader(new FileReader(LOANS_FILE))) {
            String line;
            while ((line = loanReader.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields[1].equals(bookID)) {  // bookID is the second field in LOANS_FILE entries
                    isLoaned = true;
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // If the book is on loan, do not remove it
        if (isLoaned) {
            System.out.println("Book cannot be removed as it is currently on loan: " + bookID);
            return;
        }

        // Remove the book from BOOKS_FILE if it's not on loan
        List<String> updatedBooks = new ArrayList<>();
        try (BufferedReader bookReader = new BufferedReader(new FileReader(BOOKS_FILE))) {
            String line;
            while ((line = bookReader.readLine()) != null) {
                String[] fields = line.split(",");
                if (!fields[0].equals(bookID)) {  // bookID is the first field in BOOKS_FILE entries
                    updatedBooks.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Write the updated book list back to BOOKS_FILE
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(BOOKS_FILE))) {
            for (String bookEntry : updatedBooks) {
                writer.write(bookEntry + System.lineSeparator());
            }
            System.out.println("Book removed from file: " + bookID);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static User fetchUser(String userId) {
        ensureFileExists(USERS_FILE);

        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields[0].equals(userId)) {
                    // Assume the format is: userID, name, email, phone, address, userType, totalLoanFees
                    String userType = fields[5].trim();
                    switch (userType) {
                        case "Student":
                            return new Student(fields[0], fields[1], fields[2], fields[3], fields[4]);
                        case "Faculty":
                            return new Faculty(fields[0], fields[1], fields[2], fields[3], fields[4]);
                        case "PublicMember":
                            return new PublicMember(fields[0], fields[1], fields[2], fields[3], fields[4]);
                        default:
                            System.out.println("Unknown user type found: " + userType);
                            return null;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;  // User not found
    }

    
    public static Book fetchBook(String bookId) {
        ensureFileExists(BOOKS_FILE);

        try (BufferedReader reader = new BufferedReader(new FileReader(BOOKS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields[0].equals(bookId)) {
                    // Assume the format is: bookID, title, author, isbn, publicationYear, genre, baseLoanFee, bookType, loanStatus
                    String bookType = fields[7].trim();
//                    boolean loanStatus = Boolean.parseBoolean(fields[8]);

                    Book book;
                    switch (bookType) {
                        case "Textbook":
                            book = new Textbook(fields[0], fields[1], fields[2], fields[3], Integer.parseInt(fields[4]), fields[5], Double.parseDouble(fields[6]));
                            break;
                        case "Novel":
                            book = new Novel(fields[0], fields[1], fields[2], fields[3], Integer.parseInt(fields[4]), fields[5], Double.parseDouble(fields[6]));
                            break;
                        case "ReferenceBook":
                            book = new ReferenceBook(fields[0], fields[1], fields[2], fields[3], Integer.parseInt(fields[4]), fields[5], Double.parseDouble(fields[6]));
                            break;
                        default:
                            System.out.println("Unknown book type found: " + bookType);
                            return null;
                    }
                    book.setLoanStatus(Boolean.parseBoolean(fields[8]));
                    return book;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;  // Book not found
    }
    
    public static boolean isBookLoanedToUser(String userId, String bookId) {
        ensureFileExists(LOANS_FILE);
        try (BufferedReader reader = new BufferedReader(new FileReader(LOANS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields[0].equals(userId) && fields[1].equals(bookId)) {
                    return true;  // Loan record found
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;  // No loan record found
    }

    public static int getAllowedDaysForBook(String bookId) {
        ensureFileExists(BOOKS_FILE);
        try (BufferedReader reader = new BufferedReader(new FileReader(BOOKS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields[0].equals(bookId)) {
                    String bookType = fields[7].trim();
                    switch (bookType) {
                        case "Textbook": return 14;
                        case "Novel": return 7;
                        default: return 0;  // Reference books cannot be loaned
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;  // Default if no book record is found
    }

    public static List<Book> searchBook(String query) {
        ensureFileExists(BOOKS_FILE);
        List<Book> books = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(BOOKS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields[0].contains(query) || fields[1].contains(query) || fields[2].contains(query)) {
                    books.add(createBookFromFields(fields));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return books;
    }

    // Search user by ID, name, or email in USERS_FILE
    public static List<User> searchUser(String query) {
        ensureFileExists(USERS_FILE);
        List<User> users = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields[0].contains(query) || fields[1].contains(query) || fields[2].contains(query)) {
                    users.add(createUserFromFields(fields));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return users;
    }
    public static boolean deleteUser(String userId) {
        ensureFileExists(LOANS_FILE);
        ensureFileExists(USERS_FILE);

        // Check for active loans
        try (BufferedReader reader = new BufferedReader(new FileReader(LOANS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields[0].equals(userId)) {  // User has an active loan
                    System.out.println("Cannot delete user with active loans.");
                    return false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Proceed with user deletion if no active loans
        List<String> users = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith(userId + ",")) {
                    users.add(line);  // Retain non-matching entries
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Rewrite USERS_FILE without the deleted user
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE))) {
            for (String userLine : users) {
                writer.write(userLine + System.lineSeparator());
            }
            System.out.println("User deleted: " + userId);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Delete book if it is not currently loaned out
    public static boolean deleteBook(String bookId) {
        ensureFileExists(BOOKS_FILE);

        List<String> books = new ArrayList<>();
        boolean foundAndDeletable = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(BOOKS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields[0].equals(bookId)) {
                    if (Boolean.parseBoolean(fields[8])) {  // loanStatus is true
                        System.out.println("Cannot delete a book that is currently on loan.");
                        return false;
                    } else {
                        foundAndDeletable = true;  // Mark for deletion
                    }
                } else {
                    books.add(line);  // Retain non-matching entries
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!foundAndDeletable) {
            System.out.println("Book not found.");
            return false;
        }

        // Rewrite BOOKS_FILE without the deleted book
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(BOOKS_FILE))) {
            for (String bookLine : books) {
                writer.write(bookLine + System.lineSeparator());
            }
            System.out.println("Book deleted: " + bookId);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    public static List<Book> sortBooks(String criterion) {
        ensureFileExists(BOOKS_FILE);
        List<Book> books = loadBooks2();  // Load all books from file

        switch (criterion) {
            case "title" -> books.sort(Comparator.comparing(Book::getTitle));
            case "id" -> books.sort(Comparator.comparing(Book::getBookID));
            default -> throw new IllegalArgumentException("Invalid sorting criterion: " + criterion);
        }
        return books;
    }

    // Sort users by name (alphabetical order) or by userID
    public static List<User> sortUsers(String criterion) {
        ensureFileExists(USERS_FILE);
        List<User> users = loadUsers();  // Load all users from file

        switch (criterion) {
            case "name" -> users.sort(Comparator.comparing(User::getName));
            case "id" -> users.sort(Comparator.comparing(User::getUserID));
            default -> throw new IllegalArgumentException("Invalid sorting criterion: " + criterion);
        }
        return users;
    }

    // Helper methods to load books and users
    private static List<Book> loadBooks2() {
        List<Book> books = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(BOOKS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                books.add(createBookFromFields(line.split(",")));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return books;
    }

    private static List<User> loadUsers() {
        List<User> users = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                users.add(createUserFromFields(line.split(",")));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return users;
    }


}
