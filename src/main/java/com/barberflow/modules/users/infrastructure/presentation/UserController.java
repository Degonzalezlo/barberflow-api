package com.barberflow.modules.users.infrastructure.presentation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.barberflow.modules.users.application.dtos.UserRegistrationDTO;
import com.barberflow.modules.users.application.services.UserService;
import com.barberflow.modules.users.domain.entities.User;


@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    // POST: http://localhost:8080/api/users/register?barbershopId=1
   @PostMapping("/register")
public ResponseEntity<?> register(@RequestBody UserRegistrationDTO dto) {
    try {
        // Mapeamos del DTO a la Entidad
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        user.setRole(dto.getRole());
        user.setBarbershop(null);

        User savedUser = userService.registerUser(user, dto.getBarbershopId());
        
        // Limpiamos datos sensibles antes de responder
        savedUser.setPassword(null); 
        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    } catch (RuntimeException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
}

}
   