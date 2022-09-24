import React from 'react';
import { forwardRef, VStack, HStack, Box, Heading, MenuGroup, MenuItem, useMergeRefs } from '@chakra-ui/react';
import { PlusIcon } from '@/Icons/Icons';
import { useOverflowBox } from '@/hooks/useOverflowShadow';
import { ContextMenuContext } from '@/App/ContextMenuContext';
import { AnimatePresence, motion } from 'framer-motion';


export const KanbanCard = forwardRef(({ children, isOver }, ref) => {
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
    minHeight="3rem"
    listStyleType={"none"}
    bg="interaction.surface"
    position="relative"
    py={2}
    px={3}
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
        isExclusive: true,
        key: "card"
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

export const KanbanColumn = forwardRef((props, ref) => {
  const { children, isOver } = props;

  return (
    <VStack
      flex="1 1 100%"
      position="relative"
      align="stretch"
      bg="background.floor"
      borderRadius="md"
      maxHeight="100%"
      width="30ch"
      overflowY="auto"
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
      <VStack
        spacing="2px"
        minHeight="calc(3em + 3.375em)" // height of a card plus height of the col header
        overflowY="auto"
        align="stretch"
        listStyleType="none"
        ref={ref}
        axis="y"

      >
        <AnimatePresence initial={false}>
          {children}
        </AnimatePresence>
      </VStack>
    </VStack>
  );
});


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

export const KanbanSwimlane = forwardRef((props, ref) => {
  const { name, children, ...laneProps } = props;
  const scrollBoxRef = React.useRef();
  const { overflowBox, onScroll } = useOverflowBox(scrollBoxRef);

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

  const innerVPadding = 3;
  const innerHPadding = 3;

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
        "WebkitOverflowScrolling": "touch",
      }}
      {...laneProps}
    >
      <Heading
        color="foreground.secondary"
        size="sm"
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
        onScroll={onScroll}
      >
        {children}
      </HStack>
    </VStack>
  );
});

export const KanbanBoard = forwardRef((props, ref) => {
  const { children } = props;
  return (
    <VStack
      ref={ref}
      align="stretch"
      spacing={2}
      py={2}
    >
      {children}
    </VStack>
  );
})
