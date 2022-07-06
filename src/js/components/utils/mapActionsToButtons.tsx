import {
  Menu,
  IconButton,
  MenuButton,
  Button,
  Portal
} from "@chakra-ui/react";
import { EllipsisHorizontalIcon } from "@/Icons/Icons";
import { mapActionsToMenuList } from './mapActionsToMenuList';

const mapItemToButton = (action) => {
  const { label, fn, icon, leftIcon, rightIcon } = action;
  return <Button
    key={label}
    onClick={fn}
    leftIcon={leftIcon || icon}
    rightIcon={rightIcon}
  >{action.label}</Button>
}

export const mapActionsToButtons = (actions, limitShown: number) => {
  const showableActions = actions.slice(0, limitShown);
  const overflowActions = actions.slice(limitShown);

  return <>
    {showableActions.map(action => mapItemToButton(action))}

    {!!overflowActions.length &&
      <Menu size="sm" isLazy>
        <IconButton variant="ghost" aria-label="Menu" as={MenuButton} icon={<EllipsisHorizontalIcon />} />
        <Portal>
          {mapActionsToMenuList(overflowActions)}
        </Portal>
      </Menu>}
  </>;
};
