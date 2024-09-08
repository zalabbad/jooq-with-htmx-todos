package com.example.jooqwithhtmx;

import static com.example.jooq.tables.Todos.TODOS;

import com.example.jooq.tables.records.TodosRecord;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TodosService {

    private final DSLContext dslContext;
    private final ApplicationEventPublisher eventPublisher;

    // Read Functions
    @Transactional(readOnly = true)
    public Optional<TodosRecord> findBy(Condition... conditions) {
        return dslContext.selectFrom(TODOS)
            .where(conditions)
            .fetchOptional();
    }

    @Transactional(readOnly = true)
    public List<TodosRecord> findListBy(Condition... conditions) {
        // TODO: add pagination
        Result<TodosRecord> result = dslContext.selectFrom(TODOS)
            .where(conditions)
            .fetch();
        return result.into(TodosRecord.class);
    }

    @Transactional(readOnly = true)
    public Integer countBy(Condition... conditions) {
        return dslContext.selectCount().from(TODOS).where(conditions).fetchOne().value1();
    }

    // Write Functions
    @Transactional
    public TodosRecord createTodo(String title) {
        return dslContext.insertInto(TODOS)
            .set(TODOS.TITLE, title)
            .returning().fetchOne();
    }

    @Transactional
    public List<TodosRecord> toggleTodo(Integer id) {
        TodosRecord todo = findBy(TODOS.ID.eq(id))
            .orElseThrow(() -> new RuntimeException("not found"));
        todo.setIsDone(!todo.getIsDone());
        todo.store();
        TodoToggledEvent event = new TodoToggledEvent(todo.getId());
        eventPublisher.publishEvent(event);
        return findListBy();
    }

    @ApplicationModuleListener
    public void handleTodoToggled(TodoToggledEvent event) {
        log.info("Todo: {} was toggled", event.id());
        if (countBy(TODOS.IS_DONE.isTrue()) == countBy()) {
            log.info("User has completed all the todos, deleting them");
            dslContext.deleteFrom(TODOS).execute();
        }
    }
}
