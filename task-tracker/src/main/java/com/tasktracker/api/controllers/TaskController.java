package com.tasktracker.api.controllers;

import com.tasktracker.api.dto.AnswerDto;
import com.tasktracker.api.dto.TaskDto;
import com.tasktracker.api.factories.TaskDtoFactory;
import com.tasktracker.api.services.TaskService;
import com.tasktracker.api.util.JWTUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class TaskController {

    private final JWTUtil jwtUtil;
    private final TaskDtoFactory taskDtoFactory;
    private final TaskService taskService;

    private static final String CREATE_TASK="/api/task-states/{task_state_id}/tasks";
    private static final String GET_TASKS="/api/task-states/{task_state_id}/tasks";
    private static final String DELETE_TASK="/api/tasks/{task_id}";
    private static final String UPDATE_TASK="/api/tasks";
    private static final String CHANGE_TASK_POSITION="/api/tasks/{task_id}/position/change";

    @PostMapping(CREATE_TASK)
    public TaskDto createTask(@PathVariable("task_state_id") Long taskStateId,
                              @RequestParam("task_name") String taskName,
                              HttpServletRequest request){
        Long personId = jwtUtil.getPersonId(request);
        return taskService.createTask(taskStateId, taskName, personId);
    }

    @GetMapping(GET_TASKS)
    public List<TaskDto> getTasks(@PathVariable("task_state_id") Long taskStateId,
                                  HttpServletRequest request){
        Long personId = jwtUtil.getPersonId(request);
        return  taskDtoFactory.makeTaskDtoList(taskService.getTasks(taskStateId, personId));
    }

    @DeleteMapping(DELETE_TASK)
    public AnswerDto deleteTask(@PathVariable("task_id") Long taskId,
                                HttpServletRequest request){
        Long personId = jwtUtil.getPersonId(request);
        return taskService.deleteTask(taskId, personId);
    }

    @PatchMapping(UPDATE_TASK)
    public TaskDto updateTask(@RequestBody TaskDto taskDto,  HttpServletRequest request){
        Long personId = jwtUtil.getPersonId(request);
        return taskService.update(taskDto, personId);
    }

    @PatchMapping(CHANGE_TASK_POSITION)
    public TaskDto changeTaskPosition(@PathVariable("task_id") Long taskId,
                                      @RequestParam(value = "previous_task_id",required = false) Optional<Long> previousTaskId,
                                      @RequestParam(value = "next_task_id" , required = false)Optional<Long> nextTaskId,
                                      HttpServletRequest request){
        Long personId = jwtUtil.getPersonId(request);
        return taskService.changeTaskPosition(taskId, previousTaskId, nextTaskId,personId);
    }

}
