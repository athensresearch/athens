import React from 'react';
import { VStack, HStack, Box, Heading, Button, MenuGroup, MenuItem, useMergeRefs, useTheme, theme } from '@chakra-ui/react';
import { PlusIcon } from '@/Icons/Icons';
import { useOverflowBox } from '@/hooks/useOverflowShadow';
import { ContextMenuContext } from '@/App/ContextMenuContext';
import { AnimatePresence, motion } from 'framer-motion';

const KanbanBoardContext = React.createContext(null);

const useKanbanBoard = (props) => {
  const { shouldSyncScroll } = props;
  const [sharedScrollPos, setSharedScrollPos] = React.useState(0);
  const swimlaneRefs = React.useRef([]);

  const addLaneRef = React.useCallback((el) => {
    if (el && !swimlaneRefs?.current?.includes(el)) {
      swimlaneRefs.current.push(el);
    }
  }, []);

  const onScrollSwimlane = React.useCallback((e) => {
    setSharedScrollPos(e.target.scrollLeft);
  }, []);

  return {
    addLaneRef,
    sharedScrollPos,
    shouldSyncScroll,
    onScrollSwimlane
  }
}

export const KanbanCard = React.forwardRef(({ children, isOver }, ref) => {
  const { addToContextMenu, getIsMenuOpen } = React.useContext(ContextMenuContext);
  const innerRef = React.useRef();
  const boxRef = useMergeRefs(innerRef, ref);
  const isMenuOpen = getIsMenuOpen(innerRef);

  const Menu = React.memo(() => {
    return <MenuGroup title="Card">
      <MenuItem icon={<PlusIcon />}>Open in right sidebar</MenuItem>
    </MenuGroup>
  })


  return <Box
    ref={boxRef}
    as={motion.div}
    animate={{
      height: "auto",
      opacity: 1,
    }}
    exit={{
      height: 0,
      opacity: 0,
    }}
    initial={{
      height: 0,
      opacity: 0,
    }}
    minHeight="4rem"
    listStyleType={"none"}
    bg="interaction.surface"
    position="relative"
    p={2}
    px={4}
    pr={3}
    _hover={{
      bg: 'interaction.surface.hover',
    }}
    {...(isMenuOpen && {
      bg: 'interaction.surface.active',
      _hover: {}
    })}
    onContextMenu={(e) => {
      addToContextMenu({
        ref: innerRef,
        event: e,
        component: Menu,
        isExclusive: true
      })
    }}
    {...(isOver && {
      _after: {
        content: '""',
        position: "absolute",
        inset: 0,
        top: "auto",
        height: "2px",
        bg: "link",
        _hover: {}
      }
    }
    )}
  >
    {children}
  </Box>
});

export const KanbanColumn = (props) => {
  const { children, isOver } = props;

  return (
    <VStack
      flex="1 1 100%"
      minHeight="6.75em"
      position="relative"
      align="stretch"
      listStyleType="none"
      spacing="2px"
      bg="background.floor"
      borderRadius="md"
      axis="y"
      overflowY="auto"
      maxHeight="100%"
      width="30ch"
      _after={{
        content: "''",
        position: "absolute",
        pointerEvents: "none",
        inset: 0,
        borderRadius: "inherit",
        border: "1px solid",
        borderColor: "separator.divider",

        ...(isOver && {
          border: "none",
          bg: "link",
          opacity: 0.1,
        })
      }}
    >
      <AnimatePresence initial={false}>
        {children}
      </AnimatePresence>
    </VStack>
  );
};

const scrollShadow = (top, right, bottom, left, color, depth, blur, inset) => {
  const shadowLeft = `inset ${Math.min(
    left,
    depth
  )}px 0 ${blur}px -${blur + inset}px ${color}`;

  const shadowRight = `inset ${Math.max(
    1 - right,
    -1 * depth
  )}px 0 ${blur}px -${blur + inset}px ${color}`;

  const shadowTop = `inset 0 ${Math.min(
    top,
    depth
  )}px ${blur}px -${blur + inset}px ${color}`;

  const shadowBottom = `inset 0 ${Math.max(
    1 - bottom,
    -1 * depth
  )}px ${blur}px -${blur + inset}px ${color}`;

  return [shadowLeft, shadowRight, shadowTop, shadowBottom].join(", ");
};

export const KanbanSwimlane = (props) => {
  const { name, children, ...laneProps } = props;
  const ref = React.useRef();
  const scrollBoxRef = React.useRef();
  const { overflowBox, onScroll } = useOverflowBox(scrollBoxRef);
  const { addLaneRef, shouldSyncScroll, sharedScrollPos, onScrollSwimlane } = React.useContext(KanbanBoardContext);

  React.useEffect(() => {
    addLaneRef(scrollBoxRef);
  }, [addLaneRef, scrollBoxRef]);

  React.useLayoutEffect(() => {
    if (shouldSyncScroll && sharedScrollPos) {
      if (scrollBoxRef?.current) {
        const el = scrollBoxRef.current as HTMLElement;
        el.scrollLeft = sharedScrollPos;
      }
    }
  }, [scrollBoxRef, sharedScrollPos, shouldSyncScroll]);

  const shadowDepth = 12;
  const shadowBlur = shadowDepth;
  const shadowInset = shadowDepth / 2;
  const shadowColor = "#000";

  const shadow = React.useMemo(() => scrollShadow(
    overflowBox.top,
    overflowBox.right,
    overflowBox.bottom,
    overflowBox.left,
    shadowColor,
    shadowDepth,
    shadowBlur,
    shadowInset
  ), [overflowBox]);

  const innerVPadding = 4;
  const innerHPadding = 4;

  return (
    <VStack
      ref={ref}
      align="stretch"
      borderRadius="lg"
      spacing={3}
      maxHeight="700px"
      flex="1 1 100%"
      position="relative"
      _after={{
        content: "''",
        position: "absolute",
        pointerEvents: "none",
        inset: 0,
        borderRadius: "inherit",
        boxShadow: shadow
      }}
      sx={{
        "-webkit-overflow-scrolling": "touch",
      }}
      {...laneProps}
    >
      <Heading
        color="foreground.secondary"
        size="md"
        pt={innerVPadding}
        px={innerHPadding}
      >
        {name}
      </Heading>
      <HStack
        flex="1 1 100%"
        spacing={2}
        alignItems="stretch"
        justify="stretch"
        px={innerHPadding}
        pb={innerVPadding}
        overflowY="hidden"
        overflowX="auto"
        ref={scrollBoxRef}
        onScroll={(e) => {
          onScroll()
          onScrollSwimlane(e)
        }}
      >
        {children}
      </HStack>
    </VStack>
  );
}

export const KanbanBoard = (props) => {
  const { children, shouldSyncScroll = true } = props;
  const kanbanState = useKanbanBoard({ shouldSyncScroll });

  return (
    <KanbanBoardContext.Provider value={kanbanState}>
      <VStack
        align="stretch"
        spacing={3}
        py={2}
      >
        {children}
      </VStack>
    </KanbanBoardContext.Provider>
  );
}
