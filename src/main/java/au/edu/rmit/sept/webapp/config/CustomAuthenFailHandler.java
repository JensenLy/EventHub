package au.edu.rmit.sept.webapp.config;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAuthenFailHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {
        
        String errorMessage;

        if (exception instanceof BadCredentialsException) {
            errorMessage = "Invalid email or password. Please try again.";
        } 
        else if (exception instanceof UsernameNotFoundException) {
            //  if the message contains "banned" or "suspended"
            String exceptionMessage = exception.getMessage();
            if (exceptionMessage.contains("banned")) {
                errorMessage = "Your account has been banned. Please contact support.";
            } else if (exceptionMessage.contains("suspended")) {
                errorMessage = "Your account has been suspended. Please contact support.";
            } else {
                errorMessage = "Account not found. Please check your email or sign up.";
            }
        } else {
            errorMessage = "Authentication failed. Please try again.";
        }

        // URL encode the error message
        String encodedMessage = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);
        
        // error
        response.sendRedirect("/login?error=true&message=" + encodedMessage);
    }
}