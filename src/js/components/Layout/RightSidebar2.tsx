import { Box, Text, VStack, HStack } from "@chakra-ui/react";
import { motion, useMotionValue, useTransform } from "framer-motion";
import * as React from "react";
import { AnimatePresence } from "framer-motion";
import { LayoutContext, layoutAnimationProps } from "./useLayoutState";

const MIN_WIDTH = 0;
const MAX_WIDTH = 1000;

/** Right Sidebar */
export const RightSidebar = ({ children, onResizeSidebar }) => {
  const {
    rightSidebarWidth,
    setRightSidebarWidth,
    hasRightSidebar,
    toolbarHeight
  } = React.useContext(LayoutContext);

  const [localWidth, setLocalWidth] = React.useState(rightSidebarWidth);

  const startResizing = () => {
    document.body.style.cursor = "col-resize";
  }

  const stopResizing = () => {
    document.body.style.cursor = "default";
    setRightSidebarWidth(localWidth);
  }

  console.log(localWidth);

  // const defaultAnimationProps = layoutAnimationProps(mvWidth.get() + "px");
  // const animationProps = {
  //   ...defaultAnimationProps,
  //   animate: {
  //     ...defaultAnimationProps.animate,
  //     transition: defaultAnimationProps.animate.transition
  //   }
  // }

  return (
    <AnimatePresence initial={false}>
      {hasRightSidebar && (
        <HStack
          as={motion.div}
          // {...animationProps}
          {...layoutAnimationProps(rightSidebarWidth + "px")}
          spacing={0}
          overflowY="auto"
          align="stretch"
          border="2px solid red"
          position="fixed"
          height="100vh"
          inset={0}
          pt={`calc(${toolbarHeight} + 1rem)`}
          left="auto"
        >
          <motion.div
            style={{
              background: "white",
              height: "100%",
              width: "2px",
              cursor: "col-resize",
              position: "absolute",
              top: 0,
              bottom: 0,
              left: 0,
              zIndex: 1,
            }}
            drag="x"
            dragElastic={0.025}
            dragMomentum={false}
            whileTap="active"
            whileHover="active"
            onDrag={(event, info) => {
              const { delta } = info;
              const newWidth = localWidth - delta.x;
              setLocalWidth(newWidth);
              setRightSidebarWidth(newWidth);
            }}
            onPointerDown={startResizing}
            onPointerUp={stopResizing}
            onPanEnd={stopResizing}
            onTap={stopResizing}
          />
          <Box overflow="hidden" flex="1 1 100%">
            {children}
          </Box>
        </HStack>
      )}
    </AnimatePresence>
  );
};
