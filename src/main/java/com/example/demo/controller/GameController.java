package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import com.example.demo.model.*;

import java.util.*;

@Controller
public class GameController {

    private Map<String, GameState> activeRooms = new HashMap<>();

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/game.action")
    public void handleGameAction(GameRequest request) {
        GameState state = activeRooms.computeIfAbsent(request.getRoomId(), k -> new GameState());

        // ကစားသမား ဝင်ရောက်လာပါက State ထဲသို့ မှတ်သားရန်
        if (request.getPlayerName() != null && !request.getPlayerName().isEmpty()) {
            state.addPlayer(request.getPlayerName());
        }

        if ("START".equals(request.getType()) || "JOIN".equals(request.getType())) {
            state.resetAndDeal();
        } else if ("DRAW".equals(request.getType())) {
            state.drawCard(request.getPlayerName());
        } else if ("STAND".equals(request.getType())) {
            state.setPlayerReady(request.getPlayerName());
        }

        // Send update to specific room topic
        messagingTemplate.convertAndSend("/topic/room/" + request.getRoomId(), new GameResponse(state));
    }
}
