import React from 'react';
import { BulletIcon, ArchiveIcon, ArrowLeftOnBoxIcon, CheckboxIcon, UnreadIcon } from "@/Icons/Icons";
import { mapActionsToButtons } from "@/utils/mapActionsToButtons";
import { useTheme, Flex, Box, ButtonGroup, HStack, MenuGroup, MenuItem, Text, VStack } from "@chakra-ui/react";
import { motion } from "framer-motion";
import { ContextMenuContext } from "@/App/ContextMenuContext";

const Subject = ({ children }) => {
  return (
    <Text as="span" fontWeight="bold" noOfLines={2}>{children}</Text>
  );
}

const messageForNotification = (notification: NOTIFICATION): React.ReactNode => {
  const { type, subject, object } = notification;
  const subjectName = subject.username;
  const objectName = object.string || object.name;

  if (type === "Created") {
    return <Subject>{subjectName} created {objectName}</Subject>;
  } else if (type === "Edited") {
    return <Subject>{subjectName} edited {objectName}</Subject>;
  } else if (type === "Deleted") {
    return <Subject>{subjectName} deleted {objectName}</Subject>;
  } else if (type === "Comments") {
    return <Subject>{subjectName} commented on {objectName}</Subject>;
  } else if (type === "Mentions") {
    return <Subject>{subjectName} mentioned you in  {objectName}</Subject>;
  } else if (type === "Assignments") {
    return <Subject>{subjectName} assigned you to {objectName}</Subject>;
  } else if (type === "Completed") {
    return <Subject>{subjectName} completed {objectName}</Subject>;
  }
}

const NotificationStatusIndicator = ({ isRead }) => {
  const theme = useTheme();

  const height = `calc(${theme.lineHeights.base} * ${theme.fontSizes.md})`;

  return <Flex
    width={2}
    flexShrink={0}
    placeItems="center"
    placeContent="center"
    height={height}
  >
    {isRead ? (null) : (
      <BulletIcon
        fontSize="3xl"
        color="info"
      />
    )}
  </Flex>
}

export const NotificationItem = (props) => {
  const { notification, ...otherProps } = props;
  const { id, isRead, body, object, notificationTime } = notification;
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
        ? <MenuItem onClick={() => onMarkAsUnread(id)} icon={<UnreadIcon />}>Mark as unread</MenuItem>
        : <MenuItem onClick={() => onMarkAsRead(id)} icon={<CheckboxIcon />}>Mark as read</MenuItem>}
      <MenuItem onClick={() => onArchive(id)} icon={<ArchiveIcon />}>Archive</MenuItem>
    </MenuGroup>
  }

  return <Box
    key={id}
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
    bg={isRead ? "transparent" : "interaction.surface"}
    color={isRead ? "foreground.secondary" : "foreground.primary"}
    _hover={{
      cursor: "pointer",
      bg: "interaction.surface.hover"
    }}
    onClick={(e) => { if (e?.button === 0) onOpenItem(object.parentUid, id) }}
    onContextMenu={(e) => {
      addToContextMenu({ event: e, component: ContextMenuItems, ref });
    }}
    {...boxProps}
  >
      <HStack
        alignItems="flex-start"
        textAlign="left"
        spacing={1.5}
      >
        <NotificationStatusIndicator isRead={isRead} />
        <VStack flexShrink={1} spacing={0} align="stretch">
          <Text fontSize="sm">{messageForNotification(notification)}</Text>
          {body && <Text fontSize="sm">{body}</Text>}
        </VStack>
      </HStack>
      <HStack justifyContent="space-between" >
        <Text fontSize="sm"
          marginLeft="14px"
          color="gray">{notificationTime}</Text>
        <ButtonGroup
          variant="ghost"
          flex="0 0 auto"
          onClick={(e) => e.stopPropagation()}
          size="xs"
          alignSelf="flex-end"
        >
          {mapActionsToButtons(getActionsForNotification(notification), 1)}
        </ButtonGroup>
      </HStack>
    </VStack>
  </Box>
}

