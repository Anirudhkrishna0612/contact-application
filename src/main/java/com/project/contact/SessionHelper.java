package com.project.contact; // Ensure this package matches your Message.java and other core classes
 
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
 
@Component // This annotation marks it as a Spring-managed component (bean)
public class SessionHelper {
 
    // This method is designed to be called from your Thymeleaf template
    // to remove the 'message' attribute from the session after it has been displayed.
    public void removeMessageFromSession() {
        try {
            // Get the current HTTP session
            HttpSession session = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getSession();
 
            // Remove the "message" attribute from the session
            session.removeAttribute("message");
        } catch (IllegalStateException e) {
            // This can happen if the request attributes are no longer available (e.g., after response is committed)
            // or if it's called from a non-web context.
            System.err.println("Session not available or already invalidated: " + e.getMessage());
        } catch (NullPointerException e) {
            // This can happen if RequestContextHolder.getRequestAttributes() returns null
            System.err.println("RequestContextHolder attributes are null. Not in a web request context: " + e.getMessage());
        }
    }
}
