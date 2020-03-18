package org.bmsource.dwh.security;

import org.bmsource.dwh.security.model.User;
import org.bmsource.dwh.security.model.UserDto;
import org.bmsource.dwh.security.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
public class SecurityController {

    @Autowired
    UserRepository userRepository;

    @RequestMapping(value = "/me", method = RequestMethod.GET)
    @ResponseBody
    public UserDto currentUserName(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByUsername(auth.getName());
        return user.toUserDto();
    }
}
