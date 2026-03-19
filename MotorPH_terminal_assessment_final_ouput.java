package cp1milestone2;

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

public class CP1Milestone2 {
    //These are the constant variable used to calculate total hours work.
    static final String[] months = {"June", "July", "August", "September", "October", "November", "December"};
    static final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("H:mm");

    //This method is the main payroll calculation.
    public static void computePayroll(String employeeNumber, String employeeFirstName, String employeeLastName,
                                      String employeeBirthday, double employeeHourlyRate,
                                      Map<String, List<String[]>> attendanceMap) {
        System.out.println("");
        System.out.println("Employee Number: " + employeeNumber);
        System.out.println("Employee Name: " + employeeFirstName + " " + employeeLastName);
        System.out.println("Birthday: " + employeeBirthday);

        for (int month = 6; month <= 12; month++) {
            double firstHalf = 0;
            double secondHalf = 0;

            List<String[]> records = attendanceMap.get(employeeNumber);
            if (records != null) {
                for (String[] data : records) {
                    if (data.length < 6) continue;

                    String[] dateParts = data[3].split("/");
                    int recordMonth = Integer.parseInt(dateParts[0]);
                    int day = Integer.parseInt(dateParts[1]);
                    int year = Integer.parseInt(dateParts[2]);

                    if (year != 2024 || recordMonth != month) continue;

                    LocalTime login = LocalTime.parse(data[4].trim(), timeFormat);
                    LocalTime logout = LocalTime.parse(data[5].trim(), timeFormat);

                    double hours = computeTotalHoursWorked(login, logout);

                    if (day <= 15) firstHalf += hours;
                    else secondHalf += hours;
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
    //This method reads the file of employee database.
    public static String[] readEmployeeData (String employeeDatabase, String inputEmployeeNumber) {
        try (BufferedReader br = new BufferedReader(new FileReader(employeeDatabase))) {
        br.readLine(); 
        String line;

        while ((line = br.readLine()) != null) {
            if (line.trim().isEmpty()) continue;

            String[] data = line.split(",");

            if (data[0].trim().equals(inputEmployeeNumber)) {
                return new String[] {
                    data[0].trim(), //employeeNumber
                    data[1].trim(), // lastName
                    data[2].trim(), // firstName
                    data[3].trim(), // birthday
                    data[data.length - 1].trim() // hourlyRate
                };
            }
        }
    } catch (Exception e) {
        System.out.println("Error reading employee file.");
    }
    return null; 
    }
    //This method reads the file of employee attendance records.
    public static Map<String, List<String[]>> loadAttendanceRecords(String attendanceRecords) {
        Map<String, List<String[]>> attendanceMap = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(attendanceRecords))) {
            br.readLine(); 
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] data = line.split(",");
                String employeeNumber = data[0].trim();
                attendanceMap.putIfAbsent(employeeNumber, new ArrayList<>());
                attendanceMap.get(employeeNumber).add(data);
            }
        } catch (Exception e) {
            System.out.println("Error reading attendance file.");
        }
        return attendanceMap;
    }
    /**
    * Computes the total number of hours worked by an employee based on login
    * and logout times from the attendance record.
    *
    * The method applies MotorPH attendance policies:
    * - Official start time: 8:00 AM
    * - Grace period: until 8:10 AM
    * - Official end time: 5:00 PM
    * - Maximum working hours per day: 8 hours
    *
    * If an employee logs in before the official start time,
    * the login time is adjusted to 8:00 AM.
    *
    * If the employee logs out after the official end time,
    * the logout time is adjusted to 5:00 PM.
    *
    * @param login  the recorded login time of the employee
    * @param logout the recorded logout time of the employee
    * @return the total number of hours worked for the day
    */
    public static double computeTotalHoursWorked(LocalTime login, LocalTime logout) {
        LocalTime officialStart = LocalTime.of(8, 0);
        LocalTime graceTime = LocalTime.of(8, 10);
        LocalTime officialEnd = LocalTime.of(17, 0);

        if (logout.isAfter(officialEnd)) logout = officialEnd;
        if (login.isBefore(officialStart)) login = officialStart;
        if (!login.isAfter(graceTime)) login = officialStart;
        if (login.isAfter(logout)) return 0;

        long minutesWorked = Duration.between(login, logout).toMinutes();
        double hoursWorked = minutesWorked / 60.0;
        if (hoursWorked > 1) hoursWorked -= 1;
        return Math.min(hoursWorked, 8.0);
    }
     /**
    * Computes the Social Security System (SSS) deduction for an employee
    * based on the official SSS salary contribution table.
    *
    * The deduction is determined by the employee's total gross salary
    * for the payroll period.
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
    * shared equally between employer and employee. Only the employee share
    * is deducted in this program.
    *
    * Salary limits are applied based on PhilHealth contribution rules.
     *
     * @param grossSalary the employee's total gross salary
     * @return the PhilHealth deduction amount
    */
    public static double computePhilHealth(double grossSalary) {
        if (grossSalary <= 10000) return 10000 * 0.03 / 2;
        else if (grossSalary <= 59999.99) return grossSalary * 0.03 / 2;
        else return 60000 * 0.03 / 2;
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
    public static double computePagIbig(double grossSalary){
        if (grossSalary >= 1000 && grossSalary <= 1500) return grossSalary * 0.01;
        else if (grossSalary > 1500) return grossSalary * 0.02;
        else return 0;
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
        if (monthlyGross <= 20832) return 0;
        else if (monthlyGross <= 33332) return (monthlyGross - 20833) * 0.20;
        else if (monthlyGross <= 66666) return 2500 + (monthlyGross - 33333) * 0.25;
        else if (monthlyGross <= 166666) return 10833 + (monthlyGross - 66667) * 0.30;
        else if (monthlyGross <= 666666) return 40833.33 + (monthlyGross - 166667) * 0.32;
        else return 200833.33 + (monthlyGross - 666667) * 0.35;
    }
    /**
    * Main method that runs the MotorPH Payroll System.
    *
    * The program performs the following operations:
    * 1. Authenticates the user through username and password.
    * 2. Determines whether the user is an employee or payroll staff.
    * 3. Displays menu options based on the user role.
    * 4. Reads employee and attendance records from CSV files.
    * 5. Computes payroll including hours worked, gross salary,
    *    government deductions, and net salary.
    * 6. Displays payroll information for a selected employee or
    *    all employees.
    *
    * @param args command-line arguments
    */
    public static void main(String[] args) {
        String employeeDatabase = "src/MotorPH_Employee Data - Employee Details.csv";
        String attendanceRecords = "src/MotorPH_Employee Data - Attendance Record.csv";

        String validUsername1 = "employee";
        String validUsername2 = "payroll_staff";
        try (Scanner input = new Scanner(System.in)) {
            boolean isAuthenticated = false;
            String correctPassword = "12345";
            String selectedUsername = "";
            
            // ===== LOGIN =====
            for (int attempt = 1; attempt <= 5; attempt++) {
                System.out.print("Enter Username: ");
                String username = input.nextLine();
                
                System.out.print("Enter Password: ");
                String password = input.nextLine();
                
                if ((username.equals(validUsername1) || username.equals(validUsername2)) && password.equals(correctPassword)) {
                    isAuthenticated = true;
                    selectedUsername = username;
                    System.out.println("Login Successful!\n");
                    break;
                } else {
                    System.out.println("Incorrect username and/or password. Attempt " + attempt + " of 5.\n");
                }
            }
            
            if (!isAuthenticated) {
                System.out.println("Maximum attempts reached. Program terminated.");
                input.close();
                return;
            }
            
            //This section will help the attendance to load at once.
            Map<String, List<String[]>> attendanceMap = loadAttendanceRecords(attendanceRecords);
            
            boolean running = true;
            while (running) {
                //This block of code dislays the main menu.
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
                            
                            System.out.println("\nEmployee Number: " + displayNumber);
                            System.out.println("Employee Name: " + displayFirstName + " " + displayLastName);
                            System.out.println("Birthday: " + displayBirthday);
                            
                        } else if (selectedUsername.equals("payroll_staff")) {
                            // Payroll staff view
                            System.out.println("\nSelect Process:");
                            System.out.println("1 - Display One Employee");
                            System.out.println("2 - Display All Employees");
                            System.out.print("Selected Process: ");
                            int selectedOption = Integer.parseInt(input.nextLine());
                            
                            switch (selectedOption) {
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
                                    
                                    computePayroll(employeeNumber, employeeFirstName, employeeLastName, employeeBirthday, employeeHourlyRate, attendanceMap);
                                }
                                
                                case 2 -> {
                                    try (BufferedReader br = new BufferedReader(new FileReader(employeeDatabase))) {
                                        br.readLine();
                                        String line;
                                        while ((line = br.readLine()) != null) {
                                            if (line.trim().isEmpty()) continue;
                                            String[] data = line.split(",");
                                            String employeeNumber = data[0].trim();
                                            String employeeLastName = data[1].trim();
                                            String employeeFirstName = data[2].trim();
                                            String employeeBirthday = data[3].trim();
                                            double employeeHourlyRate = Double.parseDouble(data[data.length - 1].trim());
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
                    case "2" -> running = false;
                    
                    default -> System.out.println("Invalid option selected. Please restart the program and choose either 1 or 2.");
                }
            }
        }
    }
}
