package com.codeloom.backend.controller;

import static com.codeloom.backend.config.OpenApiConfig.BAD_REQUEST_RESPONSE_REF;
import static com.codeloom.backend.config.OpenApiConfig.NOT_FOUND_RESPONSE_REF;

import com.codeloom.backend.dto.CreateTopicRequest;
import com.codeloom.backend.dto.UpdateTopicRequest;
import com.codeloom.backend.model.Topic;
import com.codeloom.backend.service.TopicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/topics")
@RequiredArgsConstructor
@Tag(name = "Topics", description = "Problem topic administration")
public class TopicController {
    private final TopicService service;

    @GetMapping
    @Operation(operationId = "listTopics", summary = "List topics", description = "Returns every available topic.")
    @ApiResponse(
            responseCode = "200",
            description = "Topics returned",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Topic.class))))
    public Iterable<Topic> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    @Operation(operationId = "getTopic", summary = "Get a topic")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Topic returned"),
        @ApiResponse(responseCode = "404", description = "Topic not found", ref = NOT_FOUND_RESPONSE_REF)
    })
    public Topic findAll(@Parameter(description = "Topic identifier") @PathVariable UUID id) {
        return service.findById(id);
    }

    @PostMapping
    @Operation(operationId = "createTopic", summary = "Create a topic", description = "Requires the ADMIN role.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Topic created"),
        @ApiResponse(responseCode = "400", description = "Invalid topic", ref = BAD_REQUEST_RESPONSE_REF)
    })
    public Topic create(@Valid @RequestBody CreateTopicRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    @Operation(operationId = "updateTopic", summary = "Update a topic", description = "Requires the ADMIN role.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Topic updated"),
        @ApiResponse(responseCode = "400", description = "Invalid topic", ref = BAD_REQUEST_RESPONSE_REF),
        @ApiResponse(responseCode = "404", description = "Topic not found", ref = NOT_FOUND_RESPONSE_REF)
    })
    public Topic update(
            @Parameter(description = "Topic identifier") @PathVariable UUID id,
            @Valid @RequestBody UpdateTopicRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(operationId = "deleteTopic", summary = "Delete a topic", description = "Requires the ADMIN role.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Topic deleted"),
        @ApiResponse(responseCode = "404", description = "Topic not found", ref = NOT_FOUND_RESPONSE_REF)
    })
    public void delete(@Parameter(description = "Topic identifier") @PathVariable UUID id) {
        service.delete(id);
    }
}
