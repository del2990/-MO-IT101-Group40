package finalcodeoutput;

/*
MotorPH Payroll System
MO-IT101

Group Members:
Ronachel Digma – GitHub: ronachel
Markus Joaquin Crisostomo – GitHub: markus103006
Adelfa Catugo – GitHub: del2990
*/

import java.util.Scanner;
import java.io.*;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class FinalCodeOutput {
 //These are the constant variables used to calculate the total hours worked.
 static final String[] months = {"June", "July", "August", "September", "October", "November", "December"};
 static final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("H:mm");
 /**
 * Prompts the user to log in by entering a username and password.
 * The user is allowed up to 5 attempts to provide valid credentials.
 * Login is considered successful if the entered username matches
 * either of the predefined valid usernames.
 *
 * @param input Scanner used to read user input
 * @param validUsername1 the first accepted username (employee)
 * @param validUsername2 the second accepted username (payroll staff)
 * @return the authenticated username if login succeeds; otherwise null
 */
    public static String loginPage(Scanner input, String validUsername1, String validUsername2) {

        String correctPassword = "12345";

        // Loop for maximum of 5 login attempts
        for (int attempt = 1; attempt <= 5; attempt++) {

            System.out.print("Enter Username: ");
            String username = input.nextLine();

            System.out.print("Enter Password: ");
            String password = input.nextLine();

        // Validates user credentials
        if ((username.equals(validUsername1) || username.equals(validUsername2)) 
            && password.equals(correctPassword)) {
            System.out.println("Login Successful!\n");
            // Returns the username if login is successful.
            return username;
             } else {
            System.out.println("Incorrect username and/or password. Attempt " + attempt + " of 5.\n");
             }
        }
        // Displays a message after 5 failed login attempts.
        System.out.println("Maximum attempts reached. Program terminated.");
        return null;
    }
 /**
 * Reads the employee CSV file and searches for a record that matches
 * the specified employee number.
 *
 * Iterates through each row in the file and, once a match is found,
 * extracts and returns the relevant employee details.
 *
 * @param employeeDatabase the path to the employee CSV file
 * @param inputEmployeeNumber the employee number to look up
 * @return an array containing the employee details in the format:
 * [employeeNumber, lastName, firstName, birthday, hourlyRate];
 * returns null if no matching employee is found
 */
    public static String[] readEmployeeData(String employeeDatabase, String inputEmployeeNumber) {
        try (BufferedReader br = new BufferedReader(new FileReader(employeeDatabase))) {
            br.readLine(); // Skip the header row to avoid processing column titles
            String line;
            // Iterate through each record in the employee database file
            while ((line = br.readLine()) != null) {
                // Skip empty lines to prevent processing invalid data
                if (line.trim().isEmpty()) continue;
                String[] employeeData = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                if (employeeData[0].trim().equals(inputEmployeeNumber)){
                    return new String[] {
                    employeeData[0].trim(),// Array containing the employee number of the matched record.
                    employeeData[1].trim(),// Array containing the employee's last name
                    employeeData[2].trim(),// Array containing the employee's first name
                    employeeData[3].trim(),// Array containing the employee's birthday
                    employeeData[employeeData.length - 1].trim()//Array containing the employee’s hourly rate (last column).
                    };
                } 
                
            } 
        }catch (Exception e){
            System.out.println("Error reading employee file.");
            }
        return null;
    }
    /**
    * Reads the attendance CSV file and stores all records in a Map.
    * Each employee number is mapped to a list of their attendance records.
    *
    * @param attendanceRecords the file path of the attendance CSV file
    * @return a Map where the key is employee number and the value is a list of records
    */
    public static Map<String, List<String[]>> loadAttendanceRecords(String attendanceRecords) {
        Map<String, List<String[]>> attendanceMap = new HashMap<>();

        // Note: Attendance file is read multiple times for simplicity.
        // This can be optimized by loading data into memory using a Map.
        try (BufferedReader br = new BufferedReader(new FileReader(attendanceRecords))) {
            br.readLine(); // Skip the header row to avoid processing column titles
            String line;

            // Iterate through each attendance record in the file
            while ((line = br.readLine()) != null) {
            // Skip empty lines to prevent invalid data processing
            if (line.trim().isEmpty()) continue;
            String[] attendanceData = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
            String employeeNumber = attendanceData[0].trim();
            // If employee is not yet in the map, initialize a new list
            if (!attendanceMap.containsKey(employeeNumber)) {
                attendanceMap.put(employeeNumber, new ArrayList<>());
            }
            // Adds the attendance record to the corresponding employee
            attendanceMap.get(employeeNumber).add(attendanceData);
            }

        } catch (Exception e) {
            System.out.println("Error reading attendance file.");
        }
        return attendanceMap;
    }
 
    /**
    * Calculates the total hours worked by an employee based on their
    * login and logout times from the attendance record.
    *
    * The calculation follows MotorPH attendance policies:
    * - Official start time: 8:00 AM
    * - Grace period: until 8:10 AM
    * - Official end time: 5:00 PM
    * - Maximum working hours per day: 8 hours
    *
    * Adjustments:
    * - Login times before 8:00 AM are treated as 8:00 AM.
    * - Logout times after 5:00 PM are treated as 5:00 PM.
    *
    * @param login  the employee's recorded login time
    * @param logout the employee's recorded logout time
    * @return the total hours worked by the employee for the day
    */

    public static double computeTotalHoursWorked(LocalTime login, LocalTime logout) {

        LocalTime officialStart = LocalTime.of(8, 0); // Official work start time (8:00 AM)
        LocalTime graceTime = LocalTime.of(8, 10);    // Grace period limit (8:10 AM)
        LocalTime officialEnd = LocalTime.of(17, 0);  // Official end time (5:00 PM)

        // Adjusts the logout time if it exceeds the official end of working hours.
        if (logout.isAfter(officialEnd)) {
            logout = officialEnd;
        }

        // Adjusts the login time if it is earlier than the official start time.
        if (login.isBefore(officialStart)) {
            login = officialStart;
        }

        // Apply grace period rule: if within grace time, treat as on-time (8:00 AM)
        if (!login.isAfter(graceTime)) {
            login = officialStart;
        }

        // Ensures total hours worked cannot be negative.
        if (login.isAfter(logout)) {
            return 0;
        }

        // Compute total minutes worked and convert to hours
        long minutesWorked = Duration.between(login, logout).toMinutes();
        double hoursWorked = minutesWorked / 60.0;

        // Subtract 1 hour for lunch break as per company policy
        if (hoursWorked > 1) {
            hoursWorked -= 1;
        }

        // Cap working hours to a maximum of 8 hours per day
        return Math.min(hoursWorked, 8.0);
    } 
    /**
    * Computes the Social Security System (SSS) deduction for an employee
    * based on the official SSS salary contribution table.
    *
    * Deduction is calculated based on the employee’s total gross salary for the payroll period.
    *
    * @param grossSalary the employee's total gross salary
    * @return the SSS deduction amount
    */
    public static double computeSSS(double grossSalary) {
        if (grossSalary < 3250) return 135.00;
        else if (grossSalary <= 3750) return 157.50;
        else if (grossSalary <= 4250) return 180.00;
        else if (grossSalary <= 4750) return 202.50;
        else if (grossSalary <= 5250) return 225.00;
        else if (grossSalary <= 5750) return 247.50;
        else if (grossSalary <= 6250) return 270.00;
        else if (grossSalary <= 6750) return 292.50;
        else if (grossSalary <= 7250) return 315.00;
        else if (grossSalary <= 7750) return 337.50;
        else if (grossSalary <= 8250) return 360.00;
        else if (grossSalary <= 8750) return 382.50;
        else if (grossSalary <= 9250) return 405.00;
        else if (grossSalary <= 9750) return 427.50;
        else if (grossSalary <= 10250) return 450.00;
        else if (grossSalary <= 10750) return 472.50;
        else if (grossSalary <= 11250) return 495.00;
        else if (grossSalary <= 11750) return 517.50;
        else if (grossSalary <= 12250) return 540.00;
        else if (grossSalary <= 12750) return 562.50;
        else if (grossSalary <= 13250) return 585.00;
        else if (grossSalary <= 13750) return 607.50;
        else if (grossSalary <= 14250) return 630.00;
        else if (grossSalary <= 14750) return 652.50;
        else if (grossSalary <= 15250) return 675.00;
        else if (grossSalary <= 15750) return 697.50;
        else if (grossSalary <= 16250) return 720.00;
        else if (grossSalary <= 16750) return 742.50;
        else if (grossSalary <= 17250) return 765.00;
        else if (grossSalary <= 17750) return 787.50;
        else if (grossSalary <= 18250) return 810.00;
        else if (grossSalary <= 18750) return 832.50;
        else if (grossSalary <= 19250) return 855.00;
        else if (grossSalary <= 19750) return 877.50;
        else if (grossSalary <= 20250) return 900.00;
        else if (grossSalary <= 20750) return 922.50;
        else if (grossSalary <= 21250) return 945.00;
        else if (grossSalary <= 21750) return 967.50;
        else if (grossSalary <= 22250) return 990.00;
        else if (grossSalary <= 22750) return 1012.50;
        else if (grossSalary <= 23250) return 1035.00;
        else if (grossSalary <= 23750) return 1057.50;
        else if (grossSalary <= 24250) return 1080.00;
        else if (grossSalary <= 24750) return 1102.50;
        else return 1125.00;
    }
    /**
    * Computes the PhilHealth contribution deduction for an employee.
    *
    * PhilHealth contribution is calculated as 3% of the employee's salary,
    * shared equally between the employer and employee. Only the employee share
    * is deducted in this program.
    *
    * Salary limits are applied based on PhilHealth contribution rules.
    *
    * @param grossSalary the employee's total gross salary
    * @return the PhilHealth deduction amount
    */
    public static double computePhilHealth(double grossSalary) {
        // Apply PhilHealth contribution limits and compute employee share (50%)
        if (grossSalary <= 10000) {
            return 10000 * 0.03 / 2;
        } else if (grossSalary <= 59999.99) {
            return grossSalary * 0.03 / 2;
        } else {
            return 60000 * 0.03 / 2;
        }
    }
    /**
    * Computes the Pag-IBIG contribution deduction for an employee.
    *
    * Contribution rules:
    * - 1% of salary if salary is between 1,000 and 1,500
    * - 2% of salary if salary is above 1,500
    *
    * @param grossSalary the employee's total gross salary
    * @return the Pag-IBIG deduction amount
    */
    public static double computePagIbig(double grossSalary) {
        // Apply Pag-IBIG contribution rules based on salary range
        if (grossSalary >= 1000 && grossSalary <= 1500) {
            return grossSalary * 0.01;
        } else if (grossSalary > 1500) {
            return grossSalary * 0.02;
        } else {
            return 0;
        }
    }
    /**
    * Computes the employee's withholding income tax based on the
    * Philippine TRAIN tax table.
    *
    * The tax amount is calculated using progressive tax brackets
    * depending on the employee's monthly gross income.
    *
    * @param monthlyGross the employee's total gross salary
    * @return the withholding tax deduction
    */
    public static double computeIncomeTax(double monthlyGross) {
        // Apply TRAIN law tax brackets progressively
        if (monthlyGross <= 20832) return 0;
        else if (monthlyGross <= 33332) return (monthlyGross - 20833) * 0.20;
        else if (monthlyGross <= 66666) return 2500 + (monthlyGross - 33333) * 0.25;
        else if (monthlyGross <= 166666) return 10833 + (monthlyGross - 66667) * 0.30;
        else if (monthlyGross <= 666666) return 40833.33 + (monthlyGross - 166667) * 0.32;
        else return 200833.33 + (monthlyGross - 666667) * 0.35;
    }
    /**
    * Calculates and displays the payroll for a specific employee.
    *
    * The method computes total hours worked per month (June to December),
    * separates them into first (1-15) and second (16-end) cutoffs,
    * calculates gross salary, applies deductions on the second cut-off (SSS, PhilHealth, Pag-IBIG, Income Tax),
    * and prints net salary for each cutoff.
    *
    * @param employeeNumber employee's unique ID
    * @param employeeFirstName the employee's first name
    * @param employeeLastName employee's last name
    * @param employeeBirthday employee's birthday
    * @param employeeHourlyRate hourly rate of the employee
    * @param attendanceMap map containing all attendance records by employee number
    */
    public static void computePayroll(String employeeNumber, String employeeFirstName, String employeeLastName,
    String employeeBirthday, double employeeHourlyRate,Map<String, List<String[]>> attendanceMap){
        
        System.out.println("\n==============================");
        System.out.println("Employee Number: " + employeeNumber);
        System.out.println("Employee Name: " + employeeFirstName + " " + employeeLastName);
        System.out.println("Birthday: " + employeeBirthday);
        // Loop through each month from June (6) to December (12)
        for (int month = 6; month <= 12; month++){
            double firstHalf = 0;
            double secondHalf = 0;
        // Get all attendance records for this employee
        List<String[]> records = attendanceMap.get(employeeNumber);
        if (records != null) {
            for (String[] data : records) {
            if (data.length < 6) continue;
                // Parsing the date of the attendance record
                String[] dateParts = data[3].split("/");
                int recordMonth = Integer.parseInt(dateParts[0]);
                int day = Integer.parseInt(dateParts[1]);
                int year = Integer.parseInt(dateParts[2]);
             
            // Only consider records for the current month and year 2024
            if (year != 2024 || recordMonth != month) continue;
                // Parse login and logout times from CSV
                LocalTime login = LocalTime.parse(data[4].trim(), timeFormat);
                LocalTime logout = LocalTime.parse(data[5].trim(), timeFormat);
                
                double hours = computeTotalHoursWorked(login, logout);
            
            // Separate working hours into first cutoff (1–15) and second cutoff (16–end of month)
            if (day <= 15) 
            firstHalf += hours;
            else 
            secondHalf += hours;
            }
        }
            double firstGrossSalary = firstHalf * employeeHourlyRate;
            double secondGrossSalary = secondHalf * employeeHourlyRate;
            double totalGrossSalary = firstGrossSalary + secondGrossSalary;

            double sssDeductions = computeSSS(totalGrossSalary);
            double philHealthDeductions = computePhilHealth(totalGrossSalary);
            double pagibigDeductions = computePagIbig(totalGrossSalary);
            double incomeTax = computeIncomeTax(totalGrossSalary);
            double totalDeductions = sssDeductions + philHealthDeductions + pagibigDeductions + incomeTax;

            double firstNetSalary = firstGrossSalary; 
            double secondNetSalary = totalGrossSalary - totalDeductions;

            int index = month - 6;

            System.out.println("\nMonth: " + months[index]);

            System.out.println("\nFirst Cutoff: " + months[index] + " 1 to 15");
            System.out.println("Total Hours Worked: " + firstHalf);
            System.out.println("Gross Salary: " + firstGrossSalary);
            System.out.println("Net Salary: " + firstNetSalary);

            System.out.println("\nSecond Cutoff: " + months[index] + " 16 to 30");
            System.out.println("Total Hours Worked: " + secondHalf);
            System.out.println("Gross Salary: " + secondGrossSalary);
            System.out.println("SSS Deduction: " + sssDeductions);
            System.out.println("PhilHealth Deduction: " + philHealthDeductions);
            System.out.println("Pag-IBIG Deduction: " + pagibigDeductions);
            System.out.println("Withholding Tax Deduction: " + incomeTax);
            System.out.println("Total Deductions: " + totalDeductions);
            System.out.println("Net Salary: " + secondNetSalary);     
        }
    }
 
    /**
    * Entry point of the MotorPH Payroll System.
    *
    * Handles user login, loads employee and attendance data,
    * displays main menu options based on user type (employee or payroll staff),
    * and calls appropriate methods to view or process payroll.
    *
    * @param args command-line arguments (not used)
    */
    public static void main(String[] args)  {
        // File paths for employee details and attendance CSV files
        String employeeDatabase = "src/MotorPH_Employee Data - Employee Details.csv";
        String attendanceRecords = "src/MotorPH_Employee Data - Attendance Record.csv";
        // Predefined valid usernames for login system
        String validUsername1 = "employee";
        String validUsername2 = "payroll_staff";
        Scanner input = new Scanner(System.in);
        // Call loginPage method to authenticate user
        String selectedUsername = loginPage(input, validUsername1, validUsername2);
        // This block of code checks if the login failed (null is returned if max attempts exceeded)
        if (selectedUsername == null) {
        input.close();
        return;
        } 
        // Load all attendance records into a Map for quick access with employee number as the key.
        Map<String, List<String[]>> attendanceMap = loadAttendanceRecords(attendanceRecords);
        boolean running = true;
        // Main menu loop, continues until the user chooses to exit
        while(running) {
            // Display the main menu based on logged-in user type
            System.out.println("\n=== MAIN MENU ===");
            System.out.println("Logged in as: " + selectedUsername);
                
            if (selectedUsername.equals("employee")) {
            System.out.println("1 - View my Payroll");
            System.out.println("2 - Exit");
            } else if (selectedUsername.equals("payroll_staff")) {
            System.out.println("1 - Process Payroll");
            System.out.println("2 - Exit");
            }
            System.out.print("Select option: ");
            String chosenOption = input.nextLine();
        switch (chosenOption) {    
            case "1" -> {
                
                if (selectedUsername.equals("employee")) {
                // Employee option: view their own payroll info    
                System.out.print("Enter your Employee Number: ");
                String inputNumber = input.nextLine();
                String[] employeeData = readEmployeeData(employeeDatabase, inputNumber);
                if (employeeData == null) {
                System.out.println("Employee number does not exist.");
                break;
                } 
                String displayNumber = employeeData[0];
                String displayLastName = employeeData[1];
                String displayFirstName = employeeData[2];
                String displayBirthday = employeeData[3];
                //Print basic employee information
                System.out.println("\nEmployee Number: " + displayNumber);
                System.out.println("Employee Name: " + displayFirstName + " " + displayLastName);
                System.out.println("Birthday: " + displayBirthday);
            
            } else if (selectedUsername.equals("payroll_staff")) {
                // Payroll staff option: choose to process one or all employees
                System.out.println("\nSelect Process:");
                System.out.println("1 - Display One Employee");
                System.out.println("2 - Display All Employees");
                System.out.print("Selected Process: ");
                int selectedProcess = Integer.parseInt(input.nextLine());
            switch (selectedProcess) {
            case 1 -> {
                System.out.print("\nEnter Employee Number: ");
                String inputEmployeeNumber = input.nextLine();
                String[] employeeData = readEmployeeData(employeeDatabase, inputEmployeeNumber);
                if (employeeData == null) {
                    System.out.println("Employee number does not exist.");
                   break;
                }
                String employeeNumber = employeeData[0];
                String employeeLastName = employeeData[1];
                String employeeFirstName = employeeData[2];
                String employeeBirthday = employeeData[3];
                double employeeHourlyRate = Double.parseDouble(employeeData[4]);
                // Compute and display payroll for this employee using the method
                computePayroll(employeeNumber, employeeFirstName, employeeLastName, employeeBirthday, employeeHourlyRate, attendanceMap);     
            }
            case 2 -> {
            // Display payroll for all employees
            try (BufferedReader br = new BufferedReader(new FileReader(employeeDatabase))) {
                br.readLine();
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    String[] employeeData = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                    String employeeNumber = employeeData[0].trim();
                    String employeeLastName = employeeData[1].trim();
                    String employeeFirstName = employeeData[2].trim();
                    String employeeBirthday = employeeData[3].trim();
                    double employeeHourlyRate = Double.parseDouble(employeeData[employeeData.length - 1].trim());
                    //Compute payroll for each employee
                    computePayroll(employeeNumber, employeeFirstName, employeeLastName, employeeBirthday, employeeHourlyRate, attendanceMap);
                }
                System.out.println("Payroll calculation successful. All employees displayed.");
                } catch (Exception e) {
                System.out.println("Error reading employee file.");
                }
            }
            default -> System.out.println("Invalid option selected. Please restart the program and choose either 1 or 2.");
            } 
            } 
            }
        case "2" -> running = false; // If the user chooses to exit the program (2)
        //If the user selects an option that is not 1 or 2
        default -> System.out.println("Invalid option selected. Please restart the program and choose either 1 or 2.");
        }
        }
    }
}
