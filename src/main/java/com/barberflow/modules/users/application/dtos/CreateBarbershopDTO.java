package com.barberflow.modules.users.application.dtos;


import com.barberflow.modules.users.domain.entities.SubscriptionPlan;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateBarbershopDTO {

    @NotBlank(message = "El NIT es obligatorio")
    @Size(min = 5, max = 20, message = "El NIT debe tener entre 5 y 20 caracteres")
    private String nit;

    @NotBlank(message = "El nombre de la empresa es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String businessName;

    @NotBlank(message = "La dirección es obligatoria")
    private String address;

    @NotBlank(message = "El teléfono es obligatorio")
    private String phone;

    // Spring deserializa automáticamente el valor del JSON ("FREE", "PREMIUM", "ENTERPRISE") a este Enum
    private SubscriptionPlan subscriptionPlan;
    
}