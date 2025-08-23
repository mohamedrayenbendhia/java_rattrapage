package services;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

public class SMSService {


    private static final String ACCOUNT_SID = "AC2c3f3690d37107f34e0fa58b23263456";
    private static final String AUTH_TOKEN = "df7f129fbb83f62508d6b40a4308561c";
    private static final String FROM_NUMBER = "+12055126362";
    static {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
    }


    public static void sendSMS(String toNumber, String messageBody) {
        Message message = Message.creator(
                        new PhoneNumber(toNumber),
                        new PhoneNumber(FROM_NUMBER),
                        messageBody)
                .create();

        System.out.println("SMS envoyé avec l'ID: " + message.getSid());
    }
}
