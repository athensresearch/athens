import React from 'react';
import { BulletIcon, ArchiveIcon, ArrowLeftOnBoxIcon, CheckboxIcon, UnreadIcon } from "@/Icons/Icons";
import { useTheme, Button, Flex, Box, ButtonGroup, HStack, MenuGroup, MenuItem, Text, VStack } from "@chakra-ui/react";
import { ContextMenuContext } from "@/App/ContextMenuContext";

export const NotificationItem = (props) => {
  const { notification, children, ...otherProps } = props;
  const { id, isRead, object, notificationTime } = notification;
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

  return (<VStack
    className="notification"
    overflow="hidden"
    flexShrink={0}
    key={id + notificationTime}
    ref={ref}
    p={2}
    spacing={1}
    align="stretch"
    userSelect="none"
    transitionProperty="common"
    transitionDuration="fast"
    transitionTimingFunction="ease-in-out"
    border="1px solid"
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
        {children}
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
  </VStack>)
}

