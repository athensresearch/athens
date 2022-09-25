import React from 'react';
import { Alert, AlertIcon, AlertTitle, Box, forwardRef } from "@chakra-ui/react";
import { withErrorBoundary } from "react-error-boundary";

const ERROR_MESSAGE = <Alert ml={4} status='error'><AlertIcon /><AlertTitle>An error occurred while rendering this block.</AlertTitle></Alert>;

const _Container = forwardRef(({ children, isActive, isHoveredNotChild, isDragging, isSelected, onMouseEnter, isOpen, hasChildren, hasPresence, isLinkedRef, uid, childrenUids, menu, actions, reactions, isEditing, ...props }, ref) => {

  return <Box
    ref={ref}
    className={[
      "block-container",
      isDragging ? "is-dragging" : "",
      isSelected ? "is-selected" : "",
      isOpen ? "is-open" : "",
      isActive && 'isActive',
      (hasChildren && isOpen) ? "show-tree-indicator" : "",
      isLinkedRef ? "is-linked-ref" : "",
      isHoveredNotChild && "is-hovered-not-child",
      hasPresence ? "is-presence" : "",
    ].filter(Boolean).join(' ')}
    py={1.5}
    lineHeight="base"
    position="relative"
    borderRadius="0.125rem"
    bg={isActive ? "background.upper" : undefined}
    justifyContent="flex-start"
    flexDirection="column"
    display="block"
    background="var(--block-surface-color)"
    opacity={isDragging ? 0.5 : 1}
    data-uid={uid}
    data-childrenuids={childrenUids}
    sx={{
      ".anchor": {
        minHeight: "100%"
      },
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
        gridTemplateColumns: "1em auto 1em 1fr calc(var(--page-right-gutter-width) / 2) calc(var(--page-right-gutter-width) / 2)",
        gridTemplateRows: "0 1fr auto auto 0",
        gridTemplateAreas:
          `'above above above above above above'
            'toggle name anchor content refs presence'
            'a a a reactions reactions reactions'
            'a a a comments b b'
            'below below below below below below'`,
        borderRadius: "0.5rem",
        // minHeight: '2em',
        position: "relative",
      },
      ".block-body > .inline-presence": {
        gridArea: "presence",
        justifySelf: "flex-end"
      },
      ".block-body > .block-toggle": {
        opacity: 0
      },
      ".block-body > .block-toggle:focus": {
        opacity: 1
      },
      "&.is-hovered-not-child > .block-body > .block-toggle, &:focus-within > .block-body > .block-toggle": {
        opacity: "1"
      },
      "&.is-hovered-not-child": {
        boxShadow: "inset 0 0 0 1px yellow"
      },
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
        // minHeight: "1.5em",
      },
      "&.is-linked-ref": { bg: "background-attic" },
      "&.isMenuOpen": { bg: "background.attic" },
      ".block-container": {
        marginLeft: "2em",
        gridArea: "body"
      },
      ".block-ref > .block > h2": {
        margin: 0,
      },
      "&:not(:first-of-type):has(> .block-body .block-content .block > h1)": {
        mt: 3
      },
      "&:not(:first-of-type):has(> .block-body .block-content .block > h2)": {
        mt: 3
      },
      "&:not(:first-of-type):has(> .block-body .block-content .block > h3)": {
        mt: 2
      },
      "&:not(:first-of-type):has(> .block-body .block-content .block > h4)": {
        mt: 2
      },
      "&:not(:first-of-type):has(> .block-body .block-content .block > h5)": {
        mt: 1
      },
      "&:not(:first-of-type):has(> .block-body .block-content .block > h6)": {
        mt: 1
      },
    }}
    {...props}
  >
    {children}
  </Box>;
})

export const Container = withErrorBoundary(_Container, { fallback: <p>{ERROR_MESSAGE}</p> });
