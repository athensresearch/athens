import { Box, Text, Heading, VStack, Divider } from "@chakra-ui/react";
import { motion } from "framer-motion";
import * as React from "react";
import { AnimatePresence } from "framer-motion";
import { LayoutContext, layoutAnimationProps } from "./useLayoutState";

/**
 * Main Sidebar
 */
export const MainSidebar = (props) => {
  const { children } = props;
  const {
    sidebarWidth,
    viewMode,
    toolbarHeight,
    isMainSidebarFloating
  } = React.useContext(LayoutContext);

  return (
    <AnimatePresence initial={false}>
      {(viewMode === "full" || viewMode === "regular") && (
        <Box
          as={motion.div}
          key="main sidebar"
          bg="background.upper"
          pt={toolbarHeight}
          height="100vh"
          position="sticky"
          top={0}
          bottom={0}
          overflowY="auto"
          {...layoutAnimationProps(sidebarWidth)}
          {...(isMainSidebarFloating && {
            position: "absolute",
            inset: 0,
            right: "auto"
          })}
        >
          <Box width={sidebarWidth}>
            {children}
          </Box>
        </Box>
      )}
    </AnimatePresence>
  );
};
