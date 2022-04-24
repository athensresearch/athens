import React from 'react';
import { Button, ButtonGroup, Menu, MenuButton, MenuItem, MenuList } from '@chakra-ui/react';
import { EllipsisHorizontalIcon } from "@/Icons/Icons";



export const Actions = ({ actions, pos, isUsingActions, setIsUsingActions, clearActions, actionsUid }) => {
  // Exit if there are no actions to show
  if (!actions)
    return null;

  // Update the provided actions with
  // behavior specific to use in the floating actions menu
  const actionsWithBehavior = actions.map(a => ({
    ...a,
    onClick: (e) => {
      a.onClick(e, { uid: actionsUid });
      setIsUsingActions(false);
    }
  }));

  // Divide up the actions into normal and extras (for the menu)
  const defaultActions = actionsWithBehavior.filter(a => !a.isExtra);
  const extraActions = actionsWithBehavior.filter(a => a.isExtra);

  const [isFocused, setIsFocused] = React.useState(false);
  const [isHovered, setIsHovered] = React.useState(false);

  // Handle when you're interacting with the actions
  React.useLayoutEffect(() => {
    if (isHovered || isFocused) {
      console.log('setIsUsingActions', true);
      setIsUsingActions(true);
    } else {
      console.log('setIsUsingActions', false);
      setIsUsingActions(false);
    }
  }, [isFocused, isHovered, isUsingActions]);

  // Close the actions on esc
  const handleKeyDown = (e: React.KeyboardEvent<HTMLDivElement>): void => {
    if (e.key === 'Escape') {
      setIsFocused(false);
      clearActions();
    }
  };

  return (
    <ButtonGroup
      onFocus={() => setIsFocused(true)}
      onBlur={() => setIsFocused(false)}
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
      onKeyDown={handleKeyDown}
      className="block-actions"
      size="xs"
      zIndex="tooltip"
      isAttached={true}
      position="absolute"
      top={pos?.y}
      left={pos?.x}
      transform="translateY(-50%) translateX(-100%)"
    >
      {/* Common actions are displayed directly */}
      {defaultActions.map(a => <Button key={a.children} {...a} />)}
      {/* Uncommon actions are placed in an overflow menu */}
      {extraActions.length > 0 && (
        <Menu
          isLazy={true}
        >
          <MenuButton
            as={Button}
            size="xs"
            zIndex="tooltip"
          >
            <EllipsisHorizontalIcon />
          </MenuButton>
          <MenuList>
            {extraActions.map(a => <MenuItem key={a.children} {...a} />)}
          </MenuList>
        </Menu>
      )}
    </ButtonGroup>
  );
};
