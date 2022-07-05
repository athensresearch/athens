import React from 'react';
import {
  Box,
  HStack,
  Flex,
  MenuItem,
  MenuList,
  Menu,
  IconButton,
  MenuButton,
  ButtonGroup,
  Button,
  VStack,
  Text,
  Heading,
} from "@chakra-ui/react";
import { LayoutGroup, AnimatePresence, motion } from "framer-motion";
import { EllipsisHorizontalIcon } from "@/Icons/Icons";
import { useContextMenu } from '@/utils/useContextMenu';

export const InboxView = ({ children }) => {
  return <HStack
    align="stretch"
    flex="1 1 100%"
    gridArea="main-content"
    pt="calc(var(--app-header-height) + 1rem)"
    height="100vh"
    width="100vw"
    px={4}
  >
    {children}
  </HStack>
}

export const InboxViewListItemBody = ({ isRead, message }) => {
  return <HStack
    align="baseline"
    textAlign="left"
    spacing={1.5}
  >
    <Box
      flexShrink={0}
      bg={isRead ? "transparent" : "info"}
      borderRadius="100%"
      position="relative"
      top="-0.15ch"
      w="0.5em"
      h="0.5em"
    />
    <VStack spacing={0}>
      <Text fontSize="sm">{message}</Text>
    </VStack>
  </HStack>
}

export const InboxViewListItemActions = ({ children }) => {
  return <>{children}</>
}

export const InboxViewListGroupHeader = ({ title, count }) => {
  return <Box
    layout
    as={motion.div}
    key={title}
    animate={{
      height: "auto",
      opacity: 1,
    }}
    exit={{
      height: 0,
      opacity: 0,
    }}
  >
    <HStack
      justifyContent="space-between"
      position="sticky"
      top={0}
      bg="background.floor"
      pt={4}
    >
      <Heading color="foreground.secondary" size="sm">{title}</Heading>
      <Text color="foreground.secondary">{count}</Text>
    </HStack>
  </Box>
}

export const InboxViewListGroup = ({ children }) => {
  return <>{children}</>
}

const mapActionsToMenuList = (actions) => {
  return <MenuList>
    {actions.map(action => <MenuItem key={action.label} onClick={action.fn}>{action.label}</MenuItem>)}
  </MenuList>
}

const mapActionsToButtons = (actions, limitShown: number) => {
  const showableActions = actions.slice(0, limitShown);
  const overflowActions = actions.slice(limitShown);

  return <>
    {showableActions.map(action => <Button key={action.label} onClick={(e) => {
      e.stopPropagation();
      action.fn(e)
    }}>{action.label}</Button>)}

    {!!overflowActions.length &&
      <Menu size="sm" isLazy>
        <IconButton variant="ghost" aria-label="Menu" as={MenuButton} icon={<EllipsisHorizontalIcon />} />
        {mapActionsToMenuList(overflowActions)}
      </Menu>}
  </>
}

export const InboxViewListItem = (props): JSX.Element => {
  const {
    isSelected,
    onOpen,
    onMarkAsRead,
    onMarkAsUnread,
    onMarkAsArchived,
    onMarkAsUnarchived,
    onIncSelection,
    onDecSelection,
    onSelect,
    onDeselect,
    ...notificationProps } = props;
  const { id, isRead, isArchived, message, actions } = notificationProps;
  const ref = React.useRef();

  const [isButtonMenuOpen, setIsButtonMenuOpen] = React.useState(false);
  const { isOpen: isContextMenuOpen, ContextMenu, menuSourceProps } = useContextMenu({
    menuProps: { size: "sm" },
    ref, source: "cursor"
  })
  const isAnyMenuOpen = isButtonMenuOpen || isContextMenuOpen;

  const menuList = mapActionsToMenuList(actions)

  return (
    <>
      <Box
        layout
        as={motion.div}
        key={id}
        bg="background.floor"
        animate={{
          height: "auto",
          opacity: 1,
        }}
        exit={{
          height: 0,
          opacity: 0,
        }}
      >
        <VStack
          p={2}
          mt={2}
          flexShrink={0}
          overflow="hidden"
          ref={ref}
          align="stretch"
          userSelect="none"
          onClick={(e) => {
            console.log(e);
            e.preventDefault();
            isSelected ? onDeselect() : onSelect(id, ref)
          }}
          borderRadius="md"
          bg={(isAnyMenuOpen || isSelected) ? "interaction.surface.active" : "interaction.surface"}
          onDoubleClick={() => onOpen(id)}
          {...menuSourceProps}
        >
          <InboxViewListItemBody
            isRead={isRead}
            message={message}
          />
          <InboxViewListItemActions>
            <ButtonGroup
              onDoubleClick={(e) => e.stopPropagation()}
              size="xs"
              alignSelf="flex-end"
            >
              {mapActionsToButtons(actions, 1)}
            </ButtonGroup>
          </InboxViewListItemActions>
        </VStack>
        <ContextMenu>
          {menuList}
        </ContextMenu>
      </Box>
    </>)
}

export const InboxViewListHeader = ({ title, subtitle, actions }): JSX.Element => {
  return <HStack
    p={2}
    align="flex-end"
    justifyContent="space-between"
    borderBottom="1px solid"
    borderColor="separator.divider"
  >
    <Box>
      <Heading>{title}</Heading>
      <Text color="foreground.secondary">{subtitle}</Text>
    </Box>
    {actions}
  </HStack>
}

export const InboxViewListBody = ({ children }): JSX.Element => {
  return <Flex
    flexDirection="column"
    alignItems="stretch"
    height="100%"
    overflowY="auto"
    position="relative"
    zIndex={0}
    px={2}
    pb={4}
  >
    <LayoutGroup>
      <AnimatePresence initial={false}>
        {children}
      </AnimatePresence>
    </LayoutGroup>
  </Flex>
}

export const InboxViewList = ({ children }) => {
  return <VStack
    align="stretch"
    flexBasis="30em"
    flexShrink={1}
    flexGrow={0}
    spacing={0}
  >
    {children}
  </VStack>
}

export const InboxViewContent = ({ children }) => {
  return <Box flex={1}>{children}</Box>
}