import { Flex } from "@chakra-ui/react";
import { AnimatePresence, motion } from "framer-motion";
import * as React from "react";
import { LayoutContext, layoutAnimationTransition } from "./useLayoutState";

/** Main Content */
export const MainContent = ({ children, isRightSidebarOpen, rightSidebarWidth }) => {
  const {
    toolbarHeight,
    mainContentRef,
    isResizingLayout,
    unsavedRightSidebarWidth
  } = React.useContext(LayoutContext);

  const localSidebarWidth = unsavedRightSidebarWidth || rightSidebarWidth;

  return (
    // AnimatePresence is required to prevent
    // the content from jumping when the sidebar starts open
    <AnimatePresence initial={false}>
      <Flex
        zIndex={1}
        ref={mainContentRef}
        as={motion.div}
        key="main content"
        flex="1 1"
        position="relative"
        flexDir="column"
        alignItems="center"
        justifyContent="stretch"
        display="flex"
        overflowY="auto"
        sx={{
          "--app-header-height": toolbarHeight,
        }}
        animate={{
          paddingRight: isRightSidebarOpen ? localSidebarWidth + "vw" : 0,
          transition: isResizingLayout ? {
            ...layoutAnimationTransition,
            mass: 0,
          } : layoutAnimationTransition
        }}
      >
        {children}
      </Flex>
    </AnimatePresence>
  );
};
