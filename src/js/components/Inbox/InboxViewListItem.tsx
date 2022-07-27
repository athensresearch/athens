import React from 'react';
import {
  Box,
  HStack,
  ButtonGroup,
  VStack,
  Text
} from "@chakra-ui/react";
import { motion } from "framer-motion";
import { mapActionsToButtons } from '../utils/mapActionsToButtons';

const InboxItemStatusIndicator = ({ isRead, }) => {
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

const InboxViewListItemBody = ({ isRead, message, body }) => {
  return <HStack
    as="li"
    align="baseline"
    textAlign="left"
    spacing={1.5}
  >
    <InboxItemStatusIndicator isRead={isRead} />
    <VStack flexShrink={1} spacing={0} align="stretch">
      <Text color={isRead ? "foreground.secondary" : "foreground.primary"} fontSize="sm">{message}</Text>
      {body && <Text color={isRead ? "foreground.secondary" : "foreground.primary"} fontSize="sm">{body}</Text>}
    </VStack>
  </HStack>
}

export const InboxViewListItem = (props): JSX.Element => {
  const {
    isSelected, onOpen, onMarkAsRead, onMarkAsUnread, onMarkAsArchived, onMarkAsUnarchived, onIncSelection, onDecSelection, onSelect, onDeselect, ...notificationProps } = props;
  const { id, isRead, isArchived, message, body, actions, object, notificationTime } = notificationProps;
  const ref = React.useRef();



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
          _hover={{
            cursor: "pointer"
          }}
          borderRadius="md"
          bg={"interaction.surface"}
          onClick={() => onOpen(object.parentUid, id)}
        >
          <InboxViewListItemBody
            isArchived={isArchived}
            isRead={isRead}
            message={message}
            body={body}
          />
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
              {mapActionsToButtons(actions, 1)}
            </ButtonGroup>
          </HStack>
        </VStack>
      </Box>
    </>);
};
