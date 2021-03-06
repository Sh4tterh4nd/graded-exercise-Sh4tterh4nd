package com.lydbook.audiobook.services;

import com.lydbook.audiobook.dao.DAOPassword;
import com.lydbook.audiobook.entity.User;
import com.lydbook.audiobook.entity.UserRole;
import com.lydbook.audiobook.repository.tag.TagRepository;
import com.lydbook.audiobook.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

/**
 * User service.
 */
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TagRepository tagRepository;
    private Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Instantiates a new User service.
     *
     * @param userRepository  the user repository
     * @param passwordEncoder the password encoder
     * @param tagRepository   the tag repository
     */
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, TagRepository tagRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tagRepository = tagRepository;
    }

    /**
     * Create a new user.
     *
     * @param user the user
     */
    @Transactional
    public void createUser(User user) {
        user.setRole(UserRole.USER);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.addTag(tagRepository.findOrCreateFirstByName("Audiobook", false));
        log.info("User {} created.", user.getUsername());
        userRepository.save(user);
    }

    /**
     * Delete user by id
     *
     * @param id the id
     */
    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteUserById(id);
        log.info("User with the id: {} has been deleted", id);
    }

    /**
     * Get user by id
     *
     * @param id the id
     * @return the user
     */
    public User getUser(Long id) {
        User user = userRepository.findUserById(id);
        user.setPassword("");
        user.getProgressList().clear();
        return user;
    }

    /**
     * Update user
     *
     * @param updatedUser the updated user
     */
    @Transactional
    public void updateUser(User updatedUser) {
        User user = userRepository.findUserById(updatedUser.getId());
        if (!updatedUser.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }
        user.getTags().clear();
        updatedUser.getTags().forEach(t -> user.addTag(t));
        log.info("Tags or Password of User {} have been updated by an admin", user.getUsername());
        userRepository.save(user);
    }

    /**
     * Update password of certain user
     *
     * @param username the username
     * @param password the password
     * @return the boolean
     */
    public boolean updatePassword(String username, DAOPassword password) {
        User user = userRepository.findUserByUsername(username);
        if (passwordEncoder.matches(password.getCurrentPassword(), user.getPassword())) {
            user.setPassword(passwordEncoder.encode(password.getNewPassword()));
            userRepository.save(user);
            log.info("Password of user {} has been successfully updated.", user.getUsername());
            return true;
        }
        log.info("Update password of user {} has failed.", user.getUsername());
        return false;
    }
}
