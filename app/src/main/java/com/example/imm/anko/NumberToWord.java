package com.example.imm.anko;

import java.util.HashMap;

public class NumberToWord {
    HashMap<Integer, String> digitToWordMap;

    public NumberToWord() {
        digitToWordMap = new HashMap<Integer, String>();
        fillMap();
    }

    public String numberToWords(int num) {
        StringBuilder sb = new StringBuilder();

        if(num==0){
            return digitToWordMap.get(0);
        }

        if(num >= 1000000000){
            int extra = num/1000000000;
            sb.append(convert(extra) + " Billion");
            num = num%1000000000;
        }

        if(num >= 1000000){
            int extra = num/1000000;
            sb.append(convert(extra) + " Million");
            num = num%1000000;
        }

        if(num >= 1000){
            int extra = num/1000;
            sb.append(convert(extra) + " Thousand");
            num = num%1000;
        }

        if(num > 0){
            sb.append(convert(num));
        }

        return sb.toString().trim();
    }

    public String convert(int num){

        StringBuilder sb = new StringBuilder();

        if(num>=100){
            int numHundred = num/100;
            sb.append(" " +digitToWordMap.get(numHundred)+ " Hundred");
            num=num%100;
        }

        if(num > 0){
            if(num>0 && num<=20){
                sb.append(" "+digitToWordMap.get(num));
            }else{
                int numTen = num/10;
                sb.append(" "+digitToWordMap.get(numTen*10));

                int numOne=num%10;
                if(numOne>0){
                    sb.append(" " + digitToWordMap.get(numOne));
                }
            }
        }

        return sb.toString();
    }

    public void fillMap(){
        digitToWordMap.put(0, "Zero");
        digitToWordMap.put(1, "One");
        digitToWordMap.put(2, "Two");
        digitToWordMap.put(3, "Three");
        digitToWordMap.put(4, "Four");
        digitToWordMap.put(5, "Five");
        digitToWordMap.put(6, "Six");
        digitToWordMap.put(7, "Seven");
        digitToWordMap.put(8, "Eight");
        digitToWordMap.put(9, "Nine");
        digitToWordMap.put(10, "Ten");
        digitToWordMap.put(11, "Eleven");
        digitToWordMap.put(12, "Twelve");
        digitToWordMap.put(13, "Thirteen");
        digitToWordMap.put(14, "Fourteen");
        digitToWordMap.put(15, "Fifteen");
        digitToWordMap.put(16, "Sixteen");
        digitToWordMap.put(17, "Seventeen");
        digitToWordMap.put(18, "Eighteen");
        digitToWordMap.put(19, "Nineteen");
        digitToWordMap.put(20, "Twenty");
        digitToWordMap.put(30, "Thirty");
        digitToWordMap.put(40, "Forty");
        digitToWordMap.put(50, "Fifty");
        digitToWordMap.put(60, "Sixty");
        digitToWordMap.put(70, "Seventy");
        digitToWordMap.put(80, "Eighty");
        digitToWordMap.put(90, "Ninety");
    }
}