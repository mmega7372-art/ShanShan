package com.example.demo.model;

import java.util.*;

public class ShanKoeMheeEngine {

    public static int calculateScore(List<Card> cards) {
        int sum = 0;
        for (Card card : cards) {
            int val = card.getValue();
            if (val >= 10) val = 0; // J, Q, K, 10 count as 0
            sum += val;
        }
        return sum % 10;
    }

    public static int calculateMultiplier(List<Card> cards) {
        if (cards.size() == 2) {
            // Check same suit (2x)
            if (cards.get(0).getSuit().equals(cards.get(1).getSuit())) return 2;
        } else if (cards.size() == 3) {
            // Check 3-of-a-kind (5x)
            if (cards.get(0).getValue() == cards.get(1).getValue() && 
                cards.get(1).getValue() == cards.get(2).getValue()) return 5;
            // Check 3 same suits (3x)
            if (cards.get(0).getSuit().equals(cards.get(1).getSuit()) && 
                cards.get(1).getSuit().equals(cards.get(2).getSuit())) return 3;
        }
        return 1;
    }
}
