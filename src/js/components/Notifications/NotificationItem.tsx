import React from 'react';
import { BulletIcon, ArchiveIcon, ArrowLeftOnBoxIcon, CheckboxIcon, UnreadIcon } from "@/Icons/Icons";
import { useTheme, Button, Flex, Box, ButtonGroup, HStack, MenuGroup, MenuItem, Text, VStack } from "@chakra-ui/react";
import { motion } from "framer-motion";
import { ContextMenuContext } from "@/App/ContextMenuContext";

const verbForType = (type: string): string => {
  if (type === "Created") {
    return "created";
  } else if (type === "Edited") {
    return "edited";
  } else if (type === "Deleted") {
    return "deleted";
  } else if (type === "Comments") {
    return "commented on";
  } else if (type === "Mentions") {
    return "mentioned you in";
  } else if (type === "Assignments") {
    return "assigned you to";
  } else if (type === "Completed") {
    return "completed";
  }
}

export const NotificationItem = (props) => {
  const { notification, ...otherProps } = props;
  const { id, isRead, body, object, notificationTime } = notification;
  const { onOpenItem, onMarkAsRead, onMarkAsUnread, onArchive, onUnarchive, ...boxProps } = otherProps;
  const { addToContextMenu, getIsMenuOpen } = React.useContext(ContextMenuContext);
  const ref = React.useRef(null);
  const isMenuOpen = getIsMenuOpen(ref);
  const theme = useTheme();
  const indicatorHeight = `calc(${theme.lineHeights.base} * ${theme.fontSizes.md})`;

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
    key={id + notificationTime}
    ref={ref}
    flexShrink={0}
    overflow="hidden"
  ><VStack
    className="notification"
    p={2}
    spacing={1}
    align="stretch"
    userSelect="none"
    transitionProperty="common"
    transitionDuration="fast"
    transitionTimingFunction="ease-in-out"
    border="1px solid"
    // borderColor={isRead ? "separator.divider" : "transparent"}
    borderColor={"separator.divider"}
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
        <Flex
          width={2}
          flexShrink={0}
          placeItems="center"
          placeContent="center"
          height={indicatorHeight}
        >
          {isRead ? (null) : (
            <BulletIcon
              fontSize="3xl"
              color="info"
            />
          )}
        </Flex>
        <VStack flexShrink={1} spacing={0} align="stretch">
          <Text fontWeight="bold" noOfLines={2} fontSize="sm">
            {notification.subject.username.slice(1)} {verbForType(notification.type)} "{object.name || object.string}"
          </Text>
          {body && <Text fontSize="sm">{body}</Text>}
        </VStack>
      </HStack>
      <HStack justifyContent="space-between" >
        <Text fontSize="sm"
          marginLeft="14px"
          color="gray">{notificationTime}</Text>
        <ButtonGroup
          transitionProperty="common"
          transitionDuration="fast"
          transitionTimingFunction="ease-in-out"
          sx={{
            opacity: 0,
            ".notification:hover &": {
              opacity: 1
            }
          }}
          variant="ghost"
          flex="0 0 auto"
          onClick={(e) => e.stopPropagation()}
          size="xs"
          alignSelf="flex-end"
        >
          <Button onClick={() => onArchive(id)} leftIcon={<ArchiveIcon />}>Archive</Button>
        </ButtonGroup>
      </HStack>
    </VStack>
  </Box>
}

