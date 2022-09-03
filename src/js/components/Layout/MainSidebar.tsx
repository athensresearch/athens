import { Box, Collapse, Flex, Heading, IconButton, Text, useDisclosure, VStack } from "@chakra-ui/react";
import { motion } from "framer-motion";
import * as React from "react";
import { AnimatePresence } from "framer-motion";
import { LayoutContext, layoutAnimationProps } from "./useLayoutState";
import { ChevronDownIcon, ChevronLeftIcon } from "@/Icons/Icons";

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
          height="100vh"
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

export const SidebarSectionHeading = (props) => {
  const { children, ...rest } = props;
  return <Heading
    color="foreground.secondary"
    size="xs"
    flex={1}
    {...rest}
  >{children}</Heading>
}

export const SidebarSection = (props) => {
  const { isOpen, children, title, count, ...rest } = props;
  const { isOpen: _isOpen, onClose, onOpen } = useDisclosure({ defaultIsOpen: isOpen })

  return (
    <VStack spacing={0} align="stretch" flex="0 0 auto" {...rest}>
      <Flex align="center" gap={1}>
        {typeof title === "string" ? <SidebarSectionHeading>{title}</SidebarSectionHeading> : title}
        {count && <Text fontSize="xs" color="foreground.secondary">{count}</Text>}
        <IconButton variant="ghost" colorScheme="subtle" size="xs" icon={_isOpen ? <ChevronDownIcon /> : <ChevronLeftIcon />} aria-label="Toggle group" onClick={_isOpen ? onClose : onOpen} />
      </Flex>
      <Collapse in={_isOpen}>
        {children}
      </Collapse>
    </VStack>
  )

}