package com.project.contact;

import java.io.File;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid; // Required for @Valid annotation
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult; // Required for BindingResult
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // Required for RedirectAttributes
import org.springframework.data.domain.Pageable;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	
	// Method for adding common data to response (e.g., current user info)
	@ModelAttribute
	public void addCommonData(Model model,Principal principal) {
		String userName = principal.getName();
		System.out.println("USERNAME: " + userName); // Changed print statement for clarity
		
		// Get the user using email (assuming username is email)
		User user = userRepository.getUserByUserName(userName);
		
		System.out.println("USER: " + user); // Changed print statement for clarity
		model.addAttribute("user",user);
	}

	// Handler for user dashboard
	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal) {
		model.addAttribute("title","User Dashboard");
		return "normal/user_dashboard";
	}
	
	// Handler to open the add contact form
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		model.addAttribute("title","Add Contact");
		// Ensure a new Contact object is always added to the model for a fresh form
		model.addAttribute("contact",new Contact());
		return "normal/add_contact_form";
	}
	
	//show contacts handler
	// per page = 5[n]
	//current page = 0 [page]
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page,Model m,Principal principal) {
		m.addAttribute("title","Show User Contacts");
		
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		Pageable pageable = PageRequest.of(page, 5);
		
		Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(),pageable);
		
		m.addAttribute("contacts",contacts);
		m.addAttribute("currentPage",page);
		m.addAttribute("totalPages",contacts.getTotalPages());
		
		
		return "normal/show_contacts";
		
	}
	
	// Showing particular contact details
	
	@RequestMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId") Integer cId, Model model, Principal principal) {
		
		System.out.println("CID"+cId);
		Optional<Contact> contactOptional =this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();
		
		
		
		//
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		if(user.getId()==contact.getUser().getId()) {
			
			model.addAttribute("contact",contact);
			model.addAttribute("title",contact.getName());

		}
		
		
		return "normal/contact_detail";
		
	}
	
	// Delete contact handler
	
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cId,Model model,HttpSession session) {
		
		
		
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		
		
		System.out.println("CID"+cId);

		
		Contact contact = contactOptional.get();
		
		//Check...Assignment...
		System.out.println("Contact"+contact.getCId());
		
		contact.setUser(null);
		
		this.contactRepository.delete(contact);
		
		
		System.out.println("DELETED");
		session.setAttribute("message", new Message ("Contact deleted successfully...","success"));
		
		
		return "redirect:/user/show-contacts/0";
	}
	
	
	
	
	// Handler for processing the add contact form submission
	@PostMapping("/process-contact")
	public String processContact(
	    @Valid @ModelAttribute("contact") Contact contact, 
	    BindingResult result, 
	    @RequestParam("profileImage") MultipartFile file,
	    Principal principal,
	    RedirectAttributes redirectAttributes, 
	    Model model) { 
		
		try {
		   
		    if (result.hasErrors()) {
		        System.out.println("Validation Errors: " + result.toString());
		        model.addAttribute("title", "Add Contact");
		        model.addAttribute("message", new Message("Please correct the errors in the form.", "danger"));
		      
		        return "normal/add_contact_form"; 
		    }

            // 2. Process File Upload (if validation passes)
		    String name = principal.getName();
			User user = this.userRepository.getUserByUserName(name);
			
			if(file.isEmpty()) {
				System.out.println("File is empty, using default image.");
				// Set a default image if no file is uploaded. Ensure 'default.png' exists in static/img.
				contact.setImage("default.png"); 
			} else {
				contact.setImage(file.getOriginalFilename());
				
				// Resolve the path to the static/img directory dynamically
				File uploadDir = new ClassPathResource("static/img").getFile();
				// Create the directory if it doesn't exist
				if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }
                Path path = Paths.get(uploadDir.getAbsolutePath() + File.separator + file.getOriginalFilename());
                
                // Copy the file to the target location
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                
                System.out.println("Image is uploaded successfully: " + file.getOriginalFilename());
            }
			
            // 3. Associate Contact with User and Save
			user.getContacts().add(contact);
			contact.setUser(user); // Set the user for the contact

			this.userRepository.save(user); // Save the user (which cascades to contacts)
			
			System.out.println("Contact Data: " + contact); // Log saved contact data
			System.out.println("Contact added to database successfully.");
			
			// 4. Set Success Message (using RedirectAttributes for one-time display)
			redirectAttributes.addFlashAttribute("message", new Message("Your contact is added successfully!", "success"));
			
		} catch(Exception e) {
			System.out.println("ERROR adding contact: " + e.getMessage()); // Log error message
			e.printStackTrace();
			// 5. Set Error Message (using RedirectAttributes for one-time display)
			redirectAttributes.addFlashAttribute("message", new Message("Something went wrong: " + e.getMessage(), "danger"));
		}
		
		// Always redirect after processing a form submission (success or general error)
		return "redirect:/user/add-contact"; 
	}
}