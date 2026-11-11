package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import com.example.demo.model.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class GameController {

    private final Map<String, GameState> activeRooms = new ConcurrentHashMap<>();
    private final Map<String, String> sessionRoomMap = new ConcurrentHashMap<>();
    private final Map<String, String> sessionPlayerMap = new ConcurrentHashMap<>();

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/game.action")
    public void handleGameAction(GameRequest request, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        String roomId = request.getRoomId();
        String playerName = request.getPlayerName();

        if (roomId == null || roomId.isEmpty()) return;

        GameState state = activeRooms.computeIfAbsent(roomId, k -> new GameState());

        // Player အရေအတွက် (Mode) နှင့် Bot ပါမပါ သတ်မှတ်ခြင်း
        if (request.getMaxPlayers() > 0) {
            state.setMaxPlayers(request.getMaxPlayers());
            state.setWithBots(request.isWithBots());
        }

        if (playerName != null && !playerName.isEmpty()) {
            state.addPlayer(playerName);
            if (sessionId != null) {
                sessionRoomMap.put(sessionId, roomId);
                sessionPlayerMap.put(sessionId, playerName);
            }
        }

        if ("START".equals(request.getType()) || "JOIN".equals(request.getType())) {
            state.resetAndDeal();
        } else if ("DRAW".equals(request.getType())) {
            state.drawCard(playerName);
        } else if ("STAND".equals(request.getType())) {
            state.setPlayerReady(playerName);
        }

        messagingTemplate.convertAndSend("/topic/room/" + roomId, new GameResponse(state));
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        if (sessionId != null && sessionRoomMap.containsKey(sessionId)) {
            String roomId = sessionRoomMap.remove(sessionId);
            String playerName = sessionPlayerMap.remove(sessionId);

            GameState state = activeRooms.get(roomId);
            if (state != null && playerName != null) {
                state.getPlayers().remove(playerName);

                if (state.getPlayers().isEmpty()) {
                    activeRooms.remove(roomId);
                } else {
                    messagingTemplate.convertAndSend("/topic/room/" + roomId, new GameResponse(state));
                }
            }
        }
    }
}
