package com.example.jooqwithhtmx;

import static com.example.jooq.tables.Todos.TODOS;

import com.example.jooq.tables.records.TodosRecord;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
@Slf4j
public class TodosController {

    private final TodosService todosService;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("count", todosService.countBy(TODOS.IS_DONE.isFalse()));
        List<TodosRecord> todos = todosService.findListBy();
        model.addAttribute("todos", todos);
        return "index";
    }

    @HxRequest
    @PostMapping("/todos")
    public String create(Model model, String todo) {
        todosService.createTodo(todo);
        List<TodosRecord> todos = todosService.findListBy();
        model.addAttribute("todos", todos);
        model.addAttribute("count", todosService.countBy(TODOS.IS_DONE.isFalse()));
        return "fragments :: todos";
    }

    @HxRequest
    @PostMapping("/todos/{id}/toggle")
    public String toggle(Model model, @PathVariable Integer id) {
        model.addAttribute("todos", todosService.toggleTodo(id));
        model.addAttribute("count", todosService.countBy(TODOS.IS_DONE.isFalse()));
        return "fragments :: todos";
    }

}
