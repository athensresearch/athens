import React, { ReactNode } from 'react';
import { IconButton, Portal, Popover, PopoverArrow, PopoverTrigger, Box, Text, PopoverContent } from '@chakra-ui/react';

// export const AnchorButton = styled.button`
//   flex-shrink: 0;
//   grid-area: bullet;
//   position: relative;
//   z-index: 2;
//   cursor: pointer;
//   appearance: none;
//   border: 0;
//   background: transparent;
//   transition: all 0.05s ease;
//   color: inherit;
//   margin-right: 0.25em;
//   display: flex;
//   place-items: center;
//   place-content: center;
//   padding: 0;
//   height: 2em;
//   width: 1em;

//   svg {
//     pointer-events: none;
//     transform: scale(1.0001); // Prevents the bullet being squished
//     overflow: visible; // Prevents the bullet being cropped
//     width: 1em;
//     height: 1em;
//     color: var(--user-color, var(--body-text-color---opacity-low));

//     * {
//       vector-effect: non-scaling-stroke;
//     }
//   }

//   circle {
//     fill: currentColor;
//     transition: fill 0.05s ease, opacity 0.05s ease;
//   }

//   &:focus {
//     outline: none;
//   }

//   &:before {
//     content: '';
//     inset: 0.25rem -0.125rem;
//     z-index: -1;
//     transition: opacity 0.1s ease;
//     position: absolute;
//     border-radius: 0.25rem;
//     opacity: 0;
//     background: var(--background-plus-2);
//     box-shadow: var(--depth-shadow-8);
//   }

//   &:hover {
//     color: var(--link-color);
//     z-index: 100;
//   }

//   &:hover,
//   &:hover:before,
//   &:focus-visible:before {
//     opacity: 1;
//   }

//   &.closed-with-children {
//     circle {
//       stroke: var(--body-text-color);
//       fill: var(--body-text-color---opacity-low);
//       r: 5;
//       stroke-width: 2px;
//       opacity: var(--opacity-med);
//     }
//   }

//   &:hover svg {
//     transform: scale(1.3);
//   }

//   &.dragging {
//     z-index: 1;
//     cursor: grabbing;
//     color: var(--body-text-color);
//   }
// `;

const anchorElements = {
  circle: <svg viewBox="0 0 24 24">
    <circle cx="12" cy="12" r="4" />
  </svg>,
  dash: <svg viewBox="0 0 1 1">
    <line x1="-1" y1="0" x2="1" y2="0" stroke="currentColor" strokeWidth="0.5" />
  </svg>
}

const showValue = (value) => {
  if (typeof value === 'object') return (value = JSON.stringify(value));
  else if (typeof value === 'boolean') return (value = value ? 'true' : 'false');
  else return value;
}

const properties = (block) => ({
  "uid": block?.uid,
  "db/id": block?.id,
  "order": block?.order,
  "open": block?.open,
  "refs": block?._refs?.length || 0,
});

const Item = ({ children }) => {
  return (<Text
    as="li"
    fontSize="sm"
    margin={0}
    padding={0}
    display="flex"
    justifyContent={'space-between'}
    sx={{
      "span": {
        color: "foreground.secondary",
        flex: "1 1 50%",
        fontWeight: "medium"
      },
      "span + span": {
        marginLeft: "1ch",
        color: "foreground.primary",
        fontWeight: "normal"
      }
    }}
  >{children}</Text>)
}

const propertiesList = (block) => {
  return Object.entries(properties(block)).map(([ key, value ]) => {
    return <Item key={key}>
      <span>{key}</span>
      <span>{showValue(value)}</span>
    </Item>
  })
}

export interface AnchorProps {
  /**
   * What style of anchor to display
   */
  anchorElement?: 'circle' | 'dash' | number;
  /**
   * Whether block is closed and has children
   */
  isClosedWithChildren: boolean;
  block: any;
  shouldShowDebugDetails: boolean;
}

interface AnchorButtonProps {
  children: ReactNode;
  isClosedWithChildren: boolean;
}

const AnchorButton = React.forwardRef((props: AnchorButtonProps, ref) => {
  const { children, isClosedWithChildren } = props;

  return (<IconButton
    ref={ref as any}
    bg="transparent"
    aria-label="Block anchor"
    className={[ 'anchor', isClosedWithChildren && 'closed-with-children' ].filter(Boolean).join(' ')}
    draggable={true}
    gridArea="bullet"
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
        transform: "scale(1.0001)", // Prevents the bullet being squished
        overflow: "visible", // Prevents the bullet being cropped
        width: "1em",
        height: "1em",
        color: "foreground.secondary",
        "*": {
          vectorEffect: "non-scaling-stroke"
        }
      },
      "circle": {
        fill: "currentColor",
      }
    }
    }
    {...props}
  >
    {children}
  </IconButton>);
}
);


/**
 * A handle and indicator of a block's position in the document
*/
export const Anchor = (props: AnchorProps) => {
  const { isClosedWithChildren, anchorElement, shouldShowDebugDetails, block } = props;

  const anchor = (<AnchorButton isClosedWithChildren={isClosedWithChildren}>
    {anchorElements[ anchorElement ] || anchorElements[ 'circle' ]}
  </AnchorButton>);

  if (!shouldShowDebugDetails) {
    return anchor
  } else {
    return (
      <Popover
        size="sm"
        isLazy={true}
        trigger="hover"
        placement="right-start"
      >
        <PopoverTrigger>
          {anchor}
        </PopoverTrigger>
        <Portal>
          <PopoverContent width="8rem">
            <PopoverArrow />
            <Box p={1}>{propertiesList(block)}</Box>
          </PopoverContent>
        </Portal>
      </Popover>
    )
  }
};
