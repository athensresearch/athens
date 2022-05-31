import React from 'react';
import { Box, Button, ButtonGroup, ButtonProps, Menu, MenuButton, MenuItem, MenuList } from "@chakra-ui/react";
import { EllipsisHorizontalIcon } from "@/Icons/Icons";

interface ActionObject extends ButtonProps {
  label: string;
  onClick: () => void;
  isExtra?: boolean;
  children?: React.ReactNode;
}

type Action = ActionObject | React.ReactElement;

export interface ActionsProps {
  actions: Action[];
  setIsUsingActions: (isUsingActions: boolean) => void;
}

export const Actions = ({ actions, setIsUsingActions }: ActionsProps): JSX.Element | null => {
  if (!actions) return null;

  let componentActions = []; // Components as actions
  let defaultActions = []; // Actions as object
  let extraActions = []; // Actions as object, but with isExtra: true

  actions.forEach(action => {
    if (React.isValidElement(action)) {
      componentActions.push(action);
    } else if (action.isExtra) {
      extraActions.push(action);
    } else {
      defaultActions.push(action);
    }
  })

  return (
    <ButtonGroup
      className="block-actions"
      size="xs"
      zIndex="tooltip"
      isAttached={true}
      position="absolute"
      right={0}
      transform="translateY(-50%)"
      top="1em"
      borderRadius="md"
      onFocus={() => setIsUsingActions(true)}
      onBlur={() => setIsUsingActions(false)}
      _after={{
        content: "''",
        zIndex: -1,
        position: "absolute",
        top: 0,
        left: 0,
        width: "100%",
        height: "100%",
        borderRadius: "inherit",
        bg: "background.vibrancy",
        backdropFilter: "blur(12px)"
      }}
    >
      {componentActions}
      {defaultActions && defaultActions.map(a => <Button
        key={a.label}
        {...a} />)}
      {extraActions.length > 0 && (
        <Menu isLazy={true} size="sm" placement="bottom-end">
          {({ isOpen }) => {
            setIsUsingActions(isOpen);

            return (
              <>
                <MenuButton
                  as={Button}
                  size="xs"
                  zIndex="tooltip"
                >
                  <EllipsisHorizontalIcon />
                </MenuButton>
                {/* Create a backdrop to capture
                mouse events and prevent accidentally
                closing the menu */}
                {isOpen && <Box
                  position="fixed"
                  left="-100vw"
                  top="-100vh"
                  width="200vw"
                  height="200vh"
                  zIndex="1" />}
                <MenuList>
                  {extraActions.map(a => <MenuItem key={a.label} {...a} />)}
                </MenuList>
              </>
            );
          }}
        </Menu>
      )}
    </ButtonGroup>
  );
};