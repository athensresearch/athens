import {
  Avatar,
  Box,
  Text,
  HStack,
  ButtonGroup,
  VStack,
} from "@chakra-ui/react";
import { InboxViewListBody } from "./InboxViewListBody";
import { mapActionsToButtons } from '../utils/mapActionsToButtons';
import { motion } from "framer-motion";
import * as React from "react";
import {
  ArchiveIcon, ChatBubbleFillIcon,
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

const NotificationIcon = (props) => {
  const { subject, type } = props;
  const username = subject.username;

  return <Box position="relative" flex={0}>
    <Avatar name={username} />
    <ChatBubbleFillIcon
      boxSize={5}
      position="absolute"
      right={0}
      bottom={0}
    />
  </Box>
}


const messageForNotification = (notification: NOTIFICATION): React.ReactNode => {
  const { type, subject, object } = notification;
  const subjectName = subject.username;
  const objectName = object.string || object.name;

  let text;

  if (type === "Created") {
    text = <Text as="span"><Text as="span" fontWeight="semibold">{subjectName}</Text> created <Text as="span" fontWeight="semibold">{objectName}</Text></Text>;
  } else if (type === "Edited") {
    text = <Text as="span"><Text as="span" fontWeight="semibold">{subjectName}</Text> edited <Text as="span" fontWeight="semibold">{objectName}</Text></Text>;
  } else if (type === "Deleted") {
    text = <Text as="span"><Text as="span" fontWeight="semibold">{subjectName}</Text> deleted <Text as="span" fontWeight="semibold">{objectName}</Text></Text>;
  } else if (type === "Comments") {
    text = <Text as="span"><Text as="span" fontWeight="semibold">{subjectName}</Text> commented on <Text as="span" fontWeight="semibold">{objectName}</Text></Text>;
  } else if (type === "Mentions") {
    text = <Text as="span"><Text as="span" fontWeight="semibold">{subjectName}</Text> mentioned you in  <Text as="span" fontWeight="semibold">{objectName}</Text></Text>;
  } else if (type === "Assignments") {
    text = <Text as="span"><Text as="span" fontWeight="semibold">{subjectName}</Text> assigned you to <Text as="span" fontWeight="semibold">{objectName}</Text></Text>;
  } else if (type === "Completed") {
    text = <Text as="span"><Text as="span" fontWeight="semibold">{subjectName}</Text> completed <Text as="span" fontWeight="semibold">{objectName}</Text></Text>;
  }

  return text;
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
        fn: (e) => onArchive(e, notification.id),
        icon: <ArchiveIcon />
      });
    }
    return actions;
  }

  const itemsList = notificationsList.map((notification) => {

  });

  return <InboxViewListBody>
    {itemsList}
  </InboxViewListBody>
}

export const NotificationItem = (props) => {
  const { isSelected, onOpen, onMarkAsRead, onMarkAsUnread, onMarkAsArchived, onMarkAsUnarchived, onIncSelection, onDecSelection, onSelect, onDeselect, ...notificationProps } = props;
  const { id, isRead, type, isArchived, message, body, actions, object, notificationTime, ...boxProps } = notificationProps;

  return <VStack
    key={id}
    layout
    as={motion.div}
    animate={{
      height: "auto",
      opacity: 1,
      zIndex: 1
    }}
    exit={{
      height: 0,
      opacity: 0,
      zIndex: -1,
    }}
    p={2}
    mt={2}
    spacing={1}
    flexShrink={0}
    overflow="hidden"
    align="stretch"
    userSelect="none"
    _hover={{
      cursor: "pointer"
    }}
    borderRadius="md"
    bg={"interaction.surface"}
    onClick={() => onOpen(object.parentUid, id)}
    {...boxProps}
  >
    <HStack
      as="li"
      align="baseline"
      textAlign="left"
      spacing={1.5}
    >
      <NotificationStatusIndicator isRead={isRead} />
      <VStack flexShrink={1} spacing={0} align="stretch">
        <Text color={isRead ? "foreground.secondary" : "foreground.primary"} fontSize="sm">{message}</Text>
        {body && <Text color={isRead ? "foreground.secondary" : "foreground.primary"} fontSize="sm">{body}</Text>}
      </VStack>
    </HStack>
    <HStack justifyContent="space-between">
      <Text fontSize="sm"
        marginLeft="14px"
        color="gray">{notificationTime}</Text>
      <ButtonGroup
        flex="0 0 auto"
        onDoubleClick={(e) => e.stopPropagation()}
        size="xs"
        alignSelf="flex-end"
      >
        {/* {mapActionsToButtons(actions, 1)} */}
      </ButtonGroup>
    </HStack>
  </VStack>
}

