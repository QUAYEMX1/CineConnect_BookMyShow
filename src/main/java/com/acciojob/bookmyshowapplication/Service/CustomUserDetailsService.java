package com.acciojob.bookmyshowapplication.Service;

import com.acciojob.bookmyshowapplication.Models.User;
import com.acciojob.bookmyshowapplication.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String mobNo) throws UsernameNotFoundException {
        User user1 = userRepository.findUserByMobNo(mobNo);

        if (user1 == null) {
            throw new UsernameNotFoundException("User not found");
        }

      UserDetails userDetails = org.springframework.security.core.userdetails.User.withUsername(user1.getMobNo())
              .password(user1.getPassword())
              .roles(user1.getRole())  //USER, ADMIN
              .build();

        return userDetails;
    }
}
