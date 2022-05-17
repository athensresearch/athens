import { AnimatePresence, motion } from 'framer-motion';
import { XmarkIcon, ChevronRightIcon, PageIcon, PageFillIcon, BlockIcon, BlockFillIcon, GraphIcon } from '@/Icons/Icons';
import { Button, IconButton, Box, Collapse, VStack } from '@chakra-ui/react';

const Container = motion(Box);

export const RightSidebarContainer = ({ isOpen, width, isDragging, children }) => {
  return <AnimatePresence initial={false}>
    {isOpen &&
      <Container
        className="right-sidebar"
        display="flex"
        flexDirection="column"
        height="calc(100% - 3.25rem)"
        marginTop="3.25rem"
        alignItems="stretch"
        justifySelf="stretch"
        transformOrigin="right"
        justifyContent="space-between"
        overflowX="visible"
        position="relative"
        gridArea="secondary-content"
        sx={{
          "WebkitAppRegion": "no-drag",
          "--page-title-font-size": "1.25rem",
          ".node-page, .block-page": {
            "--page-padding": "1rem",
          },
          ".page-header": {
            pb: 1
          },
          ".page-body": {},
          ".page-footer": {
            px: 0,
            py: 4
          }
        }}
        initial={{
          width: 0,
          opacity: 0
        }}
        transition={isDragging ? { duration: 0 } : undefined}
        animate={{
          width: isOpen ? `${width}vw` : 0,
          opacity: 1
        }}
        exit={{
          width: 0,
          opacity: 0
        }}
      >
        {children}
      </Container>}
  </AnimatePresence>;
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
