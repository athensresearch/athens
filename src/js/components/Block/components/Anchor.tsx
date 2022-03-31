import React, { ReactNode } from 'react';
import { Menu, MenuList, MenuItem, MenuGroup, MenuDivider, MenuButton, IconButton, Portal, Box, Text } from '@chakra-ui/react';

const ANCHORS = {
  CIRCLE: <svg viewBox="0 0 24 24">
    <circle cx="12" cy="12" r="4" />
  </svg>,
  DASH: <svg viewBox="0 0 1 1">
    <line x1="-1" y1="0" x2="1" y2="0" />
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
  as: ReactNode;
  onContextMenu: (event: React.MouseEvent<HTMLDivElement, MouseEvent>) => void;
  onCopyRef: () => void;
  onCopyUnformatted: () => void;
  onDragStart: () => void;
  onDragEnd: () => void;
  onClick: () => void;
}

const anchorButtonStyleProps = (isClosedWithChildren) => {
  return ({
    bg: "transparent",
    "aria-label": "Block anchor",
    className: [ 'anchor', isClosedWithChildren && 'closed-with-children' ].filter(Boolean).join(' '),
    draggable: true,
    gridArea: "bullet",
    flexShrink: 0,
    position: 'relative',
    appearance: "none",
    border: "0",
    color: "inherit",
    display: "flex",
    placeItems: "center",
    placeContent: "center",
    zIndex: 2,
    minWidth: "0",
    minHeight: "0",
    h: "2em",
    w: "fit-content",
    fontSize: "inherit",
    mx: "-0.125em",
    size: "sm",
    p: 0,
    sx: {
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
        transformOrigin: 'center',
        transition: 'all 0.15s ease-in-out',
        stroke: "transparent",
        strokeWidth: "0.125em",
        fill: "currentColor",
        ...isClosedWithChildren && ({
          transform: "scale(1.25)",
          stroke: 'currentColor',
          fill: "none",
        })
      }
    }
  })
};


/**
 * A handle and indicator of a block's position in the document
*/
export const Anchor = (props: AnchorProps) => {
  const { isClosedWithChildren,
    anchorElement,
    shouldShowDebugDetails,
    onCopyRef,
    onCopyUnformatted,
    onDragStart,
    onDragEnd,
    onClick,
    block } = props;

  const [ isOpen, setIsOpen ] = React.useState(false);

  return (
    <Menu isOpen={isOpen} onClose={() => setIsOpen(false)}>
      <IconButton
        aria-label="Block anchor"
        {...anchorButtonStyleProps(isClosedWithChildren)}
        onDragStart={onDragStart}
        onClick={onClick}
        onDragEnd={onDragEnd}
        onContextMenu={(e) => {
          e.preventDefault();
          e.stopPropagation();
          setIsOpen(true);
        }}
        as={MenuButton}
      >
        {ANCHORS[ anchorElement ] || ANCHORS.CIRCLE}
      </IconButton>
      <Portal>
        <MenuList>
          <MenuItem onClick={onCopyRef}>Copy block refs</MenuItem>
          <MenuItem onClick={onCopyUnformatted}>Copy unformatted</MenuItem>
          {shouldShowDebugDetails && (
            <>
              <MenuDivider />
              <MenuGroup title="Debug details">
                <Box px={4} pb={3}>
                  {propertiesList(block)}
                </Box>
              </MenuGroup>
            </>)}
        </MenuList>
      </Portal>
    </Menu>
  )
};
