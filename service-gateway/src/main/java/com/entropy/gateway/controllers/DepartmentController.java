package com.entropy.gateway.controllers;

import com.entropy.gateway.models.requests.DepartmentRequest;
import com.entropy.gateway.models.responses.DepartmentResponse;
import com.entropy.gateway.services.impls.DepartmentServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DepartmentController {
    private DepartmentServiceImpl service;

    @Autowired
    public DepartmentController(DepartmentServiceImpl service) {
        this.service = service;
    }

    @GetMapping(value = "/department/findById")
    public ResponseEntity<DepartmentResponse> findById(@RequestParam(value = "id") int id) {
        DepartmentResponse response = service.findById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping(value = "/department/findAll")
    public ResponseEntity<List<DepartmentResponse>> findAll() {
        List<DepartmentResponse> responses = service.findAll();
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    @DeleteMapping(value = "/department/removeById")
    public ResponseEntity<Void> removeById(@RequestParam(value = "id") int id) {
        service.removeById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping(value = "/department/saveDepartment")
    public ResponseEntity<Void> saveDepartment(@RequestBody DepartmentRequest request) {
        service.saveDepartment(request);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
