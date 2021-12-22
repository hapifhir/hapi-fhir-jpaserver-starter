package general;


import config.ConfigProperties;

import java.time.LocalDate;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class SendEmailAfterExecution {


    public SendEmailAfterExecution() {
    }

    public static void sendReportAfterExecution(Integer passed, Integer failed, Integer skipped) throws MessagingException {
        Integer Total = passed + failed + skipped;
        if (ConfigProperties.sendEmail.toLowerCase().equals("true")) {
            String[] recepientTo = ConfigProperties.To;
            String senderFrom = ConfigProperties.from;
            String path = System.getProperty("user.dir") + "/reports/ExtentReport.html";

            Properties prop = System.getProperties();
            prop.setProperty("mail.smtp.host", "smtp.gmail.com");
            Session session = Session.getDefaultInstance(prop);
            MimeMessage msg = new MimeMessage(session);
            InternetAddress frmAddress = new InternetAddress(senderFrom);
            msg.setFrom(frmAddress);

            for(int i = 0; i < recepientTo.length; ++i) {
                msg.addRecipients(RecipientType.TO, String.valueOf(new InternetAddress(recepientTo[i])));
            }

            LocalDate dateTime = LocalDate.now();
            msg.setSubject(ConfigProperties.Project + "-" + ConfigProperties.Platform + "-" + ConfigProperties.Environment + "-Execution Report " + dateTime);
            BodyPart msgBody = new MimeBodyPart();
            msgBody.setText(ConfigProperties.Project + "-" + ConfigProperties.Platform + "-" + ConfigProperties.Environment + "-Execution Report " + dateTime + "\nTotal Cases Executed:" + Total + "\nPassed:" + passed + "\nFailed:" + failed + "\nSkipped:" + skipped);
            Multipart multiPart = new MimeMultipart();
            multiPart.addBodyPart(msgBody);
            msgBody = new MimeBodyPart();
             DataSource source = new FileDataSource(path);
             DataHandler dataHandler = new DataHandler(source);
            msgBody.setDataHandler(dataHandler);
             msgBody.setFileName(path);

            multiPart.addBodyPart(msgBody);
            msg.setContent(multiPart);
            Transport transport = session.getTransport("smtps");
            transport.connect("smtp.gmail.com", 465, ConfigProperties.from, ConfigProperties.fromPassword);
            transport.sendMessage(msg, msg.getAllRecipients());
            transport.close();
            System.out.println("Email send to respective Recipients");
        } else {
            System.out.println("Email Not sent as permissions are not given");
        }

    }

}
