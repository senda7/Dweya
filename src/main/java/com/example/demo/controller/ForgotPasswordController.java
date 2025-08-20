package com.example.demo.controller;

import com.example.demo.model.Utilisateur;
import com.example.demo.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.mail.internet.MimeMessage;

import java.io.File;
import java.nio.file.Paths;

@Controller
@SessionAttributes("otp")
public class ForgotPasswordController {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private JavaMailSender mailSender;

    @GetMapping("/forgot-password")
    public String showEmailForm() {
        return "forgot-password-email";
    }

    @PostMapping("/forgot-password")
    public String processEmail(@RequestParam String email, Model model) {
        Utilisateur user = utilisateurRepository.findByEmail(email);
        if (user == null) {
            model.addAttribute("error", "Email non trouvé.");
            return "forgot-password-email";
        }

        int otp = (int) (Math.random() * 900000) + 100000;

        model.addAttribute("otp", otp);
        model.addAttribute("email", email);
        model.addAttribute("message", "Un code de réinitialisation a été envoyé à votre email.");

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(email);
            helper.setSubject("Code de réinitialisation Dwaya");

            String htmlContent = "<html>" +
                    "<body>" +
                    "<div style='text-align: center; font-family: Arial, sans-serif;'>" +
                    "<img src='cid:logoImage' alt='Logo Dwaya' style='width: 150px; margin-bottom: 20px; animation: float 2s infinite; display: block; margin-left: auto; margin-right: auto;'/>" +
                    "<h2 style='color: #264653;'>Code de réinitialisation</h2>" +
                    "<p>Bonjour,</p>" +
                    "<p>Voici votre code pour réinitialiser votre mot de passe :</p>" +
                    "<h1 style='color: #2a9d8f; font-size: 32px;'>" + otp + "</h1>" +
                    "<p>Ce code est valable pendant quelques minutes.</p>" +
                    "<br/>" +
                    "<p style='font-size: 14px; color: gray;'>Merci d'utiliser l'application <strong>Dwaya</strong>.</p>" +
                    "</div>" +
                    "</body>" +
                    "</html>";

            helper.setText(htmlContent, true);

            // Chemin absolu dynamique vers le logo
            File logo = Paths.get("src", "main", "resources", "static", "logo.png").toFile();
            if (logo.exists()) {
                helper.addInline("logoImage", logo);
            } else {
                System.out.println(" Logo introuvable à : " + logo.getAbsolutePath());
            }

            mailSender.send(message);

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Une erreur est survenue lors de l'envoi de l'email.");
            return "forgot-password-email";
        }

        return "forgot-password-otp";
    }

    @PostMapping("/forgot-password/verify")
    public String verifyOtpAndReset(@RequestParam String email,
                                    @RequestParam int otpInput,
                                    @RequestParam String newPassword,
                                    @ModelAttribute("otp") int otpSession,
                                    Model model) {
        if (otpInput != otpSession) {
            model.addAttribute("error", "Code incorrect.");
            model.addAttribute("email", email);
            return "forgot-password-otp";
        }

        Utilisateur user = utilisateurRepository.findByEmail(email);
        if (user == null) {
            model.addAttribute("error", "Utilisateur non trouvé.");
            return "forgot-password-email";
        }

        user.setMotDePasse(newPassword);
        utilisateurRepository.save(user);


        model.addAttribute("utilisateur", new Utilisateur());
        model.addAttribute("message", "Mot de passe modifié avec succès, veuillez vous connecter.");
        return "login";
    }
    // ✅ Ajouter dans ton ForgotPasswordController

    @GetMapping("/resendCode1")
    public String resendOtp(@RequestParam String email, Model model) {
        Utilisateur user = utilisateurRepository.findByEmail(email);
        if (user == null) {
            model.addAttribute("error", "Utilisateur non trouvé.");
            return "forgot-password-email";
        }

        int otp = (int) (Math.random() * 900000) + 100000;

        model.addAttribute("otp", otp);
        model.addAttribute("email", email);
        model.addAttribute("message", "Un nouveau code a été renvoyé à votre email.");

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(email);
            helper.setSubject("Nouveau code de réinitialisation Dwaya");

            String htmlContent = "<html>" +
                    "<body>" +
                    "<div style='text-align: center; font-family: Arial, sans-serif;'>" +
                    "<img src='cid:logoImage' alt='Logo Dwaya' style='width: 150px; margin-bottom: 20px;'/>" +
                    "<h2 style='color: #264653;'>Nouveau code de réinitialisation</h2>" +
                    "<p>Bonjour,</p>" +
                    "<p>Voici votre nouveau code pour réinitialiser votre mot de passe :</p>" +
                    "<h1 style='color: #2a9d8f; font-size: 32px;'>" + otp + "</h1>" +
                    "<p>Ce code est valable pendant quelques minutes.</p>" +
                    "<br/>" +
                    "<p style='font-size: 14px; color: gray;'>Merci d'utiliser l'application <strong>Dwaya</strong>.</p>" +
                    "</div>" +
                    "</body>" +
                    "</html>";

            helper.setText(htmlContent, true);

            // Logo
            File logo = Paths.get("src", "main", "resources", "static", "logo.png").toFile();
            if (logo.exists()) {
                helper.addInline("logoImage", logo);
            }

            mailSender.send(message);

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Erreur lors de l'envoi de l'email.");
            return "forgot-password-email";
        }

        return "forgot-password-otp";
    }

}
