import React from 'react';
import { Box, AlertIcon, Alert, AlertTitle, useMergeRefs } from "@chakra-ui/react";
import { withErrorBoundary } from "react-error-boundary";
import { useContextMenu } from '@/utils/useContextMenu';

const ERROR_MESSAGE = <Alert ml={4} status='error'><AlertIcon /><AlertTitle>An error occurred while rendering this block.</AlertTitle></Alert>;

// Don't open the context menu on these elements
const CONTAINER_CONTEXT_MENU_FILTERED_TAGS = ["A", "BUTTON", "INPUT", "TEXTAREA", "LABEL", "VIDEO", "EMBED", "IFRAME", "IMG"];

const isEventTargetIsCurrentBlockNotChild = (target: HTMLElement, thisBlockUid: string): boolean => {
  if (!target) return false;

  // if hovered element's closest block container has the current UID,
  // we're hovering the current block. Otherwise return false
  const closestBlockContainer = target.closest('.block-container') as HTMLElement;
  return (closestBlockContainer?.dataset?.uid === thisBlockUid)
}

const _Container = React.forwardRef(({ children, isDragging, isHidden, isSelected, isOpen, hasChildren, hasPresence, isLinkedRef, uid, childrenUids, menu, actions, reactions, isEditing, ...props }, ref) => {
  const [isHoveredNotChild, setIsHoveredNotChild] = React.useState(false);

  const internalRef = React.useRef(null)
  const refs = useMergeRefs(internalRef, ref)

  const handleMouseOver = (e) => setIsHoveredNotChild(isEventTargetIsCurrentBlockNotChild(e.target, uid));
  const handleMouseLeave = () => isHoveredNotChild && setIsHoveredNotChild(false);

  const {
    menuSourceProps,
    ContextMenu,
    isOpen: isContextMenuOpen
  } = useContextMenu({
    ref: internalRef,
    menuProps: {
      size: "sm"
    },
    source: "cursor",
  });

  return <>
    <Box
      ref={refs}
      className={[
        "block-container",
        isDragging ? "is-dragging" : "",
        isSelected ? "is-selected" : "",
        isOpen ? "is-open" : "",
        isContextMenuOpen && 'isContextMenuOpen',
        (hasChildren && isOpen) ? "show-tree-indicator" : "",
        isLinkedRef ? "is-linked-ref" : "",
        isHoveredNotChild && "is-hovered-not-child",
        hasPresence ? "is-presence" : "",
      ].filter(Boolean).join(' ')}
      lineHeight="2em"
      position="relative"
      borderRadius="0.125rem"
      justifyContent="flex-start"
      flexDirection="column"
      display={isHidden ? "none" : "block"}
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
          gridTemplateColumns: "1em auto 1em 1fr auto",
          gridTemplateRows: "0 1fr auto auto 0",
          gridTemplateAreas:
            `'above above above above above above'
            'toggle name anchor content refs presence'
            '_ _ _ reactions reactions reactions'
            '_ _ _ comments comments comments'
            'below below below below below below'`,
          borderRadius: "0.5rem",
          minHeight: '2em',
          position: "relative",
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
        "&.isContextMenuOpen": { bg: "background.attic" },
        ".block-container": {
          marginLeft: "2em",
          gridArea: "body"
        }
      }}
      {...menuSourceProps}
      onContextMenu={
        (e) => {
          const target = e.target as HTMLElement;
          // Don't open the context menu on these e.target as HTMLElement;
          if (!CONTAINER_CONTEXT_MENU_FILTERED_TAGS.includes(target.tagName)) {
            menuSourceProps.onContextMenu(e);
          }
        }
      }
      {...props}
      onMouseOver={handleMouseOver}
      onMouseLeave={(e) => {
        if (props?.onMouseLeave) props.onMouseLeave(e);
        handleMouseLeave();
      }}
    >
      {children}
    </Box>
    <ContextMenu>
      {menu}
    </ContextMenu>
  </>;
})

export const Container = withErrorBoundary(_Container, { fallback: <p>{ERROR_MESSAGE}</p> });
