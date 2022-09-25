import React from 'react'
import { IconButton, useTheme } from '@chakra-ui/react';

interface ToggleProps extends React.HTMLAttributes<HTMLButtonElement> {
  isOpen: boolean;
  onClick: () => void;
}

/**
 * Button to toggle the visibility of a block's child blocks.
 */
export const Toggle = (props: ToggleProps) => {
  const { isOpen, ...rest } = props;

  return (
    <IconButton
      className="toggle block-toggle"
      variant="ghost"
      aria-label="Block toggle"
      colorScheme="subtle"
      size="sm"
      sx={{
        gridArea: "toggle",
        flexShrink: 0,
        position: 'relative',
        appearance: "none",
        placeItems: "center",
        placeContent: "center",
        display: "flex",
        alignItems: "flex-start",
        alignSelf: "flex-start",
        minHeight: "inherit",
        zIndex: 2,
        minWidth: "0",
        h: "var(--control-height)",
        w: "auto",
        fontSize: "inherit",
        p: 0,
        "svg": {
          pointerEvents: "none",
          overflow: "visible",
          width: "1em",
          height: "1em",
          transform: `rotate(${isOpen ? 90 : 0}deg)`,

          "*": {
            vectorEffect: "non-scaling-stroke"
          }
        },
      }}
      {...rest}
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
