import { Box, Flex } from "@chakra-ui/react";
import { motion } from "framer-motion";
import * as React from "react";
import { AnimatePresence } from "framer-motion";
import { LayoutContext, layoutAnimationProps } from "./useLayoutState";

/**
 * Main Sidebar
 */
export const MainSidebar = (props) => {
  const { children, isMainSidebarOpen } = props;
  const {
    mainSidebarWidth,
    toolbarHeight,
    isMainSidebarFloating
  } = React.useContext(LayoutContext);

  return (
    <AnimatePresence initial={false}>
      {isMainSidebarOpen && (
        <Box
          as={motion.div}
          userSelect="none"
          key="main sidebar"
          bg="background.upper"
          transitionProperty="background"
          transitionTimingFunction="ease-in-out"
          transitionDuration="fast"
          pt={toolbarHeight}
          height="var(--app-height)"
          position="sticky"
          overflow="hidden"
          overscrollBehavior="contain"
          top={0}
          bottom={0}
          {...layoutAnimationProps(mainSidebarWidth + "px")}
          {...(isMainSidebarFloating && {
            position: "absolute",
            inset: 0,
            right: "auto"
          })}
        >
          <Flex
            flexDirection="column"
            height="100%"
            overflowY="auto"
            overscrollBehavior="contain"
            width={mainSidebarWidth + "px"}
            pt={4}
          >
            {children}
          </Flex>
        </Box>
      )}
    </AnimatePresence>
  );
};
