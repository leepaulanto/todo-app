package com.example.controller;

import com.example.entity.Todo;
import com.example.entity.User;
import com.example.service.TodoService;
import com.example.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class TodoController {

    @Autowired
    private TodoService todoService;

    @Autowired
    private UserService userService;

    private User getAuthenticatedUser(UserDetails userDetails) {
        return userService.findByUsername(userDetails.getUsername());
    }

    @GetMapping("/todos")
    @Operation(summary = "Get all todos for the authenticated user")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    public String getTodos(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User user = getAuthenticatedUser(userDetails);
        model.addAttribute("todos", todoService.getTodosForUser(user));
        model.addAttribute("username", user.getUsername());
        return "todos";
    }

    @GetMapping("/todos/new")
    @Operation(summary = "Show form to create a new todo")
    public String showCreateForm(Model model) {
        model.addAttribute("todo", new Todo());
        return "todo-form";
    }

    @PostMapping("/todos")
    @Operation(summary = "Create a new todo")
    public String createTodo(@Valid Todo todo, BindingResult result, @AuthenticationPrincipal UserDetails userDetails) {
        if (result.hasErrors()) {
            return "todo-form";
        }
        todoService.saveTodo(todo, getAuthenticatedUser(userDetails));
        return "redirect:/todos";
    }

    @GetMapping("/todos/edit/{id}")
    @Operation(summary = "Show form to edit an existing todo")
    public String showEditForm(@PathVariable Long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Todo todo = todoService.getTodoByIdAndUser(id, getAuthenticatedUser(userDetails));
            model.addAttribute("todo", todo);
            return "todo-form";
        } catch (IllegalArgumentException e) {
            return "redirect:/todos?error=permission";
        }
    }

    @PostMapping("/todos/update/{id}")
    @Operation(summary = "Update an existing todo")
    public String updateTodo(@PathVariable Long id, @Valid Todo todo, BindingResult result, @AuthenticationPrincipal UserDetails userDetails) {
        if (result.hasErrors()) {
            todo.setId(id); // Keep the ID for the form resubmission
            return "todo-form";
        }
        // Ensure user owns the todo before saving
        todoService.getTodoByIdAndUser(id, getAuthenticatedUser(userDetails));
        todo.setId(id);
        todoService.saveTodo(todo, getAuthenticatedUser(userDetails));
        return "redirect:/todos";
    }

    @GetMapping("/todos/delete/{id}")
    @Operation(summary = "Delete a todo")
    public String deleteTodo(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            todoService.deleteTodoByIdAndUser(id, getAuthenticatedUser(userDetails));
            return "redirect:/todos";
        } catch (IllegalArgumentException e) {
            return "redirect:/todos?error=permission";
        }
    }
}