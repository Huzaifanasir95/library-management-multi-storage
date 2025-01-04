# 📚 Library Management System (Multi-Storage)

**"Flexible, Scalable, and Efficient Library Management with File and SQL Storage Options"**

## 📚 Overview  
This project implements a **Library Management System (LMS)** in **Java**, supporting **dual storage options**:  
1. **File-Based Storage:** Data stored in structured files (`.txt`, `.csv`).  
2. **SQL Server (SSMS):** Database integration for persistent storage and CRUD operations.  

The user is prompted to select the storage method at the start, and both storage types are fully functional and seamlessly integrated.

---

## 🚀 **Key Features**

### ✅ **Multi-Storage Data Persistence:**  
- File-based and SQL Server storage options available.  
- Seamless switching between storage methods during runtime.  

### ✅ **File-Based Storage:**  
- Efficient file handling techniques.  
- Data is structured, readable, and well-organized (`.txt`, `.csv`).  

### ✅ **SQL Database Integration:**  
- CRUD operations (Create, Read, Update, Delete).  
- Data synchronization during retrieval, updates, and deletion.  

### ✅ **Input Validation and Deletion Logic:**  
- Unique ID validation for users and books.  
- Proper handling of deletions with active loans or ongoing transactions.  

### ✅ **Error Handling:**  
- Comprehensive error handling for file operations and SQL connections.  
- Clear, user-friendly error messages for failed operations.  

### ✅ **Search and Sorting Functionalities:**  
- Search books and users by multiple attributes (`Book ID`, `User ID`).  
- Sort books and users alphabetically or by custom criteria.  

### ✅ **Loan Extensions and Fines:**  
- Automatic fine calculation for late returns.  
- Support for loan extension requests.  

### ✅ **Program Execution and User Interface (UI):**  
- Intuitive menu system for easy navigation.  
- Clear and concise code documentation.  

---

## 🛠️ **Technologies Used:**  
- **Java:** Core language for backend logic.  
- **SQL Server (SSMS):** Database management system for persistent data storage.  
- **File I/O:** File handling for text and CSV data storage.  
- **JDBC:** For connecting Java with SQL Server.  

---

## 🗂️ **Project Structure:**  
- **Storage:** Handles file-based and SQL database operations.  
- **Controllers:** Manage interactions between UI and storage layers.  
- **Models:** Represent core data entities (Books, Users, Loans).  
- **Utilities:** Error handling and validation utilities.  
- **Views:** User interface for interacting with the system.  

---

## 📥 **Setup Instructions:**  
1. Clone the repository:  
   ```bash
   git clone https://github.com/Huzaifanasir95/library-management-multi-storage.git
