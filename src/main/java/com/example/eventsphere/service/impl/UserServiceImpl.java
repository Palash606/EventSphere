package com.example.eventsphere.service.impl;

import com.example.eventsphere.constants.EventManagementSystemConstants;
import com.example.eventsphere.dto.UserDto;
import com.example.eventsphere.dto.mapper.UserMapper;
import com.example.eventsphere.exception.ResourceNotFoundException;
import com.example.eventsphere.model.Event;
import com.example.eventsphere.model.Role;
import com.example.eventsphere.model.User;
import com.example.eventsphere.repository.RoleRepository;
import com.example.eventsphere.repository.UserRepository;
import com.example.eventsphere.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private UserRepository userRepository;
    private RoleRepository roleRepository;

    private PasswordEncoder passwordEncoder;

    /**
     * Save user into db
     *
     * @param user
     * @return boolean
     */
    @Override
    public boolean createUser(User user) {

        Optional<User> optionalUser = userRepository.findByEmail(user.getEmail());
        if (optionalUser.isPresent()) {
            // throw new DataAlreadyExistsException("User already exists with given email "
            // + userDto.getEmail());
            return false;
        }
        // todo send comm
        Role userRole = roleRepository.getByRoleName(EventManagementSystemConstants.USER_ROLE);
        // Role adminRole =
        // roleRepository.getByRoleName(EventManagementSystemConstants.ADMIN_ROLE);
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        // roles.add(adminRole);
        user.setRoles(roles);
        user.setPwd(passwordEncoder.encode(user.getPwd()));

        User savedUser = userRepository.save(user);

        return true;
    }

    /**
     * Fetch User by email
     *
     * @param email
     * @return UserDto
     */
    @Override
    public UserDto fetchUser(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        UserDto userDto = UserMapper.mapToUserDto(user, new UserDto());

        return userDto;
    }

    /**
     * Update user
     *
     * @param userDto
     * @return boolean
     */
    @Override
    public boolean updateUser(UserDto userDto) {

        User user = userRepository.findByEmail(userDto.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userDto.getEmail()));
        UserMapper.mapToUser(userDto, user);

        userRepository.save(user);

        return true;
    }

    /**
     * Delete user by email
     *
     * @param email
     * @return boolean
     */
    @Override
    public boolean deleteUser(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        userRepository.delete(user);

        return true;
    }

    /**
     * Check user is admin or not
     *
     * @return boolean
     */
    @Override
    public boolean isAdmin() {

        User user = userRepository.readByEmail(SecurityContextHolder.getContext().getAuthentication().getName());

        for (Role role : userRepository.findRolesByUserId(user.getUserId())) {
            if (role.getRoleName().equals(EventManagementSystemConstants.ADMIN_ROLE))
                return true;
        }

        return false;
    }

    /**
     * Fetch Optional user for null check by email
     *
     * @param email
     * @return Optional Email
     */
    @Override
    public Optional<User> readUser(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Update user
     *
     * @param user
     */
    @Override
    public void updateUser(User user) {
        userRepository.save(user);
    }

    /**
     * Find events that participated by userId
     *
     * @param userId
     * @return Event List
     */
    @Override
    public List<Event> findEventsByUserId(int userId) {
        return userRepository.findEventsByUserId(userId);
    }

}