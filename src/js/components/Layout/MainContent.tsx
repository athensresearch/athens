import { Box, Flex } from "@chakra-ui/react";
import { AnimatePresence, motion } from "framer-motion";
import * as React from "react";
import { LayoutContext, layoutAnimationTransition } from "./useLayoutState";
import { useInView } from 'react-intersection-observer';

/** Main Content */
export const MainContent = ({ children, isRightSidebarOpen }) => {
  const {
    toolbarHeight,
    mainContentRef,
    isResizingLayout,
    isScrolledPastTitle,
    setIsScrolledPastTitle,
    unsavedRightSidebarWidth
  } = React.useContext(LayoutContext);

  const { ref: markerRef, inView } = useInView({ threshold: 0 });

  React.useEffect(() => {
    if (inView) {
      if (isScrolledPastTitle["mainContent"]) {
        setIsScrolledPastTitle(prev => ({ ...prev, "mainContent": false }));
      }
    } else {
      if (!isScrolledPastTitle["mainContent"]) {
        setIsScrolledPastTitle(prev => ({ ...prev, "mainContent": true }));
      }
    }
  }, [inView, setIsScrolledPastTitle]);

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
          paddingRight: isRightSidebarOpen ? unsavedRightSidebarWidth + "vw" : 0,
          transition: isResizingLayout ? {
            ...layoutAnimationTransition,
            mass: 0,
          } : layoutAnimationTransition
        }}
      >
        <Box
          aria-hidden
          position="absolute"
          ref={markerRef}
          height="20px"
          top={0}
        />
        {children}
      </Flex>
    </AnimatePresence>
  );
};
