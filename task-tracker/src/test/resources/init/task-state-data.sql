INSERT INTO Person (username, password) VALUES ('test', 'test_password');
INSERT INTO Board (name, person_id) VALUES ('test board', 1);
INSERT INTO Task_state (name, board_id,left_task_state_id, right_task_state_id) values ('first', 1, null, null);
INSERT INTO Task_state (name, board_id,left_task_state_id, right_task_state_id) values ('second', 1, null, null);
INSERT INTO Task_state (name, board_id,left_task_state_id, right_task_state_id) values ('third', 1, null, null);

update task_state set right_task_state_id=2 where id = 1;

update task_state set left_task_state_id=1 where id = 2;
update task_state set right_task_state_id=3 where id = 2;

update task_state set left_task_state_id=2 where id = 3;
