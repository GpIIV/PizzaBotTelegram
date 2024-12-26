package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Main extends TelegramLongPollingBot {

    public static void main(String[] args) {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new Main());
            System.out.println("PizzaBot is running!");
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private Connection getConnection() throws SQLException {
        String url = "jdbc:postgresql://localhost:5432/PizzaBot";
        String user = "postgres";
        String password = "root";

        return DriverManager.getConnection(url, user, password);
    }

    @Override
    public String getBotUsername() {
        return "@trtrtrtrtrtrtrtrtrt_bot";
    }

    @Override
    public String getBotToken() {
        return "6589936377:AAFWb89Tiyj4pd7FylqBZoQjDM5hdlP2urs";
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            if (messageText.equals("/start")) {
                SendMessage message = new SendMessage();
                message.setChatId(chatId);
                message.setText("Welcome to PizzaBot! How can I help you today?");

                ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
                List<KeyboardRow> keyboard = new ArrayList<>();
                KeyboardRow row = new KeyboardRow();
                KeyboardButton button = new KeyboardButton("Share Contact");
                button.setRequestContact(true);
                row.add(button);
                keyboard.add(row);

                KeyboardRow row2 = new KeyboardRow();
                KeyboardButton button2 = new KeyboardButton("Create");
                row2.add(button2);
                keyboard.add(row2);

                keyboardMarkup.setKeyboard(keyboard);
                keyboardMarkup.setResizeKeyboard(true);
                keyboardMarkup.setOneTimeKeyboard(false);
                message.setReplyMarkup(keyboardMarkup);

                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            } else if (messageText.equals("Create")) {
                SendMessage message = new SendMessage();
                message.setChatId(chatId);
                message.setText("Send me a message with a list of your ingredients, separated by commas.");

                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            } else {
                String[] ingredients = messageText.split(",");
                if (ingredients.length > 1) {
                    try (Connection connection = getConnection()) {
                        String sql = "INSERT INTO recipes (user_id, ingredient1, ingredient2, ingredient3, ingredient4) VALUES (?, ?)";
                        PreparedStatement statement = connection.prepareStatement(sql);
                        statement.setString(1, String.valueOf(update.getMessage().getFrom().getId()));
                        statement.setString(2, messageText);

                        int rowsAffected = statement.executeUpdate();
                        System.out.println("Inserted " + rowsAffected + " row(s) into the recipes table.");

                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    SendMessage message = new SendMessage();
                    message.setChatId(chatId);
                    message.setText("Your recipe has been saved!");

                    try {
                        execute(message);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                } else {
                    SendMessage message = new SendMessage();
                    message.setChatId(chatId);
                    message.setText("Invalid recipe format. Please send a message with ingredients separated by commas.");

                    try {
                        execute(message);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else if (update.hasMessage() && update.getMessage().hasContact()) {
            String phoneNumber = update.getMessage().getContact().getPhoneNumber();
            String firstName = update.getMessage().getContact().getFirstName();
            String lastName = update.getMessage().getContact().getLastName();
            String userId = String.valueOf(update.getMessage().getFrom().getId());
            long chatId = update.getMessage().getChatId();

            System.out.println("User Information:");
            System.out.println("User ID: " + userId);
            System.out.println("First Name: " + firstName);
            System.out.println("Last Name: " + lastName);
            System.out.println("Phone Number: " + phoneNumber);

            try (Connection connection = getConnection()) {
                String sql = "INSERT INTO users (user_id, first_name, last_name, phone_number) VALUES (?, ?, ?, ?)";
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.setString(1, userId);
                statement.setString(2, firstName);
                statement.setString(3, lastName);
                statement.setString(4, phoneNumber);

                int rowsAffected = statement.executeUpdate();
                System.out.println("Inserted " + rowsAffected + " row(s) into the database.");

            } catch (SQLException e) {
                e.printStackTrace();
            }

            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText("Thank you, " + firstName + "! We've received your contact number: " + phoneNumber);

            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }
}