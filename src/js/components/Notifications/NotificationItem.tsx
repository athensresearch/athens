import React from 'react';
import { ArchiveIcon, ArrowLeftOnBoxIcon, ArrowRightIcon } from "@/Icons/Icons";
import { mapActionsToButtons } from "@/utils/mapActionsToButtons";
import { Box, ButtonGroup, HStack, MenuGroup, MenuItem, Text, VStack } from "@chakra-ui/react";
import { motion } from "framer-motion";
import { ContextMenuContext } from "@/App/ContextMenuContext";

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

const NotificationStatusIndicator = ({ isRead, }) => {
  return <Box
    flexShrink={0}
    borderRadius="100%"
    position="relative"
    top="-0.15ch"
    w="0.5em"
    h="0.5em"
    {...isRead ? {
      bg: "transparent",
      boxShadow: 'inset 0 0 0 1px foreground.secondary'
    } : {
      bg: "info",
    }}
  />
}

export const NotificationItem = (props) => {
  const { notification, ...otherProps } = props;
  const { id, isRead, type, isArchived, body, object, notificationTime } = notification;
  const { onOpenItem, onMarkAsRead, onMarkAsUnread, onArchive, onUnarchive, ...boxProps } = otherProps;
  const { addToContextMenu, getIsMenuOpen } = React.useContext(ContextMenuContext);
  const ref = React.useRef(null);
  const isMenuOpen = getIsMenuOpen(ref);

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

  const ContextMenuItems = () => {
    return <MenuGroup>
      <MenuItem onClick={() => onOpenItem(object.parentUid, id)} icon={<ArrowLeftOnBoxIcon />}>Open {object.name ? "page" : "block"}</MenuItem>
      {isRead
        ? <MenuItem onClick={() => onMarkAsUnread(id)} icon={<ArchiveIcon />}>Mark as unread</MenuItem>
        : <MenuItem onClick={() => onMarkAsRead(id)} icon={<ArchiveIcon />}>Mark as read</MenuItem>}
      <MenuItem onClick={() => onArchive(id)} icon={<ArchiveIcon />}>Archive</MenuItem>
    </MenuGroup>
  }

  return <Box
    key={id + notificationTime}
    layout
    initial={{
      height: 0,
      opacity: 0,
    }}
    animate={{
      height: "auto",
      opacity: 1,
    }}
    exit={{
      height: 0,
      opacity: 0,
    }}
    ref={ref}
    flexShrink={0}
    overflow="hidden"
    as={motion.div}
  ><VStack
    p={2}
    spacing={1}
    align="stretch"
    userSelect="none"
    boxShadow={isMenuOpen ? "focusInset" : "none"}
    borderRadius="md"
    bg={"interaction.surface"}
    color={isRead ? "foreground.secondary" : "foreground.primary"}
    _hover={{
      cursor: "pointer",
      bg: "interaction.surface.hover"
    }}
    // onClick={(e) => { if (e?.button === 0) onOpenItem(object.parentUid, id) }}
    onContextMenu={(e) => {
      addToContextMenu({ event: e, component: ContextMenuItems, ref });
    }}
    {...boxProps}
  >
      <HStack
        align="baseline"
        textAlign="left"
        spacing={1.5}
      >
        <NotificationStatusIndicator isRead={isRead} />
        <VStack flexShrink={1} spacing={0} align="stretch">
          <Text fontSize="sm">{messageForNotification(notification)}</Text>
          {body && <Text fontSize="sm">{body}</Text>}
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
          {mapActionsToButtons(getActionsForNotification(notification), 1)}
        </ButtonGroup>
      </HStack>
    </VStack>
  </Box>
}

