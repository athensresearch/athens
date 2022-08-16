
import React from 'react';
import {
  Box,
  Popover,
  Button,
  PopoverTrigger,
  useOutsideClick,
  PopoverContent,
  Portal,
  ButtonProps
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

export const AutocompleteButton = (props: ButtonProps) => {
  const { children, onClick, isActive, ...buttonProps } = props;
  const buttonRef = React.useRef(null);

  React.useEffect(() => {
    if (isActive && buttonRef.current) {
      scrollToEl(buttonRef.current);
    }
  }, [isActive]);

  return (
    <Button
      ref={buttonRef}
      variant="ghost"
      size="sm"
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
      {...buttonProps}
    >
      {children}
    </Button>
  );
}

export const Autocomplete = ({ isOpen, onClose, event, children }) => {
  const menuRef = React.useRef(null);
  const [menuPosition, setMenuPosition] = React.useState({ left: null, top: null });

  useOutsideClick({
    ref: menuRef,
    handler: () => onClose(),
  })

  React.useEffect(() => {
    if (isOpen) {
      setMenuPosition(getCaretPositionFromKeyDownEvent(event))
    }
  }, [isOpen]);

  return (
    <Popover
      isOpen={isOpen}
      placement="bottom-start"
      isLazy
      size="sm"
      returnFocusOnClose={false}
      closeOnBlur={false}
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
          left={menuPosition?.left || 0 + 'px'}
          top={menuPosition?.top || 0 + "px"}
        >
        </Box>
      </PopoverTrigger>
      <Portal>
        <PopoverContent
          shadow="menu"
          bg="background.vibrancy"
          borderColor="separator.divider"
          backdropFilter="blur(20px)"
          maxWidth="max-content"
          ref={menuRef}
          p={0}
          overflow="auto"
          maxHeight={`calc(100vh - 2rem - 2rem - ${menuPosition?.top || 0}px)`}
        >
          {children}
        </PopoverContent>
      </Portal>
    </Popover>)
}