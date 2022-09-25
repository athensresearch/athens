import React, { ReactNode } from 'react';
import { IconButton, useMergeRefs } from '@chakra-ui/react';
import { ColonIcon, BulletIcon, DashIcon } from '@/Icons/Icons';

const ANCHORS = {
  "bullet": <BulletIcon />,
  "colon": <ColonIcon />,
  "dash": <DashIcon />
}


type Anchors = typeof ANCHORS;
type AnchorImage = keyof Anchors;

export interface AnchorProps extends ButtonProps {
  anchorElement?: AnchorImage | number;
  isClosedWithChildren: boolean;
  block: any;
  uidSanitizedBlock: any;
  shouldShowDebugDetails: boolean;
  as: ReactNode;
}

const anchorButtonStyleProps = (isClosedWithChildren: boolean) => {
  return ({
    bg: "transparent",
    "aria-label": "Block anchor",
    className: ['anchor', isClosedWithChildren && 'closed-with-children'].filter(Boolean).join(' '),
    gridArea: "anchor",
    flexShrink: 0,
    position: 'relative',
    appearance: "none",
    border: "0",
    color: "foreground.secondary",
    display: "flex",
    alignItems: "flex-start",
    alignSelf: "flex-start",
    minHeight: "inherit",
    zIndex: 2,
    minWidth: "0",
    h: "auto",
    w: "auto",
    fontSize: "inherit",
    size: "sm",
    p: 0,
    sx: {
      "svg": {
        pointerEvents: "none",
        transform: "scale(1.0001)", // Prevents the bullet being squished
        overflow: "visible", // Prevents the bullet being cropped
        width: "1em",
        height: "1em",
        "*": {
          vectorEffect: "non-scaling-stroke"
        }
      },
      "svg path": {
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
export const Anchor = React.forwardRef((props: AnchorProps, ref) => {

  const { isClosedWithChildren,
    anchorElement,
    shouldShowDebugDetails,
    uidSanitizedBlock,
    ...buttonProps
  } = props;
  const innerRef = React.useRef(null);
  const refs = useMergeRefs(innerRef, ref);

  return <>
    <IconButton
      ref={refs}
      aria-label="Block anchor"
      {...anchorButtonStyleProps(isClosedWithChildren)}
      {...buttonProps}
    >
      {ANCHORS[anchorElement] ? ANCHORS[anchorElement] : anchorElement}
    </IconButton>
  </>

});

Anchor.defaultProps = {
  anchorElement: "bullet"
}