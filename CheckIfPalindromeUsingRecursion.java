package com.shubhlab.jweencryption;

public class CheckIfPalindromeUsingRecursion {

    private boolean isPalindrome(String str) {
        if (str.length() <= 1) {
            return true;
        }

        if (str.charAt(0) != str.charAt(str.length() - 1)) {
            return false;
        }

        return isPalindrome(str.substring(1, str.length() - 1)); // leave out the first and last character and check the remaining string
    }

    public static void main(String[] args) {
        CheckIfPalindromeUsingRecursion checkIfPalindromeUsingRecursion = new CheckIfPalindromeUsingRecursion();
        String str1 = "madam";
        int number1 = 12321;
        String str2 = "hello";
        int number2 = 12345;
        boolean isPalindrome_str1 = checkIfPalindromeUsingRecursion.isPalindrome(str1);
        boolean isPalindrome_str2 = checkIfPalindromeUsingRecursion.isPalindrome(str2);
        boolean isPalindrome_number1 = checkIfPalindromeUsingRecursion.isPalindrome(String.valueOf(number1));
        boolean isPalindrome_number2 = checkIfPalindromeUsingRecursion.isPalindrome(String.valueOf(number2));
        System.out.println("Is palindrome:isPalindrome_str1: " + isPalindrome_str1);
        System.out.println("Is palindrome:isPalindrome_str2: " + isPalindrome_str2);
        System.out.println("Is palindrome:isPalindrome_number1: " + isPalindrome_number1);
        System.out.println("Is palindrome:isPalindrome_number2: " + isPalindrome_number2);
    }
}
