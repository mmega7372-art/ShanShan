package com.example.demo.model;

public class Card {
    private String suit; // e.g., "♥", "♦", "♣", "♠"
    private int value;   // 1 to 13 (A=1, J=11, Q=12, K=13)

    public Card(String suit, int value) {
        this.suit = suit;
        this.value = value;
    }

    public String getSuit() {
        return suit;
    }

    public int getValue() {
        return value;
    }
}