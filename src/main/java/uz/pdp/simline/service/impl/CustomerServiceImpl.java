package uz.pdp.simline.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import uz.pdp.simline.dto.request.CustomerLoginDto;
import uz.pdp.simline.dto.request.CustomerRegisterDto;
import uz.pdp.simline.dto.request.CustomerUpdateDto;
import uz.pdp.simline.dto.respone.CustomerDto;
import uz.pdp.simline.dto.respone.JwtDto;
import uz.pdp.simline.entity.Customer;
import uz.pdp.simline.exception.AlreadyExistsException;
import uz.pdp.simline.exception.InvalidArgumentException;
import uz.pdp.simline.exception.NotFoundException;
import uz.pdp.simline.exception.NullOrEmptyException;
import uz.pdp.simline.repository.CustomerRepository;
import uz.pdp.simline.service.CustomerService;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public JwtDto register(CustomerRegisterDto customerRegisterDto) {
        if (customerRegisterDto == null)
            throw new NullOrEmptyException("CustomerRegisterDto");
        if (customerRegisterDto.getUsername() == null)
            throw new NullOrEmptyException("Username");
        if (customerRegisterDto.getPassword() == null)
            throw new NullOrEmptyException("Password");
        if (customerRegisterDto.getEmail() == null && customerRegisterDto.getPhoneNumber() == null)
            throw new NullOrEmptyException("Email");
        if (customerRepository.findByUsername(customerRegisterDto.getUsername()).isPresent())
            throw new AlreadyExistsException("Username");
        if (customerRegisterDto.getEmail() != null && customerRepository.findByEmail(customerRegisterDto.getEmail()).isPresent())
            throw new AlreadyExistsException("Email");
        if (customerRegisterDto.getPhoneNumber() != null && customerRepository.findByPhoneNumber(customerRegisterDto.getPhoneNumber()).isPresent())
            throw new AlreadyExistsException("Phone number");
        customerRepository.save(
                Customer.builder()
                        .username(customerRegisterDto.getUsername())
                        .password(passwordEncoder.encode(customerRegisterDto.getPassword()))
                        .email(customerRegisterDto.getEmail())
                        .phoneNumber(customerRegisterDto.getPhoneNumber())
                        .build()
        );
        return new JwtDto("todo");
    }

    @Override
    public JwtDto login(CustomerLoginDto customerLoginDto) {
        if (customerLoginDto == null)
            throw new NullOrEmptyException("CustomerLoginDto");
        if (customerLoginDto.getUsername() == null)
            throw new NullOrEmptyException("Username");
        if (customerLoginDto.getPassword() == null)
            throw new NullOrEmptyException("Password");
        Customer customer = customerRepository.findByUsername(customerLoginDto.getUsername())
                .orElseThrow(() -> new NotFoundException("Customer"));
        if (customer.getPassword().equals(passwordEncoder.encode(customerLoginDto.getPassword()))) {
            return new JwtDto("todo");
        }
        throw new InvalidArgumentException("password");
    }

    @Override
    public CustomerDto update(CustomerUpdateDto customerDto) {
        if (customerDto == null)
            throw new NullOrEmptyException("CustomerDto");
        if (customerDto.getId() == null)
            throw new NullOrEmptyException("Id");
        if (customerDto.getUsername() != null && customerRepository.findByUsername(customerDto.getUsername()).isPresent())
            throw new AlreadyExistsException("Username");
        if (customerDto.getEmail() != null && customerRepository.findByEmail(customerDto.getEmail()).isPresent())
            throw new AlreadyExistsException("Email");
        if (customerDto.getPhoneNumber() != null && customerRepository.findByPhoneNumber(customerDto.getPhoneNumber()).isPresent())
            throw new AlreadyExistsException("Phone number");
        Customer customer = customerRepository.findById(customerDto.getId()).orElseThrow(
                () -> new NotFoundException("Customer")
        );
        Customer updatedCustomer = Customer.builder()
                .id(customerDto.getId())
                .username(Objects.requireNonNullElse(customerDto.getUsername(), customer.getUsername()))
                .password(
                        Objects.requireNonNullElse(passwordEncoder.encode(customerDto.getPassword()),
                                customer.getPassword())
                )
                .build();
        if (customerDto.getEmail() != null)
            updatedCustomer.setEmail(customerDto.getEmail());
        if (customerDto.getPhoneNumber() != null)
            updatedCustomer.setPhoneNumber(customerDto.getPhoneNumber());
        return new CustomerDto(customerRepository.save(updatedCustomer));
    }

    @Override
    public void delete(UUID id) {
        if (id == null)
            throw new NullOrEmptyException("Id");
        customerRepository.delete(customerRepository.findById(id)
                .orElseThrow(
                        () -> new NotFoundException("Customer")
                ));
    }

    @Override
    public CustomerDto getById(UUID id) {
        if (id == null)
            throw new NullOrEmptyException("Id");
        return new CustomerDto(customerRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Customer")
        ));
    }

    @Override
    public List<CustomerDto> getAll() {
        List<Customer> list = customerRepository.findAll();
        if (list.isEmpty())
            throw new NullOrEmptyException("Customers");
        return list.stream().map(CustomerDto::new).toList();
    }

    @Override
    public CustomerDto getByPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty() || phoneNumber.isBlank())
            throw new NullOrEmptyException("Phone number");
        return new CustomerDto(customerRepository.findByPhoneNumber(phoneNumber).orElseThrow(
                () -> new NotFoundException("Customer")
        ));
    }

    @Override
    public CustomerDto getByUsername(String username) {
        if (username == null || username.isEmpty() || username.isBlank())
            throw new NullOrEmptyException("Username");
        return new CustomerDto(customerRepository.findByUsername(username).orElseThrow(
                () -> new NotFoundException("Customer")
        ));
    }

    @Override
    public CustomerDto getByEmail(String email) {
        if (email == null || email.isEmpty() || email.isBlank())
            throw new NullOrEmptyException("Email");
        return new CustomerDto(customerRepository.findByEmail(email).orElseThrow(
                () -> new NotFoundException("Customer")
        ));
    }
}
