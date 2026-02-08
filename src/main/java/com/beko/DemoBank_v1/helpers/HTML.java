package com.beko.DemoBank_v1.helpers;

public class HTML {

    public static String htmlEmailTemplate(String token, String code) {
        String url = "http://127.0.0.1:8070/verify?token=" + token + "&code=" + code;
        String emailTemplate = "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px; }" +
                ".container { background-color: #ffffff; padding: 30px; border-radius: 10px; max-width: 600px; margin: 0 auto; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }"
                +
                "h1 { color: #333333; }" +
                "p { color: #666666; font-size: 16px; line-height: 1.6; }" +
                ".button { display: inline-block; padding: 12px 30px; background-color: #007bff; color: #ffffff; text-decoration: none; border-radius: 5px; margin-top: 20px; }"
                +
                ".button:hover { background-color: #0056b3; }" +
                ".code { font-size: 24px; font-weight: bold; color: #007bff; margin: 20px 0; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<h1>Welcome to DemoBank!</h1>" +
                "<p>Thank you for registering with us. Please verify your account by clicking the button below:</p>" +
                "<a href='" + url + "' class='button'>Verify Your Account</a>" +
                "<p style='margin-top: 30px;'>Or use this verification code: <span class='code'>" + code + "</span></p>"
                +
                "<p style='font-size: 14px; color: #999999; margin-top: 30px;'>If you didn't create this account, please ignore this email.</p>"
                +
                "</div>" +
                "</body>" +
                "</html>";
        return emailTemplate;
    }
}
