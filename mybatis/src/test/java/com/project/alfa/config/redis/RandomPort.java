package com.project.alfa.config.redis;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Random;

public class RandomPort {
    
    public static int getRandomAvailablePort(final int minPort, final int maxPort) {
        Random random = new Random();
        int    port;
        do {
            port = random.nextInt((maxPort - minPort) + 1) + minPort;
        } while (!isPortAvailable(port));
        return port;
    }
    
    private static boolean isPortAvailable(final int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocket.setReuseAddress(true);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
}
