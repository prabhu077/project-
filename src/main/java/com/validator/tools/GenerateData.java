package com.validator.tools;

import com.github.javafaker.Faker;

import java.io.FileWriter;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Locale;
import java.util.Random;

/**
 * GenerateData
 * ------------
 * Generates synthetic (fake) retail data for all four sources: Store, App,
 * Loyalty, Supplier (section 6). All data is synthetic - no real customer,
 * employee, supplier, or financial information is used (section 18).
 *
 * Run with:
 *   mvn compile exec:java -Dexec.mainClass="com.validator.tools.GenerateData"
 */
public class GenerateData {

    private static final Faker faker = new Faker(new Locale("en-IND"));
    private static final Random random = new Random();
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final int ROW_COUNT = 100;

    public static void main(String[] args) throws IOException {
        new java.io.File("data/raw").mkdirs();
        generateStoreData();
        generateAppData();
        generateLoyaltyData();
        generateSupplierData();
        System.out.println("Synthetic data generated in data/raw/");
    }

    private static void generateStoreData() throws IOException {
        try (FileWriter writer = new FileWriter("data/raw/store_data.csv")) {
            writer.write("transaction_id,customer_name,card_number,product_category,purchase_amount,purchase_date\n");
            for (int i = 1; i <= ROW_COUNT; i++) {
                writer.write(String.format("TXN%04d,%s,%s,%s,%d,%s%n",
                        i, escape(faker.name().fullName()), randomCardNumber(),
                        escape(faker.commerce().department()),
                        faker.number().numberBetween(100, 5000),
                        faker.date().birthday().toString().substring(0, 10)));
            }
        }
    }

    private static void generateAppData() throws IOException {
        try (FileWriter writer = new FileWriter("data/raw/app_data.csv")) {
            writer.write("user_id,full_name,contact_email,mobile_number,password,last_login\n");
            for (int i = 1; i <= ROW_COUNT; i++) {
                writer.write(String.format("USR%04d,%s,%s,%s,%s,%s%n",
                        i, escape(faker.name().fullName()), faker.internet().emailAddress(),
                        randomPhoneNumber(), randomPassword(),
                        faker.date().past(90, java.util.concurrent.TimeUnit.DAYS).toString().substring(0, 10)));
            }
        }
    }

    private static void generateLoyaltyData() throws IOException {
        try (FileWriter writer = new FileWriter("data/raw/loyalty_data.csv")) {
            writer.write("loyalty_id,member_name,email_address,phone,aadhaar_number,points_balance\n");
            for (int i = 1; i <= ROW_COUNT; i++) {
                writer.write(String.format("LOY%04d,%s,%s,%s,%s,%d%n",
                        i, escape(faker.name().fullName()), faker.internet().emailAddress(),
                        randomPhoneNumber(), randomAadhaarNumber(),
                        faker.number().numberBetween(0, 10000)));
            }
        }
    }

    private static void generateSupplierData() throws IOException {
        try (FileWriter writer = new FileWriter("data/raw/supplier_data.csv")) {
            writer.write("supplier_id,supplier_name,contact_email,contact_number,gstin,pan_number,supply_category\n");
            for (int i = 1; i <= ROW_COUNT; i++) {
                writer.write(String.format("SUP%04d,%s,%s,%s,%s,%s,%s%n",
                        i, escape(faker.company().name()), faker.internet().emailAddress(),
                        randomPhoneNumber(), randomGstNumber(), randomPanNumber(),
                        escape(faker.commerce().department())));
            }
        }
    }

    private static String randomPhoneNumber() {
        int firstDigit = 6 + random.nextInt(4);
        StringBuilder sb = new StringBuilder().append(firstDigit);
        for (int i = 0; i < 9; i++) sb.append(random.nextInt(10));
        return sb.toString();
    }

    private static String randomCardNumber() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16; i++) sb.append(random.nextInt(10));
        return sb.toString();
    }

    private static String randomPanNumber() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) sb.append((char) ('A' + random.nextInt(26)));
        for (int i = 0; i < 4; i++) sb.append(random.nextInt(10));
        sb.append((char) ('A' + random.nextInt(26)));
        return sb.toString();
    }

    private static String randomGstNumber() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%02d", random.nextInt(37)));
        sb.append(randomPanNumber());
        sb.append(random.nextInt(10));
        sb.append('Z');
        sb.append(random.nextInt(2) == 0 ? (char) ('A' + random.nextInt(26)) : (char) ('0' + random.nextInt(10)));
        return sb.toString();
    }

    private static String randomAadhaarNumber() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) sb.append(random.nextInt(10));
        return sb.toString();
    }

    private static String randomPassword() {
        String[] words = {"Sunrise", "Falcon", "Cricket", "Monsoon", "Rocket", "Tiger", "Chennai", "Coffee"};
        String word = words[random.nextInt(words.length)];
        int number = 10 + secureRandom.nextInt(9000);
        return word + number + "!";
    }

    private static String escape(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
