package services;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

public class SMSService {


    private static final String ACCOUNT_SID = "AC4df3c904f289a91c5135d27ee19de4a4";
    private static final String AUTH_TOKEN = "cf62f75eba40c470f8ad2f4354955d3e";
    private static final String FROM_NUMBER = "+19705571479";
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
