import { Box, Text, VStack } from "@chakra-ui/react";
import { motion } from "framer-motion";
import * as React from "react";
import { AnimatePresence } from "framer-motion";
import { LayoutContext, layoutAnimationProps } from "./useLayoutState";

/** Right Sidebar */
export const RightSidebar = () => {
  const {
    rightSidebarWidth,
    hasRightSidebar,
    toolbarHeight
  } = React.useContext(LayoutContext);

  return (
    <AnimatePresence initial={false}>
      {hasRightSidebar && (
        <Box
          as={motion.div}
          {...layoutAnimationProps(rightSidebarWidth)}
          overflowY="auto"
          position="fixed"
          height="100vh"
          borderLeft="1px solid"
          borderColor="separator.border"
          inset={0}
          pt={`calc(${toolbarHeight} + 1rem)`}
          left="auto"
          width={rightSidebarWidth}
        >
          <VStack width={rightSidebarWidth}>
            {new Array(5).fill(true).map((_, index) => (
              <Text key={index}>Cool thing</Text>
            ))}
          </VStack>
        </Box>
      )}
    </AnimatePresence>
  );
};
