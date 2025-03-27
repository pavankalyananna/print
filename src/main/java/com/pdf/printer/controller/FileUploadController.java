package com.pdf.printer.controller;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.pdf.printer.dto.FileInfo;
import com.pdf.printer.service.FileStorageService;
import com.pdf.printer.service.RazorpayService;
import com.razorpay.RazorpayException;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.mail.Authenticator;
import jakarta.mail.BodyPart;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;


@Controller
public class FileUploadController {
	
	 @Value("${file.upload-dir}")
	 private String save;
	 
	 @Value("${username}")
	 private String id;
	 
	 @Value("${password}")
	 private String pass;
	 
	 @Value("${p-email}")
	 private String printMail;

    private final FileStorageService fileStorageService;
    
    FileInfo fileInfo; 

    @Autowired
    public FileUploadController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            fileInfo = fileStorageService.storeFile(file);
            response.put("url", fileInfo.getUrl());
            response.put("fileName", fileInfo.getFileName());
            response.put("pageCount", fileInfo.getPageCount());
            System.out.println(fileInfo.getPageCount());
            System.out.println(fileInfo.toString());
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            response.put("error", "File upload failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    @PostMapping("/proceed")
    public ResponseEntity<Map<String, Object>> print()
    {
    	Map<String, Object> response = new HashMap<>();
    	System.out.println("mail sending started");
        final String username = id; // Your Gmail address
        final String password = pass; // Your Gmail password

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            // Create a default MimeMessage object
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(printMail));
           // message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("umg47089t0xhm4@print.epsonconnect.com")); // Replace with recipient's email
            message.setSubject("Test Email with Attachment");

            // Create the message part
            BodyPart messageBodyPart = new MimeBodyPart();
            // Create a multipart message
            Multipart multipart = new MimeMultipart();
            System.out.println("message bodypart");
            // Part two is the attachment
            messageBodyPart = new MimeBodyPart();
            //String filename = "/home/pavan/Desktop/printerFiles/uploads/"+fileInfo.getFileName(); // Replace with the path to your file
            String filename = save+fileInfo.getFileName();
            DataSource source = new FileDataSource(filename);
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(fileInfo.getFileName()); // The name that will be shown in the email
            multipart.addBodyPart(messageBodyPart);

            // Send the complete message parts
            message.setContent(multipart);

            // Send the message
            System.out.println("transporting");
            Transport.send(message);
            System.out.println("Email sent successfully with attachment!");
            //return "upload";
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        response.put("status", "failure");
        return ResponseEntity.internalServerError().body(response);
    }
    
    //payment code
    @Autowired
    private RazorpayService razorpayService;
    @PostMapping("/api/payments/create-order")
    public ResponseEntity<Map<String, Object>> createOrder(){
    	Map<String, Object> response = new HashMap<>();

        try {
        	System.out.println("payment page");
        	int amount=fileInfo.getPageCount()*10;
        	String currency="INR";
        	String order=razorpayService.createOrder(amount, currency, "recepient_100");
        	System.out.println("order details"+order.toString());
        	String status=extractStatusFromString(order);
        	System.out.println(status);
        	response.put("status", status);
        	response.put("amount", amount);
            return ResponseEntity.ok(response);
        } catch (RazorpayException e) {
            throw new RuntimeException(e);
        }
    }
    public static String extractStatusFromString(String orderString) {
        int jsonStartIndex = orderString.indexOf("{");
        if (jsonStartIndex == -1) {
            throw new IllegalArgumentException("Invalid order string format: JSON part not found.");
        }
        String jsonString = orderString.substring(jsonStartIndex);
        JSONObject jsonObject = new JSONObject(jsonString);
        return jsonObject.getString("status");
    }
}