package com.utilitybilling.service;

import com.utilitybilling.dto.request.CustomerRequest;
import com.utilitybilling.dto.response.CustomerResponse;
import com.utilitybilling.entity.Customer;
import com.utilitybilling.exception.BusinessRuleException;
import com.utilitybilling.exception.ResourceNotFoundException;
import com.utilitybilling.repository.CustomerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service managing customer lifecycle: creation, listing, update, and soft-delete (status INACTIVE).
 * Enforces unique national ID constraint and validates status transitions.
 */
@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        if (customerRepository.existsByNationalId(request.nationalId())) {
            throw new BusinessRuleException("Customer with national ID already exists: " + request.nationalId());
        }

        Customer customer = new Customer();
        customer.setFullNames(request.fullNames());
        customer.setNationalId(request.nationalId());
        customer.setEmail(request.email());
        customer.setPhoneNumber(request.phoneNumber());
        customer.setAddress(request.address());
        customer.setStatus(Customer.CustomerStatus.ACTIVE);

        customerRepository.save(customer);
        return mapToResponse(customer);
    }

    public Page<CustomerResponse> findAll(Pageable pageable) {
        return customerRepository.findAll(pageable).map(this::mapToResponse);
    }

    public CustomerResponse findById(UUID id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
        return mapToResponse(customer);
    }

    @Transactional
    public CustomerResponse update(UUID id, CustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));

        if (!customer.getNationalId().equals(request.nationalId()) && customerRepository.existsByNationalId(request.nationalId())) {
            throw new BusinessRuleException("Customer with national ID already exists: " + request.nationalId());
        }

        customer.setFullNames(request.fullNames());
        customer.setNationalId(request.nationalId());
        customer.setEmail(request.email());
        customer.setPhoneNumber(request.phoneNumber());
        customer.setAddress(request.address());
        if (request.status() != null) {
            customer.setStatus(Customer.CustomerStatus.valueOf(request.status()));
        }

        customerRepository.save(customer);
        return mapToResponse(customer);
    }

    @Transactional
    public void delete(UUID id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
        customer.setStatus(Customer.CustomerStatus.INACTIVE);
        customerRepository.save(customer);
    }

    private CustomerResponse mapToResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getFullNames(),
                customer.getNationalId(),
                customer.getEmail(),
                customer.getPhoneNumber(),
                customer.getAddress(),
                customer.getStatus().name(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }

    public Customer getCustomerEntity(UUID id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
    }
}
