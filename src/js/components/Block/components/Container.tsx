import { Box } from "@chakra-ui/react";
import { withErrorBoundary } from "react-error-boundary";

const ERROR_MESSAGE = "An error occurred while rendering this block.";

const _Container = ({ children, isDragging, isSelected, isOpen, hasChildren, hasPresence, isLinkedRef, uid, childrenUids, ...props }) => {
  return <Box
    className={[
      "block-container",
      isDragging ? "is-dragging" : "",
      isSelected ? "is-selected" : "",
      isOpen ? "is-open" : "",
      (hasChildren && isOpen) ? "show-tree-indicator" : "",
      isLinkedRef ? "is-linked-ref" : "",
      hasPresence ? "is-presence" : "",
    ].filter(Boolean).join(' ')}
    display="flex"
    lineHeight="2em"
    position="relative"
    borderRadius="0.125rem"
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
      ".block-container": {
        marginLeft: "2rem",
        gridArea: "body"
      }
    }}
    {...props}
  > {children}</Box >;
}

export const Container = withErrorBoundary(_Container, { fallback: <p>{ERROR_MESSAGE}</p> });