import java.util.*;
import java.io.FileWriter;
import java.io.IOException;

public class Messenger2 {

    // Message class inside same file
    static class Message {
        private String messageID;
        private int numSentMessages;
        private String recipient;
        private String messageContent;
        private String messageHash;

        // Constructor
        public Message(String recipient, String messageContent, int messageNumber) {
            this.messageID = generateMessageID();
            this.numSentMessages = messageNumber;
            this.recipient = recipient;
            this.messageContent = messageContent;
            this.messageHash = createMessageHash();
        }

        // Generate 10-digit Message ID
        private String generateMessageID() {
            Random rand = new Random();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 10; i++) {
                sb.append(rand.nextInt(10));
            }
            return sb.toString();
        }

        // Validate Message ID
        public boolean checkMessageID() {
            return messageID.length() == 10;
        }

        // Validate recipient
        public String checkRecipientCell() {
            // Only accept numbers starting with '+' and length between 11 and 13 (e.g., +27718693002)
            if (recipient.startsWith("+") && recipient.length() >= 11 && recipient.length() <= 13) {
                return "Cell phone number successfully captured.";
            } else {
                return "Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.";
            }
        }

        // Validate message length
        public String checkMessageLength() {
            int wordCount = messageContent.trim().isEmpty() ? 0 : messageContent.trim().split("\\s+").length;
            if (wordCount <= 250) {
                return "Message ready to send.";
            } else {
                int over = wordCount - 250;
                return "Message exceeds 250 words by " + over + "; please reduce the size.";
            }
        }

        // Create message hash
        public String createMessageHash() {
            String[] words = messageContent.split("\\s+");
            String firstWord = words[0].replaceAll("[^A-Za-z0-9]", "").toUpperCase();
            String lastWord = words[words.length - 1].replaceAll("[^A-Za-z0-9]", "").toUpperCase();
            return messageID.substring(0, 2) + ":" + numSentMessages + ":" + firstWord + lastWord;
        }

        // Send / Store / Discard
        public String sendMessage(String action) {
            switch(action.toLowerCase()) {
                case "send":
                    return "Message successfully sent.";
                case "discard":
                    return "Press O to delete the message.";
                case "store":
                    storeMessage();
                    return "Message successfully stored.";
                default:
                    return "Invalid action.";
            }
        }

        // Store message in plain text
        public void storeMessage() {
            try (FileWriter writer = new FileWriter("stored_messages.txt", true)) {
                writer.write("Message ID: " + messageID + "\n");
                writer.write("Message Hash: " + messageHash + "\n");
                writer.write("Recipient: " + recipient + "\n");
                writer.write("Message: " + messageContent + "\n");
                writer.write("---------------------------\n");
            } catch (IOException e) {
                System.out.println("Error storing message: " + e.getMessage());
            }
        }

        // Print message details
        public void printMessageDetails() {
            System.out.println("Message ID: " + messageID);
            System.out.println("Message Hash: " + createMessageHash());
            System.out.println("Recipient: " + recipient);
            System.out.println("Message: " + messageContent);
            System.out.println("-----------------------------------");
        }

        // Getter for message number
        public int getNumSentMessages() {
            return numSentMessages;
        }
    }

    // Main method
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        List<Message> messages = new ArrayList<>();
        int totalMessages = 0;

        // 1. Login Simulation
        System.out.println("Login to QuickChat");
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        System.out.println("Login successful!\n");

        // 2. Welcome message
        System.out.println("Welcome to QuickChat.");

        boolean running = true;
        while (running) {
            System.out.println("\nMenu:");
            System.out.println("1) Send Messages");
            System.out.println("2) Show Recently Sent Messages");
            System.out.println("3) Quit");
            System.out.print("Select option: ");
            String choice = scanner.nextLine();

            switch(choice) {

                case "1":
                    System.out.print("How many messages would you like to send? ");
                    int numMessages = Integer.parseInt(scanner.nextLine());

                    for (int i = 0; i < numMessages; i++) {
                        System.out.print("Enter recipient number: ");
                        String recipient = scanner.nextLine();

                        System.out.print("Enter message: ");
                        String content = scanner.nextLine();

                        Message msg = new Message(recipient, content, totalMessages);

                        // Validate recipient
                        System.out.println(msg.checkRecipientCell());

                        // Validate message length
                        System.out.println(msg.checkMessageLength());

                        // Show message hash and ID
                        System.out.println("Message ID generated: " + msg.getNumSentMessages());
                        System.out.println("Message Hash: " + msg.createMessageHash());

                        // Ask user action
                        System.out.print("Select action (Send / Discard / Store): ");
                        String action = scanner.nextLine();
                        System.out.println(msg.sendMessage(action));

                        // Store sent messages in memory if sent
                        if (action.equalsIgnoreCase("send")) {
                            messages.add(msg);
                            totalMessages++;
                        }

                        System.out.println("\nMessage details:");
                        msg.printMessageDetails();
                    }
                    break;

                case "2":
                    if (messages.isEmpty()) {
                        System.out.println("No messages have been sent yet.");
                        // Immediately continue to next menu iteration
                        break;
                    } else {
                        System.out.println("Recently Sent Messages:");
                        for (int i = 0; i < messages.size(); i++) {
                            Message m = messages.get(i);
                            System.out.println((i + 1) + ") To: " + m.recipient + " | Content: " + m.messageContent);
                        }
                        System.out.print("Select a message number to view details, or 0 to go back: ");
                        int sel = -1;
                        try {
                            sel = Integer.parseInt(scanner.nextLine());
                        } catch (NumberFormatException e) {
                            sel = -1;
                        }
                        if (sel > 0 && sel <= messages.size()) {
                            System.out.println("\nMessage details:");
                            messages.get(sel - 1).printMessageDetails();
                        } else if (sel != 0) {
                            System.out.println("Invalid selection.");
                        }
                    }
                    break;

                case "3":
                    running = false;
                    System.out.println("Exiting program.");
                    break;

                default:
                    System.out.println("Invalid option. Try again.");
            }
        }

        // Print total messages sent
        System.out.println("Total messages sent: " + totalMessages);
        scanner.close();
    }
}