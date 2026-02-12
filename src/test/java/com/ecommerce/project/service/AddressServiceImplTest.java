package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Address;
import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.AddressDTO;
import com.ecommerce.project.repositories.AddressRepository;
import com.ecommerce.project.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressServiceImplTest {

    @InjectMocks
    private AddressServiceImpl addressService;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapper modelMapper;

    // ---------- createAddress ----------

    @Test
    void createAddress_success() {
        User user = new User();
        AddressDTO dto = new AddressDTO();
        Address address = new Address();
        Address savedAddress = new Address();

        // DTO → Entity
        when(modelMapper.map(dto, Address.class)).thenReturn(address);
        // Save
        when(addressRepository.save(address)).thenReturn(savedAddress);
        // Entity → DTO
        when(modelMapper.map(savedAddress, AddressDTO.class))
                .thenReturn(new AddressDTO());

        AddressDTO result = addressService.createAddress(dto, user);

        assertNotNull(result);
        verify(addressRepository, times(1)).save(address);
    }

    // ---------- getAddresses ----------

    @Test
    void getAddresses_success() {
        Address address = new Address();
        List<Address> addresses = List.of(address);

        when(addressRepository.findAll()).thenReturn(addresses);
        when(modelMapper.map(any(Address.class), eq(AddressDTO.class)))
                .thenReturn(new AddressDTO());

        List<AddressDTO> result = addressService.getAddresses();

        assertEquals(1, result.size());
    }

    // ---------- getAddressesById ----------

    @Test
    void getAddressesById_success() {
        Address address = new Address();
        address.setAddressId(1L);

        when(addressRepository.findById(1L))
                .thenReturn(Optional.of(address));
        when(modelMapper.map(address, AddressDTO.class))
                .thenReturn(new AddressDTO());

        AddressDTO result = addressService.getAddressesById(1L);

        assertNotNull(result);
    }

    @Test
    void getAddressesById_notFound() {
        when(addressRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> addressService.getAddressesById(1L));
    }

    // ---------- getUserAddresses ----------

    @Test
    void getUserAddresses_success() {
        User user = new User();
        Address address = new Address();
        user.setAddresses(List.of(address));

        when(modelMapper.map(address, AddressDTO.class))
                .thenReturn(new AddressDTO());

        List<AddressDTO> result = addressService.getUserAddresses(user);

        assertEquals(1, result.size());
    }

    // ---------- updateAddress ----------

    @Test
    void updateAddress_success() {
        Long addressId = 1L;

        User user = new User();
        user.setAddresses(new ArrayList<>());

        Address existingAddress = new Address();
        existingAddress.setAddressId(addressId);
        existingAddress.setUser(user);

        AddressDTO dto = new AddressDTO();

        when(addressRepository.findById(addressId))
                .thenReturn(Optional.of(existingAddress));

        when(addressRepository.save(existingAddress))
                .thenReturn(existingAddress);

        when(userRepository.save(user))
                .thenReturn(user);

        when(modelMapper.map(existingAddress, AddressDTO.class))
                .thenReturn(new AddressDTO());

        AddressDTO result = addressService.updateAddress(addressId, dto);

        assertNotNull(result);
        verify(addressRepository).save(existingAddress);
        verify(userRepository).save(user);
    }

    // ---------- deleteAddress ----------

    @Test
    void deleteAddress_success() {
        Long addressId = 1L;

        User user = new User();
        user.setAddresses(new ArrayList<>());

        Address address = new Address();
        address.setAddressId(addressId);
        address.setUser(user);

        when(addressRepository.findById(addressId))
                .thenReturn(Optional.of(address));

        String result = addressService.deleteAddress(addressId);

        assertTrue(result.contains("Address deleted successfully"));
        verify(userRepository).save(user);
        verify(addressRepository).delete(address);
    }
}