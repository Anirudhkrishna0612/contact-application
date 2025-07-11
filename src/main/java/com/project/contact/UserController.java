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
import org.springframework.web.bind.annotation.RequestMethod;
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
	public String deleteContact(@PathVariable("cid") Integer cId, Model model, HttpSession session, Principal principal) {
	    try {
	        // Find the contact by ID
	        Optional<Contact> contactOptional = this.contactRepository.findById(cId);

	        // If contact not found, redirect with an error message
	        if (!contactOptional.isPresent()) {
	            session.setAttribute("message", new Message("Contact not found.", "danger"));
	            return "redirect:/user/show-contacts/0";
	        }

	        Contact contact = contactOptional.get();

	        // Get the current logged-in user
	        String userName = principal.getName();
	        User user = this.userRepository.getUserByUserName(userName);

	        // CRITICAL SECURITY CHECK: Ensure the contact belongs to the logged-in user
	        if (user.getId() != contact.getUser().getId()) {
	            session.setAttribute("message", new Message("You are not authorized to delete this contact.", "danger"));
	            return "redirect:/user/show-contacts/0";
	        }

	        // --- Database Deletion Logic ---

	        // 1. (Optional but good practice for consistency): Remove the contact from the user's collection in memory.
	        // This ensures the in-memory 'user' object is consistent with the impending database state.
	        // If your User entity has cascade = CascadeType.REMOVE or orphanRemoval = true,
	        // this step followed by userRepository.save(user) might be enough.
	        // However, for guaranteed explicit deletion, we'll also use contactRepository.delete(contact).
	        user.getContacts().remove(contact);
	        this.userRepository.save(user); // Save the user to update its collection

	        // 2. Explicitly delete the contact from the database.
	        // This is the direct command to remove the Contact record.
	        this.contactRepository.delete(contact);
	        // --- End Database Deletion Logic ---


	        // Optional: Delete the profile image from the server if it's not the default
	        if (contact.getImage() != null && !contact.getImage().equals("default.png")) {
	            try {
	                File imageFile = new ClassPathResource("static/img").getFile();
	                File fileToDelete = new File(imageFile, contact.getImage());
	                if (fileToDelete.exists()) {
	                    fileToDelete.delete();
	                    System.out.println("Deleted contact image from file system: " + contact.getImage());
	                }
	            } catch (Exception e) {
	                System.out.println("Error deleting image file for contact " + cId + ": " + e.getMessage());
	                e.printStackTrace();
	            }
	        }

	        System.out.println("Contact with ID " + cId + " and its image DELETED successfully.");
	        session.setAttribute("message", new Message("Contact deleted successfully...", "success"));

	    } catch (Exception e) {
	        System.out.println("ERROR deleting contact with ID " + cId + ": " + e.getMessage());
	        e.printStackTrace();
	        session.setAttribute("message", new Message("Something went wrong during deletion: " + e.getMessage(), "danger"));
	    }

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
	
	
	//Open Update form
	
	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cid,Model m) {
		
		m.addAttribute("title","Update Contact");
		
		Contact contact = this.contactRepository.findById(cid).get();
		
		m.addAttribute("contact", contact);
		
		return "normal/update_form";
	}
	
	// Update Contact Handler
	
	@RequestMapping(value = "/process-update",method = RequestMethod.POST)
	public String updateHandler(@ModelAttribute Contact contact,@RequestParam("profileImage")MultipartFile file,Model m,HttpSession session,Principal principal) {
		
		try {
			
			//old contact details 
			Contact oldcontactDetail = this.contactRepository.findById(contact.getCId()).get();
			
			
			//image...
			if(!file.isEmpty()) {
				
				//file works...
				//rewrite
//				delete old photo
				
				File deleteFile = new ClassPathResource("static/img").getFile();
				File file1 = new File(deleteFile, oldcontactDetail.getImage());
				file1.delete();
				
				
//              update new photo	
				
				
				File uploadDir = new ClassPathResource("static/img").getFile();
				// Create the directory if it doesn't exist
				if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }
                Path path = Paths.get(uploadDir.getAbsolutePath() + File.separator + file.getOriginalFilename());
                
                // Copy the file to the target location
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				
                contact.setImage(file.getOriginalFilename());
                
                
				
			}else {
				
				contact.setImage(oldcontactDetail.getImage());
				
			}
			User user = this.userRepository.getUserByUserName(principal.getName());
			
			contact.setUser(user);
			
			this.contactRepository.save(contact);
			
			session.setAttribute("message", new Message("Your Contact is updated...","success"));
			
			
			
		}catch (Exception e) {
			 
			e.printStackTrace();
			
		}
		
		
		System.out.println("CONTACT NAME "+contact.getName());
		System.out.println("CONTACT ID "+contact.getCId());
		return "redirect:/user/"+contact.getCId()+"/contact";
	}
	
	
	//Your profile handler
	@GetMapping("/profile")
	public String yourProfile(Model model) {
		
		model.addAttribute("title", "Profile Page");
		return "normal/profile";
		
	}
	
	
	
	
	
	
}