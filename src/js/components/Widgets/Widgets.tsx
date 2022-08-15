import React from 'react';
import {
  VStack,
  Collapse,
  Flex,
  Button,
  ButtonGroup,
  Text,
  Checkbox,
} from '@chakra-ui/react';
import { SidebarSection } from '@/Layout/MainSidebar';

const Widget = (props) => {
  const { children, isOpen } = props;
  return <Collapse in={isOpen}>{children}</Collapse>
}

export const DailyNotesWidget = (props) => {
  const { isOpen } = props;
  return (
    <Widget isOpen={isOpen}>
      <ButtonGroup size="xs" justifyContent="stretch" as={Flex} pb={2}>
        <Button>Yesterday</Button>
        <Button>Today</Button>
        <Button>Tomorrow</Button>
      </ButtonGroup>
    </Widget>
  )
}

const dummyTasks = [{
  name: "Layout Refresh",
  isShown: true,
  tasks: [
    { name: "Fix failing tests", description: "Description 1", status: "todo" },
  ]
}, {
  name: "Product Roadmap",
  isShown: true,
  tasks: [
    { name: "Review projects", description: "Description 1", status: "todo" },
    { name: "Reply to Jeff", description: "Description 2", status: "todo" },
  ]
}, {
  name: "Project: Tasks",
  isShown: true,
  tasks: [
    { name: "Task popover UI", description: "Description 1", status: "todo" },
    { name: "Task group summaries", description: "Description 2", status: "todo" },
    { name: "Tasks have project", description: "Description 3", status: "todo" },
    { name: "Tasks have multiple assignment", description: "Description 4", status: "todo" },
  ]
}
]

export const TasksWidget = (props) => {
  const [groups, setGroups] = React.useState(dummyTasks);

  // filter out done tasks
  const filteredTasks = groups.map(group => {
    return {
      ...group,
      tasks: group.tasks.filter(task => task.status !== "done")
    }
  }).filter(group => group.tasks.length > 0);

  const toggleTask = (groupName, taskName) => {
    const newTasks = [...groups];
    const group = newTasks.find(group => group.name === groupName);
    const task = group.tasks.find(task => task.name === taskName);
    task.status = task.status === "done" ? "todo" : "done";
    setGroups(newTasks);
  }

  const totalAvailableTasks = filteredTasks.reduce((acc, group) => {
    return acc + group.tasks.length;
  }, 0);

  const [isOpen, setIsOpen] = React.useState(true);

  return (
    <>
      <SidebarSection title="Tasks" isOpen={true} count={totalAvailableTasks} pl={6} pr={4}>
        <Widget isOpen={isOpen}>
          <VStack align="stretch" spacing={4} pt={2}>
            {filteredTasks.map((group, groupIndex) => (
              <SidebarSection title={group.name} count={group.tasks.length} isOpen={group.isShown} key={groupIndex}>
                <VStack align="stretch" spacing={1}>
                  {group.tasks.map((task, taskIndex) => (
                    <Flex key={taskIndex} gap={1}>
                      <Checkbox
                        onChange={() => toggleTask(group.name, task.name)}
                        isChecked={task.status === "done"}
                      />
                      <Text fontSize="sm" fontWeight="medium">{task.name}</Text>
                    </Flex>
                  ))}
                </VStack>
              </SidebarSection>
            ))}
          </VStack>
        </Widget>
      </SidebarSection>
    </>
  )
}