package com.example.myapplication.EmailUtils;

import com.example.myapplication.InternetUtils.GetServer;

import java.net.URLEncoder;
import java.util.Properties;
import java.util.UUID;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


public class SendEmail {
    private final String SMTP_HOST="smtp.qq.com";
    private final String SMTP_AUTH="true";
    private GetServer getServer = new GetServer();
    private final String CONTENT="<h1>激活邮件<h1><br/><span>"+ getServer.getIPADDRESS()+"/audiobook/activate?account=";
    private final String TYPE="text/html;charset=UTF-8";
    private final String FROM="492556292@qq.com";
    private final String PASSWORD="gzvbqnsdnohdcbde";


    public void sendEmail(String to,String account){
        String code = UUID.randomUUID().toString().replace("-", "");
        try {
            Properties properties = System.getProperties();
            properties.put("mail.smtp.host", SMTP_HOST);
            properties.put("mail.smtp.auth", SMTP_AUTH);

            Session session = Session.getDefaultInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(FROM, PASSWORD);
                }
            });

            MimeMessage message = new MimeMessage(session);

            message.setFrom(new InternetAddress(FROM));
            message.setRecipient(Message.RecipientType.TO,new InternetAddress(to));
            message.setContent(CONTENT+ URLEncoder.encode(account,"UTF-8") +"&code="+code+"</span>",TYPE);//URLEncoder对中文转码,防止后端收到乱码
            Transport.send(message);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
