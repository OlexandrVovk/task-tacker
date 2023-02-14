package com.tasktracker.api.controllers;


import com.tasktracker.api.dto.AnswerDto;
import com.tasktracker.api.dto.TaskStateDto;
import com.tasktracker.api.factories.TaskStateDtoFactory;
import com.tasktracker.api.services.TaskService;
import com.tasktracker.api.services.TaskStateService;
import com.tasktracker.api.util.JWTUtil;
import com.tasktracker.store.entities.TaskEntity;
import com.tasktracker.store.entities.TaskStateEntity;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
public class TaskStateController {

    private final JWTUtil jwtUtil;
    private  final TaskStateDtoFactory taskStateDtoFactory;
    private final TaskStateService taskStateService;
    private final TaskService taskService;

    public static final String GET_TASK_STATES="/api/boards/{board_id}/task-states";

    public static final String CREATE_TASK_STATE="/api/boards/{board_id}/task-states";

    public static final String UPDATE_TASK_STATE="/api/task-states/{tusk_state_id}";

    public static final String DELETE_TASK_STATE="/api/task-states/{tusk_state_id}";

    public static final String CHANGE_TASK_STATE_POSITION="/api/task-states/{tusk_state_id}/position/change";


    @GetMapping(GET_TASK_STATES)
    public List<TaskStateDto> getTaskStates(@PathVariable("board_id") Long boardId,
                                            HttpServletRequest request){
        Long personId = jwtUtil.getPersonId(request);
        List<TaskStateEntity> list = taskStateService.getTaskStates(boardId, personId);
        if (list.isEmpty()) return Collections.emptyList();

        list.forEach(taskState ->{
            List<TaskEntity> tasks = taskState.getTasks();
            if (!tasks.isEmpty()){
                taskState.setTasks(taskService.toSort(taskState.getTasks()));
            }
        });
        return taskStateDtoFactory.makeTaskStateDtoList(taskStateService.toSort(list));

    }
    @PostMapping(CREATE_TASK_STATE)
    public TaskStateDto createTaskState(@PathVariable("board_id") Long boardId,
                                        @RequestParam("task_state_name") String taskStateName,
                                        HttpServletRequest request){
        Long personId = jwtUtil.getPersonId(request);
        return taskStateService.createTaskState(boardId, taskStateName, personId);
    }

    @PatchMapping(UPDATE_TASK_STATE)
    public TaskStateDto updateTaskState(@PathVariable("tusk_state_id") Long tuskStateId,
                                        @RequestParam("new_name") String newTaskStateName,
                                        HttpServletRequest request){
        Long personId = jwtUtil.getPersonId(request);
        return taskStateService.updateTaskState(tuskStateId, newTaskStateName, personId);
    }

    @DeleteMapping(DELETE_TASK_STATE)
    public AnswerDto deleteTuskState(@PathVariable("tusk_state_id") Long taskStateId,
                                     @RequestParam(value = "delete_all", required = false) Optional<Boolean> deleteAll,
                                     HttpServletRequest request){
        Long personId = jwtUtil.getPersonId(request);
        return taskStateService.deleteTuskState(taskStateId, deleteAll, personId);
    }

    @PatchMapping(CHANGE_TASK_STATE_POSITION)
    public List<TaskStateDto> changeTaskStatePosition(@PathVariable("tusk_state_id") Long taskStateId,
                                                      @RequestParam(value = "previous_task_state_id",required = false) Optional<Long> previousTaskStateId,
                                                      @RequestParam(value = "next_task_state_id" , required = false)Optional<Long> nextTaskStateId,
                                                      @RequestParam(value = "to_sort", required = false) boolean toSort,
                                                      HttpServletRequest request){
        Long personId = jwtUtil.getPersonId(request);
        List<TaskStateEntity> taskStateEntityList = taskStateService.changeTaskStatePosition(
                taskStateId, previousTaskStateId, nextTaskStateId, personId);

        if (toSort){
            return taskStateDtoFactory.makeTaskStateDtoList(
                    taskStateService.toSort(taskStateEntityList)
            );
        }
        return taskStateDtoFactory.makeTaskStateDtoList(taskStateEntityList);
    }
}
