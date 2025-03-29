package com.example.speech.aiservice.vn.service.console;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.util.Scanner;

public class CommandListenerApp {
    public static void main(String[] args) {
        new CommandListenerApp().listenForCommands();
    }

    private void listenForCommands() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("Enter command: ");
            String commandInput = scanner.nextLine().trim();

            CommandType commandType = CommandType.fromString(commandInput.toUpperCase());

            if (commandType == CommandType.STOP) {
                System.out.println("STOP command received! Stopping workflow...");
                sendStopRequest();
            } else if (commandType == CommandType.UPDATE_ALL) {
                System.out.println("Update all command received!");
                sendUpdateAllRequest();
            } else if (isValidURL(commandInput)) {
                System.out.println("Valid URL detected! Sending update request...");
                sendScanRequest(commandInput);
            } else {
                System.out.println("Invalid command! Please enter a valid command (e.g., STOP or a valid URL).");
            }
        }
    }

    private void sendStopRequest() {
        sendPostRequest("http://localhost:8080/workflow/stop", "{}");
    }

    private void sendScanRequest(String url) {
        String jsonPayload = "{\"url\": \"" + url + "\"}";
        sendPostRequest("http://localhost:8080/workflow/scan", jsonPayload);
    }

    private void sendUpdateAllRequest() {
        sendPostRequest("http://localhost:8080/workflow/update-all", "{}");
    }

    private void sendPostRequest(String endpoint, String payload) {
        try {
            URL url = new URL(endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setInstanceFollowRedirects(false);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");

            OutputStream os = conn.getOutputStream();
            os.write(payload.getBytes());
            os.flush();
            os.close();

            System.out.println("Response Code: " + conn.getResponseCode());
        } catch (Exception e) {
            System.out.println("Error: Unable to send request!");
            e.printStackTrace();
        }
    }

    private boolean isValidURL(String input) {
        return input.matches("^https?://chivi\\.app/wn/books/\\w+$");
    }
}
