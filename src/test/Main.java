package test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.Period;
import java.util.Base64;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        StudentDataReader studentDataReader = new StudentDataReader();
        Thread thread1 = new Thread(studentDataReader);
        thread1.start();
        try {
            thread1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Student student = studentDataReader.getStudent();
        if (student != null) {
            Thread thread2 = new Thread(new AgeCalculator(student));
            Thread thread3 = new Thread(new PrimeChecker(student));
            thread2.start();
            thread3.start();
            try {
                thread2.join();
                thread3.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            ResultWriter resultWriter = new ResultWriter();
            resultWriter.writeResultToFile(student);

            // Đọc kết quả từ file kq.xml
            readResultFromFile();
        } else {
            System.out.println("Không có dữ liệu sinh viên.");
        }
    }

    public static void readResultFromFile() {
        try {
            String content = new String(Files.readAllBytes(Paths.get("kq.xml")));
            System.out.println("Kết quả:");
            System.out.println(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class StudentDataReader implements Runnable {
    private Student student;

    @Override
    public void run() {
        try {
            File file = new File("student.xml");
            Scanner scanner = new Scanner(file);

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.contains("<Student>")) {
                    student = new Student();
                } else if (line.contains("<Id>")) {
                    student.setId(line.substring(line.indexOf(">") + 1, line.lastIndexOf("<")));
                } else if (line.contains("<Name>")) {
                    student.setName(line.substring(line.indexOf(">") + 1, line.lastIndexOf("<")));
                } else if (line.contains("<Address>")) {
                    student.setAddress(line.substring(line.indexOf(">") + 1, line.lastIndexOf("<")));
                } else if (line.contains("<DateOfBirth>")) {
                    student.setDateOfBirth(line.substring(line.indexOf(">") + 1, line.lastIndexOf("<")));
                }
            }
            scanner.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Student getStudent() {
        return student;
    }
}

class AgeCalculator implements Runnable {
    private Student student;

    public AgeCalculator(Student student) {
        this.student = student;
    }

    @Override
    public void run() {
        if (student != null) {
            LocalDate birthDate = LocalDate.parse(student.getDateOfBirth());
            LocalDate currentDate = LocalDate.now();
            Period period = Period.between(birthDate, currentDate);
            student.setAge(period.getYears());

            // Mã hoá ngày sinh
            byte[] encodedBytes = Base64.getEncoder().encode(student.getDateOfBirth().getBytes());
            student.setEncodedDateOfBirth(new String(encodedBytes));
        }
    }
}

class PrimeChecker implements Runnable {
    private Student student;

    public PrimeChecker(Student student) {
        this.student = student;
    }

    @Override
    public void run() {
        if (student != null) {
            String dob = student.getDateOfBirth();
            int sum = 0;
            for (int i = 0; i < dob.length(); i++) {
                if (Character.isDigit(dob.charAt(i))) {
                    sum += Character.getNumericValue(dob.charAt(i));
                }
            }
            student.setSumIsPrime(isPrime(sum));
        }
    }

    private boolean isPrime(int num) {
        if (num <= 1) {
            return false;
        }
        for (int i = 2; i <= Math.sqrt(num); i++) {
            if (num % i == 0) {
                return false;
            }
        }
        return true;
    }
}

class ResultWriter {
    public void writeResultToFile(Student student) {
        try {
            FileWriter writer = new FileWriter("kq.xml");
            writer.write("<Student>\n");
            writer.write("\t<Age>" + student.getAge() + "</Age>\n");
            writer.write("\t<SumIsPrime>" + student.isSumPrime() + "</SumIsPrime>\n");
            writer.write("\t<EncodedDateOfBirth>" + student.getEncodedDateOfBirth() + "</EncodedDateOfBirth>\n");
            writer.write("</Student>");
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class Student {
    private String id;
    private String name;
    private String address;
    private String dateOfBirth;
    private int age;
    private boolean sumIsPrime;
    private String encodedDateOfBirth;

    // Constructors, getters, setters, and other methods

    public String getEncodedDateOfBirth() {
        return encodedDateOfBirth;
    }

    public void setEncodedDateOfBirth(String encodedDateOfBirth) {
        this.encodedDateOfBirth = encodedDateOfBirth;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public boolean isSumPrime() {
        return sumIsPrime;
    }

    public void setSumIsPrime(boolean sumIsPrime) {
        this.sumIsPrime = sumIsPrime;
    }
}
