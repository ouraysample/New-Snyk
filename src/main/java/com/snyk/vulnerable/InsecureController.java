
package com.snyk.vulnerable;

import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.Random;
import java.sql.*;

@RestController
public class InsecureController {

    private final String SECRET = "hardcoded_secret";

    @GetMapping("/insecure-hash")
    public String hashPassword(@RequestParam String password) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5"); // Weak hash
        byte[] hash = md.digest(password.getBytes("UTF-8"));
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    @GetMapping("/cmd")
    public void runCmd(@RequestParam String cmd, HttpServletResponse response) throws IOException {
        Process process = Runtime.getRuntime().exec(cmd); // Command injection
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            response.getWriter().println(line);
        }
    }

    @GetMapping("/read")
    public String readFile(@RequestParam String filename) throws IOException {
        File file = new File("/tmp/" + filename); // Path traversal
        BufferedReader reader = new BufferedReader(new FileReader(file));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

    @GetMapping("/redirect")
    public void redirect(@RequestParam String url, HttpServletResponse response) throws IOException {
        response.sendRedirect(url); // Open redirect
    }

    @PostMapping("/sql")
    public String sqlInjection(@RequestParam String user) throws Exception {
        Connection conn = DriverManager.getConnection("jdbc:sqlite:test.db");
        Statement stmt = conn.createStatement();
        String query = "SELECT * FROM users WHERE username = '" + user + "'"; // SQL Injection
        ResultSet rs = stmt.executeQuery(query);
        if (rs.next()) return "Found user";
        return "User not found";
    }

    @GetMapping("/random")
    public int generateOTP() {
        Random rand = new Random(); // Insecure Random
        return rand.nextInt(100000);
    }
}
