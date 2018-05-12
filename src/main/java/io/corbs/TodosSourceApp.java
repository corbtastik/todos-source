package io.corbs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.http.MediaType;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@SpringBootApplication
@RestController
@EnableBinding(TodosSourceApp.SourceChannels.class)
public class TodosSourceApp {

    interface SourceChannels {
        @Output
        MessageChannel output();
    }

    private final SourceChannels channels;

    @Autowired
    public TodosSourceApp(SourceChannels channels) {
        this.channels = channels;
    }

    @PostMapping("/")
    public Todo createTodo(@RequestBody Todo todo) {
        this.channels.output().send(new GenericMessage<>(todo));
        // TODO return Todo value after its been inserted
        return todo;
    }

    @GetMapping(value = "/stats", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    private Flux<Long> live() {
        return Flux.create(fluxSink -> {
            while(true) {
                fluxSink.next(System.currentTimeMillis());
            }
        });
    }

    public static void main(String[] args) {
        SpringApplication.run(TodosSourceApp.class, args);
    }
}




