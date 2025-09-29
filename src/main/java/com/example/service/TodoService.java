package com.example.service;

import com.example.entity.Todo;
import com.example.entity.User;
import com.example.repository.TodoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TodoService {

    @Autowired
    private TodoRepository todoRepository;

    public List<Todo> getTodosForUser(User user) {
        return todoRepository.findByUser(user);
    }

    public void saveTodo(Todo todo, User user) {
        todo.setUser(user);
        todoRepository.save(todo);
    }

    public Todo getTodoByIdAndUser(Long id, User user) {
        return todoRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("Todo not found or you do not have permission to access it."));
    }

    public void deleteTodoByIdAndUser(Long id, User user) {
        Todo todo = getTodoByIdAndUser(id, user); // This ensures the user owns the todo
        todoRepository.delete(todo);
    }
}