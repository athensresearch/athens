import * as React from "react";
import { LayoutContext, layoutAnimationProps } from "./useLayoutState";
import { AnimatePresence, motion } from 'framer-motion';
import { XmarkIcon, ChevronRightIcon, PageIcon, PageFillIcon, BlockIcon, BlockFillIcon, GraphIcon } from '@/Icons/Icons';
import { Button, IconButton, HStack, Box, Collapse, VStack } from '@chakra-ui/react';

/** Right Sidebar */
export const RightSidebar = ({ children }) => {
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
          {...layoutAnimationProps(rightSidebarWidth + "px")}
          overflowY="auto"
          borderLeft="1px solid"
          borderColor="separator.divider"
          position="fixed"
          height="100vh"
          inset={0}
          pt={`calc(${toolbarHeight} + 1rem)`}
          left="auto"
        >
          <Box overflow="hidden" width={rightSidebarWidth + "px"}>
            {children}
          </Box>
        </Box>
      )}
    </AnimatePresence>
  );
};

const typeIcon = (type, isOpen) => {
  return isOpen ? { "node": <PageFillIcon />, "graph": <GraphIcon />, "block": <BlockFillIcon /> }[type]
    : { "node": <PageIcon />, "graph": <GraphIcon />, "block": <BlockIcon /> }[type];
};

export const SidebarItem = ({ title, type, isOpen, onToggle, onRemove, children }) => {
  const canToggle = type !== 'graph';
  return (
    <VStack
      align="stretch"
      position="relative"
      spacing={0}
      ml="1px" // Account for the floating separator
      _notFirst={{
        borderTop: "1px solid",
        borderColor: "separator.divider"
      }}>
      <Box
        top="-1px"
        zIndex={2}
        position="sticky"
        background="background.floor"
        display="grid"
        gridTemplateColumns="1fr 3rem"
        pr={2}
        alignItems="center"
        justifyContent="center"
      >
        <Button
          onClick={canToggle ? onToggle : undefined}
          as={canToggle ? undefined : 'div'}
          {...(!canToggle && {
            _hover: {},
            _focus: {},
            _active: {},
          })}
          display="flex"
          bg="transparent"
          borderRadius="0"
          gap={2}
          py={3}
          pl={5}
          pr={0}
          height="auto"
          textAlign="left"
          overflow="hidden"
          whiteSpace="nowrap"
          sx={{ maskImage: "linear-gradient(to right, black, black calc(100% - 1rem), transparent calc(100%))" }}
        >
          {canToggle && (
            <ChevronRightIcon
              transform={isOpen ? "rotate(90deg)" : null}
              transitionProperty="common"
              transitionDuration="0.15s"
              transitionTimingFunction="ease-in-out"
              justifySelf="center" />
          )}
          {typeIcon(type, isOpen)}
          <Box
            flex="1 1 100%"
            tabIndex={-1}
            pointerEvents="none"
            position="relative"
            bottom="1px"
            overflow="hidden"
            color="foreground.secondary"
          >{title}</Box>
        </Button>
        <IconButton
          onClick={onRemove}
          size="sm"
          color="foreground.secondary"
          alignSelf="center"
          justifySelf="center"
          bg="transparent"
          aria-label="Close"
        >
          <XmarkIcon />
        </IconButton>
      </Box>
      <Box
        as={Collapse}
        in={isOpen}
        className={`${type}-page`}
        animateOpacity
        unmountOnExit
        zIndex={1}
        px={4}
      >
        {children}
      </Box>
    </VStack>);
};
