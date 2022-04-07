
import React from 'react';
import {
  Box, PopoverBody, Popover,
  Button,
  PopoverTrigger,
  PopoverContent,
  Portal
} from '@chakra-ui/react';
import getCaretCoordinates from '@/../textarea'

let lastETargetValue;
let currentEventPosition = { left: null, top: null };

const getCaretPositionFromKeyDownEvent = (event) => {
  if (event?.target) {
    const localCaretCoordinates = getCaretCoordinates(event?.target);
    const { x: targetLeft, y: targetTop } = event?.target.getBoundingClientRect();
    const position = {
      left: localCaretCoordinates.left + targetLeft,
      top: localCaretCoordinates.top + targetTop,
    }
    return position;
  }
}

const scrollToEl = (el) => {
  if (el) {
    el.scrollIntoView({
      behavior: 'smooth',
      block: 'center',
      inline: 'center',
    });
  }
}

export const AutocompleteButton = ({ children, onClick, isActive, ...props }) => {
  const buttonRef = React.useRef(null);

  React.useEffect(() => {
    if (isActive) {
      scrollToEl(buttonRef.current);
    }
  }, [isActive]);

  return (
    <Button
      ref={buttonRef}
      borderRadius={0}
      justifyContent="flex-start"
      isActive={isActive}
      textOverflow="ellipsis"
      whiteSpace={"nowrap"}
      width="100%"
      _first={{
        borderTopRadius: "inherit",
      }}
      _last={{
        borderBottomRadius: "inherit"
      }}
      {...props}
      onClick={onClick}
    >
      {children}
    </Button>
  );
}

export const Autocomplete = ({ isOpen, onClose, event, children }) => {
  const newETargetValue = event?.target?.value;
  const isNewEvent = newETargetValue !== lastETargetValue;

  if (isOpen && event?.target && isNewEvent) {
    currentEventPosition = getCaretPositionFromKeyDownEvent(event)
  };

  lastETargetValue = event?.target?.value;

  return (
    <Popover
      isOpen={isOpen}
      placement="bottom-start"
      isLazy={true}
      returnFocusOnClose={false}
      closeOnBlur={true}
      closeOnEsc={true}
      onClose={onClose}
      autoFocus={false}
    >
      <PopoverTrigger>
        <Box
          bg="red"
          position="fixed"
          width="10px"
          height="10px"
          zIndex="100"
          left={currentEventPosition.left + 'px'}
          top={currentEventPosition.top + "px"}
        >
        </Box>
      </PopoverTrigger>
      <Portal>
        <PopoverContent>
          <PopoverBody
            p={0}
            overflow="auto"
            borderRadius="inherit"
            maxHeight={`calc(100vh - 2rem - ${currentEventPosition.top}px)`}
          >
            {children}
          </PopoverBody>
        </PopoverContent>
      </Portal>
    </Popover>)
}