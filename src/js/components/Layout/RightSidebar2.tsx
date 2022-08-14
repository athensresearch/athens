import { Box, Text, VStack, HStack } from "@chakra-ui/react";
import { motion } from "framer-motion";
import * as React from "react";
import { AnimatePresence } from "framer-motion";
import { LayoutContext, layoutAnimationProps } from "./useLayoutState";

const MIN_WIDTH = 100;
const MAX_WIDTH = 500;

/** Right Sidebar */
export const RightSidebar = ({ children, onResizeSidebar }) => {
  const {
    rightSidebarWidth,
    setRightSidebarWidth,
    hasRightSidebar,
    toolbarHeight
  } = React.useContext(LayoutContext);

  const [isDragging, setIsDragging] = React.useState(false);
  const draggedWidth = React.useRef(0)

  const defaultAnimationProps = layoutAnimationProps(rightSidebarWidth + "px");

  const animationProps = {
    ...defaultAnimationProps,
    animate: {
      ...defaultAnimationProps.animate,
      transition: isDragging
        ? { type: "tween", duration: 0 }
        : defaultAnimationProps.animate.transition
    }
  }

  return (
    <AnimatePresence initial={false}>
      {hasRightSidebar && (
        <HStack
          as={motion.div}
          {...animationProps}
          spacing={0}
          overflowY="auto"
          position="fixed"
          height="100vh"
          inset={0}
          pt={`calc(${toolbarHeight} + 1rem)`}
          left="auto"
        >
          <Box
            alignSelf="stretch"
            zIndex={10}
            width="5px"
            background="white"
            position="relative"
            _hover={{
              bg: "foreground.primary"
            }}
            bg={
              isDragging ? "link" :
                "separator.divider"
            }
          >
            <motion.div
              drag="x"
              style={{
                position: "absolute",
                opacity: 0,
                inset: 0
              }}
              dragElastic={false}
              dragMomentum={false}
              onDrag={
                (event, info) => {
                  if (!isDragging) { setIsDragging(true) };
                  const width = window.innerWidth - info.point.x;
                  if (width !== draggedWidth.current) {
                    console.log(width)
                    setRightSidebarWidth(width);
                  }
                  draggedWidth.current = width;
                }
              }
              onDragEnd={() => setIsDragging(false)}
            />
          </Box>
          <Box overflow="hidden" flex={1}>
            {children}
          </Box>
        </HStack>
      )}
    </AnimatePresence>
  );
};
