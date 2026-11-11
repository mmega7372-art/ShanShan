package com.example.demo.model;

import java.util.*;

public class GameState {
    private Map<String, PlayerData> players = new HashMap<>();
    private List<Card> deck = new ArrayList<>();
    private String winner = null;
    private String status = "PLAYING"; 
    private int maxPlayers = 2; 
    private boolean withBots = true; // Default: Bot ဖြင့်

    public synchronized void setMaxPlayers(int maxPlayers) { this.maxPlayers = maxPlayers; }
    public synchronized int getMaxPlayers() { return maxPlayers; }

    public synchronized void setWithBots(boolean withBots) { this.withBots = withBots; }
    public synchronized boolean isWithBots() { return withBots; }

    public synchronized void addPlayer(String playerName) {
        players.computeIfAbsent(playerName, k -> new PlayerData());
    }

    public synchronized void addBotsIfNeeded() {
        players.keySet().removeIf(name -> name.startsWith("🤖 Bot"));

        if (!withBots) return; // PvP Mode ဖြစ်ပါက Bot လုံးဝ မထည့်ပါ
        
        while (players.size() < maxPlayers) {
            int botIndex = players.size() + 1;
            String botName = "🤖 Bot " + botIndex;
            players.computeIfAbsent(botName, k -> new PlayerData());
        }
    }

    public synchronized void resetAndDeal() {
        deck.clear();
        winner = null;
        status = "PLAYING";
        
        addBotsIfNeeded();

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
            player.setRestartReady(false); // ပွဲသစ်စသည်နှင့် restart status ကို ရှင်းမည်
            if (deck.size() >= 2) {
                player.getCards().add(deck.remove(0));
                player.getCards().add(deck.remove(0));
                player.setScore(ShanKoeMheeEngine.calculateScore(player.getCards()));
                player.setMultiplier(ShanKoeMheeEngine.calculateMultiplier(player.getCards()));
            }
        }

        processBotActions();
    }

    public synchronized void drawCard(String playerName) {
        if ("FINISHED".equals(status)) return;

        PlayerData player = players.get(playerName);
        if (player != null && player.getCards().size() < 3 && !deck.isEmpty()) {
            player.getCards().add(deck.remove(0));
            player.setScore(ShanKoeMheeEngine.calculateScore(player.getCards()));
            player.setMultiplier(ShanKoeMheeEngine.calculateMultiplier(player.getCards()));
            checkGameEnd();
        }
    }

    public synchronized void setPlayerReady(String playerName) {
        if ("FINISHED".equals(status)) return;

        PlayerData player = players.get(playerName);
        if (player != null) {
            player.setReady(true);
            checkGameEnd();
        }
    }

    // ကစားသမားများမှ ပွဲသစ်စရန် တောင်းဆိုမှုကို စီမံခြင်း
    public synchronized void handleRestartVote(String playerName) {
        PlayerData player = players.get(playerName);
        if (player != null) {
            player.setRestartReady(true);
        }

        // Bot များပါဝင်ပါက Bot အားလုံးကို ချက်ချင်း Restart Ready ဖြစ်သည်ဟု သတ်မှတ်မည်
        for (Map.Entry<String, PlayerData> entry : players.entrySet()) {
            if (entry.getKey().startsWith("🤖 Bot")) {
                entry.getValue().setRestartReady(true);
            }
        }

        // အခန်းထဲရှိ Player အားလုံး (လူ + Bot) Restart Ready ဖြစ်မဖြစ် စစ်ဆေးမည်
        boolean allRestartReady = true;
        for (PlayerData pData : players.values()) {
            if (!pData.isRestartReady()) {
                allRestartReady = false;
                break;
            }
        }

        // အားလုံး အသင့်ဖြစ်ပါက ပွဲသစ် ချက်ချင်းစတင်မည်
        if (allRestartReady) {
            resetAndDeal();
        }
    }

    private synchronized void processBotActions() {
        if (!withBots) return;

        for (Map.Entry<String, PlayerData> entry : players.entrySet()) {
            String name = entry.getKey();
            PlayerData pData = entry.getValue();
            if (name.startsWith("🤖 Bot")) {
                int score = pData.getScore();
                if (score < 6 && pData.getCards().size() < 3 && !deck.isEmpty()) {
                    pData.getCards().add(deck.remove(0));
                    pData.setScore(ShanKoeMheeEngine.calculateScore(pData.getCards()));
                    pData.setMultiplier(ShanKoeMheeEngine.calculateMultiplier(pData.getCards()));
                }
                pData.setReady(true);
            }
        }
        checkGameEnd();
    }

    private synchronized void checkGameEnd() {
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

    private synchronized void determineWinner() {
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
                    if (cardCount < minCardCount) {
                        minCardCount = cardCount;
                        maxMultiplier = multiplier;
                        bestPlayer = entry.getKey();
                        isTie = false;
                    } else if (cardCount == minCardCount) {
                        if (multiplier > maxMultiplier) {
                            maxMultiplier = multiplier;
                            bestPlayer = entry.getKey();
                            isTie = false;
                        } else if (multiplier == maxMultiplier) {
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

    public synchronized Map<String, PlayerData> getPlayers() { return players; }
    public synchronized String getWinner() { return winner; }
    public synchronized String getStatus() { return status; }

    public static class PlayerData {
        private List<Card> cards = new ArrayList<>();
        private int score;
        private int multiplier;
        private boolean ready = false;
        private boolean restartReady = false;

        public List<Card> getCards() { return cards; }
        public int getScore() { return score; }
        public void setScore(int score) { this.score = score; }
        public int getMultiplier() { return multiplier; }
        public void setMultiplier(int multiplier) { this.multiplier = multiplier; }
        public boolean isReady() { return ready; }
        public void setReady(boolean ready) { this.ready = ready; }
        public boolean isRestartReady() { return restartReady; }
        public void setRestartReady(boolean restartReady) { this.restartReady = restartReady; }
    }
}
