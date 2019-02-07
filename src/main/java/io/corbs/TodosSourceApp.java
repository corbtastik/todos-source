package io.corbs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicInteger;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
class Todo {
    private Integer id;
    private String title = "";
    private Boolean completed = false;
}

@SpringBootApplication
@RestController
@EnableBinding(TodosSourceApp.SourceChannels.class)
public class TodosSourceApp {

    private static final Logger LOG = LoggerFactory.getLogger(TodosSourceApp.class);

    private final static AtomicInteger seq = new AtomicInteger(1);

    interface SourceChannels {
        @Output
        MessageChannel output();
        @Output
        MessageChannel output2();
    }

    private SourceChannels channels;

    @Autowired
    public TodosSourceApp(SourceChannels channels) {
        this.channels = channels;
    }

    @PostMapping("/")
    public Todo eventTodo(@RequestBody Todo todo) {
        if(todo.getId() == null) {
            todo.setId(seq.getAndIncrement());
        }
        this.channels.output2().send(new GenericMessage<>(todo));

        this.channels.output().send(new GenericMessage<>(todo));
        LOG.info("Firing event for " + todo.toString());
        return todo;
    }

    public static void main(String[] args) {
        SpringApplication.run(TodosSourceApp.class, args);
    }
}




