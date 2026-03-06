# MotorPH Payroll System

## Project Overview
The **MotorPH Payroll System** is a Java console-based application that processes employee payroll using attendance records and statutory payroll deductions in the Philippines.

The system reads employee information and attendance records from CSV files, computes hours worked per cutoff period, and calculates the corresponding payroll values including gross salary, deductions, and net salary.

This project was developed as part of the **MotorPH Payroll System milestone requirement**.

---

## Program Details

The system uses a **role-based login system** with two types of users:

### Employee Role
Employees can log in using the employee account and view their personal information.

After logging in, the employee can:

- Enter their **Employee Number**
- View their:
  - Employee Number
  - Employee Name
  - Birthday

---

### Payroll Staff Role
Payroll staff can process payroll calculations.

After logging in, payroll staff can choose between two processing options:

#### Option 1 – Display One Employee
The payroll staff enters an employee number and the system:

1. Reads employee details from the employee database CSV file  
2. Reads attendance records from the attendance CSV file  
3. Calculates hours worked for each cutoff period  
4. Computes payroll values including:
   - Gross Salary
   - SSS Contribution
   - PhilHealth Contribution
   - Pag-IBIG Contribution
   - Withholding Tax
5. Displays payroll results from **June to December**

---

#### Option 2 – Display All Employees
The system automatically:

1. Reads all employees from the employee database  
2. Processes payroll calculations for each employee  
3. Displays payroll computation results for every employee in the system.

---

## Payroll Computation Logic

### Working Hours Calculation
The payroll system follows these rules:

- Official work start: **8:00 AM**
- Grace period: **until 8:10 AM**
- Official work end: **5:00 PM**
- Maximum daily work hours: **8 hours**

Total hours worked are calculated using login and logout times from the attendance records.

---

### Salary Computation

Gross Salary = Total Hours Worked × Hourly Rate

Payroll is computed per cutoff period:

- **First cutoff:** Day 1–15  
- **Second cutoff:** Day 16–30  

---

### Government Deductions
The system automatically calculates statutory deductions:

- SSS Contribution  
- PhilHealth Contribution  
- Pag-IBIG Contribution  
- Withholding Tax  

Total deductions are subtracted from gross salary to determine the **net salary**.

---

## Technologies Used

- Java  
- Java File I/O  
- Java Time API  
- CSV File Processing  
- Git & GitHub Version Control  

---

## System Features

- Role-based login authentication  
- Employee information lookup  
- Attendance-based payroll computation  
- Automatic deduction calculation  
- Payroll reporting per employee or for all employees  
- CSV database integration  

---

## Sample Program Output

Example payroll information displayed by the system:

Employee Number: 10002

Employee Name: Antonio Lim

Birthday: 06/19/1988


Month: June


First Cutoff: June 1 to 15

Total Hours Worked: 73.38

Gross Salary: 26208.12

Net Salary: 26208.12


Second Cutoff: June 16 to 30

Total Hours Worked: 73.90

Gross Salary: 26392.64

SSS Deduction: 1125.00

PhilHealth Deduction: 789.01

Pag-IBIG Deduction: 1052.01

Withholding Tax Deduction: 7316.94

Total Deductions: 10282.96

Net Salary: 16109.67


---

## Team Details

| Team Member | Contribution |
|-------------|--------------|
| **Markus Joaquin Crisostomo** | Led requirements analysis, created the use case diagram, implemented employee and attendance database file handling, and developed payroll computation logic including hours worked, gross salary, and net salary calculations. |
| **Ronachel Marie Digma** | Implemented username and password authentication, developed the display options menu, and created the display sub-options for payroll processing (single employee and all employees). |
| **Adelfa Catugo** | Designed the UI layout/wireframe, implemented employee information display, conducted final system testing, and created the GitHub repository for the project. |

---

## Version Control & Collaboration

The project was managed using **GitHub** for version control and team collaboration.

Workflow used by the team:

1. One member created the **GitHub repository (Host)**  
2. Other members were added as **Collaborators**  
3. Each member:
   - Cloned the repository using NetBeans Git tools  
   - Implemented assigned program features  
   - Committed changes locally  
   - Pushed updates to GitHub  

This allowed the team to:

- Track code revisions  
- Avoid overwriting teammates' work  
- Maintain a secure backup of the project  
- Work on the same project version  

---

## Project Plan Link

**[Project Plan](https://docs.google.com/spreadsheets/d/1cBklH5QW7xFamQZRZQX-qEJxnJeZDuZU8miHq1C-LS8/edit?gid=2134013708#gid=2134013708)**

---

## Learning Outcomes

This project allowed the team to practice:

- Java programming fundamentals  
- File handling using CSV datasets  
- Payroll computation logic  
- Attendance-based working hour calculation  
- Role-based program design  
- Version control using GitHub  

---

## How to Run the Program

1. Clone the repository  
2. Open the project in **NetBeans**  
3. Ensure the CSV files are located in the **src directory**  
4. Run the main Java program  
