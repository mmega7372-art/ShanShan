package com.example.demo.model;

public class GameRequest {
    private String type;
    private String roomId;
    private String playerName;
    private int maxPlayers;
    private boolean withBots; // Bot ထည့်ရန်/မထည့်ရန်

    // Getters and Setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    
    public int getMaxPlayers() { return maxPlayers; }
    public void setMaxPlayers(int maxPlayers) { this.maxPlayers = maxPlayers; }

    public boolean isWithBots() { return withBots; }
    public void setWithBots(boolean withBots) { this.withBots = withBots; }
}
