package com.jpmc.midascore.controller;

import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Balance;
import com.jpmc.midascore.repository.UserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController()
public class ApiController {
    private final UserRepository userRepository;

    public ApiController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/balance")
    public Balance balance(@RequestParam("userId")Long userid) {
        boolean userExists = userRepository.findById(userid).isPresent();
        Balance balance;
        if(userExists) {
            UserRecord user = userRepository.findById(userid).get();
            balance = new Balance(user.getBalance());
        }else{
            balance = new Balance(0);
        }
        return balance;
    }
}
