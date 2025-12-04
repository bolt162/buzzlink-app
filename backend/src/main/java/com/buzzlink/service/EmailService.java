package com.buzzlink.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Email service for sending invitations
 * Uses Spring Mail with SMTP
 */
@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@buzzlink.com}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    /**
     * Send workspace invitation email
     */
    public void sendWorkspaceInvitation(
        String toEmail,
        String workspaceName,
        String inviterName,
        String invitationToken
    ) {
        String subject = String.format("You've been invited to join %s on BuzzLink", workspaceName);
        String message = buildInvitationEmail(workspaceName, inviterName, invitationToken);

        // Log to console for debugging
        System.out.println("=== EMAIL INVITATION ===");
        System.out.println("To: " + toEmail);
        System.out.println("Subject: " + subject);
        System.out.println("=======================");

        // Send actual email if mail sender is configured
        if (mailSender != null) {
            try {
                SimpleMailMessage mailMessage = new SimpleMailMessage();
                mailMessage.setFrom(fromEmail);
                mailMessage.setTo(toEmail);
                mailMessage.setSubject(subject);
                mailMessage.setText(message);

                mailSender.send(mailMessage);
                System.out.println("✓ Email sent successfully to " + toEmail);
            } catch (Exception e) {
                System.err.println("✗ Failed to send email: " + e.getMessage());
                System.err.println("  Email content logged above for reference");
            }
        } else {
            System.out.println("⚠ Mail sender not configured. Email content logged above.");
            System.out.println("  To enable emails, configure SMTP in application.properties");
        }
    }

    private String buildInvitationEmail(String workspaceName, String inviterName, String token) {
        return String.format("""
            Hi there!

            %s has invited you to join the "%s" workspace on BuzzLink.

            To accept this invitation, please sign up or log in to BuzzLink at:
            %s

            Your invitation will be automatically applied when you join with this email address.

            Invitation Token: %s
            (This is for reference only - no action needed)

            This invitation will expire in 7 days.

            Best regards,
            The BuzzLink Team
            """, inviterName, workspaceName, frontendUrl, token);
    }
}
