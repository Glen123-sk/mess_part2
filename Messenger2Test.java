import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

public class Messenger2Test {
    // Helper to create a Message with optional override for messageID
    private Messenger2.Message createMessage(String recipient, String content, int num, String forcedID) {
        Messenger2.Message msg = new Messenger2.Message(recipient, content, num);
        if (forcedID != null) {
            try {
                java.lang.reflect.Field idField = Messenger2.Message.class.getDeclaredField("messageID");
                idField.setAccessible(true);
                idField.set(msg, forcedID);
            } catch (Exception e) { throw new RuntimeException(e); }
        }
        return msg;
    }


    @Test
    void testMessageLengthValidation() {
        Messenger2.Message valid = createMessage("+27718693002", "Hi Mike, can you join us for dinner tonight?", 0, null);
        assertEquals("Message ready to send.", valid.checkMessageLength());

        // 251 words (should fail)
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 251; i++) sb.append("word ");
        Messenger2.Message tooLong = createMessage("+27718693002", sb.toString().trim(), 0, null);
        assertEquals("Message exceeds 250 words by 1; please reduce the size.", tooLong.checkMessageLength());

        // 250 words (should pass)
        sb = new StringBuilder();
        for (int i = 0; i < 250; i++) sb.append("word ");
        Messenger2.Message edge = createMessage("+27718693002", sb.toString().trim(), 0, null);
        assertEquals("Message ready to send.", edge.checkMessageLength());
    }

    @Test
    void testRecipientNumberValidation() {
        Messenger2.Message valid = createMessage("+27718693002", "Test", 0, null);
        assertEquals("Cell phone number successfully captured.", valid.checkRecipientCell());

        Messenger2.Message invalidLocal = createMessage("08575975889", "Test", 0, null);
        assertEquals("Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.", invalidLocal.checkRecipientCell());

        Messenger2.Message invalidFormat = createMessage("12345", "Test", 0, null);
        assertEquals("Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.", invalidFormat.checkRecipientCell());
    }

    @Test
    void testMessageID() {
        Messenger2.Message msg = createMessage("+27718693002", "Test", 0, null);
        assertTrue(msg.checkMessageID());

        Messenger2.Message fake = createMessage("+27718693002", "Test", 0, null);
        // Simulate invalid ID
        try {
            java.lang.reflect.Field idField = Messenger2.Message.class.getDeclaredField("messageID");
            idField.setAccessible(true);
            idField.set(fake, "1234");
        } catch (Exception e) { throw new RuntimeException(e); }
        assertFalse(fake.checkMessageID());
    }

    @Test
    void testMessageHash() {
        Messenger2.Message msg1 = createMessage("+27718693002", "Hi Mike, can you join us for dinner tonight?", 0, "0012345678");
        assertEquals("00:0:HITONIGHT", msg1.createMessageHash());

        Messenger2.Message msg2 = createMessage("+27718693002", "Hello John how are you", 1, "9912345678");
        assertEquals("99:1:HELLOYOU", msg2.createMessageHash());

        Messenger2.Message msg3 = createMessage("+27718693002", "Hello", 2, "5512345678");
        assertEquals("55:2:HELLOHELLO", msg3.createMessageHash());
    }

    @Test
    void testSendDiscardStore() {
        Messenger2.Message msg = createMessage("+27718693002", "Test", 0, null);
        assertEquals("Message successfully sent.", msg.sendMessage("send"));
        assertEquals("Press O to delete the message.", msg.sendMessage("discard"));
        assertEquals("Message successfully stored.", msg.sendMessage("store"));
    }

    @Test
    void testTotalMessagesCounter() {
        List<Messenger2.Message> messages = new ArrayList<>();
        int total = 0;
        Messenger2.Message m1 = createMessage("+27718693002", "Test1", 0, null);
        if (m1.sendMessage("send").equals("Message successfully sent.")) {
            messages.add(m1); total++;
        }
        assertEquals(1, total);
        Messenger2.Message m2 = createMessage("+27718693002", "Test2", 1, null);
        if (m2.sendMessage("send").equals("Message successfully sent.")) {
            messages.add(m2); total++;
        }
        assertEquals(2, total);
        Messenger2.Message m3 = createMessage("+27718693002", "Test3", 2, null);
        if (m3.sendMessage("discard").equals("Press O to delete the message.")) {
            // do not increment
        }
        assertEquals(2, total);
    }

    @Test
    void testPrintMessages() {
        Messenger2.Message msg = createMessage("+27718693002", "Hi Mike, can you join us for dinner tonight?", 0, "0012345678");
        // Capture output
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        System.setOut(new java.io.PrintStream(out));
        msg.printMessageDetails();
        String printed = out.toString();
        assertTrue(printed.contains("0012345678"));
        assertTrue(printed.contains("00:0:HITONIGHT"));
        assertTrue(printed.contains("+27718693002"));
        assertTrue(printed.contains("Hi Mike, can you join us for dinner tonight?"));
        System.setOut(System.out);
    }

    @Test
    void testLoopMultipleMessages() {
        String[] recipients = {"+27711111111", "+27722222222", "+27733333333"};
        String[] messages = {"Hello there friend", "How are you today", "See you soon"};
        String[] expectedHashes = {":0:HELLOFRIEND", ":1:HOWTODAY", ":2:SEESOON"};
        for (int i = 0; i < 3; i++) {
            Messenger2.Message msg = createMessage(recipients[i], messages[i], i, "991234567" + i);
            String hash = msg.createMessageHash();
            assertTrue(hash.startsWith("99"));
            assertTrue(hash.endsWith(expectedHashes[i]));
        }
    }
}
