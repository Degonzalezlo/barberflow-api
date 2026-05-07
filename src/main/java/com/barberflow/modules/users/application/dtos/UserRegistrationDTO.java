package com.barberflow.modules.users.application.dtos;

import com.barberflow.modules.users.domain.entities.Barbershop;
import com.barberflow.modules.users.domain.entities.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationDTO {
    
   
    private String email;
    private String password;
    private UserRole role; // ADMIN, BARBER, CLIENT

    private String name; // Solo para CLIENT y BARBER, null para ADMIN
    private String phone; // Solo para CLIENT y BARBER
    private Long barbershopId; // Solo para BARBER y ADMIN, null para CLIENT

}

