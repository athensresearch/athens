import { Flex } from "@chakra-ui/react";
import { AnimatePresence, motion } from "framer-motion";
import * as React from "react";
import { LayoutContext, layoutAnimationTransition } from "./useLayoutState";

/** Main Content */
export const MainContent = ({ children }) => {
  const {
    toolbarHeight,
    mainContentRef,
    hasRightSidebar,
    rightSidebarWidth
  } = React.useContext(LayoutContext);

  return (
    <AnimatePresence initial={false}>
      <Flex
        ref={mainContentRef}
        as={motion.div}
        key="main content"
        sx={{
          "--app-header-height": toolbarHeight,
        }}
        animate={{
          paddingRight: hasRightSidebar ? rightSidebarWidth : 0,
          transition: layoutAnimationTransition
        }}
        flex={1}
        justifyContent="center"
        overflowY="auto"
      >
        {children}
      </Flex>
    </AnimatePresence>
  );
};
