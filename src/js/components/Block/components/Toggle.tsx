import React from 'react'
import { IconButton } from '@chakra-ui/react';

interface ToggleProps extends React.HTMLAttributes<HTMLButtonElement> {
  isOpen: boolean;
  onClick: () => void;
}

/**
 * Button to toggle the visibility of a block's child blocks.
 */
export const Toggle = (props: ToggleProps) => {
  const { isOpen, onClick } = props;

  return (
    <IconButton
      className="toggle"
      bg="transparent"
      aria-label="Block toggle"
      draggable={true}
      gridArea="toggle"
      flexShrink={0}
      position='relative'
      appearance="none"
      border="0"
      color="foreground.secondary"
      display="flex"
      placeItems="center"
      placeContent="center"
      alignSelf="flex-start"
      minHeight="inherit"
      zIndex={2}
      minWidth="0"
      fontSize="inherit"
      h="auto"
      size="sm"
      p={0}
      sx={{
        "svg": {
          pointerEvents: "none",
          overflow: "visible",
          width: "1em",
          height: "1em",
          transition: 'transform 0.1s ease-in-out',
          transform: `rotate(${isOpen ? 90 : 0}deg)`,

          "*": {
            vectorEffect: "non-scaling-stroke"
          }
        },
      }}
      onClick={onClick}
    >
      <svg
        viewBox="0 0 24 24"
      >
        <path d="M10 6L16 11.5L10 17"
          fill="none"
          stroke="currentColor"
          strokeWidth={2}
          strokeLinecap="round"
        />
      </svg>
    </IconButton>
  )
};
