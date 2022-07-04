import {
  Box,
  HStack,
  ButtonGroup,
  Button,
  VStack,
  Text
} from "@chakra-ui/react";
import * as React from "react";
import { faker } from "@faker-js/faker";

type PAGE = {
  name: string
  url: string
  breadcrumb: any
}

type BLOCK = {
  string: string
  url: string
  breadcrumb: any
}

type PROPERTY = {
  name: string
  breadcrumb: any
}

type OBJECT = PAGE | PROPERTY | BLOCK;

const notificationTypes = ["created", "edited", "deleted", "commented", "mentioned", "taskAssigned", "taskCompleted"]
type NOTIFICATION_TYPE = typeof notificationTypes[number];

type NOTIFICATION = {
  id: string
  // time: string
  type: NOTIFICATION_TYPE
  subject: Person
  object: OBJECT
  isRead: boolean,
  isArchived: boolean
}

const messageForNotification = (notification: NOTIFICATION) => {
  const { type, subject, object } = notification;
  const subjectName = subject.username;
  const objectName = object.string || object.name;

  if (type === "created") {
    return `${subjectName} created ${objectName}`;
  } else if (type === "edited") {
    return `${subjectName} edited ${objectName}`;
  } else if (type === "deleted") {
    return `${subjectName} deleted ${objectName}`;
  } else if (type === "commented") {
    return `${subjectName} commented on ${objectName}`;
  } else if (type === "mentioned") {
    return `${subjectName} mentioned you in ${objectName}`;
  } else if (type === "taskAssigned") {
    return `${subjectName} assigned you to ${objectName}`;
  } else if (type === "taskCompleted") {
    return `${subjectName} completed ${objectName}`;
  }
}

const makeNotificationSubject = () => ({
  personId: faker.datatype.uuid(),
  username: faker.name.findName(),
  color: faker.internet.color(),
})

const makeNotificationPageObject = () => ({
  name: faker.lorem.words(),
  url: faker.internet.url(),
  breadcrumb: faker.lorem.words(),
})
const makeNotificationBlockObject = () => ({
  string: faker.lorem.sentence(),
  url: faker.internet.url(),
  breadcrumb: faker.lorem.words(),
})
const makeNotificationPropertyObject = () => ({
  name: faker.lorem.sentence(),
  breadcrumb: faker.lorem.words(),
})
const makeNotificationObject = () => faker.helpers.arrayElement([makeNotificationPageObject,
  makeNotificationBlockObject,
  makeNotificationPropertyObject])()

const makeNotification = (): NOTIFICATION => ({
  id: faker.datatype.uuid(),
  // time: faker.date.past().toISOString(),
  type: faker.helpers.arrayElement(notificationTypes),
  subject: makeNotificationSubject(),
  object: makeNotificationObject(),
  isRead: false,
  isArchived: false
})

interface NOTIFICATION_ITEM extends NOTIFICATION {
  onMarkAsRead: (id) => void
  onMarkAsUnread: (id) => void
  onMarkAsArchived: (id) => void
  onMarkAsUnarchived: (id) => void
}

const NotificationItem = (props: NOTIFICATION_ITEM): JSX.Element => {
  const { onMarkAsRead,
    onMarkAsUnread,
    onMarkAsArchived,
    onMarkAsUnarchived,
    ...notificationProps } = props;
  const message = messageForNotification(notificationProps);
  const { id, isRead, isArchived } = notificationProps;
  return <VStack>
    <Box bg={isRead ? "transparent" : "blue"} w="1em" h="1em" /><Text>{message}</Text>
    <ButtonGroup>
      <Button onClick={() => onMarkAsRead(id)}></Button>
      {isArchived && <Button onClick={() => onMarkAsArchived(id)}></Button>}
    </ButtonGroup>
  </VStack>
}

const ITEMS = new Array(12).fill(true).map(() => makeNotification());


export const Inbox = () => {
  const [items, setItems] = React.useState(ITEMS);

  const updateItemProperty = (id, property, value) => {
    const editedIndex = items.findIndex(item => item.id === id);
    const editedItem = { ...items[editedIndex], [property]: value };
    const editedItems = [...items.slice(0, editedIndex), editedItem, ...items.slice(editedIndex + 1)];
    setItems(editedItems);
  }

  const markAsRead = (id) => {
    updateItemProperty(id, "isRead", true);
  }

  const markAsUnread = (id) => {
    updateItemProperty(id, "isRead", false);
  }

  const markAsArchived = (id) => {
    updateItemProperty(id, "isArchived", false);
  }
  const markAsUnarchived = (id) => {
    updateItemProperty(id, "isArchived", false);
  }


  return (
    <VStack width="30em" align="stretch" p={4}>
      {items.map((i) => (
        <NotificationItem
          onMarkAsRead={markAsRead}
          onMarkAsUnread={markAsUnread}
          onMarkAsArchived={markAsArchived}
          onMarkAsunArchived={markAsUnarchived}
          key={i.id}
          {...i}
        />
      ))}
    </VStack>
  );
}
