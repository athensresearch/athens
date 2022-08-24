import React from 'react';
import {
  Flex,
} from "@chakra-ui/react";
import { LayoutGroup, AnimatePresence } from "framer-motion";

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

