package com.barberflow.modules.auth.services;

import com.barberflow.modules.auth.dto.AuthResponse;
import com.barberflow.modules.auth.dto.LoginRequest;
import com.barberflow.modules.users.application.dtos.UserRegistrationDTO;
import com.barberflow.modules.users.domain.entities.Barber;
import com.barberflow.modules.users.domain.entities.Barbershop;
import com.barberflow.modules.users.domain.entities.Client;
import com.barberflow.modules.users.domain.entities.User;
import com.barberflow.modules.users.domain.entities.UserRole;
import com.barberflow.modules.users.domain.repositories.IBarberRepository;
import com.barberflow.modules.users.domain.repositories.IBarbershopRepository;
import com.barberflow.modules.users.domain.repositories.IClientRepository;
import com.barberflow.modules.users.domain.repositories.IUserRepository;
import com.barberflow.modules.users.infrastructure.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private IUserRepository userRepository;
    @Mock
    private IBarberRepository barberRepository;
    @Mock
    private IClientRepository clientRepository;
    @Mock
    private IBarbershopRepository barbershopRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private UserRegistrationDTO baseRegistrationDto;
    private Barbershop sampleBarbershop;

    @BeforeEach
    void setUp() {
        sampleBarbershop = new Barbershop();
        sampleBarbershop.setBarbershopId(1L);
        sampleBarbershop.setBusinessName("Barberia Central");

        baseRegistrationDto = new UserRegistrationDTO();
        baseRegistrationDto.setEmail("test@barberflow.com");
        baseRegistrationDto.setPassword("password123");
        baseRegistrationDto.setName("Carlos Perez");
        baseRegistrationDto.setPhone("3001234567");
    }

    @Nested
    @DisplayName("Pruebas de Registro (register)")
    class RegisterTests {

        @Test
        @DisplayName("Debe lanzar excepción si el email ya existe")
        void register_ShouldThrowException_WhenEmailAlreadyExists() {
            baseRegistrationDto.setRole(UserRole.CLIENT);
            when(userRepository.findByEmail(baseRegistrationDto.getEmail()))
                    .thenReturn(Optional.of(new User()));

            RuntimeException exception = assertThrows(RuntimeException.class, () ->
                    authService.register(baseRegistrationDto));

            assertEquals("Email ya registrado", exception.getMessage());
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Debe registrar exitosamente un usuario con rol CLIENT")
        void register_ShouldRegisterClient_Successfully() {
            baseRegistrationDto.setRole(UserRole.CLIENT);

            when(userRepository.findByEmail(baseRegistrationDto.getEmail())).thenReturn(Optional.empty());
            when(passwordEncoder.encode(baseRegistrationDto.getPassword())).thenReturn("encodedPassword");
            when(jwtService.createToken(any(User.class), eq(null))).thenReturn("mocked-jwt-token");

            AuthResponse response = authService.register(baseRegistrationDto);

            assertNotNull(response);
            assertEquals("mocked-jwt-token", response.getToken());

            verify(userRepository, times(2)).save(any(User.class)); // 1 en switch, 1 en registerClient
            verify(clientRepository, times(1)).save(any(Client.class));
            verify(barberRepository, never()).save(any(Barber.class));
        }

        @Test
        @DisplayName("Debe lanzar excepción si STAFF (BARBER/ADMIN) no envía barbershopId")
        void register_ShouldThrowException_WhenStaffHasNoBarbershopId() {
            baseRegistrationDto.setRole(UserRole.BARBER);
            baseRegistrationDto.setBarbershopId(null);

            when(userRepository.findByEmail(baseRegistrationDto.getEmail())).thenReturn(Optional.empty());
            when(passwordEncoder.encode(baseRegistrationDto.getPassword())).thenReturn("encodedPassword");

            RuntimeException exception = assertThrows(RuntimeException.class, () ->
                    authService.register(baseRegistrationDto));

            assertEquals("El personal debe estar asociado a una barbería", exception.getMessage());
        }

        @Test
        @DisplayName("Debe lanzar excepción si la barbería no existe para STAFF")
        void register_ShouldThrowException_WhenBarbershopNotFound() {
            baseRegistrationDto.setRole(UserRole.BARBER);
            baseRegistrationDto.setBarbershopId(99L);

            when(userRepository.findByEmail(baseRegistrationDto.getEmail())).thenReturn(Optional.empty());
            when(passwordEncoder.encode(baseRegistrationDto.getPassword())).thenReturn("encodedPassword");
            when(barbershopRepository.findById(99L)).thenReturn(Optional.empty());

            RuntimeException exception = assertThrows(RuntimeException.class, () ->
                    authService.register(baseRegistrationDto));

            assertEquals("Barbería no encontrada", exception.getMessage());
        }

        @Test
        @DisplayName("Debe lanzar excepción si se registra un ADMIN y la barbería ya tiene uno")
        void register_ShouldThrowException_WhenAdminAlreadyExistsForBarbershop() {
            baseRegistrationDto.setRole(UserRole.ADMIN);
            baseRegistrationDto.setBarbershopId(1L);

            when(userRepository.findByEmail(baseRegistrationDto.getEmail())).thenReturn(Optional.empty());
            when(passwordEncoder.encode(baseRegistrationDto.getPassword())).thenReturn("encodedPassword");
            when(barbershopRepository.findById(1L)).thenReturn(Optional.of(sampleBarbershop));
            when(userRepository.existsByBarbershopAndRole(sampleBarbershop, UserRole.ADMIN)).thenReturn(true);

            RuntimeException exception = assertThrows(RuntimeException.class, () ->
                    authService.register(baseRegistrationDto));

            assertEquals("Esta barbería ya tiene un administrador asignado.", exception.getMessage());
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Debe registrar exitosamente un usuario con rol BARBER")
        void register_ShouldRegisterBarber_Successfully() {
            baseRegistrationDto.setRole(UserRole.BARBER);
            baseRegistrationDto.setBarbershopId(1L);

            when(userRepository.findByEmail(baseRegistrationDto.getEmail())).thenReturn(Optional.empty());
            when(passwordEncoder.encode(baseRegistrationDto.getPassword())).thenReturn("encodedPassword");
            when(barbershopRepository.findById(1L)).thenReturn(Optional.of(sampleBarbershop));
            when(jwtService.createToken(any(User.class), eq(1L))).thenReturn("mocked-barber-jwt");

            AuthResponse response = authService.register(baseRegistrationDto);

            assertNotNull(response);
            assertEquals("mocked-barber-jwt", response.getToken());

            verify(userRepository, times(1)).save(any(User.class));
            verify(barberRepository, times(1)).save(any(Barber.class));
            verify(clientRepository, never()).save(any(Client.class));
        }

        @Test
        @DisplayName("Debe registrar exitosamente un usuario con rol ADMIN")
        void register_ShouldRegisterAdmin_Successfully() {
            baseRegistrationDto.setRole(UserRole.ADMIN);
            baseRegistrationDto.setBarbershopId(1L);

            when(userRepository.findByEmail(baseRegistrationDto.getEmail())).thenReturn(Optional.empty());
            when(passwordEncoder.encode(baseRegistrationDto.getPassword())).thenReturn("encodedPassword");
            when(barbershopRepository.findById(1L)).thenReturn(Optional.of(sampleBarbershop));
            when(userRepository.existsByBarbershopAndRole(sampleBarbershop, UserRole.ADMIN)).thenReturn(false);
            when(jwtService.createToken(any(User.class), eq(1L))).thenReturn("mocked-admin-jwt");

            AuthResponse response = authService.register(baseRegistrationDto);

            assertNotNull(response);
            assertEquals("mocked-admin-jwt", response.getToken());

            verify(userRepository, times(1)).save(any(User.class));
            verify(barberRepository, never()).save(any(Barber.class));
        }
    }

    @Nested
    @DisplayName("Pruebas de Login (login)")
    class LoginTests {

        @Test
        @DisplayName("Debe autenticar y retornar token correctamente al hacer login")
        void login_ShouldReturnToken_WhenCredentialsAreValid() {
            LoginRequest loginRequest = new LoginRequest("test@barberflow.com", "password123");

            User mockUser = new User();
            mockUser.setEmail(loginRequest.getEmail());
            mockUser.setBarbershop(sampleBarbershop);

            when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(mockUser));
            when(jwtService.createToken(mockUser, 1L)).thenReturn("mocked-login-token");

            AuthResponse response = authService.login(loginRequest);

            assertNotNull(response);
            assertEquals("mocked-login-token", response.getToken());

            verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(jwtService, times(1)).createToken(mockUser, 1L);
        }

        @Test
        @DisplayName("Debe lanzar excepción en login si el usuario no existe")
        void login_ShouldThrowException_WhenUserNotFound() {
            LoginRequest loginRequest = new LoginRequest("unknown@barberflow.com", "password123");

            when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

            RuntimeException exception = assertThrows(RuntimeException.class, () ->
                    authService.login(loginRequest));

            assertEquals("Usuario no encontrado", exception.getMessage());
            verify(jwtService, never()).createToken(any(), any());
        }
    }
}
