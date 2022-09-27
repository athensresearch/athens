import React from 'react';
import { useTheme, chakra, VStack, Alert, AlertIcon, AlertTitle, forwardRef, AlertDescription } from "@chakra-ui/react";
import { ErrorBoundary } from "react-error-boundary";

const ErrorMessage = ({ error }) => {
  return (<Alert ml="4rem" alignItems="flex-start" mr="var(--page-right-gutter-width)" status='error' w="auto" px={2} py={1} my={1}>
    <AlertIcon />
    <VStack align="stretch" spacing={0}>
      <AlertTitle>An error occurred while rendering this block.</AlertTitle>
      <AlertDescription opacity={0.85} fontSize="sm">{error.toString()}</AlertDescription>
    </VStack>
  </Alert>)
};

export const Container = forwardRef((props, ref) => {
  const { children, isActive, isEditing, isHoveredNotChild, isDragging, isSelected, isOpen, hasChildren, uid, childrenUids, ...rest } = props;
  const theme = useTheme();
  const controlHeight = `calc(${theme.fontSizes.md} * ${theme.lineHeights.base})`;

  return <ErrorBoundary FallbackComponent={ErrorMessage}>
    <chakra.div
      ref={ref}
      className={[
        "block-container",
        isDragging ? "is-dragging" : "",
        isSelected ? "is-selected" : "",
        isOpen ? "is-open" : "",
        isActive && 'is-active',
        isEditing && 'is-editing',
        (hasChildren && isOpen) ? "show-tree-indicator" : "",
        isHoveredNotChild && "is-hovered-not-child",
      ].filter(Boolean).join(' ')}
      opacity={isDragging ? 0.5 : 1}
      data-uid={uid}
      data-childrenuids={childrenUids}
      __css={{
        "--control-height": controlHeight,
        position: 'relative',
        mt: 2,
        borderRadius: '0.125rem',
        transitionProperty: "common",
        transitionDuration: "fast",
        transitionTimingFunction: 'ease-in-out',
        justifyContent: 'flex-start',
        flexDirection: 'column',
        opacity: isDragging ? 0.5 : 1,
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
          transitionProperty: "common",
          transitionDuration: "fast",
          transitionTimingFunction: 'ease-in-out',
          left: 0,
          right: 0,
          top: `calc(-${theme.sizes["1"]} + 1px)`,
          bottom: `calc(-${theme.sizes["1"]} + 1px)`,
          opacity: 0,
          border: "1px solid transparent",
          pointerEvents: "none",
          borderRadius: "sm",
          transition: "opacity 0.075s ease-in-out",
        },
        "&.is-selected:after": {
          opacity: 0.2,
          bg: "link"
        },
        "&.is-active:after": {
          opacity: 1,
          bg: "interaction.surface.hover",
        },
        "&.is-selected.is-active:after": {
          opacity: 0.4,
          bg: "link",
        },
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
        "&.is-hovered-not-child > .block-body > .block-toggle, &.is-editing > .block-body > .block-toggle": {
          opacity: "1"
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
        },
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
      {...rest}
    >
      {children}
    </chakra.div>
  </ErrorBoundary>
})
