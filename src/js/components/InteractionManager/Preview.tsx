import React from 'react';
import { Box, Popover, PopoverContent, PopoverTrigger, Portal } from '@chakra-ui/react';



export const Preview = ({ isOpen, pos, children }) => {
  if (!isOpen)
    return null;

  return <Popover
    isOpen={true}
    placement="bottom-start"
    autoFocus={false}
  >
    <PopoverTrigger>
      <Box
        position="fixed"
        pointerEvents="none"
        left={pos?.x}
        top={pos?.y} />
    </PopoverTrigger>
    <Portal>
      <PopoverContent
        pointerEvents="none"
        p={4}
        background="background.upper"
        width="20rem"
        overflow="hidden"
      >
        {children}
      </PopoverContent>
    </Portal>
  </Popover>;
};
