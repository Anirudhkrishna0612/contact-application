package com.project.contact;
 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service; // Add @Service annotation
 
// Assuming you have a CustomUserDetails class
// Assuming you have a UserRepository interface that extends JpaRepository or similar
 
@Service // It's good practice to mark this as a Spring service
public class UserDetailsServiceImpl implements UserDetailsService{
 
    @Autowired
    private UserRepository userRepository;
 
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //fetching user from database
        User user = userRepository.getUserByUserName(username); // Make sure this method exists in UserRepository
 
        if(user == null) { // Use '==' for null check
            throw new UsernameNotFoundException("Could not find user with username: " + username);
        }
        // Assuming CustomUserDetails correctly implements UserDetails and takes a User object
        CustomUserDetails customUserDetails = new CustomUserDetails(user);
        return customUserDetails;
    }
}