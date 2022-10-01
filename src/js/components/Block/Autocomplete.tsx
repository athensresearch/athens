
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
import getCaretCoordinates from 'textarea-caret';

const getCaretPositionFromKeyDownEvent = (event) => {
  if (event?.target) {
    const target = event.target;
    const selectionStart = target.selectionStart;
    const { left: caretLeft, top: caretTop, height: caretHeight } = getCaretCoordinates(event.target, selectionStart);
    const { x: targetLeft, y: targetTop } = event?.target.getBoundingClientRect();
    const position = {
      left: caretLeft + targetLeft,
      top: caretTop + targetTop,
      height: caretHeight + targetTop,
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
  const { children, isActive, ...buttonProps } = props;
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
    const target = event?.target;
    if (isOpen && target) {
      const position = getCaretPositionFromKeyDownEvent(event);
      if (position.left && position.top) {
        setMenuPosition(position);
      } else {
        onClose();
      }
    }
  }, [isOpen]);

  if (!isOpen) {
    return false;
  }

  return (
    <Popover
      isOpen={isOpen}
      placement="bottom-start"
      isLazy
      offset={[0, 0]}
      size="sm"
      returnFocusOnClose={false}
      closeOnBlur={false}
      autoFocus={false}
    >
      <PopoverTrigger>
        <Box
          bg="red"
          visibility="hidden"
          position="fixed"
          width="px"
          height="1.5em"
          zIndex="100"
          left={menuPosition.left + 'px'}
          top={menuPosition.top + "px"}
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
          maxHeight={`calc(var(--app-height) - 2rem - 2rem - ${menuPosition?.top || 0}px)`}
        >
          {children}
        </PopoverContent>
      </Portal>
    </Popover>)
}