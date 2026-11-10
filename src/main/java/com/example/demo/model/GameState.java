package com.example.demo.model;

import java.util.*;

public class GameState {
    private Map<String, PlayerData> players = new HashMap<>();
    private List<Card> deck = new ArrayList<>();
    private String winner = null;
    private String status = "PLAYING"; // "PLAYING" or "FINISHED"

    public void addPlayer(String playerName) {
        players.computeIfAbsent(playerName, k -> new PlayerData());
    }

    public void resetAndDeal() {
        deck.clear();
        winner = null;
        status = "PLAYING";
        String[] suits = {"♥", "♦", "♣", "♠"};
        for (String suit : suits) {
            for (int i = 1; i <= 13; i++) {
                deck.add(new Card(suit, i));
            }
        }
        Collections.shuffle(deck);

        for (PlayerData player : players.values()) {
            player.getCards().clear();
            player.setReady(false);
            if (deck.size() >= 2) {
                player.getCards().add(deck.remove(0));
                player.getCards().add(deck.remove(0));
                player.setScore(ShanKoeMheeEngine.calculateScore(player.getCards()));
                player.setMultiplier(ShanKoeMheeEngine.calculateMultiplier(player.getCards()));
            }
        }
    }

    public void drawCard(String playerName) {
        if ("FINISHED".equals(status)) return;

        PlayerData player = players.get(playerName);
        if (player != null && player.getCards().size() < 3 && !deck.isEmpty()) {
            player.getCards().add(deck.remove(0));
            player.setScore(ShanKoeMheeEngine.calculateScore(player.getCards()));
            player.setMultiplier(ShanKoeMheeEngine.calculateMultiplier(player.getCards()));
            checkGameEnd();
        }
    }

    public void setPlayerReady(String playerName) {
        if ("FINISHED".equals(status)) return;

        PlayerData player = players.get(playerName);
        if (player != null) {
            player.setReady(true);
            checkGameEnd();
        }
    }

    private void checkGameEnd() {
        if (players.size() < 2) return;

        boolean allReady = true;
        for (PlayerData player : players.values()) {
            if (!player.isReady() && player.getCards().size() < 3) {
                allReady = false;
                break;
            }
        }

        if (allReady) {
            status = "FINISHED";
            determineWinner();
        }
    }

    private void determineWinner() {
        String bestPlayer = null;
        int maxScore = -1;
        int minCardCount = 99; 
        int maxMultiplier = 0; 
        boolean isTie = false;

        for (Map.Entry<String, PlayerData> entry : players.entrySet()) {
            PlayerData pData = entry.getValue();
            int score = pData.getScore();
            int cardCount = pData.getCards().size();
            int multiplier = pData.getMultiplier();

            if (bestPlayer == null) {
                bestPlayer = entry.getKey();
                maxScore = score;
                minCardCount = cardCount;
                maxMultiplier = multiplier;
            } else {
                if (score > maxScore) {
                    maxScore = score;
                    minCardCount = cardCount;
                    maxMultiplier = multiplier;
                    bestPlayer = entry.getKey();
                    isTie = false;
                } else if (score == maxScore) {
                    // (၁) အမှတ်တူလျှင် ကဒ်အရေအတွက် နည်းသူ (၂ ကဒ်ဆွဲသူ) က နိုင်မည်
                    if (cardCount < minCardCount) {
                        minCardCount = cardCount;
                        maxMultiplier = multiplier;
                        bestPlayer = entry.getKey();
                        isTie = false;
                    } else if (cardCount == minCardCount) {
                        // (၂) ကဒ်အရေအတွက်ပါ တူနေလျှင် မြှောက်ကြိမ် (Multiplier) ပိုကြီးသူက နိုင်မည်
                        if (multiplier > maxMultiplier) {
                            maxMultiplier = multiplier;
                            bestPlayer = entry.getKey();
                            isTie = false;
                        } else if (multiplier == maxMultiplier) {
                            // အားလုံးတူနေလျှင် သရေ (Tie)
                            isTie = true;
                        }
                    }
                }
            }
        }

        if (isTie) {
            this.winner = null;
        } else {
            this.winner = bestPlayer;
        }
    }

    public Map<String, PlayerData> getPlayers() { return players; }
    public String getWinner() { return winner; }
    public String getStatus() { return status; }

    public static class PlayerData {
        private List<Card> cards = new ArrayList<>();
        private int score;
        private int multiplier;
        private boolean ready = false;

        public List<Card> getCards() { return cards; }
        public int getScore() { return score; }
        public void setScore(int score) { this.score = score; }
        public int getMultiplier() { return multiplier; }
        public void setMultiplier(int multiplier) { this.multiplier = multiplier; }
        public boolean isReady() { return ready; }
        public void setReady(boolean ready) { this.ready = ready; }
    }
}
