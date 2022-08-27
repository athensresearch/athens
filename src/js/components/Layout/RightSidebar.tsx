import * as React from "react";
import { LayoutContext, layoutAnimationProps, layoutAnimationTransition } from "./useLayoutState";
import { AnimatePresence, motion } from 'framer-motion';
import { XmarkIcon, ChevronRightIcon, PageIcon, PageFillIcon, BlockIcon, BlockFillIcon, GraphIcon, ArrowLeftOnBoxIcon } from '@/Icons/Icons';
import { Button, IconButton, Box, Collapse, VStack, BoxProps } from '@chakra-ui/react';

/** Right Sidebar */


interface RightSidebarProps extends BoxProps {
  isOpen: boolean;
  rightSidebarWidth: number;
}

export const RightSidebar = (props: RightSidebarProps) => {
  const { children, rightSidebarWidth, isOpen } = props;

  const {
    toolbarHeight,
    isResizingLayout,
    unsavedRightSidebarWidth
  } = React.useContext(LayoutContext);

  const localSidebarWidth = unsavedRightSidebarWidth || rightSidebarWidth;

  const layoutAnimation = {
    ...layoutAnimationProps(localSidebarWidth + "vw"),
    animate: {
      width: localSidebarWidth + "vw",
      opacity: 1,
      transition: isResizingLayout ? {
        ...layoutAnimationTransition,
        mass: 0,
      } : layoutAnimationTransition
    },
  }

  return (
    <AnimatePresence initial={false}>
      {isOpen && (
        <Box
          as={motion.div}
          {...layoutAnimation}
          zIndex={1}
          bg="background.floor"
          transitionProperty="background"
          transitionTimingFunction="ease-in-out"
          transitionDuration="fast"
          overflowX="hidden"
          overflowY="auto"
          borderLeft="1px solid"
          borderColor="separator.divider"
          position="fixed"
          height="100vh"
          inset={0}
          pt={`calc(${toolbarHeight} + 1rem)`}
          left="auto"
        >
          <Box width={localSidebarWidth + "vw"}>
            {children}
          </Box>
        </Box>
      )}
    </AnimatePresence>
  );
};

const typeIcon = (type, isOpen) => {
  return isOpen ? { "page": <PageFillIcon />, "graph": <GraphIcon />, "block": <BlockFillIcon /> }[type]
    : { "page": <PageIcon />, "graph": <GraphIcon />, "block": <BlockIcon /> }[type];
};

export const SidebarItem = ({ title, type, isOpen, onToggle, onRemove, onNavigate, children, ...props }) => {
  const className = { "page": "node-page", "block": "block-page", "graph": "graph-page" }[type];
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
        gridTemplateColumns="1fr 3rem 3rem"
        pr={2}
        alignItems="center"
        justifyContent="center"
      >
        <Button
          onClick={onToggle}
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
          {<ChevronRightIcon
            transform={isOpen ? "rotate(90deg)" : null}
            transitionProperty="common"
            transitionDuration="0.15s"
            transitionTimingFunction="ease-in-out"
            justifySelf="center" />}
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
          onClick={onNavigate}
          size="sm"
          color="foreground.secondary"
          alignSelf="center"
          justifySelf="center"
          bg="transparent"
          aria-label="Close"
        >
          <ArrowLeftOnBoxIcon />
        </IconButton>
        {/* <IconButton
            size={"sm"}
            alignSelf="center"
            justifySelf={"center"}
            bg="transparent"
            aria-label="drag">
          <DragIcon alignSelf={"center"} justifySelf={"center"}/>
        </IconButton> */}
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
        className={className}
        animateOpacity
        unmountOnExit
        zIndex={1}
        px={4}
        onPointerDown={(e) => { e.stopPropagation() }}
      >
        {children}
      </Box>
    </VStack>
  );
};
