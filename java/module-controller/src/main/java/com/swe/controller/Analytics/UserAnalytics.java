package com.swe.controller.Analytics;

import com.swe.controller.Meeting.UserProfile;

import java.util.ArrayList;
import java.util.List;

/**
 * UserAnalytics handles fetching and managing all user profiles
 * stored in the cloud. It can display user data and categorize them.
 */
public class UserAnalytics {

    private List<UserProfile> users;

    public UserAnalytics() {
        users = new ArrayList<>();
    }

    /**
     * Fetches all users from cloud database.
     * For now, this uses mock data — replace with actual cloud fetch logic.
     */
    public void fetchUsersFromCloud() {
//        // TODO: Replace this with your actual cloud API
//        System.out.println("Fetching user data from cloud...");
//
//        users.add(new UserProfile(
//                "112201044@iitpkd.ac.in",
//                "Ram Charan",
//                "hashedPass123",
//                "https://cloud.example.com/profile/ram.png",
//                "student"
//        ));
//        users.add(new UserProfile(
//                "rca@iitpkd.ac.in",
//                "rc",
//                "hashedPass456",
//                "https://cloud.example.com/profile/priya.png",
//                "instructor"
//        ));
//        users.add(new UserProfile(
//                "112201045@iitpkd.ac.in",
//                "ram",
//                "hashedPass789",
//                "https://cloud.example.com/profile/arjun.png",
//                "student"
//        ));

        System.out.println("Users fetched successfully from cloud.\n");
    }

    /**
     * Returns all user profiles.
     */
    public List<UserProfile> getAllUsers() {
        return users;
    }

    /**
     * Filters and returns all students.
     */
//    public List<UserProfile> getAllStudents() {
//        return users.stream()
//                .filter(u -> u.getRole().equalsIgnoreCase("student"))
//                .toList();
//    }
//
//    /**
//     * Filters and returns all instructors.
//     */
//    public List<UserProfile> getAllInstructors() {
//        return users.stream()
//                .filter(u -> u.getRole().equalsIgnoreCase("instructor"))
//                .toList();
//    }

    /**
     * Prints all user profiles in a readable format.
     */
    public void printAllUsers() {
        System.out.println("=== All User Profiles ===");
        for (UserProfile user : users) {
//            System.out.println("ID: " + user.getUserId());
            System.out.println("Name: " + user.getDisplayName());
            System.out.println("Email: " + user.getEmail());
            System.out.println("Role: " + user.getRole());
            System.out.println("Logo: " + user.getLogoUrl());
        }
    }

    /**
     * Prints categorized user lists (students & instructors).
     */
//    public void printByCategory() {
//        System.out.println("\n--- Students ---");
//        for (UserProfile s : getAllStudents()) {
//            System.out.println("• " + s.getDisplayName());
//        }
//
//        System.out.println("\n--- Instructors ---");
//        for (UserProfile i : getAllInstructors()) {
//            System.out.println("• " + i.getDisplayName());
//        }
    }

    /**
     * Main method for testing this class.
     */
//    public static void main(String[] args) {
//        UserAnalytics analytics = new UserAnalytics();
//
//        // Step 1: Fetch from cloud (currently not know)
//        analytics.fetchUsersFromCloud();
//
//        // Step 2: Print all users
//        analytics.printAllUsers();
//
//        // Step 3: Print users by category
//        analytics.printByCategory();
//    }

