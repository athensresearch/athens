import {
  Text,
} from "@chakra-ui/react";
import { InboxViewListItem, } from "./InboxViewListItem";
import { InboxViewListBody, } from "./InboxViewListBody";
import * as React from "react";
import {
  ArchiveIcon,
} from "@/Icons/Icons";

type PAGE = {
  name: string
  url: string
  breadcrumb: string[]
}

type BLOCK = {
  string: string
  url: string
  breadcrumb: string[]
}

type PROPERTY = {
  name: string
  breadcrumb: string[]
}

type OBJECT = PAGE | PROPERTY | BLOCK;

const notificationTypes = ["Created", "Edited", "Deleted", "Comments", "Mentions", "Assignments", "Completed"]
type NOTIFICATION_TYPE = typeof notificationTypes[number];

type NOTIFICATION = {
  id: string
  notificationTime: string
  type: NOTIFICATION_TYPE
  subject: Person
  object: OBJECT
  isRead: boolean,
  isArchived: boolean
}

const messageForNotification = (notification: NOTIFICATION): React.ReactNode => {
  const { type, subject, object } = notification;
  const subjectName = subject.username;
  const objectName = object.string || object.name;

  if (type === "Created") {
    return <Text as="span"><Text as="span" fontWeight="semibold">{subjectName}</Text> created <Text as="span" fontWeight="semibold">{objectName}</Text></Text>;
  } else if (type === "Edited") {
    return <Text as="span"><Text as="span" fontWeight="semibold">{subjectName}</Text> edited <Text as="span" fontWeight="semibold">{objectName}</Text></Text>;
  } else if (type === "Deleted") {
    return <Text as="span"><Text as="span" fontWeight="semibold">{subjectName}</Text> deleted <Text as="span" fontWeight="semibold">{objectName}</Text></Text>;
  } else if (type === "Comments") {
    return <Text as="span"><Text as="span" fontWeight="semibold">{subjectName}</Text> commented on <Text as="span" fontWeight="semibold">{objectName}</Text></Text>;
  } else if (type === "Mentions") {
    return <Text as="span"><Text as="span" fontWeight="semibold">{subjectName}</Text> mentioned you in  <Text as="span" fontWeight="semibold">{objectName}</Text></Text>;
  } else if (type === "Assignments") {
    return <Text as="span"><Text as="span" fontWeight="semibold">{subjectName}</Text> assigned you to <Text as="span" fontWeight="semibold">{objectName}</Text></Text>;
  } else if (type === "Completed") {
    return <Text as="span"><Text as="span" fontWeight="semibold">{subjectName}</Text> completed <Text as="span" fontWeight="semibold">{objectName}</Text></Text>;
  }
}


export const InboxItemsList = (props) => {
  const { notificationsList, onOpenItem, onMarkAsRead, onMarkAsUnread, onArchive, onUnarchive } = props;


  const getActionsForNotification = (notification) => {
    const actions = [];
    if (notification.isArchived) {
      actions.push({
        label: "Unarchive",
        fn: () => onUnarchive(notification.id),
        icon: <ArchiveIcon />
      });
    } else {
      actions.push({
        label: "Archive",
        fn: () => onArchive(notification.id),
        icon: <ArchiveIcon />
      });
    }
    return actions;
  }

  const itemsList = notificationsList.map((i) => {
  return <InboxViewListItem
    message={messageForNotification(i)}
    actions={getActionsForNotification(i)}
    onOpen={onOpenItem}
    onMarkAsRead={onMarkAsRead}
    onMarkAsUnread={onMarkAsUnread}
    onMarkAsArchived={onArchive}
    onMarkAsUnarchived={onUnarchive}
    key={i.id}
    {...i}
  />});

  return <InboxViewListBody>
      {itemsList}
  </InboxViewListBody>
}

