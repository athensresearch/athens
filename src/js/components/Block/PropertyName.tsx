import React from 'react'
import { Button } from '@chakra-ui/react';

interface PropertyNameProps extends React.HTMLAttributes<HTMLButtonElement> {
  name: string;
}

export const PropertyName = (props: PropertyNameProps) => {
  const { name, onClick, onDragStart, onDragEnd } = props;

  return (
    <Button
      className="prop-name"
      bg="transparent"
      aria-label="Property name"
      gridArea="name"
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
          transform: "scale(1.1001)", // Prevents the bullet being squished
          overflow: "visible", // Prevents the bullet being cropped
          width: "1em",
          height: "1em",
          "*": {
            vectorEffect: "non-scaling-stroke"
          }
        },
        "circle": {
          transformOrigin: 'center',
          transition: 'all 0.15s ease-in-out',
          stroke: "transparent",
          strokeWidth: "0.125em",
          fill: "currentColor",
        }

      }}
      draggable={onDragStart ? true : undefined}
      onDragStart={onDragStart}
      onClick={onClick}
      onDragEnd={onDragEnd}
    >
      <svg viewBox="0 0 24 24">
        <circle cx="12" cy="7" r="2.5" />
        <circle cx="12" cy="17" r="2.5" />
      </svg>
      {name}
    </Button>
  )
};
