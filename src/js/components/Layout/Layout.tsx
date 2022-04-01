import { Box } from '@chakra-ui/react';
import { AnimatePresence, motion } from 'framer-motion';

const Container = motion(Box)

export const RightSidebarContainer = ({ isOpen, width, isDragging, children }) => {
  return <AnimatePresence initial={false}>
    {isOpen &&
      <Container
        className="right-sidebar"
        display="flex"
        WebkitAppRegion="no-drag"
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
          "--page-padding-v": "0rem",
          "--page-padding-h": "1.5rem",
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
  </AnimatePresence>
}