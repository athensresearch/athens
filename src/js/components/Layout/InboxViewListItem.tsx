import React from 'react';
import {
  Box,
  HStack,
  Divider,
  ButtonGroup,
  VStack,
  Text
} from "@chakra-ui/react";
import { ArchiveIcon } from '@/Icons/Icons';
import { motion } from "framer-motion";
import { useContextMenu } from '@/utils/useContextMenu';
import { mapActionsToMenuList } from '../utils/mapActionsToMenuList';
import { mapActionsToButtons } from '../utils/mapActionsToButtons';

const InboxItemStatusIndicator = ({ isRead, isArchived }) => {
//  if (isArchived) {
//    return <ArchiveIcon
//      flexShrink={0}
//      position="relative"
//      top="-0.15ch"
//      transform="scale(1.5)"
//      color="foreground.secondary"
//      boxSize="0.5em"
//    />
//  } else {
//    return <Box
//      flexShrink={0}
//      borderRadius="100%"
//      position="relative"
//      top="-0.15ch"
//      w="0.5em"
//      h="0.5em"
//      {...isRead ? {
//        bg: "transparent",
//        boxShadow: 'inset 0 0 0 1px foreground.secondary'
//      } : {
//        bg: "info",
//      }}
//    />
//  }
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

const InboxViewListItemBody = ({ isRead, isArchived, message, body }) => {
  return <HStack
    as="li"
    align="baseline"
    textAlign="left"
    spacing={1.5}
  >
    <InboxItemStatusIndicator isRead={isRead} isArchived={isArchived} />
    <VStack flexShrink={1} spacing={0} align="stretch">
      <Text color={isRead ? "foreground.secondary" : "foreground.primary"} fontSize="sm">{message}</Text>
      {body && <Text color={isRead ? "foreground.secondary" : "foreground.primary"} fontSize="sm">{body}</Text>}
    </VStack>
  </HStack>
}

export const InboxViewListItemActions = ({ children }) => {
  return <>{children}</>
}

export const InboxViewListItem = (props): JSX.Element => {
  const {
    isSelected, onOpen, onMarkAsRead, onMarkAsUnread, onMarkAsArchived, onMarkAsUnarchived, onIncSelection, onDecSelection, onSelect, onDeselect, ...notificationProps } = props;
  const { id, isRead, isArchived, message, body, actions, object } = notificationProps;
  const ref = React.useRef();

  const { isOpen: isContextMenuOpen, ContextMenu, menuSourceProps } = useContextMenu({
    menuProps: { size: "sm" },
    ref, source: "cursor"
  });

  const menuList = mapActionsToMenuList(actions);

  return (
    <>
      <Box
        layout
        as={motion.div}
        key={id}
        bg="inherit"
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
      >
        <VStack
          p={2}
          mt={2}
          spacing={1}
          flexShrink={0}
          overflow="hidden"
          ref={ref}
          align="stretch"
          userSelect="none"
//          onClick={(e) => {
//            e.preventDefault();
//            if (onSelect && onDeselect) {
//              isSelected ? onDeselect() : onSelect(id, ref);
//            }
//          }}
          _hover={{
            cursor: "pointer"
          }}
          borderRadius="md"
          bg={(isContextMenuOpen || isSelected) ? "interaction.surface.active" : "interaction.surface"}
          onClick={() => onOpen(object.parentUid, id)}
          {...menuSourceProps}
        >
          <InboxViewListItemBody
            isArchived={isArchived}
            isRead={isRead}
            message={message}
            body={body}
          />
          <HStack justifyContent="flex-end">
            <ButtonGroup
              flex="0 0 auto"
              onDoubleClick={(e) => e.stopPropagation()}
              size="xs"
              alignSelf="flex-end"
            >
              {mapActionsToButtons(actions, 1)}
            </ButtonGroup>
          </HStack>
        </VStack>
        {/*<ContextMenu>
          {menuList}
        </ContextMenu>*/}
      </Box>
    </>);
};
