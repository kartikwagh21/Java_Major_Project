# Driving School Enrollment & Test Management System

A Java Swing desktop application developed for **Case Study 90: Driving School Enrollment & Test Management System**. 

The system manages student enrollment, tracks attendance for theory and practical sessions, automatically evaluates eligibility criteria, and handles test scheduling across two-wheeler and four-wheeler training courses.

---

## Problem Statement

A driving school needs a system to manage student enrollment, track theory and practical training sessions, and schedule driving tests across two-wheeler and four-wheeler courses. The system should automatically flag students who are not yet eligible for their final driving test based on session attendance and fee clearance.

---

## Features & Modules

### 1. Student Enrollment (CRUD)
- Register students with ID, Name, Phone Number, Course Track, and Fee Paid.
- Update student details or delete enrollment records.
- View real-time fee payment status (Paid / Dues).

### 2. Session Scheduling & Attendance Tracking
- Schedule and log Theory and Practical training sessions.
- Record attendance status (`COMPLETED`, `SCHEDULED`, `ABSENT`).
- Maintains historical session logs using a `LinkedList`.

### 3. Automated Eligibility Determination
- Real-time pre-check evaluation before scheduling tests.
- Requires full fee payment, completion of required theory sessions, and completion of required practical sessions.

### 4. Test Scheduling & Outcome Management
- Schedule candidates for their final driving test with strict date validation (`YYYY-MM-DD`).
- Record test outcomes (`SCHEDULED`, `PASSED`, `FAILED`).
- Option to cancel or delete scheduled tests.

### 5. Search, Sorting & Reports
- **Search**: Multi-attribute filter by Student ID, Name, or Course Track.
- **TreeMap Sorting**: Ranks students in descending order by completed session count.
- **Date Sorting**: Displays driving tests chronologically.
- **Audit Report**: Generates an overall driving school summary and detailed eligibility status breakdown.

---

## Course Track Rules

| Course Track | Course Fee | Required Theory Sessions | Required Practical Sessions |
| :--- | :---: | :---: | :---: |
| **Two-Wheeler** | ₹3,500 | 5 Sessions | 10 Sessions |
| **Four-Wheeler** | ₹7,500 | 8 Sessions | 15 Sessions |

---

## Java Concepts & Data Structures Used

| Java Concept | Project Implementation |
| :--- | :--- |
| **Classes & Objects** | `Student`, `Course`, `TrainingSession`, `Test` |
| **Encapsulation** | Private member variables with getters/setters and business logic validation. |
| **Abstract Class** | `Course` base class with abstract method `getType()`. |
| **Inheritance & Polymorphism** | `TwoWheelerCourse` and `FourWheelerCourse` extend `Course`. |
| **ArrayList** | Stores student enrollment records. |
| **LinkedList** | Maintains training session history chronologically. |
| **HashMap** | Key-value mapping of `Student ID` $\rightarrow$ `Course`. |
| **TreeMap** | Ranks students by number of completed sessions. |
| **Exception Handling** | Custom checked exceptions `InvalidDataException` and `IneligibleException`. |
| **Data Validation** | Strict date parsing, numeric fee checks, duplicate ID prevention. |
| **Java Swing GUI** | Dark-themed desktop UI with `CardLayout`, custom table renderers, and interactive forms. |

---

## Class Diagram

```mermaid
classDiagram
    class Course {
        <<abstract>>
        -String name
        -double fee
        -int requiredTheory
        -int requiredPractical
        +getName() String
        +getFee() double
        +getRequiredTheory() int
        +getRequiredPractical() int
        +getType()* String
    }

    class TwoWheelerCourse {
        +TwoWheelerCourse()
        +getType() String
    }

    class FourWheelerCourse {
        +FourWheelerCourse()
        +getType() String
    }

    class Student {
        -String id
        -String name
        -String phone
        -double feePaid
        +getId() String
        +getName() String
        +getPhone() String
        +getFeePaid() double
        +setName(String)
        +setPhone(String)
        +setFeePaid(double)
    }

    class TrainingSession {
        -String sessionId
        -String studentId
        -String type
        -String date
        -String status
        +getSessionId() String
        +getStudentId() String
        +getType() String
        +getDate() String
        +getStatus() String
    }

    class Test {
        -String testId
        -String studentId
        -String date
        -String track
        -String result
        +getTestId() String
        +getStudentId() String
        +getDate() String
        +getTrack() String
        +getResult() String
        +setResult(String)
    }

    Course <|-- TwoWheelerCourse
    Course <|-- FourWheelerCourse
```

---

## How to Run

### Prerequisites
- Java Development Kit (JDK 11 or higher)

### Steps

1. **Clone the repository**:
   ```bash
   git clone https://github.com/kartikwagh21/Java_Major_Project.git
   cd Java_Major_Project
   ```

2. **Compile the source file**:
   ```bash
   javac DrivingSchoolApp.java
   ```

3. **Run the application**:
   ```bash
   java DrivingSchoolApp
   ```

*(Or run directly on Java 11+ without compiling: `java DrivingSchoolApp.java`)*

---

## Author

- **Kartik Wagh** — *B.Tech Computer Science & Engineering*
