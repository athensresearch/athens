import React from 'react';
import { Box, Portal } from "@chakra-ui/react";
import { withErrorBoundary } from "react-error-boundary";
import { Actions } from './Actions';

const ERROR_MESSAGE = "An error occurred while rendering this block.";

// Helpers

const targetIsCurrentBlockNotChild = (target: HTMLElement, thisBlockUid: string): boolean => {
  // if hovered element's closest block container has the current UID,
  // we're hovering the current block. Otherwise return false
  const closestBlockContainer = target.closest('.block-container') as HTMLElement;
  return (closestBlockContainer?.dataset?.uid === thisBlockUid)
}

// Component

const _Container = ({ children, isEditing, isDragging, isSelected, isOpen, hasChildren, hasPresence, isLinkedRef, uid, childrenUids, actions, ...props }) => {
  const [isHoveredNotChild, setIsHoveredNotChild] = React.useState(false);
  const [isUsingActions, setIsUsingActions] = React.useState(false);
  const containerRef = React.useRef<HTMLDivElement>(null);

  const handleMouseOver = (e) => {
    setIsHoveredNotChild(targetIsCurrentBlockNotChild(e.target, uid));
  }

  const handleMouseLeave = (e) => {
    if (!isUsingActions) {
      console.log('closed because not using actions');
      setIsHoveredNotChild(false);
    }
  }

  const handleFocus = (e) => setIsHoveredNotChild(targetIsCurrentBlockNotChild(e.target, uid));

  const handleBlur = (e) => {
    if (!isUsingActions) {
      console.log('closed because not using actions');
      setIsHoveredNotChild(false);
    }
  }

  return <Box
    ref={containerRef}
    className={[
      "block-container",
      isDragging ? "is-dragging" : "",
      isSelected ? "is-selected" : "",
      isOpen ? "is-open" : "",
      (hasChildren && isOpen) ? "show-tree-indicator" : "",
      isLinkedRef ? "is-linked-ref" : "",
      isHoveredNotChild && "is-hovered-not-child",
      hasPresence ? "is-presence" : "",
    ].filter(Boolean).join(' ')}
    display="flex"
    lineHeight="2em"
    position="relative"
    borderRadius="sm"
    justifyContent="flex-start"
    flexDirection="column"
    background="var(--block-surface-color)"
    opacity={isDragging ? 0.5 : 1}
    data-uid={uid}
    data-childrenuids={childrenUids}

    sx={{
      "&.show-tree-indicator:before": {
        content: "''",
        position: "absolute",
        width: "1px",
        left: "calc(1.375em + 1px)",
        top: "2em",
        bottom: "0",
        opacity: "0",
        transform: "translateX(50%)",
        transition: "background-color 0.2s ease-in-out, opacity 0.2s ease-in-out",
        background: "separator.divider",
      },
      "&:hover.show-tree-indicator:before, &:focus-within.show-tree-indicator:before": { opacity: 1 },
      "&:after": {
        content: "''",
        zIndex: 0,
        position: "absolute",
        inset: "1px 0",
        opacity: 0,
        pointerEvents: "none",
        borderRadius: "sm",
        transition: "opacity 0.075s ease-in-out",
        background: "link",
      },
      "&.is-selected:after": { opacity: 0.2 },
      ".user-avatar": {
        position: "absolute",
        left: "4px",
        top: "4px"
      },
      ".block-body": {
        display: "grid",
        gridTemplateColumns: "1em 1em 1fr auto",
        gridTemplateRows: "0 1fr 0",
        gridTemplateAreas:
          "'above above above above above' 'toggle bullet content refs presence' 'below below below below below'",
        borderRadius: "sm",
        minHeight: '2em',
        position: "relative",
      },
      "&:hover:not(:)": {
        bg: "background.upper"
      },
      "&:hover > .block-toggle, &:focus-within > .block-toggle": { opacity: "1" },
      "button.block-edit-toggle": {
        position: "absolute",
        appearance: "none",
        width: "100%",
        background: "none",
        border: 0,
        cursor: "text",
        display: "block",
        zIndex: 1,
        top: 0,
        right: 0,
        bottom: 0,
        left: 0,
      },
      ".block-embed": {
        borderRadius: "sm",
        "--block-surface-color": "background.basement",
        bg: "background.basement",
        // Blocks nested in an embed get normal indentation...
        ".block-container": { marginLeft: 8 },
        // ...except for the first one, where that would be excessive
        "& > .block-container": { marginLeft: 0.5 },
      },
      ".block-content": {
        gridArea: "content",
        minHeight: "1.5em",
      },
      "&.is-linked-ref": { bg: "background-attic" },
      ".block-container": {
        marginLeft: "2em",
        gridArea: "body"
      },
      "&.is-hovered": {
      },
    }}
    {...props}
    onMouseOver={handleMouseOver}
    onFocus={handleFocus}
    onBlur={handleBlur}
    onMouseLeave={(e) => {
      if (props?.onMouseLeave) props.onMouseLeave(e);
      handleMouseLeave(e);
    }}

  >
    {children}
    {(!isEditing && (isHoveredNotChild || isUsingActions)) && (
      <Actions
        actions={actions}
        setIsUsingActions={setIsUsingActions}
      />)}
  </Box>;
}

export const Container = withErrorBoundary(_Container, { fallback: <p>{ERROR_MESSAGE}</p> });