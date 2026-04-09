package com.ecommerce.backend.controller;

import com.ecommerce.backend.model.Address;
import com.ecommerce.backend.model.User;
import com.ecommerce.backend.repository.AddressRepository;
import com.ecommerce.backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public AddressController(AddressRepository addressRepository, UserRepository userRepository) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<Address>> getUserAddresses(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName()).orElseThrow();
        return ResponseEntity.ok(addressRepository.findByUserId(user.getId()));
    }

    @PostMapping
    public ResponseEntity<Address> addAddress(@RequestBody Address address, Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName()).orElseThrow();
        
        // If this is set as default, we should unset others
        if (address.getIs_default() != null && address.getIs_default() == 1) {
            List<Address> existing = addressRepository.findByUserId(user.getId());
            for (Address existingAddr : existing) {
                if (existingAddr.getIs_default() == 1) {
                    existingAddr.setIs_default(0);
                    addressRepository.save(existingAddr);
                }
            }
        }
        
        address.setUser(user);
        
        // Convert JS boolean to integer logic if they pass true/false in JSON somehow (handled by Spring usually but just in case)
        if (address.getIs_default() == null) {
            address.setIs_default(0);
        }
        
        return ResponseEntity.ok(addressRepository.save(address));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long id, Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName()).orElseThrow();
        Address address = addressRepository.findById(id).orElseThrow();
        
        if (address.getUser().getId().equals(user.getId())) {
            addressRepository.delete(address);
        }
        
        return ResponseEntity.ok().build();
    }
}
