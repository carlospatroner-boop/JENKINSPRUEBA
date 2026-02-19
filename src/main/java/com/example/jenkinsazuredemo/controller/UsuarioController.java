package com.example.jenkinsazuredemo.controller;

import com.example.jenkinsazuredemo.model.Usuario;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @GetMapping
    public List<Usuario> obtenerUsuarios() {
        return Arrays.asList(
                new Usuario(1L, "Carlos Perez", "carlos@example.com", "Admin"),
                new Usuario(2L, "Ana Gomez", "ana@example.com", "User"),
                new Usuario(3L, "Luis Rodriguez", "luis@example.com", "Editor")
        );
    }
}