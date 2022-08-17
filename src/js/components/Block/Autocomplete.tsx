
import React from 'react';
import {
  Box,
  Popover,
  Button,
  PopoverTrigger,
  useOutsideClick,
  PopoverContent,
  Portal
} from '@chakra-ui/react';
import getCaretCoordinates from '@/../textarea'

const getCaretPositionFromKeyDownEvent = (event) => {
  if (event?.target) {
    const localCaretCoordinates = getCaretCoordinates(event?.target);
    const { x: targetLeft, y: targetTop } = event?.target.getBoundingClientRect();
    const position = {
      left: localCaretCoordinates.left + targetLeft,
      top: localCaretCoordinates.top + targetTop,
      height: localCaretCoordinates.height + targetLeft,
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
      variant="ghost"
      borderRadius={0}
      flexShrink={0}
      maxWidth="100%"
      justifySelf="stretch"
      overflowX="hidden"
      display="inline-block"
      textAlign="left"
      justifyContent="flex-start"
      isActive={isActive}
      textOverflow="ellipsis"
      whiteSpace="nowrap"
      fontWeight="normal"
      _first={{
        borderTopRadius: "inherit",
      }}
      _last={{
        borderBottomRadius: "inherit",
      }}
      {...props}
      onClick={onClick}
    >
      {children}
    </Button>
  );
}

export const Autocomplete = ({ isOpen, onClose, event, children }) => {
  // Early return with nothing, to avoid rendering all
  // the juicy portaling goodness.
  if (!isOpen) {
    return null;
  }

  const menuRef = React.useRef(null);
  const lastEventTargetValue = React.useRef(null);
  const currentEventPosition = React.useRef({ left: null, top: null });
  const newEventTargetValue = event?.target?.value;
  const isNewEvent = newEventTargetValue !== lastEventTargetValue.current;

  useOutsideClick({
    ref: menuRef,
    handler: () => onClose(),
  })

  if (isOpen && isNewEvent) {
    currentEventPosition.current = getCaretPositionFromKeyDownEvent(event)
  };

  lastEventTargetValue.current = newEventTargetValue;

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
          visibility="hidden"
          bg="red"
          position="fixed"
          width="1px"
          height="1.5em"
          zIndex="100"
          left={currentEventPosition.current.left + 'px'}
          top={currentEventPosition.current.top + "px"}
        >
        </Box>
      </PopoverTrigger>
      <Portal>
        <PopoverContent
          borderRadius="md"
          shadow="menu"
          bg="background.vibrancy"
          borderColor="separator.divider"
          backdropFilter="blur(20px)"
          maxWidth="max-content"
          ref={menuRef}
          p={0}
          overflow="auto"
          maxHeight={`calc(100vh - 2rem - 2rem - ${currentEventPosition.current.top}px)`}
        >
          {children}
        </PopoverContent>
      </Portal>
    </Popover>)
}