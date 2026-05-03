package com.shubhlab.jweencryption;

import java.util.ArrayList;

public class PermutationsOfString {


    public ArrayList<String> findPermutation(String s) {


        ArrayList<String> solution = new ArrayList<String>();

        char[] chArr = s.toCharArray();

        findPermutationsRecursive(chArr, 0, solution);

        return solution;

    }


    private void findPermutationsRecursive(char[] chArr, int pos, ArrayList<String> solution) {

        int n = chArr.length;

        if (pos == (n - 1)) {

// Terminal condition

            String str = String.copyValueOf(chArr);

            solution.add(str);

            return;

        }


        findPermutationsRecursive(chArr, pos + 1, solution);

        for (int i = pos + 1; i < n; i++) {

            if (chArr[i] == chArr[pos]) {

                continue;

            }

            swap(chArr, pos, i);

            findPermutationsRecursive(chArr, pos + 1, solution);

// This is backtracking - restoring the original position

            swap(chArr, pos, i);

        }

    }


    private void swap(char[] chArr, int idx1, int idx2) {

        char ch = chArr[idx1];

        chArr[idx1] = chArr[idx2];

        chArr[idx2] = ch;

    }

    public static void main(String[] args) {
        PermutationsOfString permutationsOfString = new PermutationsOfString();

        String s = "abc";

        ArrayList<String> solution = permutationsOfString.findPermutation(s);

        System.out.println(solution);

        System.out.println("for repeating characters");

        String repeatingChars = "aab";

        solution = permutationsOfString.findPermutation(repeatingChars);
        System.out.println(solution);
    }


}
