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

const TARGET_KEY = '/';

const getCaretPositionFromKeyDownEvent = (event) => {
  if (event?.target) {
    console.log('got caret position');
    const localCaretCoordinates = getCaretCoordinates(event?.target);
    const { x: targetLeft, y: targetTop } = event?.target.getBoundingClientRect();
    const position = {
      left: localCaretCoordinates.left + targetLeft,
      top: localCaretCoordinates.top + targetTop,
    }
    return position;
  }
}

const AutocompleteButton = ({ children, onClick, ...props }) => {
  return (
    <Button
      borderRadius={0}
      justifyContent="flex-start"
      width="100%"
      _first={{
        borderTopRadius: "inherit",
      }}
      _last={{
        borderBottomRadius: "inherit"
      }}
      {...props}
      onClick={onClick}
    >{children}</Button>
  );
}

export const Autocomplete = ({ isOpen, onClose, event, emptyMessage, onSelect, index, options }) => {
  const newETargetValue = event?.target?.value;
  const isNewEvent = newETargetValue !== lastETargetValue;

  if (event?.target && isNewEvent && event?.key === TARGET_KEY) {
    currentEventPosition = getCaretPositionFromKeyDownEvent(event)
  };

  lastETargetValue = event?.target?.value;

  return (
    <Popover isOpen={isOpen}
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
          <PopoverBody>
            {options?.map((option) => {
              const [optIndex, [text, icon, expansion, kbd, pos]] = option;
              return <AutocompleteButton
                onClick={() => onSelect(option)}
              // isActive={optIndex === index}
              // leftIcon={icon}
              // rightIcon={kbd}
              >
                {text}
              </AutocompleteButton>
            })}
          </PopoverBody>
        </PopoverContent>
      </Portal>
    </Popover>)
}