import React from 'react'
import { IconButton } from '@chakra-ui/react';

interface ToggleProps extends React.HTMLAttributes<HTMLButtonElement> {
  isOpen: boolean;
}

/**
 * Button to toggle the visibility of a block's child blocks.
 */
export const Toggle = React.forwardRef((props: ToggleProps, ref) => {
  const {
    isOpen,
  } = props;

  return (
    <IconButton
      ref={ref as any}
      className="toggle"
      bg="transparent"
      aria-label="Block toggle"
      draggable={true}
      gridArea="toggle"
      flexShrink={0}
      position='relative'
      appearance="none"
      border="0"
      color="inherit"
      mr={0.25}
      display="flex"
      placeItems="center"
      placeContent="center"
      zIndex={2}
      minWidth="0"
      minHeight="0"
      h="2em"
      w="1.25em"
      fontSize="inherit"
      mx="-0.125em"
      size="sm"
      p={0}
      sx={{
        "svg": {
          pointerEvents: "none",
          overflow: "visible", // Prevents the bullet being cropped
          width: "1em",
          height: "1em",
          color: "foreground.secondary",
          transition: 'transform 0.1s ease-in-out',
          transform: `rotate(${isOpen ? 90 : 0}deg)`,

          "*": {
            vectorEffect: "non-scaling-stroke"
          }
        },
      }}
      {...props}
    >
      <svg
        width="24"
        height="24"
        viewBox="0 0 24 24"
        xmlns="http://www.w3.org/2000/svg"
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
});
