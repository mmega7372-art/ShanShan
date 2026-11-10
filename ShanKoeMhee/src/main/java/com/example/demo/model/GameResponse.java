package com.example.demo.model;

public class GameResponse {
    private GameState state;

    public GameResponse(GameState state) {
        this.state = state;
    }

    public GameState getState() { return state; }
    public void setState(GameState state) { this.state = state; }
}