package com.project.contact;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HomeController {

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Autowired
	private UserRepository userrepo;

	@GetMapping("/")
	public String home(Model model) {
		model.addAttribute("title", "Home - Smart Contact Manager");
		return "home";
	}

	@GetMapping("/about")
	public String about(Model model) {
		model.addAttribute("title", "About - Smart Contact Manager");
		return "about";
	}

	// Handler for the registration page
	@GetMapping("/signup")
	public String signup(Model model) {
		model.addAttribute("title", "Register - Smart Contact Manager");
		if (!model.containsAttribute("user")) { // Ensure 'user' object exists for form binding
			model.addAttribute("user", new User());
		}
		return "signup";
	}

	@GetMapping("/login")
	public String customLogin(Model model) {
		model.addAttribute("title", "Login - Smart Contact Manager");
		return "login";
	}

	// Handler for registering user
	@PostMapping("/do_register")
	public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult result1, // BindingResult for
																								// validation errors
			@RequestParam(value = "agreement", defaultValue = "false") boolean agreement,
			RedirectAttributes redirectAttributes) {
		try {
			if (!agreement) {
				System.out.println("You have not agreed to the terms and conditions !");
				// Use addFlashAttribute to persist message after redirect
				redirectAttributes.addFlashAttribute("message",
						new Message("You have not agreed to the terms and conditions !", "danger"));
				redirectAttributes.addFlashAttribute("user", user); // Retain form data
				return "redirect:/signup";
			}

			if (result1.hasErrors()) {
				System.out.println("Validation ERROR: " + result1.toString());
				// If validation errors, return to signup page to show inline errors
				return "signup";
			}

			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));

			System.out.println("Agreement: " + agreement);
			System.out.println("USER: " + user);

			this.userrepo.save(user);

			// Set success message for the LOGIN page (using RedirectAttributes)
			redirectAttributes.addFlashAttribute("message",
					new Message("Successfully Registered! Please log in.", "success"));

			return "redirect:/login"; // Redirect to login page after successful registration

		} catch (Exception e) {
			e.printStackTrace();
			// General error message (using RedirectAttributes)
			redirectAttributes.addFlashAttribute("message",
					new Message("Something went wrong :( " + e.getMessage(), "danger"));
			redirectAttributes.addFlashAttribute("user", user); // Retain form data
			return "redirect:/signup";
		}
	}

	// NEW ADDITION: Handler for the logout confirmation page
	@GetMapping("/sure/logout")
	public String sureLogout(Model model) {
		model.addAttribute("title", "Confirm Logout");
		return "surelogout"; // This will render the surelogout.html template
	}
}