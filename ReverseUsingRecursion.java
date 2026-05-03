package com.shubhlab.jweencryption;

public class ReverseUsingRecursion {

    // 1234
    private String reverseString(String str){
        if(str.length() == 1){
            return str;
        }

        return str.charAt(str.length()-1) + reverseString(str.substring(0, str.length()-1));

    }

    public static void main(String[] args) {
        ReverseUsingRecursion reverseUsingRecursion = new ReverseUsingRecursion();
        String str = "1234";
        String reversed = reverseUsingRecursion.reverseString(str);
        System.out.println("Reversed string: " + reversed);
    }

}
