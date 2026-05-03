package com.shubhlab.jweencryption;

public class CountZerosUsingRecursion {

    private static int countZeros(int number) {

        if (number == 0) {
            return 0;
        }
        int count = number % 10 == 0 ? 1 : 0;
        return count + countZeros(number / 10);

    }

    public static void main(String[] args) {
        int count = countZeros(670100900);

        System.out.println("Number of zeros: " + count);

    }
}
