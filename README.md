# Task-tracker
### Advanced to-do app that allows you to manage daily tasks

Task-tracker is JAVA-powered REST API based on Spring framework
made for comfortable planning. It allows you to store all your tasks separately,
relying on condition. App uses JWT-tokens and supports authentication

## Run the app
0. Download repository from [GitHub](https://github.com/OlexandrVovk/task-tacker)
1. Launch eureka-server
2. Launch both task-tracker and authorization-service
3. Launch api-gateway

## How to use it?
The app has three levels of hierarchy
>Board 
>>Task-state
>>>Task

## Task:
- represents the lowest hierarchy level;
- use it for describing your tasks;

### Endpoints
- create task
  - method = **POST**
  - uri = **`/api/task-states/{task_state_id}/tasks`**
  - params = `task_name={your task name}`
  > this endpoint creates new task 
- update task
  - method = **PATCH**
  - uri = **`/api/tasks`**
  - body = `{"id":1,
    "name":"cleaning",
    "description":"clean bedroom"
    }`
  > this endpoint allows you to set task's name and description 
- get task
  - method = **GET**
  - uri = `/api/task-states/{task_state_id}/tasks`
  > this endpoint returns task 
- delete task
  - method = **DELETE**
  - uri = `/api/tasks/{task_id}`
  > this endpoint deletes task
- change task order
  - method = **PATCH**
  - uri = `/api/tasks/{task_id}/position/change`
  - params = `previous_task_id, next_task_id`
  > this endpoint allows you to change task order relative to other tasks \
  > set previous_task_id to **id** of the task which will be previous after changing order \
  > set next_task_id to **id** of the task which will be next after changing order 

## Task-state
- collects tasks into a list
- represents second level of hierarchy

### Endpoints:
- create task-state
  - method = **POST**
  - uri = **`/api/boards/{board_id}/task-states`**
  - params = `task_state_name={your task-state name}`
  > this endpoint creates new task-state
- get task-state
  - method = **GET**
  - uri = `/api/boards/{board_id}/task-states`
  > this endpoint returns task-state
- delete task-state
  - method = **DELETE**
  - uri = `/api/task-states/{tusk_state_id}`
  > this endpoint deletes task-state
- update task-state
  - method = **PATCH**
  - uri = **`/api/task-states/{tusk_state_id}`**
  - params = `new_name={new task-state name}`
  > this endpoint allows you to set new task-state name
- change task-state order
  - method = **PATCH**
  - uri = `/api/task-states/{tusk_state_id}/position/change`
  - params = `previous_task_state_id, next_task_state_id`
> this endpoint allows you to change task-state order relative to other task-states \
> set previous_task_state_id to **id** of the task-state which will be previous after changing order \
> set next_task_state_id to **id** of the task-state which will be next after changing order 

## Board

- collects task-states into a list
- represents top level of hierarchy

### Endpoints
- create board
  - method = **PUT**
  - uri = **`/api/boards`**
  - params = `board_name={your task-state name}`
  > this endpoint creates new board
- get board
  - method = **GET**
  - uri = `/api/boards`
  - params = `prefix_name`
  > this endpoint returns task-state \
  > use prefix_name to filter by beginning of the name
- delete board
  - method = **DELETE**
  - uri = `/api/boards/{board_id}`
  > this endpoint deletes board
- update board
  - method = **PUT**
  - uri = **`/api/boards`**
  - params = `board_id, board_name`
  > this endpoint allows you to set new board name \
  > use board_id to specify board which will be updated

## Example of usage

1. Create a board with name "Achievements in sports".
2. Add three task-states called "To do", "Done", "On going".
3. Add some tasks to task-state:
- task "Bench 100kg" to "Done" 
- task "Lose 10kg weight" to "On going"
- task "Squat 140 kg" to "To do"

With endpoint "change task order" you can move your task to other task-states \
So after the task was done, you can replace it to the task-state called "Done" \
Now you can track your progress. 

## Authentication

As the app uses JWT, you have to add it to every request.
Also, you can't reach endpoints without authentication.
So, first of all, you have to create user and authenticate.

#### Endpoints:
- registration 
  - method = **POST**
  - url = `/api/auth/registration`
  - body = `{
    "name":"your username",
    "password":"your password"
    }`
  > this endpoint creates user

- sign in 
  - method = **POST**
  - url = `/api/auth/login`
  - body = `{
    "name":"your username",
    "password":"your password"
    }`
  > this endpoint performs login and returns JWT token \
  > use this jwt token with every request, \
  > otherwise you will get 401 unauthorized



