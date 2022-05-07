import React from 'react';
import { Box, Button, ButtonGroup, Menu, MenuButton, MenuItem, MenuList, Portal } from "@chakra-ui/react";
import { EllipsisHorizontalIcon } from "@/Icons/Icons";
import { withErrorBoundary } from "react-error-boundary";

const ERROR_MESSAGE = "An error occurred while rendering this block.";

interface Action {
  label: string;
  onClick: () => void;
  isExtra?: boolean;
  children?: React.ReactNode;
}

interface ActionsProps {
  actions: Action[];
  isUsingActions: boolean;
  setIsUsingActions: (isUsingActions: boolean) => void;
}

const TEST_ACTIONS = [
  {
    label: "Test action 1",
    onClick: () => {
      console.log("Test action 1 clicked");
    }
  },
  {
    label: "Test action 2",
    onClick: () => {
      console.log("Test action 2 clicked");
    }
  },
]

const Actions = ({ actions, isUsingActions, setIsUsingActions }: ActionsProps) => {
  const extraActions = actions.filter(a => a.isExtra);
  const defaultActions = actions.filter(a => !a.isExtra);

  console.log('showwing actions')

  return (
    <ButtonGroup
      className="block-actions"
      size="xs"
      zIndex="tooltip"
      isAttached={true}
      position="absolute"
      top={0}
      right={0}
      transform="translateY(-50%)"
      onFocus={() => setIsUsingActions(true)}
      onBlur={() => setIsUsingActions(false)}
    >
      {defaultActions.map(a => <Button
        key={a.label}
        {...a}
      />)}
      {extraActions.length > 0 && (
        <Menu isLazy={true}>
          <MenuButton
            as={Button}
            size="xs"
            zIndex="tooltip"
          >
            <EllipsisHorizontalIcon />
          </MenuButton>
          <MenuList>
            {extraActions.map(a => <MenuItem key={a.label} {...a} />)}
          </MenuList>
        </Menu>
      )}
    </ButtonGroup>
  )
}

const _Container = ({ children, isDragging, isSelected, isOpen, hasChildren, hasPresence, isLinkedRef, uid, childrenUids, actions, ...props }) => {
  const [shouldShowActions, setShouldShowActions] = React.useState(false);
  const [isUsingActions, setIsUsingActions] = React.useState(false);

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
    onMouseEnter={(e) => {
      const target = e.target as HTMLElement;
      setShouldShowActions(true);
      props.onMouseEnter(e);
    }}
    onMouseLeave={(e) => {
      props.onMouseLeave(e);
      if (!isUsingActions) {
        setShouldShowActions(false)
      }
    }}
  >
    {children}
    {(true && shouldShowActions) && (
      <Actions
        actions={TEST_ACTIONS}
        isUsingActions={isUsingActions}
        setIsUsingActions={setIsUsingActions}
      />)}
  </Box>;
}

export const Container = withErrorBoundary(_Container, { fallback: <p>{ERROR_MESSAGE}</p> });