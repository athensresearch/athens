import React from 'react';
import {
  Box,
  HStack,
  Flex,
  VStack,
  Text,
  Heading
} from "@chakra-ui/react";
import { LayoutGroup, AnimatePresence } from "framer-motion";

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


export const InboxViewListGroupHeader = ({ title, count}) => {
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
      {subtitle && <Text color="foreground.secondary">{subtitle}</Text>}
    </Box>
    {actions}
  </HStack>
}

export const InboxViewListBody = ({ children }): JSX.Element => {
  return <Flex
    as="ol"
    flexDirection="column"
    bg="inherit"
    alignItems="stretch"
    flex="1 1 100%"
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
    overflow="hidden"
  >
    {children}
  </VStack>
}

export const InboxViewContent = ({ children }) => {
  return <Box flex={1}>{children}</Box>
}